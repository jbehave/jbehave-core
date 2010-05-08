Scenario: A scenario with failed step

Given the traders:
|name |rank    |
|Larry|Stooge 3|
|Moe  |Stooge 1|
|Curly|Stooge 2|
When traders are subset to ".*y" by name
Then the traders returned are:
|name|rank    |
|Moe |Stooge 1|

Scenario: A scenario with pending and non executed steps

Given the traders:
|name |rank    |
|Larry|Stooge 3|
|Moe  |Stooge 1|
|Curly|Stooge 2|
When traders are subset to ".*y" by name
Then the traders returned are:
|name|rank    |
|Moe |Stooge 1|
