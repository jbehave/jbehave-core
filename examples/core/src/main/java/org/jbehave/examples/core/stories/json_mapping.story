Scenario: Mapping a single json string to custom types via annotation

Given a json {'string':'blah','integer':'1234','bigDecimal':'1.234'}
Then the String value is blah
And the Integer value is 1234
And the BigDecimal value is 1.234

Scenario: Mapping two separate json strings to custom types via annotation

Given a json {'string':'blah','integer':'1234','bigDecimal':'1.234'}
And another json {'doubleVar':'1.11','booleanVar':'true'}
Then the String value is blah
And the Integer value is 1234
And the BigDecimal value is 1.234
And the Double value is 1.11
And the Boolean value is true

Scenario: Mapping a list of jsons to custom types via annotation

Given a list of jsons [{'string':'blah','integer':'1234','bigDecimal':'1.234'},{'string':'blah1','integer':'5678','bigDecimal':'5.678'}]
Then the 1-st String value in list is blah
And the 2-nd String value in list is blah1
And the 1-st Integer value in list is 1234
And the 2-nd Integer value in list is 5678
And the 1-st BigDecimal value in list is 1.234
And the 2-nd BigDecimal value in list is 5.678
