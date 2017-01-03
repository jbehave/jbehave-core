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

    private MyJsonDto myJsonDto;
    private MyAnotherOneJsonDto myAnotherOneJsonDto;
    private List<MyJsonDto> listJson;

    @Given("the %myJsonDto json is mapped to MyJsonDto")
    public void givenTheJsonDto(final MyJsonDto myJsonDto) {
        this.myJsonDto = myJsonDto;
    }

    @Given("the %myJsonDto and %anotherJson jsons are mapped to the appropriate DTOs")
    public void givenTheJsonDtos(final MyJsonDto myJsonDto, final MyAnotherOneJsonDto anotherJson) {
        this.myJsonDto = myJsonDto;
        myAnotherOneJsonDto = anotherJson;
    }

    @Given("the %listJson json string is mapped to list of the MyJsonDto")
    public void givenTheListJsonDto(final List<MyJsonDto> listJson) {
        this.listJson = listJson;
    }

    @Then("the String value in MyJsonDto should be equal to %value")
    public void checkStringDtoValue(final String value) {
        Assert.assertEquals(this.myJsonDto.getString(), value);
    }

    @Then("the Double value in MyAnotherOneJsonDto should be equal to %value")
    public void checkMyAnotherOneJsonDtoStringValue(final Double value) {
        Assert.assertEquals(this.myAnotherOneJsonDto.getDouble(), value);
    }

    @Then("the %index{-st|-nd} String value in MyJsonDto list should be equal to %value")
    public void checkStringValueInListDto(final int index, final String value) {
        Assert.assertEquals(listJson.get(index - 1).getString(), value);
    }

    @Then("the Integer value in MyJsonDto should be equal to %value")
    public void checkIntegerDtoValue(final Integer value) {
        Assert.assertEquals(this.myJsonDto.getInteger(), value);
    }

    @Then("the Boolean value in MyAnotherOneJsonDto should be equal to %value")
    public void checkMyAnotherOneJsonDtoIntegerValue(final Boolean value) {
        Assert.assertEquals(this.myAnotherOneJsonDto.getBoolean(), value);
    }

    @Then("the %index{-st|-nd} Integer value in MyJsonDto list should be equal to %value")
    public void checkIntegerValueInListDto(final int index, final Integer value) {
        Assert.assertEquals(listJson.get(index - 1).getInteger(), value);
    }

    @Then("the BigDecimal value in MyJsonDto should be equal to %value")
    public void checkBigDecimalDtoValue(final BigDecimal value) {
        Assert.assertEquals(this.myJsonDto.getBigDecimal(), value);
    }

    @Then("the %index{-st|-nd} BigDecimal value in MyJsonDto list should be equal to %value")
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

    @AsJson
    public static class MyAnotherOneJsonDto {

        private Double aDouble;
        private Boolean aBoolean;

        public Double getDouble() {
            return aDouble;
        }

        public Boolean getBoolean() {
            return aBoolean;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

}
