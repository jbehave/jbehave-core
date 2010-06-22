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

    public AnnotationFinder(Class<?> annotatedClass) {
        findAnnotations(annotatedClass);
    }

    private void findAnnotations(Class<?> annotatedClass) {
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
        if (annotations == null)
            return;
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
                if (memberValueObject == null) {
                    if (memberValue == null) {
                        CtMethod cm = annotationMetaClass.getDeclaredMethod(memberName);
                        MethodInfo minfo = cm.getMethodInfo();
                        AnnotationDefaultAttribute ada = (AnnotationDefaultAttribute) minfo
                                .getAttribute(AnnotationDefaultAttribute.tag);
                        memberValue = ada.getDefaultValue(); // default
                        // value
                    }
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
            javassist.bytecode.annotation.Annotation value = ((AnnotationMemberValue) memberValue).getValue();
            HashMap<String, Object> memberValueObject = new HashMap<String, Object>();
            processAnnotation(value, (Map<String, Object>) memberValueObject);
            return (T) memberValueObject;
        }

        if (memberValue instanceof StringMemberValue) {
            String value = ((StringMemberValue) memberValue).getValue();
            return (T) value;
        }

        if (memberValue instanceof ArrayMemberValue) {
            MemberValue[] arrayValue = ((ArrayMemberValue) memberValue).getValue();
            List<Object> valueList;
            if (previousValue == null) {
                valueList = new ArrayList<Object>();
            } else {
                valueList = (List<Object>) previousValue;
            }

            for (MemberValue arrayMember : arrayValue) {
                valueList.add(processMemberValue(arrayMember, valueList));
            }
            return (T) valueList;
        }

        if (memberValue instanceof EnumMemberValue) {
            String value = ((EnumMemberValue) memberValue).getValue();
            String type = ((EnumMemberValue) memberValue).getType();
            try {
                return (T) Enum.valueOf((Class) Class.forName(type), value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (memberValue instanceof BooleanMemberValue) {
            Boolean value = ((BooleanMemberValue) memberValue).getValue();
            return (T) value;
        }

        if (memberValue instanceof IntegerMemberValue) {
            Integer value = ((IntegerMemberValue) memberValue).getValue();
            return (T) value;
        }

        if (memberValue instanceof ClassMemberValue) {
            try {
                String value = ((ClassMemberValue) memberValue).getValue();
                return (T) Class.forName(value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("Invalid member value "+memberValue);
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return annotationsMap.containsKey(annotationClass.getName());
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
        throw new MissingAnnotationException(annotationClass, memberName);
    }

    @SuppressWarnings("unchecked")
    public <T> List<Class<T>> getAnnotatedClasses(Class<? extends Annotation> annotationClass, Class<T> type,
            String memberName) {
        List memberValues = getAnnotatedValue(annotationClass, List.class, memberName);
        List<Class<T>> list = new ArrayList<Class<T>>();
        for (Object value : memberValues) {
            list.add((Class<T>) value);
        }
        return list;
    }

}
