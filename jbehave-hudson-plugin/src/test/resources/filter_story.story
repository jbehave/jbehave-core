Meta:
@skip true

Scenario: A simple failing scenario
Given a test
When a test fails
Then a tester is unhappy

Scenario: A simple scenario that will be filtered out 
Given a test
When a test is executed
Then a tester is pleased

Scenario: A simple successful scenario
Given a test
When a test is executed
Then a tester is pleased