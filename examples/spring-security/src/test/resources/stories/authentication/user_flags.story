Login with User Flags (enabled and expired)

Narrative:
In order to restrict access to users without deleting them
As a admin
I want to disable and expire accounts

Scenario: Disabled user authentication

Given an organization named Microsoft
And a default authentication policy for Microsoft
And a user for Microsoft with username billgates and password windows
And user for Microsoft billgates is disabled
When current organization is Microsoft
And user billgates authenticates with password windows
Then user should not be authenticated
And authentication failure is Disabled

Scenario: Expired user authentication

Given user for Microsoft billgates is enabled
And user for Microsoft billgates is expired
When current organization is Microsoft
And user billgates authenticates with password windows
Then user should not be authenticated
And authentication failure is AccountExpired

Scenario: Disabled and expired user authentication

Given user for Microsoft billgates is disabled
And user for Microsoft billgates is expired
When current organization is Microsoft
And user billgates authenticates with password windows
Then user should not be authenticated
And authentication failure is Disabled

Scenario: Force password change user authentication
Given the users for Microsoft:
|username|passwordCleartext|enabled|expired|forcePasswordChange|
|testFPC|fpcpassword|true|false|true|
When current organization is Microsoft
And user testFPC authenticates with password fpcpassword
Then user should not be authenticated
And authentication failure is CredentialsExpired

Scenario: Do the same with UserBuilder

Given the users for Microsoft:
|username|passwordCleartext|enabled|expired|forcePasswordChange|
|testDisabled|dpassword|false|false|false|
|testExpired|epassword|true|true|false|
|testDisabledAndExpired|depassword|false|true|false|
|testFPCDisabled|fdpassword|false|false|true|
|testFPCExpired|fepassword|true|true|true|
|testFPCDisabledAndExpired|fdepassword|false|true|true|
When current organization is Microsoft
And user testDisabled authenticates with password dpassword
Then user should not be authenticated
And authentication failure is Disabled
When user testExpired authenticates with password epassword
Then user should not be authenticated
And authentication failure is AccountExpired
When user testDisabledAndExpired authenticates with password depassword
Then user should not be authenticated
And authentication failure is Disabled
When user testFPCDisabled authenticates with password fdpassword
Then user should not be authenticated
And authentication failure is Disabled
When user testFPCExpired authenticates with password fepassword
Then user should not be authenticated
And authentication failure is AccountExpired
When user testFPCDisabledAndExpired authenticates with password fdepassword
Then user should not be authenticated
And authentication failure is Disabled
