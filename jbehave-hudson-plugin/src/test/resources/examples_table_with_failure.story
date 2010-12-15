Scenario: A scenario with a example table with a single failure
Given a test with <param1>
When a test is executed with <param2>
Then a tester is pleased

Examples:
|param1|param2|
|value1|value2|
|value1|fail|
|value1|value2|

Scenario: A scenario with a example table with a different failure
Given a test with <param1>
When a test is executed with <param2>
Then a tester is pending
And a tester is pending

Examples:
|param1|param2|
|value1|fail|
|value1|value2|
|value1|fail|


