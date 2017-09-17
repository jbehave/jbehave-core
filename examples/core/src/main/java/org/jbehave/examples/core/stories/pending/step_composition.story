Meta: 

@theme parametrisation

Scenario: Using a composite step where one of the composed steps is not found.  
The composed step not found should be reported as pending and the subsequent composed steps not performed.

!-- Annotated method:
!-- @Given("%customer returns to cart")
!-- @Composite(steps = { "Given step not found", 
!--                      "Given <customer> has a cart", })
!-- public void aCompositeStep(@Named("customer") String customer) { // composed steps use these named parameters 
!-- }

Given Mr Jones returns to cart
