Story:  Showing lifecycle behaviour with scope

Lifecycle:
Before:
Scope: STORY

Given I have a bank account
And my balance is 100

Scope: SCENARIO

When my balance is printed

After:
Scope: STORY
Outcome: ANY
Then my balance is printed

Scenario: First scenario

When I withdraw 10
Then my bank account balance should be 90

Scenario: Second scenario

When I withdraw 10
Then my bank account balance should be 80
