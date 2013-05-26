Scenario: As a story implementer I want to monitor parametrised successful step - before and after execution

Given a successful step
Then following step should be performed

Scenario: As a story implementer I want to monitor parametrized failing step - before and after execution

Given a failing step
Then following step should not be performed
