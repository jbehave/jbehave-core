Scenario:  List is retrieved

When list of pages is retrieved from Redmine at http://demo.redmine.org/projects/rossodisera/wiki
Then the index contains 3 stories

Scenario:  Page is retrieved

When a wiki page is retrieved from Redmine at http://demo.redmine.org/projects/rossodisera/wiki/Wiki
Then the page contains the stories
