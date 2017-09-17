Meta: 

@theme parametrisation

Scenario: Composite steps in action.  

!-- Composite steps are identified by the @Composite method-level annotation, which is independent of the 
!-- @Given/@When/@Then annotations.  The @Composite is optional and complements any of the @Given/@When/@Then annotations.
!-- Once the composite step is matched (via any of the supported mechanisms, e.g. normal parameters matching or a parametrised scenario), 
!-- if the @Composite annotation is found on the matched method, the "composed" steps defined in the @Composite annotations are created 
!-- using the parameters specified in the @Named annotations of the composite step.   In other words, the composed steps are treated 
!-- as a group of parametrised steps, much in the same way as the steps in a parametrised scenario.

Scenario: Using a composite step with normal parameter matching

!-- Annotated method:
!-- @Given("%customer has previously bought a %product") // used in normal parameter matching
!-- @Composite(steps = { "Given <customer> is logged in", 
!--                      "Given <customer> has a cart", 
!--                      "When a <product> is added to the cart" }) 
!-- public void aCompositeStep(@Named("customer") String customer, @Named("product") String product) { // composed steps use these named parameters 
!-- }

Given Mr Jones has previously bought a ticket

Scenario: Using a composite step in a parameterised scenario

!-- Annotated method:
!-- @Given("<customer> has previously bought a <product>") // used in parameterised scenario
!-- @Composite(steps = { "Given <customer> is logged in", 
!--                      "Given <customer> has a cart", 
!--                      "When a <product> is added to the cart" })
!-- public void aCompositeStep(@Named("customer") String customer, @Named("product") String product) { // composed steps use these named parameters 
!-- }

Given <customer> has previously bought a <product>

Examples:
|customer|product|
|Mr Jones|ticket|

