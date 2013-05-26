A story using GivenStories at story level parametrised by meta properties

Meta: @theme parametrisation
@assetClass FX @symbol STK1 @threshold 1.0

GivenStories: org/jbehave/examples/core/stories/parametrised.story

Scenario: A scenario that is executed after the given stories parametrised by meta
              
When the stock is traded at price 1.1
Then the alert status is ON

