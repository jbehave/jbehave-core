package org.jbehave.core.reporters;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.regex.Pattern;

import org.jbehave.core.failures.UUIDExceptionWrapper;

public class StackTraceFormatter {
    
    private boolean compressFailureTrace;
    
    public StackTraceFormatter(boolean compressFailureTrace) {
        this.compressFailureTrace = compressFailureTrace;
    }

    public String stackTrace(Throwable cause) {
        if (cause.getClass().getName().equals(UUIDExceptionWrapper.class.getName())) {
            cause = cause.getCause();
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        cause.printStackTrace(new PrintStream(out));
        return stackTrace(out.toString().replaceAll("\r",""));
    }

    protected String stackTrace(String stackTrace) {
        if (!compressFailureTrace) {
            return stackTrace;
        }
        // don't print past certain parts of the stack. Try them even though
        // they may be redundant.
        stackTrace = cutOff(stackTrace, "org.jbehave.core.embedder.");
        stackTrace = cutOff(stackTrace, "org.junit.runners.");
        stackTrace = cutOff(stackTrace, "org.junit.platform.");
        stackTrace = cutOff(stackTrace, "org.apache.maven.surefire.");

        // System.out.println("=====before>" + stackTrace + "<==========");

        // replace whole series of lines with '\t(summary)' The end-user
        // will thank us.
        for (Replacement replacement : REPLACEMENTS) {
            stackTrace = replacement.from.matcher(stackTrace).replaceAll(replacement.to);
        }
        return stackTrace;
    }

    private String cutOff(String stackTrace, String at) {
        if (stackTrace.indexOf(at) > -1) {
            int ix = stackTrace.indexOf(at);
            ix = stackTrace.indexOf("\n", ix);
            if (ix != -1) {
                stackTrace = stackTrace.substring(0, ix) + "\n...";
            }
        }
        return stackTrace;
    }

    private static class Replacement {
        private final Pattern from;
        private final String to;

        private Replacement(Pattern from, String to) {
            this.from = from;
            this.to = to;
        }
    }

    private static Replacement[] REPLACEMENTS = new Replacement[] {
            new Replacement( // JDK 9+
                    Pattern.compile("\\tat java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0\\(Native Method\\)\\n"
                            + "\\tat java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke\\(NativeMethodAccessorImpl.java:\\d+\\)\\n"
                            + "\\tat java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke\\(DelegatingMethodAccessorImpl.java:\\d+\\)\\n"
                            + "\\tat java.base/java.lang.reflect.Method.invoke\\(Method.java:\\d+\\)"),
                    "\t(reflection-invoke)"),
            new Replacement( // JDK 1.8
                    Pattern.compile("\\tat sun.reflect.NativeMethodAccessorImpl.invoke0\\(Native Method\\)\\n"
                            + "\\tat sun.reflect.NativeMethodAccessorImpl.invoke\\(NativeMethodAccessorImpl.java:\\d+\\)\\n"
                            + "\\tat sun.reflect.DelegatingMethodAccessorImpl.invoke\\(DelegatingMethodAccessorImpl.java:\\d+\\)\\n"
                            + "\\tat java.lang.reflect.Method.invoke\\(Method.java:\\d+\\)"),
                    "\t(reflection-invoke)"),
            new Replacement(
                    Pattern.compile("\\tat org.codehaus.groovy.reflection.CachedMethod.invoke\\(CachedMethod.java:\\d+\\)\\n"
                            + "\\tat org.codehaus.groovy.runtime.metaclass.ClosureMetaMethod.invoke\\(ClosureMetaMethod.java:\\d+\\)\\n"
                            + "\\tat org.codehaus.groovy.runtime.callsite.PojoMetaMethodSite\\$PojoMetaMethodSiteNoUnwrapNoCoerce.invoke\\(PojoMetaMethodSite.java:\\d+\\)\\n"
                            + "\\tat org.codehaus.groovy.runtime.callsite.PojoMetaMethodSite.call\\(PojoMetaMethodSite.java:\\d+\\)\\n"
                            + "\\tat org.codehaus.groovy.runtime.callsite.CallSiteArray.defaultCall\\(CallSiteArray.java:\\d+\\)\\n"
                            + "\\tat org.codehaus.groovy.runtime.callsite.AbstractCallSite.call\\(AbstractCallSite.java:\\d+\\)\\n"
                            + "\\tat org.codehaus.groovy.runtime.callsite.AbstractCallSite.call\\(AbstractCallSite.java:\\d+\\)"),
                    "\t(groovy-closure-invoke)"),
            new Replacement(
                    Pattern.compile("\\tat org.codehaus.groovy.reflection.CachedMethod.invoke\\(CachedMethod.java:\\d+\\)\\n"
                            + "\\tat groovy.lang.MetaMethod.doMethodInvoke\\(MetaMethod.java:\\d+\\)\\n"
                            + "\\tat org.codehaus.groovy.runtime.metaclass.ClosureMetaClass.invokeMethod\\(ClosureMetaClass.java:\\d+\\)\\n"
                            + "\\tat org.codehaus.groovy.runtime.ScriptBytecodeAdapter.invokeMethodOnCurrentN\\(ScriptBytecodeAdapter.java:\\d+\\)"),
                    "\t(groovy-instance-method-invoke)"),
            new Replacement(
                    Pattern.compile("\\tat org.codehaus.groovy.reflection.CachedMethod.invoke\\(CachedMethod.java:\\d+\\)\n"
                            + "\\tat org.codehaus.groovy.runtime.metaclass.ClosureMetaMethod.invoke\\(ClosureMetaMethod.java:\\d+\\)\n"
                            + "\\tat org.codehaus.groovy.runtime.callsite.PojoMetaMethodSite\\$PojoMetaMethodSiteNoUnwrapNoCoerce.invoke\\(PojoMetaMethodSite.java:\\d+\\)\n"
                            + "\\tat org.codehaus.groovy.runtime.callsite.PojoMetaMethodSite.call\\(PojoMetaMethodSite.java:\\d+\\)\n"
                            + "\\tat org.codehaus.groovy.runtime.callsite.AbstractCallSite.call\\(AbstractCallSite.java:\\d+\\)"),
                    "\t(groovy-abstract-method-invoke)"),
            new Replacement(
                    Pattern.compile("\\tat org.codehaus.groovy.reflection.CachedMethod.invoke\\(CachedMethod.java:\\d+\\)\\n"
                            + "\\tat groovy.lang.MetaMethod.doMethodInvoke\\(MetaMethod.java:\\d+\\)\\n"
                            + "\\tat groovy.lang.MetaClassImpl.invokeStaticMethod\\(MetaClassImpl.java:\\d+\\)\\n"
                            + "\\tat org.codehaus.groovy.runtime.InvokerHelper.invokeStaticMethod\\(InvokerHelper.java:\\d+\\)\\n"
                            + "\\tat org.codehaus.groovy.runtime.ScriptBytecodeAdapter.invokeStaticMethodN\\(ScriptBytecodeAdapter.java:\\d+\\)"),
                    "\t(groovy-static-method-invoke)"),
            new Replacement(
                    Pattern.compile("\\tat sun.reflect.NativeConstructorAccessorImpl.newInstance0\\(Native Method\\)\\n"
                            + "\\tat sun.reflect.NativeConstructorAccessorImpl.newInstance\\(NativeConstructorAccessorImpl.java:\\d+\\)\\n"
                            + "\\tat sun.reflect.DelegatingConstructorAccessorImpl.newInstance\\(DelegatingConstructorAccessorImpl.java:\\d+\\)\\n"
                            + "\\tat java.lang.reflect.Constructor.newInstance\\(Constructor.java:\\d+\\)"),
                    "\t(reflection-construct)"),
            new Replacement(
                    Pattern.compile("\\tat org.codehaus.groovy.runtime.callsite.CallSiteArray.defaultCall(Current|)\\(CallSiteArray.java:\\d+\\)\\n"
                            + "\\tat org.codehaus.groovy.runtime.callsite.AbstractCallSite.call(Current|)\\(AbstractCallSite.java:\\d+\\)\\n"
                            + "\\tat org.codehaus.groovy.runtime.callsite.AbstractCallSite.call(Current|)\\(AbstractCallSite.java:\\d+\\)"

                    ), "\t(groovy-call)"),
            // This one last.
            new Replacement(Pattern.compile("\\t\\(reflection\\-invoke\\)\\n" + "\\t\\(groovy\\-"), "\t(groovy-") };

}
