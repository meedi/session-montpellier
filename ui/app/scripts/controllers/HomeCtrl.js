'use strict';

/*global app: false */

app.controller('HomeCtrl', ['$scope', 'sessions', 'SessionsFactory', '$state', function($scope, sessions, SessionsFactory, $state) {
  $scope.sessions = sessions.data;
  $scope.abc = "def";
  console.log("test");
  $scope.subscribe = function(id) {
  	console.log("subscribe to" + id);
  	SessionsFactory.subscribe(id);
  };

  $scope.jump = function(id) {
    console.log(id);
    $state.go('session', {sessionId: id});
  }
}]);

app.controller('MenuLeftCtrl', function($scope, userService) {
	$scope.ok = "test";
	userService.identity().then(function(user) {
		console.debug(user);
		$scope.me = user;
	});
});