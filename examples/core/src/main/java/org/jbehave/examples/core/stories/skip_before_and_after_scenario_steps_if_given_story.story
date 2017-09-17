Scenario: A scenario where before and after steps are executed only once even if there is a given story

GivenStories: org/jbehave/examples/core/stories/given/parametrised.story#{0}
              
When the stock is traded at price 1.1
Then the alert status is ON

Examples:
|symbol|threshold|
|STK1  |1.0|