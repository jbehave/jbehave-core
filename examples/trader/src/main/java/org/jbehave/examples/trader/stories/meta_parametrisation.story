Meta: 

@theme parameters

Scenario: scenario with explicitly mentioned meta params

Meta:

@variant named

Given I have specified the <theme>
And a <variant>
Then the theme is 'parameters' with variant 'named'


Scenario: scenario with hidden meta params

Meta:

@variant foo

Given I have some step, that not node explicity mention meta tags
Then the theme is 'parameters' with variant 'foo'
