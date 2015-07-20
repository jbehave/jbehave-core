
Scenario: Ignoring steps after first

Given a successful step
When ignore steps failure occurs
Then step is ignored
Then step is ignored

Scenario: A new step of steps

Given a successful step
