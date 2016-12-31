Scenario:  Mapping json string to custom types via annotation

Given the {'aString':'blah','anInteger':'1234','aBigDecimal':'1.234'} json is mapped to dto
Then the String value in DTO should equal to blah
And the Integer value in DTO should equal to 1234
And the BigDecimal value in DTO should equal to 1.234

Given the [{'aString':'blah','anInteger':'1234','aBigDecimal':'1.234'},{'aString':'blah1','anInteger':'5678','aBigDecimal':'5.678'}] json dto list
Then the 1-st String value in DTO list should equal to blah
And the 2-nd String value in DTO list should equal to blah1
And the 1-st Integer value in DTO list should equal to 1234
And the 2-nd Integer value in DTO list should equal to 5678
And the 1-st BigDecimal value in DTO list should equal to 1.234
And the 2-nd BigDecimal value in DTO list should equal to 5.678
