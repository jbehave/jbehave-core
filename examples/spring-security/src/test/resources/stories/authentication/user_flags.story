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

Scenario: Do the same with UserBuilder (working but hacked)

Given the users for Microsoft:
|username|passwordCleartext|enabled|expired|forcePasswordChange|
|<user>|<pwd>|<enabled>|<expired>|<forcePasswordChange>|
When current organization is Microsoft
And user <username> authenticates with password <password>
Then user should not be authenticated
And authentication failure is <failure>

Examples:
|username|password|<user>|<pwd>|<enabled>|<expired>|<forcePasswordChange>|failure|
|testDisabled|dpassword|testDisabled|dpassword|false|false|false|Disabled|
|testExpired|epassword|testExpired|epassword|true|true|false|AccountExpired|
|testDisabledAndExpired|depassword|testDisabledAndExpired|depassword|false|true|false|Disabled|
|testFPCDisabled|fdpassword|testFPCDisabled|fdpassword|false|false|true|Disabled|
|testFPCExpired|fepassword|testFPCExpired|fepassword|true|true|true|AccountExpired|
|testFPCDisabledAndExpired|fdepassword|testFPCDisabledAndExpired|fdepassword|false|true|true|Disabled|

Scenario: Do the same with UserBuilder (not working but desired)

Given the users for Microsoft:
|username|passwordCleartext|enabled|expired|forcePasswordChange|
|<username>|<password>|<enabled>|<expired>|<forcePasswordChange>|
When current organization is Microsoft
And user <username> authenticates with password <password>
Then user should not be authenticated
And authentication failure is <failure>

Examples:
|username|password|enabled|expired|forcePasswordChange|failure|
|testDisabled|dpassword|false|false|false|Disabled|
|testExpired|epassword|true|true|false|AccountExpired|
|testDisabledAndExpired|depassword|false|true|false|Disabled|
|testFPCDisabled|fdpassword|false|false|true|Disabled|
|testFPCExpired|fepassword|true|true|true|AccountExpired|
|testFPCDisabledAndExpired|fdepassword|false|true|true|Disabled|
