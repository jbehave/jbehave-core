The JBehave Hudson plugin can be built with the maven package goal
$ mvn package 

For development and Testing use, Maven can start a Hudson Instance with the JBehave plugin and dependencies in Jetty
$ mvn package hpi:run -Djetty.port=8090

Installation instructions 
* The jbehave-hudson-plugin depends on the xUnit plugin and requires a minimum of version 1.9 to be installed
* The jbehave-hudson-plugin.hpi file built by “mvn package” can be installed into Hudson via the Advanced tab of the “Manage Hudson > Manage Plugins” screen

Configuring a project with JBehave test reporting
* Within the project configuration page, under the “Post-build Actions” section select “Publish testing tools result report”. A drop box labelled “Add” will be displayed and select “JBehave 3.x” from the List. Now add the file path pattern of the JBehave reports in your project to the text field labelled “JBehave-3.x Pattern”. Typically this is “**/ jbehave/*.xml“
