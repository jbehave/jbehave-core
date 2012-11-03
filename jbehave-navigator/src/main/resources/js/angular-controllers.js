'use strict';

/* Controllers */

function StoriesController($scope, $http) {
  $http.get('xref.json').success(function(data) {
    $scope.data = data;
  });

  $scope.showStory = function(storyPath) {
	    console.log("showing "+storyPath)	  
	    var url = storyPath;
	    url = url.replace(/\//g, '.');
	    url = url.replace(".story",".html");
	    console.log("showing "+url)	  
	    jQuery.FrameDialog.create({
	        width: 1000,
	        height: 600,
	        closeText: 'foo',
	        url: url,
	        title: 'Results For ' + url,
	        closeOnEscape: true,
	        buttons: [
	            {
	            }
	        ]
	    });
  };
}
