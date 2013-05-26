Story: Any free-text description can go here (and Story: is also optional)

Narrative: The narrative is keyword based

In order to cut my losses
As a Trader
I want to sell all stocks when alerted

Scenario: Trader sells it all and goes to Bermuda.
This scenario shows that the same step pattern can be used for different step types

GivenStories: org/jbehave/examples/core/stories/trader_is_alerted_of_status.story

Given the alert status is OFF
Given a trader of name Mauro
And the alert status is OFF
Given a stock of symbol STK1 and a threshold of 1.5
When the stock is traded at price 2.0
Then the alert status is ON
When the trader sells all stocks
Then the trader is left with no stocks

Scenario: Trader counts the days to go to Bermuda.

Given a date of 2010-06-21
When 2 days pass
Then the date is 2010-06-23
