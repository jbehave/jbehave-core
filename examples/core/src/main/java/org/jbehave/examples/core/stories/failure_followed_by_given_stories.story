Story: Showing that using given stories in a passing scenario following a scenario where are a failure occurred resets the overall 
build result of the story

Scenario: Fail on a step

Given I do nothing
Then I fail

Scenario: Pass after given stories

GivenStories: org/jbehave/examples/core/stories/do_nothing.story

Given I do nothing
Then I pass
