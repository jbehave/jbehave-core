Login with Expired Passwords

Narrative:
In order to support password expiration policies
As a admin
I want to configure password expiration policies

Scenario: Authentication within the timeframe

Given an organization named Cisco
And authentication policy for Cisco:
|passwordAutoExpire|passwordExpiryDays|
|true|30|
And the users for Cisco:
|username|passwordCleartext|lastPasswordResetDate|
|lenbosack|password|t-15|
|sandylerner|password|t-45|
When current organization is Cisco
And user lenbosack authenticates with password password
Then user should be authenticated

Scenario: Authentication outside the timeframe

When current organization is Cisco
And user sandylerner authenticates with password password
Then user should not be authenticated
And authentication failure is CredentialsExpired


