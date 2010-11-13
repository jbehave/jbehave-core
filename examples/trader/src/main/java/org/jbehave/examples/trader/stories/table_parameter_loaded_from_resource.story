Scenario: Traders can be loaded as table parameter from a classpath resource

Given the traders: org/jbehave/examples/trader/stories/traders.table
!-- This is a comment, which will be ignored in the execution
When traders are subset to ".*y" by name
!-- This is another comment, also ignored, 
but look Ma! I'm on a new line!
Then the traders returned are:
|name |rank    |
|Larry|Stooge 3|
|Curly|Stooge 2|


