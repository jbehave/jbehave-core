<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:ng="http://angularjs.org" xml:lang="en" lang="en">

<head>
    <script type="text/javascript" ng:autobind src="js/angular-0.9.9.min.js"></script>
    <script type="text/javascript" src="js/jquery-1.5.js"></script>
    <script type="text/javascript" src="js/cornerz-0.6.js"></script>
    <script type="text/javascript" src="js/jquery-ui.min-1.7.1.js"></script>
    <script type="text/javascript" src="js/jquery-framedialog-1.1.2.js"></script>
    <link rel="stylesheet" type="text/css" href="style/jbehave-navigator.css"/>
    <link rel="stylesheet" type="text/css" href="style/jquery-ui.css">

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>

    <title>JBehave Story Navigator</title>

    <script type="text/javascript">
        $(document).ready(function() {
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
    MyController.$inject = ["$resource"];
    function MyController($resource) {
        this.Story = $resource("xref.json");
        this.data = this.Story.get({});
    }
    MyController.prototype = {
        showResults: function(story) {

            jQuery.FrameDialog.create({
                width: 1000,
                height: 600,
                closeText: 'foo',
                url: story.html,
                title: 'Results For ' + story.path,
                closeOnEscape: true,
                buttons: [
                    {
                    }
                ]
            })
        },
        showScenarios: function(story) {

            var $dialog = $('<div></div>')
                    .html('<pre>' + story.scenarios.replace("\n", "<br>") + '</pre>')
                    .dialog({
                                width: 800,
                                height: 400,
                                autoOpen: false,
                                title: 'Scenarios for ' + story.path
                            });

            $dialog.dialog('open');
            // prevent the default action, e.g., following a link
        },
        openEditTab: function(story) {
            alert("TODO");
        }
    };
    angular.filter('newlines', function(text) {
        return text.replace(/\n/g, '<br/>');
    });
</script>

<div id="page-wrap">
    <div id="inside">

        <div id="header">
            <a href="http://www.jbehave.org"><img src="images/jbehave-logo.png" alt="header"/></a>
        </div>


        <div ng:controller="MyController" id="main-content">
            <table>
                <tr>
                    <th colspan="5">Use columns to filter stories by content and/or search text in scenarios: <input
                            name="search.scenarios"/></th>
                </tr>
                <tr>
                    <th>
                        <a href ng:click="predicate = 'path'">Path</a>(<a href ng:click="predicate = '-path'">^</a>)
                    </th>
                    <th>
                        <a href ng:click="predicate = 'description'">Description</a>(<a href ng:click="predicate = '-description'">^</a>)
                    </th>
                    <th>
                        <a href ng:click="predicate = 'meta'">Meta</a>(<a href ng:click="predicate = '-meta'">^</a>)
                    </th>
                    <th>
                        <a href ng:click="predicate = 'narrative'">Narrative</a>(<a href ng:click="predicate = '-narrative'">^</a>)
                    </th>
                    <th>
                        <a href ng:click="predicate = 'passed'">Status</a>(<a href ng:click="predicate = '-passed'">^</a>)
                    </th>
                    <th>Action</th>
                </tr>
                <tr>
                    <th><input name="search.path"/></th>
                    <th><input name="search.description"/></th>
                    <th><select name="search.meta">
                        <option value="=" selected>**All**</option>
                        <option ng:repeat="permutation in data.xref.meta">{{permutation}}</option>
                    </select></th>
                    <th><input name="search.narrative"/></th>
                    <th></th>
                    <th></th>
                </tr>
                <tr ng:repeat="story in data.xref.stories.$filter(search).$orderBy(predicate)">
                    <td>{{story.path}}</td>
                    <td>{{story.description}}</td>
                    <td>{{story.meta}}</td>
                    <td>{{story.narrative | newlines | html}}</td>
                    <td><img src="images/passed-{{story.passed}}.png" alt="Story Passed == {{story.passed}}"/></td>
                    <td>
                        <button ng:click="showResults(story)">Results</button>
                        <button ng:click="showScenarios(story)">Scenarios</button>
                        <!--button ng:click="openEditTab(story)">Edit</button-->
                    </td>
                </tr>
            </table>

            <!-- TODO: introduce a more significant structure for the steps.  Currently they can be read directly from the story results dialog.
            <h2>Steps</h2>
            <table border="1">
              <tr>
                <th>Number</th>
                <th>Story Path</th>
                <th>Scenario Title</th>
                <th>Step</th>
              </tr>
              <tr ng:repeat="match in data.xref.stepMatches">
                <td>{{$index + 1}}</td>
                <td>{{match.storyPath}}</td>
                <td>{{match.scenarioTitle}}</td>
                <td>{{match.step}}</td>
               </tr>
            </table>
            -->

        </div>
        <div id="footer">
            <div class="left">Data Generated ${date?string("dd/MM/yyyy HH:mm:ss")}</div>
            <div class="right">JBehave &#169; 2003-2010</div>
            <div class="clear"></div>
        </div>

        <div style="clear: both;"></div>

    </div>

</body>

</html>