'use strict';

app.factory('SessionsFactory', function($http) {
  return {
    getSessions: function() {
    	alert("ok1");
      return $http.get('json/sessions.json');
    },
    getMessages: function() {
    	return $http.get('json/messages.json');
    }
  };
});