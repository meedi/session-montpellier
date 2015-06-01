'use strict';

/**
 * The application.
 */
var app = angular.module('session-mtp', [
  'ngResource',
  'ui.router',
  'satellizer'
]);

app.run(function($rootScope, $state) {

  $rootScope.user = {};
});


app.config(function ($urlRouterProvider, $stateProvider, $httpProvider, $authProvider) {

  $urlRouterProvider.otherwise('/');

  $stateProvider
    .state('root', {
    	url: '/',
    	views: {
    		'root@': {
    			templateUrl: 'views/layout.html'
    		}
    	},

        resolve: {
        	sessions: ['SessionsFactory', function(SessionsFactory) {
        		return SessionsFactory.getSessions();
        	}]
        }
    })
    .state('signin', {
    	url: '/signin',
    	views: {
    		'root@': {
    			templateUrl: 'views/signin.html'
    		}
    	}
    })
    ;
  //Verifie si l'utilisateur est loggue, le renvoie sur la page signin le cas echeant
  $httpProvider.interceptors.push(function($q, $injector) {
    return {
      request: function(request) {
        var $auth = $injector.get('$auth');
        if ($auth.isAuthenticated()) {
          request.headers['X-Auth-Token'] = $auth.getToken();
        }

        return request;
      },

      responseError: function(rejection) {
        if (rejection.status === 401) {
          $injector.get('$state').go('signIn');
        }
        return $q.reject(rejection);
      }
    };
  });

  // Auth config
  $authProvider.httpInterceptor = true; // Add Authorization header to HTTP request
  $authProvider.loginRedirect = '/';
  $authProvider.logoutRedirect = '/';
  $authProvider.loginUrl = '/api/signin';
  $authProvider.loginRoute = '/signin';
  $authProvider.tokenName = 'token';
  $authProvider.tokenPrefix = 'satellizer'; // Local Storage name prefix
  $authProvider.authHeader = 'X-Auth-Token';

  // Facebook
  $authProvider.facebook({
    clientId: '768732473241625',
    url: '/api/authenticate',
    authorizationEndpoint: 'https://www.facebook.com/dialog/oauth',
    redirectUri: window.location.origin || window.location.protocol + '//' + window.location.host + '/',
    scope: 'email',
    scopeDelimiter: ',',
    requiredUrlParams: ['display', 'scope'],
    display: 'popup',
    type: '2.0',
    popupOptions: { width: 481, height: 269 }
  });

});

