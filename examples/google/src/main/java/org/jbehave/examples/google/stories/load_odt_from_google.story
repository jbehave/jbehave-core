
Scenario: A document is loaded from Google Docs.  
The credentials are provided via env vars GOOGLE_USER and GOOGLE_PASSWORD.  
The doc a_story.odt must be loaded to Google Docs.

Given Google feed https://docs.google.com/feeds/default/private/full/
When story a_story is loaded from feed 
Then content is same as org/jbehave/examples/google/steps/a_story.txt