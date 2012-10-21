'use strict';

/* Controllers */

function StoriesController($scope, $http) {
  $http.get('xref.json').success(function(data) {
    $scope.data = data;
  });

  $scope.showStory = function(performable) {
	  console.log("showing "+performable.story.path)	  
  };
}
