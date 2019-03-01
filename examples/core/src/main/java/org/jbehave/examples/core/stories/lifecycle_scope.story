Story:  Showing lifecycle behaviour with both scenario and story scope

Lifecycle:
Before:
Scope: STORY
Given I have a bank account

Scope: SCENARIO
Given my balance is 100

Scope: STEP
When I withdraw 2

After:
Scope: STORY
Then my balance is archived

Scope: SCENARIO
Then my balance is in credit

Scope: STEP
When I withdraw 1

Scenario: First scenario
When I withdraw 10
Then my bank account balance should be 85

Scenario: Second scenario
When I withdraw 20
Then my bank account balance should be 75
