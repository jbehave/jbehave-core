# Examples of replacements required when migrating from 2.x to 3.x 

groovy replaceAllInFiles.groovy scenario GivenScenarios GivenStories
groovy replaceAllInFiles.groovy pom.xml run-scenarios run-stories
groovy replaceAllInFiles.groovy pom.xml scenarioInclude storyInclude
groovy replaceAllInFiles.groovy pom.xml scenarioExclude storyExclude
groovy replaceAllInFiles.groovy build.xml run-scenarios run-stories
groovy replaceAllInFiles.groovy build.xml scenarioInclude storyInclude
groovy replaceAllInFiles.groovy build.xml scenarioExclude storyExclude

