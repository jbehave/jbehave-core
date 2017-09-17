Scenario: Traders can be searched by name

Given the trader ranks: 
|name |rank    |
|Larry|Stooge 3|
|Moe  |Stooge 1|
|Curly|Stooge 2|
Then the current trader activity is: 
|name |trades|
|Larry|30000 |
|Moe  |10000 |
|Curly|20000 |
!-- This is a comment, which will be ignored in the execution
When traders are subset to ".*y" by name
!-- This is another comment, also ignored, 
but look Ma! I'm on a new line!
Then the traders returned are:
|name |rank    |
|Larry|Stooge 3|
|Curly|Stooge 2|

Scenario: Traders can be searched by name in landscape format

Given the trader ranks: 
{transformer=FROM_LANDSCAPE}
|name |Larry   |Moe     |Curly   |
|rank |Stooge 1|Stooge 2|Stooge 3|
Then the current trader activity is: 
|name |trades|
|Larry|30000 |
|Moe  |10000 |
|Curly|20000 |
Then the traders returned are:
|name |rank    |
|Larry|Stooge 3|
|Curly|Stooge 2|


