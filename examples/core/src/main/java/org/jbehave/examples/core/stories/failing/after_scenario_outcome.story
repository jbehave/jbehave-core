Story: Showing that @AfterScenario method are executed upon appropriate outcome (ANY, SUCCESS, FAILURE)

Scenario: Pass on a step

Given I do nothing
Then I pass

Scenario: Fail on a step

Given I do nothing
Then I fail