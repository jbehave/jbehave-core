Narrative: 
In order to make story development easier
As a Story Developer
I want to auto-generate method stubs for pending steps annotated by @Pending

Scenario: Show that steps that don't match methods are treated as pending and @Pending annotated method stubs are generated 

Given a step has not been defined
And another step has not been defined
When a step has not been defined
Then a step has not been defined

Scenario: Show that steps that match methods already annotated by @Pending are treated as pending but no method stubs are generated

Given a step is annotated as pending
When a step is annotated as pending
Then a step is annotated as pending

Scenario: Show that multiple And steps can be used

Given I am not logged in
And I have been redirected from my intended page to the login page
When I log in with good credentials
Then I am redirected to my intended page
And I am logged in