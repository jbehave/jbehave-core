Scenario: A scenario that depends on a given story with parameters specified as anchor pointing to specific examples row. 
The presence of the anchor implies that the scenario is executed normally and not parametrised by examples, i.e. that the scenario is not executed for each examples row.

Meta: @theme parametrisation
@assetClass FX

GivenStories: org/jbehave/examples/core/stories/given/parametrised.story#{0}
              
When the stock is traded at price 1.1
Then the alert status is ON

Examples:
|symbol|threshold|
|STK1  |1.0|
|STK2  |2.0|
