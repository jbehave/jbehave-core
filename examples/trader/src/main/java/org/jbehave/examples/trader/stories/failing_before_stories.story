We want to show that failures in @BeforeStories methods will mark rest of steps as NOT PERFORMED, if story and scenario state is not reset via the StoryControls

Scenario:  

Given the alert status is OFF
Given a trader of name Mauro
And the alert status is OFF
Given a stock of symbol STK1 and a threshold of 1.5
When the stock is traded at price 2.0
Then the alert status is ON
When the trader sells all stocks
Then the trader is left with no stocks