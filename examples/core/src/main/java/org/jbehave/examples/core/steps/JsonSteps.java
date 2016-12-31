package org.jbehave.examples.core.steps;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jbehave.core.annotations.AsJson;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.junit.Assert;

public class JsonSteps {

    private MyJsonDto json;
    private List<MyJsonDto> listJson;

    @Given("the %json json is mapped to dto")
    public void givenTheJsonDto(final MyJsonDto json) {
        this.json = json;
    }

    @Given("the %listJson json dto list")
    public void givenTheListJsonDto(final List<MyJsonDto> listJson) {
        this.listJson = listJson;
    }

    @Then("the String value in DTO should equal to %value")
    public void checkStringDtoValue(final String value) {
        Assert.assertEquals(this.json.getString(), value);
    }

    @Then("the %index{-st|-nd} String value in DTO list should equal to %value")
    public void checkStringValueInListDto(final int index, final String value) {
        Assert.assertEquals(listJson.get(index - 1).getString(), value);
    }

    @Then("the Integer value in DTO should equal to %value")
    public void checkIntegerDtoValue(final Integer value) {
        Assert.assertEquals(this.json.getInteger(), value);
    }

    @Then("the %index{-st|-nd} Integer value in DTO list should equal to %value")
    public void checkIntegerValueInListDto(final int index, final Integer value) {
        Assert.assertEquals(listJson.get(index - 1).getInteger(), value);
    }

    @Then("the BigDecimal value in DTO should equal to %value")
    public void checkBigDecimalDtoValue(final BigDecimal value) {
        Assert.assertEquals(this.json.getBigDecimal(), value);
    }

    @Then("the %index{-st|-nd} BigDecimal value in DTO list should equal to %value")
    public void checkBigDecimalValueInListDto(final int index, final BigDecimal value) {
        Assert.assertEquals(listJson.get(index - 1).getBigDecimal(), value);
    }

    @AsJson
    public static class MyJsonDto {

        private String aString;
        private Integer anInteger;
        private BigDecimal aBigDecimal;

        public String getString() {
            return aString;
        }

        public Integer getInteger() {
            return anInteger;
        }

        public BigDecimal getBigDecimal() {
            return aBigDecimal;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

}
