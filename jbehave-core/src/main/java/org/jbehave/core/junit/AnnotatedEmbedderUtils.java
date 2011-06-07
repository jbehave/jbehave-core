package org.jbehave.core.junit;

import org.jbehave.core.embedder.EmbedderClassLoader;
import org.junit.runner.RunWith;

public class AnnotatedEmbedderUtils {

    public static AnnotatedEmbedderRunner annotatedEmbedderRunner(String annotatedClassName,
            EmbedderClassLoader classLoader) {
        Class<?> runnerClass = runnerClass(annotatedClassName, classLoader);
        return newAnnotatedEmbedderRunner(runnerClass, annotatedClassName, classLoader);
    }

    private static Class<?> runnerClass(String annotatedClassName, EmbedderClassLoader classLoader) {
        Class<?> annotatedClass = loadClass(annotatedClassName, classLoader);
        RunWith annotation = annotatedClass.getAnnotation(RunWith.class);
        if (annotation != null) {
            return annotation.value();
        }
        throw new MissingAnnotatedEmbedderRunner(annotatedClass);
    }

    private static AnnotatedEmbedderRunner newAnnotatedEmbedderRunner(Class<?> runnerClass, String annotatedClassName,
            EmbedderClassLoader classLoader) {
        try {
            Class<?> annotatedClass = loadClass(annotatedClassName, classLoader);
            return (AnnotatedEmbedderRunner) runnerClass.getConstructor(Class.class).newInstance(annotatedClass);
        } catch (Exception e) {
            throw new AnnotatedEmbedderRunnerInstantiationFailed(runnerClass, annotatedClassName, classLoader, e);
        }
    }

    private static Class<?> loadClass(String className, EmbedderClassLoader classLoader) {
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new ClassLoadingFailed(className, classLoader, e);
        }
    }

    @SuppressWarnings("serial")
    public static class ClassLoadingFailed extends RuntimeException {

        public ClassLoadingFailed(String className, EmbedderClassLoader classLoader, Throwable cause) {
            super("Failed to load class " + className + " with classLoader " + classLoader, cause);
        }

    }

    @SuppressWarnings("serial")
    public static class AnnotatedEmbedderRunnerInstantiationFailed extends RuntimeException {

        public AnnotatedEmbedderRunnerInstantiationFailed(Class<?> runnerClass, String annotatedClassName,
                EmbedderClassLoader classLoader, Throwable cause) {
            super("Failed to instantiate annotated embedder runner " + runnerClass + " with annotatedClassName "
                    + annotatedClassName + " and classLoader " + classLoader, cause);
        }

    }

    @SuppressWarnings("serial")
    public static class MissingAnnotatedEmbedderRunner extends RuntimeException {

        public MissingAnnotatedEmbedderRunner(Class<?> annotatedClass) {
            super("AnnotatedEmbedderRunner not specified via @RunWith annotation in annotatedClass " + annotatedClass);
        }

    }
}
