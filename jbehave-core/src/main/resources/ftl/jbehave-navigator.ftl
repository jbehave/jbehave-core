<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:ng="http://angularjs.org" xml:lang="en" lang="en">

<head>
	<script type="text/javascript" ng:autobind src="js/angular-0.9.9.min.js"></script>
	<script type="text/javascript" src="js/jquery-1.5.js" charset="utf-8"></script>
	<script type="text/javascript" src="js/cornerz.js" charset="utf-8"></script>

	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    
    <link rel="stylesheet" type="text/css" href="style/jbehave-navigator.css" />
    <!--[if lt IE 7]>
        <link rel="stylesheet" type="text/css" href="style/style-ie.css" />
    <![endif]-->
    
    <script type="text/javascript">
    $(document).ready(function(){
        $('#main-content').cornerz({
            background: "#black" });
        $('#header').cornerz({
            background: "#black" });
        $('#footer').cornerz({
            background: "#black" });
    });
    </script>
</head>

<body>
    
    <script type="text/javascript">
      MyController.$inject=["$resource"];
       function MyController($resource) {
        this.Story = $resource("xref.json");
        this.data = this.Story.get({});
       }
       MyController.prototype = {
          showScenarios: function(story) {
               alert(story.scenarios);
          },
          openViewTab: function(story) {
               alert("2" + story.scenarios);
          },
          openEditTab: function(story) {
               alert("3" + story.scenarios);
          }
        };
      </script>

    <div id="page-wrap">
        <div id="inside">
            
            <div id="header">
                <a href="http://www.jbehave.org"><img src="images/jbehave-logo.png" alt="header" /></a>
            </div>
        
    
            <div ng:controller="MyController" id="main-content">
                  <table>
                    <tr>
                        <th colspan="5">Use fields for each columns to subset stories, and/or the following for scenarios themselves: <input name="search.scenarios"/></th>
                    </tr>
                    <tr>
                      <th><a href ng:click="predicate = 'path'">Path</a>(<a href ng:click="predicate = '-path'">^</a>)</th>
                      <th><a href ng:click="predicate = 'description'">Description</a>(<a href ng:click="predicate = '-description'">^</a>)</th>
                      <th><a href ng:click="predicate = 'meta'">Meta</a>(<a href ng:click="predicate = '-meta'">^</a>)</th>
                      <th><a href ng:click="predicate = 'narrative'">Narrative</a>(<a href ng:click="predicate = '-narrative'">^</a>)</th>
                      <th>Action</th>
                    </tr>
                    <tr>
                      <th><input name="search.path"/></th>
                      <th><input name="search.description"/></th>
                      <th><select name="search.meta"><option value="=" selected>**All**</option><option ng:repeat="permutation in data.xref.meta">{{permutation}}</option></select></th>
                      <th><input name="search.narrative"/></th>
                      <th></th>
                    </tr>
                    <tr ng:repeat="story in data.xref.stories.$filter(search).$orderBy(predicate)">
                      <td>{{story.path}}</td>
                      <td>{{story.description}}</td>
                      <td>{{story.meta}}</td>
                      <td>{{story.narrative}}</td>
                      <td>
                        <button ng:click="showScenarios(story)">Preview</button>
                        <button ng:click="openViewTab(story)">Full Story</button>
                        <button ng:click="openEditTab(story)">Edit</button>
                      </td>
                    </tr>
                  </table>  
                  <h2>Step List</h2>
                  <table border="1">
                    <tr>
                      <th>num</th>
                      <th>Story</th>
                      <th>Scenario Title</th>
                      <th>Name</th>
                    </tr>
                    <tr ng:repeat="match in data.xref.stepMatches">
                      <td>{{$index + 1}}</td>
                      <td>{{match.storyPath}}</td>
                      <td>{{match.scenarioTitle}}</td>
                      <td>{{match.step}}</td>
                     </tr>
                  </table>
                        
            </div>
            <div class="clear"></div>
            <div id="footer">
            <div class="left">Generated on ${date?string("dd/MM/yyyy HH:mm:ss")}</div>
            <div class="right">JBehave &#169; 2003-2010</div>
            <div class="clear"></div>        
        
        <div style="clear: both;"></div>
    
    </div>

</body>

