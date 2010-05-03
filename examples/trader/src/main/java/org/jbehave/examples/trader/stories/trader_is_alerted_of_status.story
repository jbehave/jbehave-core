Trader is alerted of status

Narrative:
In order to ensure a quick response
As a trader
I want to monitor stock prices

Scenario:

Given a stock of symbol STK1 and a threshold of 15.0
When the stock is traded at price 5.0
Then the alert status is OFF
When the stock is sold at price 11.0
Then the alert status is OFF
When the stock is sold at price 16.0
!-- The next steps show step priority in action, since both textual steps could be matched by same regex pattern,  
we set a higher priority to the less-greedy pattern
Then the alert status is ON
Then the alert status is currently ON

Scenario:

Given a stock of <symbol> and a <threshold>
When the stock is traded with <price>
Then the trader is alerted with <status>

Examples:
|symbol|threshold|price|status|
|STK1  |15.0|5.0 |OFF|
|STK1  |15.0|11.0|OFF|
|STK1  |15.0|16.0|ON |