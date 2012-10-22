'use strict';

/* Controllers */

function StoriesController($scope, $http) {
  $http.get('xref.json').success(function(data) {
    $scope.data = data;
  });

  $scope.showStory = function(performable) {
	    console.log("showing "+performable.story.path)	  
	    var url = performable.story.path;
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
