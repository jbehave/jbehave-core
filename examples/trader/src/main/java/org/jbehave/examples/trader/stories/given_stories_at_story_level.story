
GivenStories: org/jbehave/examples/trader/stories/trader_is_alerted_of_status.story

Scenario: Trader counts the days to go to Bermuda.

Given a date of 2010-06-21
When 2 days pass
Then the date is 2010-06-23
