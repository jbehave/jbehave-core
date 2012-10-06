Scenario:  I want to show that different parameter delimiters can be configured via the ParameterControls

Given a [parameter]

Examples:
|parameter|
|value|

Scenario:  I want to show that parameter values are correctly delimited in the reports, 
even when the values are overlapping

Given a stock of symbol 10ABCDEF and a threshold of 10
Given a stock of symbol ABC10DEF and a threshold of 10
Given a stock of symbol ABCDEF10 and a threshold of 10
