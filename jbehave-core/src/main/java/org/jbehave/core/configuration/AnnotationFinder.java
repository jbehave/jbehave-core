package org.jbehave.core.configuration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * Helper class to find and retrieve annotated values
 * 
 * @author Cristiano Gavião
 * @author Brian Repko
 * @author Mauro Talevi
 */
public class AnnotationFinder {

    private final Class<?> annotatedClass;

    public AnnotationFinder(Class<?> annotatedClass) {
        this.annotatedClass = annotatedClass;
    }

    public <A extends Annotation> boolean isAnnotationPresent(Class<A> annotationClass) {
        return getAnnotation(annotationClass) != null;
    }

    public <A extends Annotation> boolean isAnnotationValuePresent(Class<A> annotationClass, String memberName) {
        Annotation annotation = getAnnotation(annotationClass);
        return annotation != null && getAnnotationValue(annotation, memberName) != null;
    }

    @SuppressWarnings("unchecked")
    public <T, A extends Annotation> T getAnnotatedValue(Class<A> annotationClass, Class<T> memberType,
            String memberName) {
        Annotation annotation = getAnnotation(annotationClass);
        if (annotation != null) {
            return (T) getAnnotationValue(annotation, memberName);
        }
        throw new AnnotationRequired(annotatedClass, annotationClass);
    }

    @SuppressWarnings("unchecked")
    public <T, A extends Annotation> List<T> getAnnotatedValues(Class<A> annotationClass, Class<T> type,
            String memberName) {
        Set<T> set = new LinkedHashSet<T>();
        if (!isAnnotationPresent(annotationClass)) {
            return new ArrayList<T>(set);
        }
        Object[] values = getAnnotatedValue(annotationClass, Object[].class, memberName);
        for (Object value : values) {
            set.add((T) value);
        }
        boolean inheritValues = true;
        String inheritMemberName = createInheritMemberName(memberName);
        if (isAnnotationValuePresent(annotationClass, inheritMemberName)) {
            inheritValues = getAnnotatedValue(annotationClass, boolean.class, inheritMemberName);
        }
        if (inheritValues) {
            Class<?> superClass = annotatedClass.getSuperclass();
            if (superClass != null && superClass != Object.class) {
                set.addAll(new AnnotationFinder(superClass).getAnnotatedValues(annotationClass, type, memberName));
            }
        }
        return new ArrayList<T>(set);
    }

    /**
     * Creates the inherit member name by prefixing "inherit" to the capitalized
     * member name.
     * 
     * @param memberName
     * @return The inherit member name
     */
    protected String createInheritMemberName(String memberName) {
        return "inherit" + StringUtils.capitalize(memberName);
    }

    @SuppressWarnings("unchecked")
    public <T, A extends Annotation> List<Class<T>> getAnnotatedClasses(Class<A> annotationClass, Class<T> type,
            String memberName) {
        return (List<Class<T>>) getAnnotatedValues(annotationClass, type.getClass(), memberName);
    }

    protected <A extends Annotation> Annotation getAnnotation(Class<A> annotationClass) {
        return annotatedClass.getAnnotation(annotationClass);
    }

    protected Object getAnnotationValue(Annotation annotation, String attributeName) {
        try {
            Method method = annotation.annotationType().getDeclaredMethod(attributeName, new Class[0]);
            return method.invoke(annotation);
        } catch (Exception e) {
            return null;
        }
    }
}
