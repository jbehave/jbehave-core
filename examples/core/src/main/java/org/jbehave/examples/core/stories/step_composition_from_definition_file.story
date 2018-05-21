Meta:

@theme parametrisation

Scenario: Using a composite step with normal parameter matching

Given I have a bank account with balance 50
When I withdraw 10
Then my bank account balance should be 40


Scenario: Using a composite step in a parameterised scenario

Given I have a bank account with balance <startBalance>
When I withdraw 15
Then my bank account balance should be 25

Examples:
|startBalance|
|40          |


Scenario: Composite nested steps in action.

Given I have a balance 80 and withdraw 20
Then my bank account balance should be 60
