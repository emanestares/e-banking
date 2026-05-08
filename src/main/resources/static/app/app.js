angular.module('bankingApp', [])

.controller('AppController', function($scope, $http, $interval, $filter) {

  //page state


  $scope.txnCurrentPage = 1;
  $scope.adminAccCurrentPage = 1;
  $scope.adminTxnCurrentPage = 1;


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

      // 1. Reset field errors at the start of the attempt
      $scope.signupFieldErrors = {};

      // 2. Perform local check for mismatch
      var mismatch = false;
      if ($scope.signupData.password !== $scope.signupData.confirmPassword) {
          $scope.signupFieldErrors.confirmPassword = "Passwords do not match.";
          $scope.signupError = 'Please fix the highlighted errors.';
          mismatch = true;
          // DO NOT RETURN HERE. Let the code continue so the backend can also validate.
      }

      $scope.signupLoading = true;

      var payload = {
          username: $scope.signupData.username,
          email: $scope.signupData.email,
          password: $scope.signupData.password,
          fullName: $scope.signupData.fullName,
          initialDeposit: $scope.signupData.initialDeposit
      };

      $http.post(API + '/auth/signup', payload)
          .then(function (res) {
              // Use applySession to force a redirect and data reload
              // This helper automatically sets tokens, user data,
              // and calls navigate('dashboard'), which loads the accounts.
              applySession(res.data.data);
          })

          .catch(function (err) {
              if (err.data && err.data.data) {
                  // Use angular.extend to MERGE the backend errors with our local mismatch error
                  // This ensures "Passwords do not match" stays visible alongside DTO errors
                  angular.extend($scope.signupFieldErrors, err.data.data);
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

  // Public wrappers so buttons can call them
  $scope.loadAccountsPublic      = loadAccounts;
  $scope.loadTransactionsPublic  = loadTransactions;
  $scope.refreshDashboard        = function () { loadAccounts(); loadTransactions(); };

  function loadTransactions() {
    $scope.loadingTxns = true;
    $http.get(API + '/transactions/my', { headers: authHeaders() })
      .then(function (res) {
        // 1. Assign data once
        var data = res.data.data || [];
              $scope.transactions = data;
              $scope.recentTransactions = data;

        // 2. Calculate totals using the 'data' variable
        $scope.totalCredits = data
          .filter(function (t) { return t.direction === 'CREDIT'; })
          .reduce(function (s, t) { return s + parseFloat(t.amount || 0); }, 0);

        $scope.totalDebits = data
          .filter(function (t) { return t.direction === 'DEBIT'; })
          .reduce(function (s, t) { return s + parseFloat(t.amount || 0); }, 0);

        // 3. Update the filter for the table
        $scope.updateTxnFilter();

        // 4. Build recipients list
        var seen = {};
        var myAccountNumbers = ($scope.accounts || []).map(function(a){ return a.accountNumber; });
        $scope.pastRecipients = [];
        data.forEach(function (t) {
          if (t.direction === 'DEBIT' && t.receiverAccountNumber &&
              !seen[t.receiverAccountNumber] &&
              myAccountNumbers.indexOf(t.receiverAccountNumber) === -1) {
            seen[t.receiverAccountNumber] = true;
            $scope.pastRecipients.push({
              accountNumber: t.receiverAccountNumber,
              name: t.receiverName || t.receiverAccountNumber
            });
          }
        });
      })
      .finally(function () {
        $scope.loadingTxns = false;
      });
  }

  // ── Autocomplete helpers ────────────────────────────────────
  $scope.pastRecipients       = [];
  $scope.showTransferSuggestions = false;
  $scope.showQuickSuggestions    = false;

  $scope.filteredRecipients = function (query) {
    if (!$scope.pastRecipients.length) return [];
    if (!query) return $scope.pastRecipients.slice(0, 6);
    var q = query.toLowerCase();
    return $scope.pastRecipients.filter(function (r) {
      return r.accountNumber.toLowerCase().indexOf(q) !== -1 ||
             r.name.toLowerCase().indexOf(q) !== -1;
    }).slice(0, 6);
  };

  $scope.selectRecipient = function (target, r) {
    if (target === 'transfer') {
      $scope.transfer.receiverAccountNumber = r.accountNumber;
      $scope.showTransferSuggestions  = false;
      $scope.transferRecipientPicked  = true;
    } else {
      $scope.quickTransfer.receiverAccountNumber = r.accountNumber;
      $scope.showQuickSuggestions   = false;
      $scope.quickRecipientPicked   = true;
    }
  };

  // Hide dropdown when the typed value exactly matches a known account number
  // or when the field is long enough to be a committed account number (no partial match left)
  $scope.onRecipientChange = function (target) {
    var val = target === 'transfer'
      ? ($scope.transfer.receiverAccountNumber || '')
      : ($scope.quickTransfer.receiverAccountNumber || '');

    var exactMatch = $scope.pastRecipients.some(function (r) {
      return r.accountNumber.toLowerCase() === val.toLowerCase();
    });

    // Also hide if val looks like a complete account number but not in our list
    // (e.g. user typed it manually — 8+ chars with 'ACC-' prefix)
    var looksComplete = /^ACC-\S{4,}$/i.test(val.trim());

    if (exactMatch || looksComplete) {
      if (target === 'transfer') $scope.showTransferSuggestions = false;
      else                        $scope.showQuickSuggestions    = false;
    } else {
      if (target === 'transfer') $scope.showTransferSuggestions = true;
      else                        $scope.showQuickSuggestions    = true;
    }
  };

  $scope.hideTransferSuggestions = function () {
    // Small delay so ng-mousedown on item fires first
    setTimeout(function () { $scope.$apply(function () { $scope.showTransferSuggestions = false; }); }, 150);
  };
  $scope.hideQuickSuggestions = function () {
    setTimeout(function () { $scope.$apply(function () { $scope.showQuickSuggestions = false; }); }, 150);
  };

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
    $scope.txnPageSize = 5;
    $scope.adminAccPageSize = 5;
    $scope.adminTxnPageSize = 5;

    $scope.accCurrentPage = 1;
    $scope.accPageSize = 4; // 4 for atm card grid
    $scope.Math = window.Math; // Helper for HTML templates

    $scope.getPageNumbers = function(totalItems, pageSize) {
        var totalPages = Math.ceil(totalItems / pageSize) || 0;
        var pages = [];
        for (var i = 1; i <= totalPages; i++) {
            pages.push(i);
        }
        return pages;
    };

    $scope.paginate = function(data, currentPage, pageSize) {
      if (!data) return [];
      var begin = (currentPage - 1) * pageSize;
      return data.slice(begin, begin + pageSize);
    };

    $scope.changePage = function(type, page) {
      var mapping = {
        'txn':      { current: 'txnCurrentPage',      filtered: 'filteredTransactions',      size: 'txnPageSize' },
        'adminAcc': { current: 'adminAccCurrentPage', filtered: 'filteredAdminAccounts',     size: 'adminAccPageSize' },
        'adminTxn': { current: 'adminTxnCurrentPage', filtered: 'filteredAdminTransactions', size: 'adminTxnPageSize' },
        'acc':      { current: 'accCurrentPage',      filtered: 'accounts',                  size: 'accPageSize' }
      };

      var m = mapping[type];
      if (!m) return;

      var totalPages = Math.ceil(($scope[m.filtered] || []).length / $scope[m.size]);

      // Logic to update the page
      if (page >= 1 && (totalPages === 0 || page <= totalPages)) {
        $scope[m.current] = page;
      }
    };

    // ── Search & Pagination Reset Logic ────────────────────────

      // Replace the individual search variables with a search object
      $scope.search = {
          txn: '',
          adminAcc: '',
          adminTxn: ''
      };

      $scope.$watch('search.txn', function () {
          $scope.updateTxnFilter();
      });

      $scope.$watch('search.adminAcc', function () {
          $scope.updateAdminAccFilter();
      });

      $scope.$watch('search.adminTxn', function () {
          $scope.updateAdminTxnFilter();
      });

      $scope.updateTxnFilter = function () {
          $scope.txnCurrentPage = 1;
          // Use the object property
          var query = ($scope.search.txn || '').toLowerCase().trim();
          var sourceData = $scope.transactions || [];

          if (!query) {
              $scope.filteredTransactions = sourceData;
          } else {
              $scope.filteredTransactions = sourceData.filter(function(t) {
                  if (!t) return false;
                  var ref = (t.referenceNumber || '').toLowerCase();
                  var type = (t.transactionType || '').toLowerCase();
                  var from = (t.senderAccountNumber || '').toLowerCase();
                  var to = (t.receiverAccountNumber || '').toLowerCase();
                  return ref.indexOf(query) !== -1 ||
                         type.indexOf(query) !== -1 ||
                         from.indexOf(query) !== -1 ||
                         to.indexOf(query) !== -1;
              });
          }
      };

      $scope.updateAdminAccFilter = function () {
          $scope.adminAccCurrentPage = 1;
          var query = ($scope.search.adminAcc || '').toLowerCase().trim();
          var sourceData = $scope.adminAccounts || [];

          if (!query) {
              $scope.filteredAdminAccounts = sourceData;
          } else {
              $scope.filteredAdminAccounts = sourceData.filter(function(a) {
                  return (a.accountNumber || '').toLowerCase().includes(query) ||
                         (a.ownerFullName || '').toLowerCase().includes(query) ||
                         (a.ownerUsername || '').toLowerCase().includes(query);
              });
          }
      };

      $scope.updateAdminTxnFilter = function () {
          $scope.adminTxnCurrentPage = 1;
          var query = ($scope.search.adminTxn || '').toLowerCase().trim();
          var sourceData = $scope.adminTransactions || [];

          if (!query) {
              $scope.filteredAdminTransactions = sourceData;
          } else {
              $scope.filteredAdminTransactions = sourceData.filter(function(t) {
                  return (t.referenceNumber || '').toLowerCase().includes(query) ||
                         (t.senderAccountNumber || '').toLowerCase().includes(query) ||
                         (t.receiverAccountNumber || '').toLowerCase().includes(query) ||
                         (t.transactionType || '').toLowerCase().includes(query);
              });
          }
      };


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