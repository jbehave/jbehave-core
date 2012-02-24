Scenario: Use flexible parameters with examples table

Given <client> is logged in
And <client> has a cart
When a <item> is added to the cart
Then cart contains <item>

Examples:
|client|item|
|Rui|chocolate|
|Figueira|car|

