'use strict';

/* Navigator Module */

angular.module('navigator', []).
  config(['$routeProvider', function($routeProvider) {
  $routeProvider.
      when('/stories', {templateUrl: 'html/stories.html',   controller: StoriesController}).
      when('/stories/:path', {templateUrl: 'html/story.html', controller: StoryController}).
      otherwise({redirectTo: '/stories'});
}]);
