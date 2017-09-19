Scenario: A scenario that is executed after the given stories parametrised by meta
              
Meta: @theme parametrisation
@assetClass FX @symbol STK1 @threshold 1.0

GivenStories: org/jbehave/examples/core/stories/given/parametrised.story

When the stock is traded at price 1.1
Then the alert status is ON

