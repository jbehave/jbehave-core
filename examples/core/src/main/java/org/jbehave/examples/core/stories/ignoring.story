
Scenario: Ignoring steps

Given a successful step
When ignore steps failure occurs
Then step is ignored
Then step is ignored
