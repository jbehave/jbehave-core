package org.jbehave.core.configuration;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationDefaultAttribute;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.AnnotationMemberValue;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.BooleanMemberValue;
import javassist.bytecode.annotation.ClassMemberValue;
import javassist.bytecode.annotation.EnumMemberValue;
import javassist.bytecode.annotation.IntegerMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.StringMemberValue;

public class AnnotationFinder {

    private final ClassPool classPool = ClassPool.getDefault();
    private final Map<String, Map<String, Object>> annotationsMap = new HashMap<String, Map<String, Object>>();
    private final Class<?> annotatedClass;

    public AnnotationFinder(Class<?> annotatedClass) {
        this.annotatedClass = annotatedClass;
        findAnnotations();
    }

    private void findAnnotations() {
        Stack<Class<?>> stack = new Stack<Class<?>>();
        stack.push(annotatedClass);
        Class<?> annotatedSuperClass = annotatedClass.getSuperclass();
        while (annotatedSuperClass != Object.class) {
            stack.push(annotatedSuperClass);
            annotatedSuperClass = annotatedSuperClass.getSuperclass();
        }
        while (!stack.empty()) {
            Class<?> nextClass = stack.pop();
            classPool.insertClassPath(new ClassClassPath(nextClass));
            try {
                scanClass(classPool.get(nextClass.getName()).getClassFile());
            } catch (NotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void scanClass(ClassFile classFile) {
        AnnotationsAttribute visible = (AnnotationsAttribute) classFile.getAttribute(AnnotationsAttribute.visibleTag);
        AnnotationsAttribute invisible = (AnnotationsAttribute) classFile
                .getAttribute(AnnotationsAttribute.invisibleTag);

        if (visible != null) {
            populate(visible.getAnnotations());
        }
        if (invisible != null) {
            populate(invisible.getAnnotations());
        }
    }

    private void populate(javassist.bytecode.annotation.Annotation[] annotations) {
        if (annotations == null) {
            return;
        }
        // for each annotation on class hierarchy
        for (javassist.bytecode.annotation.Annotation annotation : annotations) {
            // a map that contain each member(attribute) of the annotation
            Map<String, Object> annotationsAttributesMap = annotationsMap.get(annotation.getTypeName());
            if (annotationsAttributesMap == null) {
                annotationsAttributesMap = new HashMap<String, Object>();
                annotationsMap.put(annotation.getTypeName(), annotationsAttributesMap);
            }
            processAnnotation(annotation, annotationsAttributesMap);
        }
    }

    private void processAnnotation(javassist.bytecode.annotation.Annotation annotation,
            Map<String, Object> annotationsAttributesMap) {

        try {
            // Process Default Values
            CtClass annotationMetaClass = classPool.get(annotation.getTypeName());

            // process annotation members
            for (CtMethod memberMethod : annotationMetaClass.getDeclaredMethods()) {
                MemberValue memberValue = null;
                String memberName = memberMethod.getMethodInfo().getName();
                Object memberValueObject = annotationsAttributesMap.get(memberName);
                memberValue = annotation.getMemberValue(memberName);
                if (memberValue == null) {
                    CtMethod cm = annotationMetaClass.getDeclaredMethod(memberName);
                    MethodInfo minfo = cm.getMethodInfo();
                    AnnotationDefaultAttribute ada = (AnnotationDefaultAttribute) minfo
                            .getAttribute(AnnotationDefaultAttribute.tag);
                    memberValue = ada.getDefaultValue(); // default value
                }
                if (memberValueObject == null) {
                    memberValueObject = processMemberValue(memberValue, null);
                } else {
                    Object newMemberValue = processMemberValue(memberValue, memberValueObject);
                    if (newMemberValue != null)
                        memberValueObject = newMemberValue;
                }
                annotationsAttributesMap.put(memberName, memberValueObject);

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T processMemberValue(MemberValue memberValue, Object previousValue) {

        if (memberValue instanceof AnnotationMemberValue) {
            Map<String, Object> value = new HashMap<String, Object>();
            processAnnotation(((AnnotationMemberValue) memberValue).getValue(), value);
            return (T) value;
        }

        if (memberValue instanceof StringMemberValue) {
            String value = ((StringMemberValue) memberValue).getValue();
            return (T) value;
        }

        if (memberValue instanceof ArrayMemberValue) {
            List<Object> values;
            if (previousValue == null) {
                values = new ArrayList<Object>();
            } else {
                values = (List<Object>) previousValue;
            }

            for (MemberValue arrayMember : ((ArrayMemberValue) memberValue).getValue()) {
                values.add(processMemberValue(arrayMember, values));
            }
            return (T) values;
        }

        if (memberValue instanceof BooleanMemberValue) {
            Boolean value = ((BooleanMemberValue) memberValue).getValue();
            return (T) value;
        }

        if (memberValue instanceof IntegerMemberValue) {
            Integer value = ((IntegerMemberValue) memberValue).getValue();
            return (T) value;
        }

        if (memberValue instanceof EnumMemberValue) {
            String value = ((EnumMemberValue) memberValue).getValue();
            String type = ((EnumMemberValue) memberValue).getType();
            return (T) Enum.valueOf(loadClass(type), value);
        }

        if (memberValue instanceof ClassMemberValue) {
            String value = ((ClassMemberValue) memberValue).getValue();
            return (T) loadClass(value);
        }

        throw new RuntimeException("Invalid member value " + memberValue);
    }

    @SuppressWarnings("unchecked")
    private Class loadClass(String type) {
        try {
            return annotatedClass.getClassLoader().loadClass(type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return annotationsMap.containsKey(annotationClass.getName());
    }

    public boolean isAnnotationValuePresent(Class<? extends Annotation> annotationClass, String memberName) {
        if (isAnnotationPresent(annotationClass)) {
            return annotationsMap.get(annotationClass.getName()).containsKey(memberName);
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAnnotatedValue(Class<? extends Annotation> annotationClass, Class<T> memberType, String memberName) {
        String annotationName = annotationClass.getName();
        if (annotationsMap.containsKey(annotationName)) {
            Map<String, Object> annotationAttributeMap = annotationsMap.get(annotationName);
            Object value = annotationAttributeMap.get(memberName);
            if (value != null) {
                return (T) value;
            }
        }
        throw new AnnotationRequired(annotationClass, memberName);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getAnnotatedValues(Class<? extends Annotation> annotationClass, Class<T> type, String memberName) {
        List<T> list = new ArrayList<T>();
        for (Object value : getAnnotatedValue(annotationClass, List.class, memberName)) {
            list.add((T) value);
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    public <T> List<Class<T>> getAnnotatedClasses(Class<? extends Annotation> annotationClass, Class<T> type,
            String memberName) {
        List<Class<T>> list = new ArrayList<Class<T>>();
        for (Object value : getAnnotatedValue(annotationClass, List.class, memberName)) {
            list.add((Class<T>) value);
        }
        return list;
    }

}
