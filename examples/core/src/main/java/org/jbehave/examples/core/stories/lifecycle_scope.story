Story:  Showing lifecycle behaviour with both scenario and story scope

Lifecycle:
Before:
Scope: STORY

Given I have a bank account

Scope: SCENARIO

Given my balance is 100

After:
Scope: STORY
Then my balance is archived

Scope: SCENARIO
Then my balance is in credit

Scenario: First scenario

When I withdraw 10
Then my bank account balance should be 90

Scenario: Second scenario

When I withdraw 20
Then my bank account balance should be 80

