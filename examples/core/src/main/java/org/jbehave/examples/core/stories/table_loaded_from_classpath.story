Meta: 

@theme parametrisation

Scenario:  Table parameter loaded from a classpath resource

Given the traders: org/jbehave/examples/core/stories/traders.table
!-- This is a comment, which will be ignored in the execution
When traders are subset to ".*y" by name
!-- This is another comment, also ignored, 
but look Ma! I'm on a new line!
Then the traders returned are:
|name |rank    |
|Larry|Stooge 3|
|Curly|Stooge 2|

Scenario: Parameters table loaded from a classpath resource

Given a stock of <symbol> and a <threshold>
When the stock is traded with <price>
Then the trader is alerted with <status>

Examples:
org/jbehave/examples/core/stories/trades.table