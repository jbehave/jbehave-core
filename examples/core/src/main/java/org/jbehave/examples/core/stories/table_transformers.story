Scenario: Table can be in landscape format

Given the table: 
|name |Larry   |Moe     |Curly   |
|rank |Stooge 1|Stooge 2|Stooge 3|
Then the table transformed by FROM_LANDSCAPE is: 
|name|rank|
|Larry|Stooge 1|
|Moe|Stooge 2|
|Curly|Stooge 3|

Scenario: Table can be formatted

Given the table: 
|  name | rank     |
|  Larry|Stooge 1 |
| Moe  |  Stooge 2    |
|    Curly|       Stooge 3|
Then the table transformed by FORMATTING is: 
|name |rank    |
|Larry|Stooge 1|
|Moe  |Stooge 2|
|Curly|Stooge 3|
