# Examples of replacements required when migrating from 2.x to 3.x 
# Replace pom.xml with build.xml if using Ant

groovy replaceAllInFiles.groovy scenario GivenScenarios GivenStories
groovy replaceAllInFiles.groovy pom.xml render-reports generate-stories-view
groovy replaceAllInFiles.groovy pom.xml run-scenarios run-stories
groovy replaceAllInFiles.groovy pom.xml scenarioInclude storyInclude
groovy replaceAllInFiles.groovy pom.xml scenarioExclude storyExclude

