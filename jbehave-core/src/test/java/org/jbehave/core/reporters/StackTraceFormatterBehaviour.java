package org.jbehave.core.reporters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.junit.jupiter.api.Test;

@SuppressWarnings("checkstyle:LineLength")
class StackTraceFormatterBehaviour {

    @Test
    void stackTracesShouldBeCompressible() {

        StackTraceFormatter formatter = new StackTraceFormatter(true);

        String start = "java.lang.AssertionError: cart should have contained 68467780\n"
                + "Expected: is <true>\n"
                + "     got: <false>\n"
                + "\tat org.codehaus.groovy.runtime.callsite.CallSiteArray.defaultCallCurrent(CallSiteArray.java:44)\n"
                + "\tat org.codehaus.groovy.runtime.callsite.AbstractCallSite.callCurrent(AbstractCallSite.java:141)\n"
                + "\tat org.codehaus.groovy.runtime.callsite.AbstractCallSite.callCurrent(AbstractCallSite.java:145)\n"
                + "\tat EtsyDotComSteps.anItemInTheEtsyCart(EtsyDotComSteps.groovy:51)\n"
                + "\tat org.codehaus.groovy.runtime.callsite.CallSiteArray.defaultCall(CallSiteArray.java:44)\n"
                // renamed in Groovy 1.8 ?
                + "\tat org.codehaus.groovy.runtime.callsite.AbstractCallSite.callCurrent(AbstractCallSite.java:141)\n"
                + "\tat org.codehaus.groovy.runtime.callsite.AbstractCallSite.callCurrent(AbstractCallSite.java:145)\n"
                + "\tat EtsyDotComSteps.anItemInTheEtsyCart(EtsyDotComSteps.groovy:51)\n"
                + "\tat sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)\n"
                + "\tat sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:39)\n"
                + "\tat sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:27)\n"
                + "\tat java.lang.reflect.Constructor.newInstance(Constructor.java:513)\n"
                + "\tat org.hamcrest.MatcherAssert.assertThat(MatcherAssert.java:21)\n"
                + "\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n"
                + "\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)\n"
                + "\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n"
                + "\tat java.lang.reflect.Method.invoke(Method.java:597)\n"
                + "\tat org.codehaus.groovy.reflection.CachedMethod.invoke(CachedMethod.java:88)\n"
                + "\tat groovy.lang.MetaMethod.doMethodInvoke(MetaMethod.java:233)\n"
                + "\tat groovy.lang.MetaClassImpl.invokeStaticMethod(MetaClassImpl.java:1302)\n"
                + "\tat org.codehaus.groovy.runtime.InvokerHelper.invokeStaticMethod(InvokerHelper.java:819)\n"
                + "\tat org.codehaus.groovy.runtime.ScriptBytecodeAdapter.invokeStaticMethodN(ScriptBytecodeAdapter.java:205)\n"
                + "\tat com.github.tanob.groobe.AssertionSupport.assertWithFailureMessage(AssertionSupport.groovy:32)\n"
                + "\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n"
                + "\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)\n"
                + "\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n"
                + "\tat java.lang.reflect.Method.invoke(Method.java:597)\n"
                + "\tat org.codehaus.groovy.reflection.CachedMethod.invoke(CachedMethod.java:88)\n"
                + "\tat groovy.lang.MetaMethod.doMethodInvoke(MetaMethod.java:233)\n"
                + "\tat org.codehaus.groovy.runtime.metaclass.ClosureMetaClass.invokeMethod(ClosureMetaClass.java:362)\n"
                + "\tat org.codehaus.groovy.runtime.ScriptBytecodeAdapter.invokeMethodOnCurrentN(ScriptBytecodeAdapter.java:77)\n"
                + "\tat com.github.tanob.groobe.AssertionSupport$_assertTransformedDelegateAndOneParam_closure3.doCall(AssertionSupport.groovy:20)\n"
                + "\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n"
                + "\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)\n"
                + "\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n"
                + "\tat java.lang.reflect.Method.invoke(Method.java:597)\n"
                + "\tat org.codehaus.groovy.reflection.CachedMethod.invoke(CachedMethod.java:88)\n"
                + "\tat org.codehaus.groovy.runtime.metaclass.ClosureMetaMethod.invoke(ClosureMetaMethod.java:80)\n"
                + "\tat org.codehaus.groovy.runtime.callsite.PojoMetaMethodSite$PojoMetaMethodSiteNoUnwrapNoCoerce.invoke(PojoMetaMethodSite.java:270)\n"
                + "\tat org.codehaus.groovy.runtime.callsite.PojoMetaMethodSite.call(PojoMetaMethodSite.java:52)\n"
                + "\tat org.codehaus.groovy.runtime.callsite.CallSiteArray.defaultCall(CallSiteArray.java:40)\n"
                + "\tat org.codehaus.groovy.runtime.callsite.AbstractCallSite.call(AbstractCallSite.java:116)\n"
                + "\tat org.codehaus.groovy.runtime.callsite.AbstractCallSite.call(AbstractCallSite.java:128)\n"
                + "\tat EtsyDotComSteps.cartHasThatItem(EtsyDotComSteps.groovy:112)\n"
                + "\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n"
                + "\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)\n"
                + "\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n"
                + "\tat java.lang.reflect.Method.invoke(Method.java:597)\n"
                + "\tat org.codehaus.groovy.runtime.callsite.CallSiteArray.defaultCall(CallSiteArray.java:42)\n"
                + "\tat org.codehaus.groovy.runtime.callsite.AbstractCallSite.call(AbstractCallSite.java:108)\n"
                + "\tat org.codehaus.groovy.runtime.callsite.AbstractCallSite.call(AbstractCallSite.java:116)\n"
                + "\tat org.jbehave.core.steps.StepCreator$ParameterisedStep.perform(StepCreator.java:430)\n";

        String trace = formatter.stackTrace(start);

        String expected = "java.lang.AssertionError: cart should have contained 68467780\n"
                + "Expected: is <true>\n"
                + "     got: <false>\n" + "\t(groovy-call)\n"
                + "\tat EtsyDotComSteps.anItemInTheEtsyCart(EtsyDotComSteps.groovy:51)\n"
                + "\t(groovy-call)\n"
                + "\tat EtsyDotComSteps.anItemInTheEtsyCart(EtsyDotComSteps.groovy:51)\n"
                + "\t(reflection-construct)\n"
                + "\tat org.hamcrest.MatcherAssert.assertThat(MatcherAssert.java:21)\n"
                + "\t(groovy-static-method-invoke)\n"
                + "\tat com.github.tanob.groobe.AssertionSupport.assertWithFailureMessage(AssertionSupport.groovy:32)\n"
                + "\t(groovy-instance-method-invoke)\n"
                + "\tat com.github.tanob.groobe.AssertionSupport$_assertTransformedDelegateAndOneParam_closure3.doCall(AssertionSupport.groovy:20)\n"
                + "\t(groovy-closure-invoke)\n"
                + "\tat EtsyDotComSteps.cartHasThatItem(EtsyDotComSteps.groovy:112)\n"
                + "\t(groovy-call)\n"
                + "\tat org.jbehave.core.steps.StepCreator$ParameterisedStep.perform(StepCreator.java:430)\n";
        assertThatTraceIs(trace, expected);
    }

    @Test
    void exceptionShouldBeCompressible() {
        // Given a compressing formatter
        StackTraceFormatter formatter = new StackTraceFormatter(true);

        // When I format an Exception
        String trace = formatter.stackTrace(new Exception("some cause"));

        // Then it looks like
        String expected = "java.lang.Exception: some cause\n"
                + "\tat org.jbehave.core.reporters.StackTraceFormatterBehaviour.exceptionShouldBeCompressible(StackTraceFormatterBehaviour.java:101)\n"
                + "\t(reflection-invoke)\n"
                + "\tat org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:728)\n"
                + "...";
        assertThatTraceIs(trace, expected);
    }

    @Test
    void shouldUnwrapUuidException() {
        StackTraceFormatter formatter = new StackTraceFormatter(false);
        Exception ex = new Exception();
        Exception wrapEx = new UUIDExceptionWrapper(ex);

        String trace = formatter.stackTrace(ex);
        String expected = formatter.stackTrace(wrapEx);
        assertThatTraceIs(trace, expected);
    }

    private void assertThatTraceIs(String trace, String expected) {
        assertThat(trace, is(expected));
    }

}
