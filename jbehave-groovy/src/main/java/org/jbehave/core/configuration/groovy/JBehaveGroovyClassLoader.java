package org.jbehave.core.configuration.groovy;

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;
import org.objectweb.asm.ClassWriter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;

/**
 * Groovy does not cache the bytecode sequences for generated classes.
 * BytecodeReadingParanamer needs these to get paramater names from classes
 * The Groovy compiler does create the debug tables, and they are the same as the
 * ones made for a native Java class, so this derived GroovyClassLoader fills in
 * for the missing functionality from the base GroovyClassLoader.
 *
 * There is a mechanism to set a system property that would force Groovy's internals
 * to write out bytecode to a (temp) directory, but who want's to have to do that,
 * and clean up temp directories after a build run. Assuming that was not slower
 * anyway.
 */
public class JBehaveGroovyClassLoader extends GroovyClassLoader {

    private Map<String, byte[]> classBytes = new HashMap<String, byte[]>();

    @Override
    public InputStream getResourceAsStream(String name) {
        if (classBytes.containsKey(name)) {
            return new ByteArrayInputStream(classBytes.get(name));
        }
        return super.getResourceAsStream(name);
    }

    @Override
    protected ClassCollector createCollector(CompilationUnit unit, SourceUnit su) {
        // These six lines copied from Groovy itself, with the intention to return a subclass
        InnerLoader loader = AccessController.doPrivileged(new PrivilegedAction<InnerLoader>() {
            public InnerLoader run() {
                return new InnerLoader(JBehaveGroovyClassLoader.this);
            }
        });
        return new JBehaveClassCollector(classBytes, loader, unit, su);
    }

    public static class JBehaveClassCollector extends ClassCollector {
        private final Map<String, byte[]> classBytes;

        public JBehaveClassCollector(Map<String, byte[]> classBytes, InnerLoader loader, CompilationUnit unit, SourceUnit su) {
            super(loader, unit, su);
            this.classBytes = classBytes;
        }

        @Override
        protected Class onClassNode(ClassWriter classWriter, ClassNode classNode) {
            classBytes.put(classNode.getName() + ".class", classWriter.toByteArray());
            return super.onClassNode(classWriter, classNode);
        }
    }

}
