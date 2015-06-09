
app.factory('userService', function($http, $auth, $state, $q, $rootScope) {
  var _identity = undefined;
  var _authenticated = false;
  var _authenticate = function(identity) {
      _identity = identity;
      if(identity != null) {
        _authenticated = true;
      }     
  };
  var _isInRole = function(role) {
        if (!_authenticated || !_identity.roles) return false;

        return _identity.roles.indexOf(role) != -1;
  };
  var _isInAnyRole= function(roles) {
        if (!_authenticated || !_identity.roles) return false;

        for (var i = 0; i < roles.length; i++) {
          if (this.isInRole(roles[i])) return true;
        }

        return false;
  }
  return {
    isIdentityResolved: function() { return angular.isDefined(_identity); },
    isAuthenticated: function() { return _authenticated; },

    authenticate: function(identity) {
        _authenticate(identity)
    },
    isInRole: function(role) { return _isInRole(role) },
    isInAnyRole: function(roles) { return _isInAnyRole(roles) },
    identity: function() {
      var deferred = $q.defer();

      if(this.isIdentityResolved()) { deferred.resolve(_identity); }
      else {
        $http.get('/api/user')
            .success(function (user) {
                _authenticate(user);
                deferred.resolve(_identity);
              })
            .error(function (data, status, headers, config) {
                _authenticate(null);
                deferred.resolve(data, status, headers, config);
              });
      }

      return deferred.promise;
    },

    loginWithFacebook: function() {
      var deferred = $q.defer();
      $auth.authenticate("facebook")
        .then(function(response) {
            _authenticate(response.data.user);
            deferred.resolve(_identity, response);
        })
        .catch(function(response) {
            deferred.reject(response);
        });

      return deferred.promise;
    },

    authorize: function() {
      return this.identity()
        .then(function() {
          console.log("?");
          if(($rootScope.toState.data.requiresLogin) && !_authenticated) {
            console.log("changestate");
            $state.go('signin');
          }
          else {
            if ($rootScope.toState.data.roles && $rootScope.toState.data.roles.length > 0 && !_isInAnyRole($rootScope.toState.data.roles)) {
              if (_authenticated) {
                console.log("accessdenied");
                $state.go('accessdenied');
              }  
              else {
                console.log("go signin");
                $rootScope.returnToState = $rootScope.toState;
                $rootScope.returnToStateParams = $rootScope.toStateParams;
                $state.go('signin');
              }
            }
          }
          console.log("authorized");
        });
    }
  };
});
