class ScalaSteps {
    
  @org.jbehave.core.annotations.Given("a date of $date")
  def aDate(date: java.util.Date) {
     org.junit.Assert.assertNotNull(date)
  }

  @org.jbehave.core.annotations.When("$days days pass")
  def daysPass(days: Int) {
     org.junit.Assert.assertNotNull(days)
  }

  @org.jbehave.core.annotations.Then("the date is $date")
  def theDate(date: java.util.Date) {
     org.junit.Assert.assertNotNull(date)
  }

  override def toString(): String = "ScalaSteps";

}