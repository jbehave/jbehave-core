package org.jbehave.examples.core.service;

import org.jbehave.examples.core.model.Stock;
import org.jbehave.examples.core.model.Trader;

public class TradingService {

    public Stock newStock(String symbol, double threshold) {
        return new Stock(symbol, threshold);
    }

    public Trader newTrader(String name, String rank) {
        return new Trader(name, rank);
    }

}
