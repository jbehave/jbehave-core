Meta: 

@theme parametrisation

Scenario: A scenario that depends on a story loaded with relative path

GivenStories: parametrised.story#{0}
              
When the stock is traded at price 1.1
Then the alert status is ON

Examples:
|symbol|threshold|
|STK1  |1.0|
