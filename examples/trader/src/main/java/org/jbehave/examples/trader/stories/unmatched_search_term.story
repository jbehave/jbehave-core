Scenario: Search term does match examples table header 

Given that I am on Google's Homepage
When I enter the search term <ridiculousSearchTerm> and proceed
Then I should see ridiculous things

Examples:
|ridiculousSearchTerm|
|Hello Kitty|

Scenario: Search term does not match examples table header and step is marked as pending

Given that I am on Google's Homepage
When I enter the search term <ridiculousSearchTerm> and proceed
Then I should see ridiculous things

Examples:
|ridiculoussearchterm|
|Hello Kitty|