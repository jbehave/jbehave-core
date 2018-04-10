Login with Expired Passwords

Narrative:
In order to support security standards around account lockout based on login failures
As a admin
I want to configure account lockout based on login failure policy
As a user
I want to have a limited number of login attempts before my account is locked

Scenario: Authentication failures within lockout limit

Given an organization named HP
And authentication policy for HP:
|lockoutEnabled|lockoutCount|
|true          |3           |
And the users for HP:
|username    |passwordCleartext |loginFailureCount|
|carlyfiorina|password          |4                |
|markhurd    |password          |1                |
When current organization is HP
And user markhurd authenticates with password password
Then user should be authenticated

Scenario: Authentication failures beyond lockout

When current organization is HP
And user carlyfiorina authenticates with password password
Then user should not be authenticated
And authentication failure is Locked

Scenario: Authentication from count 0 to locked out

When current organization is HP
And user markhurd authenticates with password oops
Then user should not be authenticated
And authentication failure is BadCredentials
When user markhurd authenticates with password oops
Then user should not be authenticated
And authentication failure is BadCredentials
When user markhurd authenticates with password oops
Then user should not be authenticated
And authentication failure is BadCredentials
When user markhurd authenticates with password password
Then user should not be authenticated
And authentication failure is Locked
