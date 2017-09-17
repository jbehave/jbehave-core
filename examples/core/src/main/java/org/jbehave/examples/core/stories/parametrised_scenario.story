Scenario: Parameter does match examples table header

Given that I am on Google's Homepage
When I enter the search term <ridiculousSearchTerm> and proceed
Then I should see ridiculous things

Examples:
|ridiculousSearchTerm|
|Hello Kitty|
