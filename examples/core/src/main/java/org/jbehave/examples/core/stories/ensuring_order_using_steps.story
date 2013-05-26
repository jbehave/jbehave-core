Story: UsingSteps should honor steps instance order

Scenario: First (repeated because Heisenbug)
When running @BeforeScenario
Then A should be called before B
And B should be called before C

Scenario: Second (repeated because Heisenbug)
When running @BeforeScenario
Then A should be called before B
And B should be called before C

Scenario: Third (repeated because Heisenbug)
When running @BeforeScenario
Then A should be called before B
And B should be called before C

Scenario: Fourth (repeated because Heisenbug)
When running @BeforeScenario
Then A should be called before B
And B should be called before C

Scenario: Fifth (repeated because Heisenbug)
When running @BeforeScenario
Then A should be called before B
And B should be called before C


