
Scenario: Traders search fails

Given the trader ranks: 
|name |rank    |
|Larry|Stooge 3|
|Moe  |Stooge 1|
|Curly|Stooge 2|
!-- Verification fails
Then the traders returned are:
|name |rank    |
|Moe  |Stooge 1|
|Curly|Stooge 2|

