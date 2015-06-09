'use strict';

/*global app: false */

app.controller('SearchCtrl', ['$scope', 'SessionsFactory', function($scope, SessionsFactory) {
	console.log("ok")
 	$scope.searchSessions = function(query) {
 		console.log("search")
 		return SessionsFactory.search(query).then(function(response) {
 			return response.data.map(function(session) {
 				return session
 			})
 		});
 	}
}]);