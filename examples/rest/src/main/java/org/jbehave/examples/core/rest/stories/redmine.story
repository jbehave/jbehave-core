Scenario:  Story index is retrieved from Redmine

When index is retrieved from Redmine at http://demo.redmine.org/projects/jbehave/wiki
Then the index contains 2 stories

Scenario:  Story is loaded from Redmine

When story A_story is loaded from Redmine 
Then story contains title 'A story'
When story Another_story is loaded from Redmine 
Then story contains title 'Another story'
