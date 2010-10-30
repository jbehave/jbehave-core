Scenario: A scenario with failed step

Given I do nothing
Then I fail

Scenario: A scenario that is not executed because if followed a failed scenario

Given I do nothing
