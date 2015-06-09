'use strict';

/**
 * The application.
 */
var app = angular.module('session-mtp', [
  'ngResource',
  'ui.router',
  'satellizer',
  'ui.bootstrap'
]);

app.run(['$rootScope', '$state', '$stateParams', 'userService',
    function($rootScope, $state, $stateParams, userService) {
      $rootScope.$on('$stateChangeStart', function(event, toState, toStateParams) {
        $rootScope.toState = toState;
        $rootScope.toStateParams = toStateParams;
        if (userService.isIdentityResolved()) {
          console.log("Authorize ..");
          userService.authorize();

        } 
      });
    }
  ]);


app.config(function ($urlRouterProvider, $stateProvider, $httpProvider, $authProvider) {

  $urlRouterProvider.otherwise('/');

  $stateProvider
    .state('site', {
      'abstract': true,
      resolve: {
        authorize: ['userService',
          function(userService) {
            console.log("authorization..")
            return userService.authorize();
          }
        ]
      }
    })
    .state('main', {
      'abstract': true,
      parent: 'site',
      data: {
        requiresLogin : true,
        roles: []
      },
      views: {
        'root@': {
          templateUrl: 'views/layout.html'
        },
        'menu-left@main': {
          controller: 'MenuLeftCtrl',
          templateUrl: 'views/menuleft.html'
        }
      }
    })
    .state('home', {
      parent: 'main',
    	url: '/home',
    	views: {
        'main-content@main': {
          templateUrl: 'views/home.html',
          controller: 'HomeCtrl'
        },

        'header@main': {
          templateUrl: 'search.html'
        }

    	},
        resolve: {
        	sessions: ['SessionsFactory', function(SessionsFactory) {
        		console.log("getSessions");
            return SessionsFactory.all();
        	}]
        }
    })
    .state('signin', {
      parent: 'site',
    	url: '/signin',
      data: {
        requiresLogin: false,
        roles: []
      },
    	views: {
    		'root@': {
    			templateUrl: 'views/signin.html'
    		}
    	}
    })
    .state('session', {
      parent: 'main',
      url: '/session/{sessionId}',
      resolve: {
        session: function($stateParams, SessionsFactory) {
          return SessionsFactory.get($stateParams.sessionId);
        }
      },
      views: {
        'header@main' : {
          template: function() {
            console.log('test');
            return "<div class=\"session-title\"><h1>{{session.title}}</h1><p>{{session.description}}</p></div>";
          },
          controller: function($scope, session) {
            $scope.session = session.data;
          }
        },
        'main-content@main' : {
          templateUrl: "views/session.html"
        }
      }
    })
    ;
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
          $injector.get('$state').go('signin');
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

