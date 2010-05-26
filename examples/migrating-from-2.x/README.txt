# When migrating from 2.x to 3.x there are some simple but repetitive tasks
# that can be helped by some scripting.

groovy replaceAllInFiles.groovy scenario GivenSenarios GivenStories
groovy replaceAllInFiles.groovy pom.xml run-scenarios run-stories
groovy replaceAllInFiles.groovy pom.xml scenarioInclude storyInclude
groovy replaceAllInFiles.groovy pom.xml scenarioExclude storyExclude
groovy replaceAllInFiles.groovy build.xml run-scenarios run-stories
groovy replaceAllInFiles.groovy build.xml scenarioInclude storyInclude
groovy replaceAllInFiles.groovy build.xml scenarioExclude storyExclude

