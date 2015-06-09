'use strict';

/*global app: false */

app.controller('SignInCtrl', ['$scope', '$state', 'userService', 'SessionsFactory', function($scope, $state, userService, SessionsFactory) {
  $scope.user;
  $scope.loginSuccessFull= false;

  $scope.auth = function() {
    userService.loginWithFacebook().then(
      function(user) {
          $scope.loginSuccessFull = true
          $scope.user = user
          console.log("going home");
          $state.go("home");
      });
  }

}]);
