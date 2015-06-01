'use strict';

/*global app: false */

app.controller('SignInCtrl', ['$scope', '$auth', function($scope, $auth) {
  $scope.auth = function() {
    $auth.authenticate("facebook")
      .then(function(response) {
          alert("Bonjour " + response.data.firstName);
      })
      .catch(function(response) {
        console.log(response);
      });
  }
}]);
