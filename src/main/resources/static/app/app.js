angular.module('bankingApp', [])

.controller('AppController', function($scope, $http, $interval) {

  // ── State ──────────────────────────────────────────────────
  $scope.isLoggedIn      = false;
  $scope.loading         = false;
  $scope.loginError      = null;
  $scope.credentials     = { username: '', password: '' };
  $scope.currentUser     = {};
  $scope.currentPage     = 'dashboard';
  $scope.pageTitle       = 'Dashboard';
  $scope.sidebarCollapsed = false;
  $scope.currentTime     = new Date();

  $scope.accounts          = [];
  $scope.transactions      = [];
  $scope.recentTransactions = [];
  $scope.loadingAccounts   = false;
  $scope.loadingTxns       = false;
  $scope.totalBalance      = 0;
  $scope.totalCredits      = 0;
  $scope.totalDebits       = 0;

  $scope.transfer        = {};
  $scope.quickTransfer   = {};
  $scope.senderBalance   = null;
  $scope.transferring    = false;
  $scope.transferSuccess = null;
  $scope.transferError   = null;
  $scope.quickTransferSuccess = null;
  $scope.quickTransferError   = null;

  $scope.adminTab          = 'accounts';
  $scope.adminAccounts     = [];
  $scope.adminTransactions = [];
  $scope.loadingAdminAccounts = false;
  $scope.loadingAdminTxns     = false;

  // Update clock every second
  $interval(function(){ $scope.currentTime = new Date(); }, 1000);

  // ── Helpers ────────────────────────────────────────────────
  var API = '/api';

  function authHeaders() {
    var token = localStorage.getItem('jwt');
    return { 'Authorization': 'Bearer ' + token };
  }

  $scope.isAdmin = function() {
    return $scope.currentUser.roles &&
           $scope.currentUser.roles.indexOf('ROLE_ADMIN') !== -1;
  };

  // ── Auth ───────────────────────────────────────────────────
  $scope.login = function() {
    $scope.loginError = null;
    $scope.loading    = true;
    $http.post(API + '/auth/login', $scope.credentials)
      .then(function(res) {
        var d = res.data.data;
        localStorage.setItem('jwt', d.token);
        $scope.currentUser = {
          username : d.username,
          fullName : d.fullName,
          roles    : d.roles,
          accountId: d.accountId,
          accountNumber: d.accountNumber
        };
        $scope.isLoggedIn = true;
        $scope.navigate('dashboard');
      })
      .catch(function(err) {
        $scope.loginError = (err.data && err.data.message) || 'Invalid username or password.';
      })
      .finally(function() { $scope.loading = false; });
  };

  $scope.logout = function() {
    localStorage.removeItem('jwt');
    $scope.isLoggedIn  = false;
    $scope.currentUser = {};
    $scope.credentials = {};
    $scope.accounts    = [];
    $scope.transactions= [];
  };

  // ── Navigation ─────────────────────────────────────────────
  var pageTitles = {
    dashboard   : 'Dashboard',
    accounts    : 'My Accounts',
    transfer    : 'Transfer Funds',
    transactions: 'Transaction History',
    admin       : 'Admin Panel'
  };

  $scope.navigate = function(page) {
    $scope.currentPage  = page;
    $scope.pageTitle    = pageTitles[page] || page;
    $scope.transferSuccess = null;
    $scope.transferError   = null;

    if (page === 'dashboard')    { loadAccounts(); loadTransactions(); }
    if (page === 'accounts')     { loadAccounts(); }
    if (page === 'transactions') { loadTransactions(); }
    if (page === 'transfer')     { loadAccounts(); }
    if (page === 'admin')        { $scope.adminTab = 'accounts'; $scope.loadAdminAccounts(); }
  };

  $scope.toggleSidebar = function() {
    $scope.sidebarCollapsed = !$scope.sidebarCollapsed;
  };

  // ── Load Accounts ──────────────────────────────────────────
  function loadAccounts() {
    $scope.loadingAccounts = true;
    $http.get(API + '/accounts/my', { headers: authHeaders() })
      .then(function(res) {
        $scope.accounts = res.data.data;
        $scope.totalBalance = $scope.accounts.reduce(function(s, a) {
          return s + parseFloat(a.balance || 0);
        }, 0);
      })
      .catch(function() {})
      .finally(function() { $scope.loadingAccounts = false; });
  }

  // ── Load Transactions ──────────────────────────────────────
  function loadTransactions() {
    $scope.loadingTxns = true;
    $http.get(API + '/transactions/my', { headers: authHeaders() })
      .then(function(res) {
        $scope.transactions      = res.data.data;
        $scope.recentTransactions = res.data.data;
        $scope.totalCredits = $scope.transactions
          .filter(function(t){ return t.direction === 'CREDIT'; })
          .reduce(function(s, t){ return s + parseFloat(t.amount || 0); }, 0);
        $scope.totalDebits = $scope.transactions
          .filter(function(t){ return t.direction === 'DEBIT'; })
          .reduce(function(s, t){ return s + parseFloat(t.amount || 0); }, 0);
      })
      .catch(function() {})
      .finally(function() { $scope.loadingTxns = false; });
  }

  // ── Transfer ───────────────────────────────────────────────
  $scope.getSenderBalance = function() {
    var acc = $scope.accounts.find(function(a) {
      return a.accountNumber === $scope.transfer.senderAccountNumber;
    });
    $scope.senderBalance = acc ? parseFloat(acc.balance) : null;
  };

  $scope.doTransfer = function() {
    $scope.transferError   = null;
    $scope.transferSuccess = null;
    $scope.transferring    = true;
    $http.post(API + '/transactions/transfer', $scope.transfer, { headers: authHeaders() })
      .then(function(res) {
        $scope.transferSuccess = { message: res.data.message, data: res.data.data };
        $scope.transfer = {};
        $scope.senderBalance = null;
        loadAccounts();
        loadTransactions();
      })
      .catch(function(err) {
        $scope.transferError = (err.data && err.data.message) || 'Transfer failed.';
      })
      .finally(function() { $scope.transferring = false; });
  };

  $scope.clearTransfer = function() {
    $scope.transfer = {};
    $scope.transferError = null;
    $scope.transferSuccess = null;
    $scope.senderBalance = null;
  };

  $scope.doQuickTransfer = function() {
    $scope.quickTransferError   = null;
    $scope.quickTransferSuccess = null;
    $scope.transferring = true;
    $http.post(API + '/transactions/transfer', $scope.quickTransfer, { headers: authHeaders() })
      .then(function(res) {
        $scope.quickTransferSuccess = 'Transfer successful! Ref: ' + res.data.data.referenceNumber;
        $scope.quickTransfer = {};
        loadAccounts();
        loadTransactions();
      })
      .catch(function(err) {
        $scope.quickTransferError = (err.data && err.data.message) || 'Transfer failed.';
      })
      .finally(function() { $scope.transferring = false; });
  };

  // ── Admin ──────────────────────────────────────────────────
  $scope.loadAdminAccounts = function() {
    $scope.loadingAdminAccounts = true;
    $http.get(API + '/admin/accounts', { headers: authHeaders() })
      .then(function(res) { $scope.adminAccounts = res.data.data; })
      .catch(function() {})
      .finally(function() { $scope.loadingAdminAccounts = false; });
  };

  $scope.loadAdminTransactions = function() {
    $scope.loadingAdminTxns = true;
    $http.get(API + '/admin/transactions', { headers: authHeaders() })
      .then(function(res) { $scope.adminTransactions = res.data.data; })
      .catch(function() {})
      .finally(function() { $scope.loadingAdminTxns = false; });
  };

  // ── Auto-restore session ───────────────────────────────────
  var savedToken = localStorage.getItem('jwt');
  if (savedToken) {
    // Validate token by calling health endpoint
    $http.get(API + '/auth/health')
      .then(function() {
        // Token looks fine; we'd need to decode it, but for now we check storage
        var savedUser = localStorage.getItem('bankingUser');
        if (savedUser) {
          $scope.currentUser = JSON.parse(savedUser);
          $scope.isLoggedIn  = true;
          $scope.navigate('dashboard');
        }
      })
      .catch(function() { localStorage.removeItem('jwt'); });
  }

  // Save user on login success (override login then-block to also save)
  $scope.$watch('isLoggedIn', function(v) {
    if (v) localStorage.setItem('bankingUser', JSON.stringify($scope.currentUser));
    else   localStorage.removeItem('bankingUser');
  });
});
