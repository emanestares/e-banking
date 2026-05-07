angular.module('bankingApp', [])

.controller('AppController', function($scope, $http, $interval) {

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

  // Update clock every second
  $interval(function () { $scope.currentTime = new Date(); }, 1000);

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

  // ── Login ──────────────────────────────────────────────────
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

// ── Signup ──────────────────────────────────────────────────
  // ── Signup Navigation ──────────────────────────────────────
    $scope.goToSignup = function () {
      $scope.showSignup  = true;
      $scope.loginError  = null;
      $scope.signupError = null;
      $scope.signupData  = {};
      $scope.signupFieldErrors = {}; // Clear any previous asterisks
    };

    $scope.goToLogin = function () {
      $scope.showSignup  = false;
      $scope.signupError = null;
      $scope.loginError  = null;
    };


  $scope.clearFieldError = function(fieldName) {
      if ($scope.signupFieldErrors && $scope.signupFieldErrors[fieldName]) {
          delete $scope.signupFieldErrors[fieldName];
      }
  };


  $scope.signup = function () {
      // 1. Reset errors
      $scope.signupError = null;
      $scope.signupFieldErrors = {};

      // 2. Manual Frontend Validation: Flag specific fields
      var hasError = false;

      if (!$scope.signupData.fullName) { $scope.signupFieldErrors.fullName = true; hasError = true; }
      if (!$scope.signupData.username) { $scope.signupFieldErrors.username = true; hasError = true; }
      if (!$scope.signupData.email)    { $scope.signupFieldErrors.email = true;    hasError = true; }
      if (!$scope.signupData.password) { $scope.signupFieldErrors.password = true; hasError = true; }

      if (hasError) {
          $scope.signupError = 'Please fill in all required fields.';
          return; // Stop here
      }

      // 3. Logic checks
      if ($scope.signupData.password !== $scope.signupData.confirmPassword) {
          $scope.signupError = 'Passwords do not match.';
          $scope.signupFieldErrors.confirmPassword = true; // Flag the confirm field too
          return;
      }

      if ($scope.signupData.password.length < 6) {
          $scope.signupError = 'Password must be at least 6 characters.';
          $scope.signupFieldErrors.password = true;
          return;
      }

      // Initial Deposit check
      if (!$scope.signupData.initialDeposit || $scope.signupData.initialDeposit <= 0) {
          $scope.signupError = 'Initial deposit must be greater than 0.';
          $scope.signupFieldErrors.initialDeposit = true;
          return;
      }

      // 4. Proceed to API call
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
                  // This catches backend validation (e.g. "Username already exists")
                  $scope.signupFieldErrors = err.data.data;
                  $scope.signupError = "Please fix the highlighted errors.";
              } else {
                  $scope.signupError = (err.data && err.data.message) || "Registration failed.";
              }
          })
          .finally(function () {
              $scope.signupLoading = false;
          });
  };

  // ── Logout ─────────────────────────────────────────────────
  $scope.logout = function () {
    localStorage.removeItem('jwt');
    localStorage.removeItem('bankingUser');
    $scope.isLoggedIn   = false;
    $scope.currentUser  = {};
    $scope.credentials  = {};
    $scope.accounts     = [];
    $scope.transactions = [];
    $scope.showSignup   = false;
  };

  // ── Navigation ─────────────────────────────────────────────
  var pageTitles = {
    dashboard   : 'Dashboard',
    accounts    : 'My Accounts',
    transfer    : 'Transfer Funds',
    transactions: 'Transaction History',
    admin       : 'Admin Panel',
    enroll      : 'Enroll Account'
  };

  $scope.navigate = function (page) {
    $scope.currentPage     = page;
    $scope.pageTitle       = pageTitles[page] || page;
    $scope.transferSuccess = null;
    $scope.transferError   = null;

    if (page === 'dashboard')    { loadAccounts(); loadTransactions(); }
    if (page === 'accounts')     { loadAccounts(); }
    if (page === 'transactions') { loadTransactions(); }
    if (page === 'transfer')     { loadAccounts(); }
    if (page === 'enroll')       { $scope.resetEnroll(); loadAccounts(); }
    if (page === 'admin')        { $scope.adminTab = 'accounts'; $scope.loadAdminAccounts(); }
  };

  $scope.toggleSidebar = function () {
    $scope.sidebarCollapsed = !$scope.sidebarCollapsed;
  };

  // ── Accounts ───────────────────────────────────────────────
  function loadAccounts() {
    $scope.loadingAccounts = true;
    $http.get(API + '/accounts/my', { headers: authHeaders() })
      .then(function (res) {
        $scope.accounts     = res.data.data;
        $scope.totalBalance = $scope.accounts.reduce(function (s, a) {
          return s + parseFloat(a.balance || 0);
        }, 0);
      })
      .catch(function () {})
      .finally(function () { $scope.loadingAccounts = false; });
  }

  // Public wrappers so buttons can call them
  $scope.loadAccountsPublic      = loadAccounts;
  $scope.loadTransactionsPublic  = loadTransactions;
  $scope.refreshDashboard        = function () { loadAccounts(); loadTransactions(); };

  // ── Transactions ───────────────────────────────────────────
  function loadTransactions() {
    $scope.loadingTxns = true;
    $http.get(API + '/transactions/my', { headers: authHeaders() })
      .then(function (res) {
        $scope.transactions       = res.data.data;
        $scope.recentTransactions = res.data.data;
        $scope.totalCredits = $scope.transactions
          .filter(function (t) { return t.direction === 'CREDIT'; })
          .reduce(function (s, t) { return s + parseFloat(t.amount || 0); }, 0);
        $scope.totalDebits = $scope.transactions
          .filter(function (t) { return t.direction === 'DEBIT'; })
          .reduce(function (s, t) { return s + parseFloat(t.amount || 0); }, 0);

        // Build past recipients list from DEBIT transactions (people we sent to)
        var seen = {};
        var myAccountNumbers = ($scope.accounts || []).map(function(a){ return a.accountNumber; });
        $scope.pastRecipients = [];
        $scope.transactions.forEach(function (t) {
          if (t.direction === 'DEBIT' && t.receiverAccountNumber &&
              !seen[t.receiverAccountNumber] &&
              myAccountNumbers.indexOf(t.receiverAccountNumber) === -1) {
            seen[t.receiverAccountNumber] = true;
            $scope.pastRecipients.push({
              accountNumber: t.receiverAccountNumber,
              name        : t.receiverName || t.receiverAccountNumber
            });
          }
        });
      })
      .catch(function () {})
      .finally(function () { $scope.loadingTxns = false; });
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

  // ── Transfer ───────────────────────────────────────────────
  $scope.getSenderBalance = function () {
    var acc = $scope.accounts.find(function (a) {
      return a.accountNumber === $scope.transfer.senderAccountNumber;
    });
    $scope.senderBalance = acc ? parseFloat(acc.balance) : null;
  };

  $scope.doTransfer = function () {
    $scope.transferError   = null;
    $scope.transferSuccess = null;
    $scope.transferFieldErrors = {}; // Reset field highlighters
    $scope.transferring    = true;


    $http.post(API + '/transactions/transfer', $scope.transfer, { headers: authHeaders() })
          .then(function (res) {
            $scope.transferSuccess = { message: res.data.message, data: res.data.data };
            $scope.transfer = {};
            $scope.senderBalance = null; // Keeps UI consistent[cite: 11]
            loadAccounts();
            loadTransactions();
          })
          .catch(function (err) {
            if (err.status === 400 && err.data && err.data.data) {
                $scope.transferFieldErrors = err.data.data; // Bridges to GlobalExceptionHandler[cite: 10]
                $scope.transferError = "Please fill in all required fields.";
            } else {
                $scope.transferError = (err.data && err.data.message) || 'Transfer failed.';
            }
          })
          .finally(function () { $scope.transferring = false; });
    };

  $scope.clearTransfer = function () {
    $scope.transfer        = {};
    $scope.transferError   = null;
    $scope.transferSuccess = null;
    $scope.senderBalance   = null;
  };

  // Initialize error object
  $scope.quickTransferFieldErrors = {};

  // Updated helper to handle both Signup and Transfer errors
  $scope.clearFieldError = function(fieldName, errorObject) {
      if ($scope[errorObject] && $scope[errorObject][fieldName]) {
          delete $scope[errorObject][fieldName];
      }
  };

  $scope.doQuickTransfer = function () {
      // 1. Reset errors
      $scope.quickTransferError = null;
      $scope.quickTransferSuccess = null;
      $scope.quickTransferFieldErrors = {};

      // 2. Manual Validation
      var hasError = false;
      if (!$scope.quickTransfer.senderAccountNumber)   { $scope.quickTransferFieldErrors.senderAccountNumber = true; hasError = true; }
      if (!$scope.quickTransfer.receiverAccountNumber) { $scope.quickTransferFieldErrors.receiverAccountNumber = true; hasError = true; }

      // Amount check (Must be entered and > 0)
      if (!$scope.quickTransfer.amount || $scope.quickTransfer.amount <= 0) {
          $scope.quickTransferFieldErrors.amount = true;
          hasError = true;
      }

      if (hasError) {
          $scope.quickTransferError = 'Please fill in all required fields with valid inputs.';
          return;
      }

      // 3. Proceed with API call
      $scope.transferring = true;
      $http.post(API + '/transactions/transfer', $scope.quickTransfer, { headers: authHeaders() })
        .then(function (res) {
          $scope.quickTransferSuccess = 'Transfer successful! Ref: ' + res.data.data.referenceNumber;
          $scope.quickTransfer = {};
          loadAccounts();
          loadTransactions();
        })
        .catch(function (err) {
          if (err.data && err.data.data) {
              $scope.quickTransferFieldErrors = err.data.data; // Capture backend validation
          }
          $scope.quickTransferError = (err.data && err.data.message) || 'Transfer failed.';
        })
        .finally(function () { $scope.transferring = false; });
  };

  // ── Enroll Account ─────────────────────────────────────────
  $scope.resetEnroll = function () {
    $scope.enrollData    = { accountType: 'SAVINGS' };
    $scope.enrollError   = null;
    $scope.enrollSuccess = null;
  };

  $scope.doEnroll = function () {
      // 1. Reset state for a fresh attempt
      $scope.enrollError = null;
      $scope.enrollSuccess = null;
      $scope.enrollFieldErrors = {};
      $scope.enrollLoading = true;

      // 2. Build the payload matching your EnrollRequest.java
      var payload = {
          accountType   : $scope.enrollData.accountType,
          initialDeposit: $scope.enrollData.initialDeposit || 0,
          purpose       : $scope.enrollData.purpose
      };

      // 3. Make the call to AccountController[cite: 13]
      $http.post(API + '/accounts/enroll', payload, { headers: authHeaders() })
          .then(function (res) {
              $scope.enrollSuccess = res.data.data;
              // Clear form on success
              $scope.enrollData = { accountType: 'SAVINGS' };
              loadAccounts();
          })
          .catch(function (err) {
              // 4. Use the GlobalExceptionHandler's map
              if (err.status === 400 && err.data && err.data.data) {
                  // This will populate the red asterisks and specific messages
                  $scope.enrollFieldErrors = err.data.data;
                  $scope.enrollError = "Please fill the required fields.";
              } else {
                  $scope.enrollError = (err.data && err.data.message) || 'Failed to enroll account.';
              }
          })
          .finally(function () {
              // This ensures the spinner ALWAYS hides, even on error
              $scope.enrollLoading = false;
          });
  };

  // ── Admin ──────────────────────────────────────────────────
  $scope.loadAdminAccounts = function () {
    $scope.loadingAdminAccounts = true;
    $http.get(API + '/admin/accounts', { headers: authHeaders() })
      .then(function (res) { $scope.adminAccounts = res.data.data; })
      .catch(function () {})
      .finally(function () { $scope.loadingAdminAccounts = false; });
  };

  $scope.loadAdminTransactions = function () {
    $scope.loadingAdminTxns = true;
    $http.get(API + '/admin/transactions', { headers: authHeaders() })
      .then(function (res) { $scope.adminTransactions = res.data.data; })
      .catch(function () {})
      .finally(function () { $scope.loadingAdminTxns = false; });
  };

  // ── Auto-restore session ───────────────────────────────────
  var savedToken = localStorage.getItem('jwt');
  if (savedToken) {
    $http.get(API + '/auth/health')
      .then(function () {
        var saved = localStorage.getItem('bankingUser');
        if (saved) {
          $scope.currentUser = JSON.parse(saved);
          $scope.isLoggedIn  = true;
          $scope.navigate('dashboard');
        }
      })
      .catch(function () { localStorage.removeItem('jwt'); });
  }

  $scope.$watch('isLoggedIn', function (v) {
    if (v) localStorage.setItem('bankingUser', JSON.stringify($scope.currentUser));
    else   localStorage.removeItem('bankingUser');
  });
});
