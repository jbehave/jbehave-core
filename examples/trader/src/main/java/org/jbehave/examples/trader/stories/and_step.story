Narrative:
In order to be more communicative
As a story writer
I want to explain the use of And steps and also show that I can use keywords in scenario title and comments

Scenario: An initial And step should be marked as pending as there is not previous step

!-- What is this And of?  JBehave treats as pending 
And the wind blows
!-- Look Ma' - I can also use keywords in scenario title and step comments!

Scenario: And steps should match the previous step type

Given the wind blows
!-- This And is equivalent to another Given
And the wind blows
!-- This And shows that we can chain multiple And steps
And the wind blows
When the wind blows
!-- This And is equivalent to another When
And the wind blows