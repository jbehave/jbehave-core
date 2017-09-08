We want to show that failures in @BeforeScenario methods will not prevent rest of scenario steps to run (marked as NOT PERFORMED).
Also, the @Before/AfterScenario, @Before/AfterStory failure messages should be displayed in the story reports, while   
the @BeforeAfterStories failures are displayed in the Before/AfterStories reports.

Scenario:  

Given the alert status is OFF
Given a trader of name Mauro
And the alert status is OFF
Given a stock of symbol STK1 and a threshold of 1.5
When the stock is traded at price 2.0
Then the alert status is ON
When the trader sells all stocks
Then the trader is left with no stocks