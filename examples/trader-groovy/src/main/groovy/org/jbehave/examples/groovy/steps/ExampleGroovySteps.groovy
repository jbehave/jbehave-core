import org.jbehave.core.annotations.Given
import org.jbehave.core.annotations.Then
import org.jbehave.core.annotations.When
import static junit.framework.Assert.assertNotNull
import static junit.framework.Assert.assertEquals

class ExampleGroovySteps {

  @Given("a date of \$date")
  def aDate(Date date) {
    assertNotNull(date)
  }

  @When("\$days days pass")
  def daysPass(int days) {
    assertNotNull(days)
  }

  @Then("the date is \$date")
  def theDate(Date date) {
      assertNotNull(date)
  }

  @Then("some otherwise ambiguous two string step, with \$one and \$two as strings")
  def otherwiseAmbiguous(String two, String one) {
      assertEquals("one", one)
      assertEquals("two", two)
  }


}
