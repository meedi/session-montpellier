'use strict';

/*global app: false */

app.controller('HomeCtrl', ['$scope', 'sessions', function($scope, sessions) {
  $scope.sessions = sessions.data;
  console.log(JSON.stringify($scope.sessions));
}]);