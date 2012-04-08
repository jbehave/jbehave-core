Scenario: A scenario that depends on given stories parametrised by examples. 
Here the entire scenario is parametrised by examples, i.e. the scenario is executed for each examples row.

Meta: @assetClass FX

GivenStories: org/jbehave/examples/trader/stories/select_stock_exchange.story

Given a stock of symbol <symbol> and a threshold of <threshold>
When the stock is traded at price <price>
Then the alert status is <status>
 
Examples:    
|stockExchange|symbol|threshold|price|status|
|NASDAQ|STK1|10.0|5.0|OFF|
|FTSE|STK1|10.0|11.0|ON|