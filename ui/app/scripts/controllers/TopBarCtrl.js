'use strict';

/*global app: false */

app.controller('TopBarCtrl', ['$scope', 'userService', '$modal', function($scope, userService, $modal) {
	userService.identity().then(function(user) {
		console.debug(user);
		$scope.me = user;
	});
	$scope.open = function() {
		var m = $modal.open({
					animation: true,
					templateUrl: 'modal-post-session.html',
					controller: 'ModalPostSessionCtrl',
					size:"lg"
				});
		m.result;
	}
}])

.controller('ModalPostSessionCtrl', function($scope, $modalInstance, SessionsFactory) {
	$scope.session = {};
    $scope.save = function() {
      SessionsFactory.save($scope.session).success(function(r1) {
        SessionsFactory.get(r1.sessionID).success(function(r2) {
          console.debug(r2)
        })
      })
    }
});
