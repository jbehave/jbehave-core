# Examples of replacements required when migrating from 2.x to 3.x 

# Replace .scenario with other extension used for scenarios textual files

groovy replaceAllInFiles.groovy .scenario GivenScenarios GivenStories

# Replace in pom.xml or build.xml 

groovy replaceAllInFiles.groovy [pom.xml|build.xml] scenarioInclude include
groovy replaceAllInFiles.groovy [pom.xml|build.xml] scenarioExclude exclude

# Replace only in pom.xml

groovy replaceAllInFiles.groovy pom.xml run-scenarios run-stories-as-embeddables
groovy replaceAllInFiles.groovy pom.xml render-reports generate-stories-view

# Replace only in build.xml

groovy replaceAllInFiles.groovy build.xml StoryRunnerTask RunStoriesAsEmbeddables
groovy replaceAllInFiles.groovy build.xml ReportRendererTask GenerateStoriesView
