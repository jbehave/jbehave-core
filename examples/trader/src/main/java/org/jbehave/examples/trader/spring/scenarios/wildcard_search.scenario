Narrative: 

In order to improve the quality of my integration code
As a scenario writer
I want to compose candidate steps classes via Spring

Scenario: Traders can be searched by name

Given the traders: 
|name|rank|
|Larry|Stooge 3|
|Moe|Stooge 1|
|Curly|Stooge 2|
!-- This is a comment, which will be ignored in the execution
When a wildcard search ".*y" is executed
!-- This is another comment, also ignored, 
but look Ma! I'm on a new line!
Then the traders returned are:
|name|rank|
|Larry|Stooge 3|
|Curly|Stooge 2|


