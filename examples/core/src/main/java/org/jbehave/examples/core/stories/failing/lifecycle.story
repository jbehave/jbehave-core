Story:  Showing lifecycle behaviour

Lifecycle:
Before:

Given I have a bank account
And my balance is 100

After:
Outcome: ANY
Then my balance is printed

Outcome: SUCCESS
Then my balance is archived

Outcome: FAILURE
MetaFilter: +non-archiving
Then my balance is not archived

Scenario: Failing archiving scenario
Meta: @archiving

When I withdraw 10
Then my bank account balance should be 100

Scenario: Failing non-archiving scenario
Meta: @non-archiving

When I withdraw 10
Then my bank account balance should be 100

