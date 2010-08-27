Login with Default Authentication Policy

Narrative:
In order to access the system
As a user with my username and password
I want to login to the system for my organization

Scenario: Valid login

Given an organization named Microsoft
And a default authentication policy for Microsoft
And a user for Microsoft with username billgates and password windows
When current organization is Microsoft
And user billgates authenticates with password windows
Then user should be authenticated

Scenario: Invalid password for valid user

When current organization is Microsoft
And user billgates authenticates with password noway
Then user should not be authenticated
And authentication failure is BadCredentials

Scenario: Invalid username

When current organization is Microsoft
And user nowayjose authenticates with password noway
Then user should not be authenticated
And authentication failure is BadCredentials

Scenario: Invalid company for valid user and password

Given an organization named Oracle
And a default authentication policy for Oracle
And a user for Oracle with username larryellison and password killbill
When current organization is Microsoft
And user larryellison authenticates with password killbill
Then user should not be authenticated
And authentication failure is BadCredentials
When current organization is Oracle
And user billgates authenticates with password windows
Then user should not be authenticated
And authentication failure is BadCredentials

Scenario: Test User Builder

Given organizations named Cisco,HP
And the users for Cisco:
|username|passwordCleartext|
|alice|apassword|
|bob|bpassword|
And the users for HP:
|username|passwordCleartext|
|charlie|cpassword|
When current organization is Cisco
And user alice authenticates with password apassword
Then user should be authenticated
When user bob authenticates with password bpassword
Then user should be authenticated
When current organization is HP
And user charlie authenticates with password cpassword
Then user should be authenticated

