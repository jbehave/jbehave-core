require 'java'

java_package 'org.jbehave.examples.jruby'

class JRubySteps

   java_annotation 'org.jbehave.core.annotations.Given("a date of $date")'
   java_signature 'void givenDate(java.util.Date)'
   def date(date)
     org.junit.Assert.assertNotNull(date)
   end

  java_annotation 'org.jbehave.core.annotations.When("$days days pass")'
  java_signature 'void whenDaysPass(int)'
  def daysPass(days)
     org.junit.Assert.assertNotNull(days)
  end

  java_annotation 'org.jbehave.core.annotations.Then("the date is $date")'
  java_signature 'void thenTheDate(java.util.Date)'
  def theDate(date)
     org.junit.Assert.assertNotNull(date)
  end

end
