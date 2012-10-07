Scenario:  I want to show that different parameter delimiters can be configured via the ParameterControls

Given a [parameter]

Examples:
|parameter|
|value|

Scenario:  I want to show that parameter values are correctly delimited, even when the values are overlapping

Given a stock of symbol 10ABCDEF and a threshold of 10
Given a stock of symbol ABC10DEF and a threshold of 10
Given a stock of symbol ABCDEF10 and a threshold of 10
 
Scenario:  I want to show that parameter values are correctly delimited, but not in the table parameter

Given the traders: 
|name | rank   |
|joe  | topdog |
And a stock of symbol topdog and a threshold of 10
