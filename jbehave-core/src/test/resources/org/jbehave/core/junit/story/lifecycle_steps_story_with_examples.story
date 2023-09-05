Lifecycle:
Before:
Scope: STORY
Given a variable x with value 1
Scope: SCENARIO
When I multiply x by 2
When I multiply x by <scenarioMultiplier>
Scope: STEP
When I multiply x by 3
When I multiply x by <scenarioMultiplier>
After:
Scope: STEP
When I multiply x by 6
When I multiply x by <scenarioMultiplier>
Scope: SCENARIO
Outcome: ANY
When I multiply x by 7
When I multiply x by <scenarioMultiplier>
Outcome: SUCCESS
When I multiply x by 8
When I multiply x by <scenarioMultiplier>
Scope: STORY
Outcome: ANY
When I multiply x by 9
Outcome: SUCCESS
!-- 1 *
!--   (2 * 4) *
!--     (3 * 4) * 4 * (6 * 4) *
!--   (7 * 4) * (8 * 4) *
!--   (2 * 5) *
!--     (3 * 5) * 5 * (6 * 5) *
!--   (7 * 5) * (8 * 5) *
!-- 9
Then x should equal 2006581248000000

Scenario: x multiplied by 5
When I multiply x by <scenarioMultiplier>
Examples:
| scenarioMultiplier |
| 4                  |
| 5                  |
