'use strict';

/* Controllers */

function StoriesController($scope, $http) {
  $http.get('xref.json').success(function(data) {
    $scope.data = data;
  });

  $scope.predicate = '';
  
  $scope.showStory = function(storyPath) {
	    var url = storyPath;
	    url = url.replace(/\//g, '.');
	    url = url.replace(".story",".html");
	    console.log("Showing story: "+url)	  
	    jQuery.FrameDialog.create({
	        width: 1000,
	        height: 600,
	        closeText: 'foo',
	        url: url,
	        title: storyPath,
	        closeOnEscape: true,
	        buttons: [
	            {
	            }
	        ]
	    });
  };
}
