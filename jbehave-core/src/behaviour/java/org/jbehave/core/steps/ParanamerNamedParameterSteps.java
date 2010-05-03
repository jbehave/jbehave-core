package org.jbehave.core.steps;

class ParanamerNamedParameterSteps extends Steps {
    String ith;
    String nth;

    public void methodWithNamedParametersInNaturalOrder(String ith, String nth){
        this.ith = ith;
        this.nth = nth;
    }

    public void methodWithNamedParametersInInverseOrder(String nth, String ith){
        this.ith = ith;
        this.nth = nth;
    }

}
