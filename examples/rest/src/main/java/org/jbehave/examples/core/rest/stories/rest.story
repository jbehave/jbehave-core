Scenario:  Story index is retrieved from REST provider

Given REST provider is XWiki
When index is retrieved from http://localhost:8080/xwiki/rest/wikis/xwiki/spaces/Main/pages
Then the index is not empty

Scenario:  Story is loaded from REST provider

When story a_story is loaded
Then story text contains 'a_story'
When story another_story is loaded
Then story text contains 'another_story'

Scenario:  Story is uploaded to REST provider

When story a_story is uploaded appending 'a modification'
When story a_story is loaded
Then story text contains 'a modification'

