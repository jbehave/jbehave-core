We want to show that failures in @AfterStories methods will appear in the AfterStories report.

Scenario:  

Given the alert status is OFF
Given a trader of name Mauro
And the alert status is OFF
Given a stock of symbol STK1 and a threshold of 1.5
When the stock is traded at price 2.0
Then the alert status is ON
When the trader sells all stocks
Then the trader is left with no stocks