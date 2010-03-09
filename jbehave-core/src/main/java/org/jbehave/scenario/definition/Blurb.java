package org.jbehave.scenario.definition;

public class Blurb {

    public static final Blurb EMPTY = new Blurb("");
    private final String blurb;

    public Blurb(String blurb) {
        this.blurb = blurb;
    }

    public String asString() {
        return blurb;
    }

}
