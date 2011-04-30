Scenario: A scenario that verifies priority matching of steps, with the less-greedy pattern given higher priority

!-- Matching regex "a step that has %param"
Given a step that has a parameter
Then the parameter value is "a parameter"
!-- Matching regex "a step that has exactly one %param"
Given a step that has exactly one of the parameters
Then the parameter value is "of the parameters"