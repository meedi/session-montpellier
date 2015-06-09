'use strict';

app.factory('SessionsFactory', function($http) {
  return {
    all: function() {
    	console.log("gettingSessions");
    	return $http.get('api/sessions');
    },
    search: function(query) {
    	return $http.get('api/sessions/search', {
    		params: {
    			query: query
    		}
    	});
    },
    save: function(session) {
      return $http.post('api/sessions', session);
    },
    get: function(id) {
      return $http.get('api/sessions/'+id);
    },
    subscribe: function(id) {
      return $http.post('api/sessions/' + id + '/subscribes');
    }
  };
});
