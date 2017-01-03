Scenario: Mapping single json string to custom types via annotation

Given the {'aString':'blah','anInteger':'1234','aBigDecimal':'1.234'} json is mapped to MyJsonDto
Then the String value in MyJsonDto should be equal to blah
And the Integer value in MyJsonDto should be equal to 1234
And the BigDecimal value in MyJsonDto should be equal to 1.234

Scenario: Mapping two json strings to custom types via annotation
Given the {'aString':'blah','anInteger':'1234','aBigDecimal':'1.234'} and {'aDouble':'1.11','aBoolean':true} jsons are mapped to the appropriate DTOs
Then the String value in MyJsonDto should be equal to blah
And the Integer value in MyJsonDto should be equal to 1234
And the BigDecimal value in MyJsonDto should be equal to 1.234

And the Double value in MyAnotherOneJsonDto should be equal to 1.11
And the Boolean value in MyAnotherOneJsonDto should be equal to true

Scenario: Mapping string of jsons list to custom types via annotation
Given the [{'aString':'blah','anInteger':'1234','aBigDecimal':'1.234'},{'aString':'blah1','anInteger':'5678','aBigDecimal':'5.678'}] json string is mapped to list of the MyJsonDto
Then the 1-st String value in MyJsonDto list should be equal to blah
And the 2-nd String value in MyJsonDto list should be equal to blah1
And the 1-st Integer value in MyJsonDto list should be equal to 1234
And the 2-nd Integer value in MyJsonDto list should be equal to 5678
And the 1-st BigDecimal value in MyJsonDto list should be equal to 1.234
And the 2-nd BigDecimal value in MyJsonDto list should be equal to 5.678
