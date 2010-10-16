import org.jbehave.core.annotations.Given
import org.jbehave.core.annotations.When

class AndSteps {
  @Given("the wind blows")
  def givenWindBlows() {
    System.err.println("given the wind blows");
  }

  @When("the wind blows")
  def whenWindBlows() {
    System.err.println("when the wind blows");
  }
}
