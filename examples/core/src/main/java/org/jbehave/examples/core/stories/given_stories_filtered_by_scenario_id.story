Scenario: A scenario that depends on a given scenario

GivenStories: org/jbehave/examples/core/stories/given/scenarios.story#{id1:scenario1}
              
When the stock is traded at price 1.1
Then the alert status is ON

Scenario: A scenario that depends on another given scenario

GivenStories: org/jbehave/examples/core/stories/given/scenarios.story#{id2:scenario2}
              
When the stock is traded at price 1.1
Then the alert status is OFF
