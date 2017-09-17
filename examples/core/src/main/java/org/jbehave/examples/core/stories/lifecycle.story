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

Scenario: Simple scenario
Meta: @simple

When I withdraw 10
Then my bank account balance should be 90

Scenario: Scenario with Examples
Meta: @examples

When I add <value>
Then my bank account balance should be <balance>

Examples:
|value|balance|
|30|130|
|50|150|

Scenario: Scenario with data tables
Meta: @tables

Given these people have bank accounts with balances:
|Name|balance|
|Person1|1000|
|Person2|500|
When I take all their money
Then my bank account balance should be 1600