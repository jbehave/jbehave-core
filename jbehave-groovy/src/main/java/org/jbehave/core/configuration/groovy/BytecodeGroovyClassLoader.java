package org.jbehave.core.configuration.groovy;

import groovy.lang.GroovyClassLoader;
import groovyjarjarasm.asm.ClassWriter;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;

/**
 * Groovy does not cache the bytecode sequences for generated classes.
 * BytecodeReadingParanamer needs these to get paramater names from classes The
 * Groovy compiler does create the debug tables, and they are the same as the
 * ones made for a native Java class, so this derived GroovyClassLoader fills in
 * for the missing functionality from the base GroovyClassLoader.
 * 
 * Groovy allows a mechanism via a system property to force the dump of bytecode
 * to a (temp) directory, but caching the bytecode avoids having to clean up
 * temp directories after the run.
 */
public class BytecodeGroovyClassLoader extends GroovyClassLoader {

    private Map<String, byte[]> classBytes = new HashMap<>();

    @Override
    public InputStream getResourceAsStream(String name) {
        if (classBytes.containsKey(name)) {
            return new ByteArrayInputStream(classBytes.get(name));
        }
        return super.getResourceAsStream(name);
    }

    @Override
    protected ClassCollector createCollector(CompilationUnit unit, SourceUnit su) {
        // These six lines copied from Groovy itself, with the intention to
        // return a subclass
        InnerLoader loader = AccessController.doPrivileged(new PrivilegedAction<InnerLoader>() {
            @Override
            public InnerLoader run() {
                return new InnerLoader(BytecodeGroovyClassLoader.this);
            }
        });
        return new BytecodeClassCollector(classBytes, loader, unit, su);
    }

    public static class BytecodeClassCollector extends ClassCollector {
        private final Map<String, byte[]> classBytes;

        public BytecodeClassCollector(Map<String, byte[]> classBytes, InnerLoader loader, CompilationUnit unit,
                SourceUnit su) {
            super(loader, unit, su);
            this.classBytes = classBytes;
        }

        @Override
        protected Class<?> onClassNode(ClassWriter classWriter, ClassNode classNode) {
            classBytes.put(classNode.getName() + ".class", classWriter.toByteArray());
            return super.onClassNode(classWriter, classNode);
        }
    }

}
