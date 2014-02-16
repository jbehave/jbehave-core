Scenario:  Story index is retrieved from REST provider.  Stories are loaded and uploaded.

Given REST provider is XWiki
When index is retrieved from http://localhost:8080/xwiki/rest/wikis/xwiki/spaces/Main/pages
Then the index is not empty
When story first text contains 'first'
When story second text contains 'second'
When story first is uploaded appending 'a modification'
When story first text contains 'a modification'
