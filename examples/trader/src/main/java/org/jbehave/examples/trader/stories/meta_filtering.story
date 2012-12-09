A story description
over multiple lines

Meta:

!-- A first ignored comment
@theme filtering
!-- Another ignored comment
@author Mauro

Scenario: A scenario to be skipped
and not executed

Meta:  

@skip

Given I do nothing

Scenario: A scenario with an author

Meta:  

Given I do nothing

Scenario:  A scenario with examples whose rows can be filtered on meta

Meta: @run

Given I do nothing

Examples: 
|Meta:|Parameter|
|@run yes|value|
|@run not|value|


Scenario:  A parametrised scenario that is yet to be implemented

Meta:
@run pending

When I lookup my <car reservation>
Then I will see <car type>

Examples:
| car reservation | car type | 
| 1223395844      | midsize  | 
