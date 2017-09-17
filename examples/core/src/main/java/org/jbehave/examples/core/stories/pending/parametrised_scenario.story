Scenario: Parameter does not match examples table header and step is marked as pending

Given that I am on Google's Homepage
When I enter the search term <ridiculousSearchTerm> and proceed
Then I should see ridiculous things

Examples:
|ridiculoussearchterm|
|Hello Kitty|