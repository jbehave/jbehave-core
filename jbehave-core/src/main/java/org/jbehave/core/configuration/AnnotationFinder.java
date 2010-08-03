package org.jbehave.core.configuration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
            Object value = getAnnotationValue(annotation, memberName);
            if (value != null) {
                return (T) value;
            }
        }
        throw new AnnotationRequired(annotationClass, memberName);
    }

    @SuppressWarnings("unchecked")
    public <T, A extends Annotation> List<T> getAnnotatedValues(Class<A> annotationClass, Class<T> type,
            String memberName) {
        List<T> list = new ArrayList<T>();
        if (!isAnnotationPresent(annotationClass)) {
            return list;
        }
        Object[] values = getAnnotatedValue(annotationClass, Object[].class, memberName);
        for (Object value : values) {
            list.add((T) value);
        }
        boolean inheritValues = true;
        try {
            inheritValues = getAnnotatedValue(annotationClass, boolean.class, createInheritMemberName(memberName));
        } catch (AnnotationRequired e) {
            // no inherit property found - assume true
        }
        if (inheritValues) {
            Class<?> superClass = annotatedClass.getSuperclass();
            if (superClass != null && superClass != Object.class) {
                list.addAll(new AnnotationFinder(superClass).getAnnotatedValues(annotationClass, type, memberName));
            }
        }
        return list;
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
        } catch (Exception ex) {
            return null;
        }
    }
}
