class Ladder {

    @org.jbehave.core.annotations.Given("a ladder")
    def givenALadder() {
        println("I am a ladder")
    }

    @org.jbehave.core.annotations.When("I climb it")
    def whenIClimbIt() {
        println("I'm climbing ... ")
    }

    @org.jbehave.core.annotations.Then("I get to the top")
    def thenIGetToTheTop() {
        println("... at top!")
    }
    
    override def toString(): String = "Ladder";

}