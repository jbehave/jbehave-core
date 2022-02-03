import org.jbehave.core.annotations.Given
import org.jbehave.core.annotations.Then
import org.jbehave.core.annotations.When

class GroovySteps {

  @Given("a date of \$date")
  def date(Date date) {
    org.junit.Assert.assertNotNull(date)
  }

  @When("\$days days pass")
  def daysPass(int days) {
    org.junit.Assert.assertNotNull(days)
  }

  @Then("the date is \$date")
  def theDate(Date date) {
    org.junit.Assert.assertNotNull(date)
  }

  @Then("some otherwise ambiguous two string step, with \$one and \$two as strings")
  def otherwiseAmbiguous(String two, String one) {
    org.junit.Assert.assertEquals("one", one)
    org.junit.Assert.assertEquals("two", two)
  }


}
