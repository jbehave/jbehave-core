Scenario:  Story index is retrieved from REST provider.  Stories are loaded and uploaded.

Given REST provider is XWiki
When index is retrieved from http://localhost:8080/xwiki/rest/wikis/xwiki/spaces/Main/pages
Then the index is not empty
When story a_story is loaded
Then story text contains 'a_story'
When story another_story is loaded
Then story text contains 'another_story'
When story a_story is uploaded appending 'a modification'
When story a_story is loaded
Then story text contains 'a modification'

