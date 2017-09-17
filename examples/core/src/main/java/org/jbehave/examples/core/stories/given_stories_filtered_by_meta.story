Scenario: A scenario that depends on a given scenario that does not have matching meta info, 
but the meta filter is ignored in the given story
Meta: @run 

GivenStories: org/jbehave/examples/core/stories/given/scenarios.story#{id1:scenario1}
              
When the stock is traded at price 1.1
Then the alert status is ON

Scenario: A scenario that depends on another given scenario
Meta: @skip

GivenStories: org/jbehave/examples/core/stories/given/scenarios.story#{id2:scenario2}
              
When the stock is traded at price 1.1
Then the alert status is OFF
