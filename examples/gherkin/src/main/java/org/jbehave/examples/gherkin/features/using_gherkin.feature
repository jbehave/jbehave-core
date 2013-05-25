
Feature: Hello Car

Background: 

Given I have a license

Scenario: Car can drive

Given I have a car
Then I can drive them according to wheels:
| wheels | can_drive |
|   1    |   false   |
|   2    |   false   |
|   3    |   false   |
|   4    |   true    |
