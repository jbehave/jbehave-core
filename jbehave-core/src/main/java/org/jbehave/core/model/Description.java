package org.jbehave.core.model;

public class Description {

    public static final Description EMPTY = new Description("");
    private final String blurb;

    public Description(String blurb) {
        this.blurb = blurb;
    }

    public String asString() {
        return blurb;
    }

}
