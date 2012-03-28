Scenario: Supported action and/or event story
Meta:
@supportedActions TEST,CREATE,CANCEL,TERMINATE,AMEND,RECREATE,FACILITATE,SLEEP
@notSupportedActions FIX,SCHEDULE,RESCHEDULE,DETERMINE
@supportedEventTypes NEW,FRESH,CANCELLED,AMENDED,TERMINATED
@notSupportedEventTypes DONE,FETCHED,CREATED,TESTED,FIXED

Given a message with <actionSupportability> action and <eventTypeSupportability> event
When it is received
Then message is consumed without error

Examples:
|actionSupportability   |eventTypeSupportability|
|supported              |supported              |
|notSupported           |supported              |
|supported              |notSupported           |

