Scenario: Verify aliases from resources

Given a plan with Google calendar date of <date>
Then the complainant should receive an amount of <amount>
Then the petitioner should receive an amount of <amount>

Examples:
|date       |amount|
|none       |0.0   |
|01/06/2010 |2.15  |
