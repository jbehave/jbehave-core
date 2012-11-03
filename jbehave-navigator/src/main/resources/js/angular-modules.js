'use strict';

/* Navigator Module */

var navigatorModule = angular.module('navigator', []);

// add filters
navigatorModule.filter('newline_to_comma', function() {
	return function(text) {
		return text.replace(/\n/g, ', ');
	};
});
 