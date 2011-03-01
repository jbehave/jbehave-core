package org.jbehave.examples.trader.weld;

import javax.enterprise.inject.New;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jbehave.core.annotations.weld.WeldStep;
import org.jbehave.examples.trader.service.TradingService;
import org.jbehave.examples.trader.steps.AndSteps;
import org.jbehave.examples.trader.steps.BeforeAfterSteps;
import org.jbehave.examples.trader.steps.CalendarSteps;
import org.jbehave.examples.trader.steps.PriorityMatchingSteps;
import org.jbehave.examples.trader.steps.SandpitSteps;
import org.jbehave.examples.trader.steps.SearchSteps;

/**
 * @author aaronwalker
 *
 */
@Singleton
public class WeldStepProducer
{
    @Inject @New 
    private TradingService tradingService;
    
    @Singleton @Produces
    public TradingService getTradingService()
    {
        return tradingService;
    }
    
    //extended Trader example steps using an annotated static classes to mark them as Weld Steps 
    
    @WeldStep
    public static class WeldBeforeAfterSteps extends BeforeAfterSteps {}
    
    @WeldStep
    public static class WeldAndSteps extends AndSteps {}
    
    @WeldStep
    public static class WeldCalendarSteps extends CalendarSteps {}
    
    @WeldStep
    public static class WeldPriorityMatchingSteps extends PriorityMatchingSteps {}
    
    @WeldStep
    public static class WeldSandpitSteps extends SandpitSteps {}
    
    @WeldStep
    public static class WeldSearchSteps extends SearchSteps {}
}
