package org.jbehave.examples.core.model;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Trader implements Comparable<Trader>{

    private final String name;
    private String rank = "";
    private List<Stock> stocks = new ArrayList<Stock>();

    public Trader(String name, String rank) {
        this.name = name;
        this.rank = rank;
    }

    public Trader(String name, List<Stock> stocks) {
        this.name = name;
        this.stocks = stocks;
    }

    public String getName() {
        return name;
    }
    
    public String getRank() {
        return rank;
    }

    public List<Stock> getStocks() {
        return stocks;
    }

    public void addStocks(List<Stock> stocks) {
        this.stocks.addAll(stocks);
    }

    public void sellAllStocks(){
        this.stocks = asList(new Stock[]{});
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public int compareTo(Trader o) {
        return CompareToBuilder.reflectionCompare(this, o);
    }
    
}
