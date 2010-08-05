Narrative: 

In order to improve the quality of my integration code
As a scenario writer
I want to compose candidate steps classes via PicoContainer

Scenario: Traders can be searched by name.   

Given the traders: 
|name |rank    |
|Larry|Stooge 3|
|Moe  |Stooge 1|
|Curly|Stooge 2|
When traders are subset to ".*y" by name
Then the traders returned are:
|name |rank    |
|Larry|Stooge 3|
|Curly|Stooge 2|


