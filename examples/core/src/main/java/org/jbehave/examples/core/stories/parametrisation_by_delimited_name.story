Scenario: Use flexible parameters with examples table

Given <client> is logged in
And <client> has a cart
When a <item> is added to the cart
Then cart contains <item>

Examples:
|client|item|
|Rui|chocolate|
|Figueira|car|

Scenario: Use delimited named parameters to look up different values in the examples table

Meta:
@themes Book

Given a user <user> has borrowed books <isbns>
And a user <user2> has borrowed books <isbns2>

Examples:    
    
| user            | isbns                 | user2           | isbns2                |
| user1@dings.com | 1111111111            | user2@dings.com | 2222222222            |

