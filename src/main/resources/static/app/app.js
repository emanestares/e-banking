angular.module('bankingApp', [])

.controller('AppController', function($scope, $http, $interval, $filter) {

  // ── State ──────────────────────────────────────────────────
  $scope.isLoggedIn       = false;
  $scope.loading          = false;
  $scope.loginError       = null;
  $scope.credentials      = { username: '', password: '' };
  $scope.currentUser      = {};
  $scope.currentPage      = 'dashboard';
  $scope.pageTitle        = 'Dashboard';
  $scope.sidebarCollapsed = false;
  $scope.currentTime      = new Date();

  // Signup state
  $scope.showSignup      = false;
  $scope.signupLoading   = false;
  $scope.signupError     = null;
  $scope.signupData      = {};
  $scope.signupFieldErrors = {};

  $scope.accounts           = [];
  $scope.transactions       = [];
  $scope.recentTransactions = [];
  $scope.loadingAccounts    = false;
  $scope.loadingTxns        = false;
  $scope.totalBalance       = 0;
  $scope.totalCredits       = 0;
  $scope.totalDebits        = 0;

  $scope.transfer             = {};
  $scope.transferFieldErrors  = {};
  $scope.quickTransfer        = {};
  $scope.senderBalance        = null;
  $scope.transferring         = false;
  $scope.transferSuccess      = null;
  $scope.transferError        = null;
  $scope.quickTransferSuccess = null;
  $scope.quickTransferError   = null;

  $scope.adminTab             = 'accounts';
  $scope.adminAccounts        = [];
  $scope.adminTransactions    = [];
  $scope.loadingAdminAccounts = false;
  $scope.loadingAdminTxns     = false;

  // Balance visibility
  $scope.balanceVisible = true;
  $scope.toggleBalance  = function () { $scope.balanceVisible = !$scope.balanceVisible; };

  // Enroll Account state
  $scope.enrollData    = { accountType: 'SAVINGS' };
  $scope.enrollLoading = false;
  $scope.enrollError   = null;
  $scope.enrollSuccess = null;
  $scope.enrollFieldErrors = {};

  // Update clock every second
  $interval(function () { $scope.currentTime = new Date(); }, 1000);

  // Filtered Results
  $scope.filteredTransactions = [];
  $scope.filteredAdminAccounts = [];
  $scope.filteredAdminTransactions = [];

  // ── Helpers ────────────────────────────────────────────────
  var API = '/api';

  function authHeaders() {
    return { 'Authorization': 'Bearer ' + localStorage.getItem('jwt') };
  }

  function applySession(d) {
    localStorage.setItem('jwt', d.token);
    $scope.currentUser = {
      username     : d.username,
      fullName     : d.fullName,
      roles        : d.roles,
      accountId    : d.accountId,
      accountNumber: d.accountNumber
    };
    $scope.isLoggedIn = true;
    $scope.showSignup = false;
    $scope.navigate('dashboard');
  }

  $scope.isAdmin = function () {
    return $scope.currentUser.roles &&
           $scope.currentUser.roles.indexOf('ROLE_ADMIN') !== -1;
  };

  // Improved helper for all forms
  $scope.clearFieldError = function(fieldName, errorObjectName) {
      if ($scope[errorObjectName] && $scope[errorObjectName][fieldName]) {
          delete $scope[errorObjectName][fieldName];
      }
  };

  // ── Login / Signup ─────────────────────────────────────────
  $scope.login = function () {
    $scope.loginError = null;
    $scope.loading    = true;
    $http.post(API + '/auth/login', $scope.credentials)
      .then(function (res) { applySession(res.data.data); })
      .catch(function (err) {
        $scope.loginError = (err.data && err.data.message) || 'Invalid username or password.';
      })
      .finally(function () { $scope.loading = false; });
  };

  $scope.goToSignup = function () {
    $scope.showSignup  = true;
    $scope.loginError  = null;
    $scope.signupError = null;
    $scope.signupData  = {};
    $scope.signupFieldErrors = {};
  };

  $scope.goToLogin = function () {
    $scope.showSignup  = false;
    $scope.signupError = null;
    $scope.loginError  = null;
  };

  $scope.signup = function () {
      $scope.signupError = null;
      $scope.signupFieldErrors = {};
      var hasError = false;

      if (!$scope.signupData.fullName) { $scope.signupFieldErrors.fullName = true; hasError = true; }
      if (!$scope.signupData.username) { $scope.signupFieldErrors.username = true; hasError = true; }
      if (!$scope.signupData.email)    { $scope.signupFieldErrors.email = true;    hasError = true; }
      if (!$scope.signupData.password) { $scope.signupFieldErrors.password = true; hasError = true; }

      if (hasError) {
          $scope.signupError = 'Please fill in all required fields.';
          return;
      }

      if ($scope.signupData.password !== $scope.signupData.confirmPassword) {
          $scope.signupError = 'Passwords do not match.';
          $scope.signupFieldErrors.confirmPassword = true;
          return;
      }

      $scope.signupLoading = true;
      var payload = {
          username: $scope.signupData.username,
          email: $scope.signupData.email,
          password: $scope.signupData.password,
          fullName: $scope.signupData.fullName,
          initialDeposit: $scope.signupData.initialDeposit || 0
      };

      $http.post(API + '/auth/signup', payload)
          .then(function (res) {
              localStorage.setItem('jwt', res.data.data.token);
              $scope.isLoggedIn = true;
              $scope.currentUser = res.data.data;
              $scope.showSignup = false;
          })
          .catch(function (err) {
              if (err.data && err.data.data) {
                  $scope.signupFieldErrors = err.data.data;
                  $scope.signupError = "Please fix the highlighted errors.";
              } else {
                  $scope.signupError = (err.data && err.data.message) || "Registration failed.";
              }
          })
          .finally(function () { $scope.signupLoading = false; });
  };

  $scope.logout = function () {
    localStorage.removeItem('jwt');
    localStorage.removeItem('bankingUser');
    $scope.isLoggedIn   = false;
    $scope.currentUser  = {};
    $scope.accounts     = [];
    $scope.transactions = [];
  };

  // ── Navigation ─────────────────────────────────────────────
  var pageTitles = {
    dashboard: 'Dashboard', accounts: 'My Accounts', transfer: 'Transfer Funds',
    transactions: 'Transaction History', admin: 'Admin Panel', enroll: 'Enroll Account'
  };

  $scope.navigate = function (page) {
    $scope.currentPage = page;
    $scope.txnCurrentPage = 1;
    $scope.adminAccCurrentPage = 1;
    $scope.adminTxnCurrentPage = 1;
    $scope.pageTitle = pageTitles[page] || page;

    if (page === 'dashboard') { loadAccounts(); loadTransactions(); }
    if (page === 'accounts' || page === 'transfer' || page === 'enroll') { loadAccounts(); }
    if (page === 'transactions') { loadTransactions(); }
    if (page === 'admin') { $scope.adminTab = 'accounts'; $scope.loadAdminAccounts(); }
  };

  $scope.toggleSidebar = function () { $scope.sidebarCollapsed = !$scope.sidebarCollapsed; };

  // ── Data Loading ───────────────────────────────────────────
  function loadAccounts() {
    $scope.loadingAccounts = true;
    $http.get(API + '/accounts/my', { headers: authHeaders() })
      .then(function (res) {
        $scope.accounts = res.data.data;
        $scope.totalBalance = $scope.accounts.reduce((s, a) => s + parseFloat(a.balance || 0), 0);
      })
      .finally(function () { $scope.loadingAccounts = false; });
  }

  function loadTransactions() {
    $scope.loadingTxns = true;
    $http.get(API + '/transactions/my', { headers: authHeaders() })
      .then(function (res) {
        $scope.transactions = res.data.data;
        $scope.recentTransactions = res.data.data;
        $scope.updateTxnFilter();
        $scope.totalCredits = $scope.transactions.filter(t => t.direction === 'CREDIT').reduce((s, t) => s + parseFloat(t.amount || 0), 0);
        $scope.totalDebits = $scope.transactions.filter(t => t.direction === 'DEBIT').reduce((s, t) => s + parseFloat(t.amount || 0), 0);
      })
      .finally(function () { $scope.loadingTxns = false; });
  }

  // ── Transfers ──────────────────────────────────────────────
  $scope.getSenderBalance = function () {
    var acc = $scope.accounts.find(a => a.accountNumber === $scope.transfer.senderAccountNumber);
    $scope.senderBalance = acc ? parseFloat(acc.balance) : null;
  };

  $scope.doTransfer = function () {
    $scope.transferError = $scope.transferSuccess = null;
    $scope.transferFieldErrors = {};
    $scope.transferring = true;

    $http.post(API + '/transactions/transfer', $scope.transfer, { headers: authHeaders() })
      .then(function (res) {
        $scope.transferSuccess = { message: res.data.message, data: res.data.data };
        $scope.transfer = {};
        $scope.senderBalance = null;
        loadAccounts(); loadTransactions();
      })
      .catch(function (err) {
        if (err.status === 400 && err.data && err.data.data) {
            $scope.transferFieldErrors = err.data.data;
            $scope.transferError = "Please fill in all required fields.";
        } else {
            $scope.transferError = (err.data && err.data.message) || 'Transfer failed.';
        }
      })
      .finally(function () { $scope.transferring = false; });
  };

  $scope.doQuickTransfer = function () {
      $scope.quickTransferError = $scope.quickTransferSuccess = null;
      $scope.quickTransferFieldErrors = {};
      var hasError = false;

      if (!$scope.quickTransfer.senderAccountNumber) { $scope.quickTransferFieldErrors.senderAccountNumber = true; hasError = true; }
      if (!$scope.quickTransfer.receiverAccountNumber) { $scope.quickTransferFieldErrors.receiverAccountNumber = true; hasError = true; }
      if (!$scope.quickTransfer.amount || $scope.quickTransfer.amount <= 0) { $scope.quickTransferFieldErrors.amount = true; hasError = true; }

      if (hasError) { $scope.quickTransferError = 'Please fill in required fields.'; return; }

      $scope.transferring = true;
      $http.post(API + '/transactions/transfer', $scope.quickTransfer, { headers: authHeaders() })
        .then(function (res) {
          $scope.quickTransferSuccess = 'Transfer successful! Ref: ' + res.data.data.referenceNumber;
          $scope.quickTransfer = {};
          loadAccounts(); loadTransactions();
        })
        .catch(function (err) {
          if (err.data && err.data.data) $scope.quickTransferFieldErrors = err.data.data;
          $scope.quickTransferError = (err.data && err.data.message) || 'Transfer failed.';
        })
        .finally(function () { $scope.transferring = false; });
  };

  // ── Enroll Account ─────────────────────────────────────────
  $scope.doEnroll = function () {
      $scope.enrollError = $scope.enrollSuccess = null;
      $scope.enrollFieldErrors = {};
      $scope.enrollLoading = true;

      $http.post(API + '/accounts/enroll', $scope.enrollData, { headers: authHeaders() })
          .then(function (res) {
              $scope.enrollSuccess = res.data.data;
              $scope.enrollData = { accountType: 'SAVINGS' };
              loadAccounts();
          })
          .catch(function (err) {
              if (err.status === 400 && err.data && err.data.data) {
                  $scope.enrollFieldErrors = err.data.data;
                  $scope.enrollError = "Please fix the highlighted fields.";
              } else {
                  $scope.enrollError = (err.data && err.data.message) || 'Enrollment failed.';
              }
          })
          .finally(function () { $scope.enrollLoading = false; });
  };

  $scope.resetEnroll = function () { $scope.enrollData = { accountType: 'SAVINGS' }; $scope.enrollError = $scope.enrollSuccess = null; };

  // ── Pagination & Search Logic ──────────────────────────────
    $scope.txnPageSize = 10;
    $scope.adminAccPageSize = 10;
    $scope.adminTxnPageSize = 10;
    $scope.Math = window.Math; // Helper for HTML templates

    $scope.getPageNumbers = function(totalItems, pageSize) {
      var totalPages = Math.ceil(totalItems / pageSize) || 0;
      return Array.from({ length: totalPages }, (_, i) => i + 1);
    };

    $scope.paginate = function(data, currentPage, pageSize) {
      if (!data) return [];
      var begin = (currentPage - 1) * pageSize;
      return data.slice(begin, begin + pageSize);
    };

    $scope.changePage = function(type, page) {
      var mapping = {
        'txn': { current: 'txnCurrentPage', filtered: 'filteredTransactions', size: 'txnPageSize' },
        'adminAcc': { current: 'adminAccCurrentPage', filtered: 'filteredAdminAccounts', size: 'adminAccPageSize' },
        'adminTxn': { current: 'adminTxnCurrentPage', filtered: 'filteredAdminTransactions', size: 'adminTxnPageSize' }
      };
      var m = mapping[type];
      var totalPages = Math.ceil(($scope[m.filtered] || []).length / $scope[m.size]);
      if (page >= 1 && page <= totalPages) $scope[m.current] = page;
    };

    // SINGLE SOURCE OF TRUTH FOR FILTERS
    // We use $filter('filter') because it provides the "perfect" search across all fields
    $scope.updateTxnFilter = function () {
      $scope.filteredTransactions = $filter('filter')($scope.transactions, $scope.txnSearch) || [];
    };

    $scope.updateAdminAccFilter = function () {
      $scope.filteredAdminAccounts = $filter('filter')($scope.adminAccounts, $scope.adminAccSearch) || [];
    };

    $scope.updateAdminTxnFilter = function () {
      $scope.filteredAdminTransactions = $filter('filter')($scope.adminTransactions, $scope.adminTxnSearch) || [];
    };

    // ── Watchers ───────────────────────────────────────────────

    // These watchers ensure that every time you type, the search updates
    // AND the view jumps back to Page 1 immediately.
    $scope.$watch('txnSearch', function () {
        $scope.txnCurrentPage = 1;
        $scope.updateTxnFilter();
    });

    $scope.$watch('adminAccSearch', function () {
        $scope.adminAccCurrentPage = 1;
        $scope.updateAdminAccFilter();
    });

    $scope.$watch('adminTxnSearch', function () {
        $scope.adminTxnCurrentPage = 1;
        $scope.updateAdminTxnFilter();
    });

    //watch again
    $scope.$watch('txnSearch', function(newVal, oldVal) {
        if (newVal !== oldVal) {
            $scope.txnCurrentPage = 1;
        }
    });

    $scope.$watch('txnSearch', function() {
        $scope.txnCurrentPage = 1;
    });

    $scope.$watch('adminAccSearch', function(newVal, oldVal) {
        if (newVal !== oldVal) {
            $scope.adminAccCurrentPage = 1;
        }
    });

    $scope.$watch('adminTxnSearch', function(newVal, oldVal) {
        if (newVal !== oldVal) {
            $scope.adminTxnCurrentPage = 1;
        }
    });

    // Watch for session changes
    $scope.$watch('isLoggedIn', function (v) {
      if (v) localStorage.setItem('bankingUser', JSON.stringify($scope.currentUser));
      else localStorage.removeItem('bankingUser');
    });

    // ── Admin Loading ──────────────────────────────────────────
    $scope.loadAdminAccounts = function () {
      $scope.loadingAdminAccounts = true;
      $http.get(API + '/admin/accounts', { headers: authHeaders() })
        .then(res => { $scope.adminAccounts = res.data.data; $scope.updateAdminAccFilter(); })
        .finally(() => $scope.loadingAdminAccounts = false);
    };

    $scope.loadAdminTransactions = function () {
      $scope.loadingAdminTxns = true;
      $http.get(API + '/admin/transactions', { headers: authHeaders() })
        .then(res => { $scope.adminTransactions = res.data.data; $scope.updateAdminTxnFilter(); })
        .finally(() => $scope.loadingAdminTxns = false);
    };

    // ── Auto-restore session ───────────────────────────────────
    var savedToken = localStorage.getItem('jwt');
    if (savedToken) {
      $http.get(API + '/auth/health').then(() => {
        var saved = localStorage.getItem('bankingUser');
        if (saved) {
          $scope.currentUser = JSON.parse(saved);
          $scope.isLoggedIn = true;
          $scope.navigate('dashboard');
        }
      }).catch(() => localStorage.removeItem('jwt'));
    }
  });