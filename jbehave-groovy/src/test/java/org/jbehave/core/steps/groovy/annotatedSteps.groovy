import org.jbehave.core.annotations.Given
import org.jbehave.core.annotations.Then
import org.jbehave.core.annotations.When

import static junit.framework.Assert.assertNotNull;

class AnnotatedSteps {

  @Given("a date of \$date")
  def date(Date date) {
    assertNotNull(date);
  }

  @When("\$days days pass")
  def daysPass(int days) {
    assertNotNull(days);
  }

  @Then("the date is \$date")
  def theDate(Date date) {
      assertNotNull(date);
  }


}
