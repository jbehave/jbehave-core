
Meta: @skip

Scenario:  One

When a variable of name one is processed
Then the context includes the name one 

Scenario:  Two

When a variable of name two is processed
Then the context includes the name two
And the context does not include the name one


