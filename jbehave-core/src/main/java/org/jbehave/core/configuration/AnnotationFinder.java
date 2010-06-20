package org.jbehave.core.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.lang.annotation.Annotation;

import org.jbehave.core.annotations.exceptions.IllegalAnnotationException;
import org.jbehave.core.annotations.exceptions.MissingAnnotationException;

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

	protected final ClassPool classPool = ClassPool.getDefault();

	protected final Map<String, Map<String, Object>> annotationsMap = new HashMap<String, Map<String, Object>>();

	public AnnotationFinder(Class<?> pMyTestClass) {

		Stack<Class<?>> stack = new Stack<Class<?>>();
		stack.push(pMyTestClass);
		Class<?> classe = pMyTestClass.getSuperclass();
		while (classe != Object.class) {
			stack.push(classe);
			classe = classe.getSuperclass();
		}
		while (!stack.empty()) {
			Class<?> classe2 = stack.pop();
			classPool.insertClassPath(new ClassClassPath(classe2));
			CtClass xx;
			try {
				xx = classPool.get(classe2.getName());
				scanClass(xx.getClassFile());
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void scanClass(ClassFile cf) {
		AnnotationsAttribute visible = (AnnotationsAttribute) cf
				.getAttribute(AnnotationsAttribute.visibleTag);
		AnnotationsAttribute invisible = (AnnotationsAttribute) cf
				.getAttribute(AnnotationsAttribute.invisibleTag);

		if (visible != null)
			populate(visible.getAnnotations());
		if (invisible != null)
			populate(invisible.getAnnotations());
	}

	protected void populate(
			javassist.bytecode.annotation.Annotation[] annotations) {
		if (annotations == null)
			return;
		// for each annotation on class hierarchy
		for (javassist.bytecode.annotation.Annotation testAnnotation : annotations) {
			// a map that contain each member(attribute) of the annotation
	//		System.out.println("Populating " + testAnnotation.getTypeName());
			Map<String, Object> annotationsAttributesMap = annotationsMap
					.get(testAnnotation.getTypeName());
			if (annotationsAttributesMap == null) {
				annotationsAttributesMap = new HashMap<String, Object>();
				annotationsMap.put(testAnnotation.getTypeName(),
						annotationsAttributesMap);
			}

			processOneAnnotation(testAnnotation, annotationsAttributesMap);
		}
	}

	private void processOneAnnotation(
			javassist.bytecode.annotation.Annotation pAnnotation,
			Map<String, Object> pAnnotationsAttributesMap) {

		try {
			// Process Default Values

			CtClass annotationMetaClass = classPool.get(pAnnotation
					.getTypeName());

			// process annotation members
			for (CtMethod memberMethod : annotationMetaClass
					.getDeclaredMethods()) {
				MemberValue memberValue = null;
				String memberName = memberMethod.getMethodInfo().getName();

				Object memberValueObject = pAnnotationsAttributesMap
						.get(memberName);
				memberValue = pAnnotation.getMemberValue(memberName);
				if (memberValueObject == null) {

					if (memberValue == null) {
						CtMethod cm = annotationMetaClass
								.getDeclaredMethod(memberName);
						MethodInfo minfo = cm.getMethodInfo();
						AnnotationDefaultAttribute ada = (AnnotationDefaultAttribute) minfo
								.getAttribute(AnnotationDefaultAttribute.tag);
						memberValue = ada.getDefaultValue(); // default
						// value
					}
					memberValueObject = processMemberValue(memberValue, null);
				} else {
					Object newMemberValue = processMemberValue(memberValue,
							memberValueObject);
					if (newMemberValue != null)
						memberValueObject = newMemberValue;
				}
				pAnnotationsAttributesMap.put(memberName, memberValueObject);

			}
		} catch (Exception e) {

			// TODO "processAnnotationValues", e);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T processMemberValue(MemberValue pMemberValue, Object pOldValue) {

		if (pMemberValue instanceof AnnotationMemberValue) {
			javassist.bytecode.annotation.Annotation value = ((AnnotationMemberValue) pMemberValue)
					.getValue();
			HashMap<String, Object> memberValueObject = new HashMap<String, Object>();
			processOneAnnotation(value, (Map<String, Object>) memberValueObject);
			return (T) memberValueObject;
		}

		if (pMemberValue instanceof StringMemberValue) {
			String value = ((StringMemberValue) pMemberValue).getValue();
			return (T) value;
		}

		if (pMemberValue instanceof ArrayMemberValue) {
			MemberValue[] arrayValue = ((ArrayMemberValue) pMemberValue)
					.getValue();
			List<Object> valueList;
			if (pOldValue == null) {
				valueList = new ArrayList<Object>();
			} else {
				valueList = (List<Object>) pOldValue;
			}

			for (MemberValue arrayMember : arrayValue) {

				valueList.add(processMemberValue(arrayMember, valueList));
			}
			return (T) valueList;
		}

		if (pMemberValue instanceof EnumMemberValue) {
			String value = ((EnumMemberValue) pMemberValue).getValue();
			String type = ((EnumMemberValue) pMemberValue).getType();
			try {
				@SuppressWarnings("rawtypes")
				Class cc = Class.forName(type);
				return (T) Enum.valueOf(cc, value);
			} catch (Exception e) {
				// TODO logger.log(Level.SEVERE,
				// "NÃ£o foi possÃ­vel instanciar o Enum !", e);
			}

		}

		if (pMemberValue instanceof BooleanMemberValue) {
			Boolean value = ((BooleanMemberValue) pMemberValue).getValue();
			return (T) value;
		}

		if (pMemberValue instanceof IntegerMemberValue) {
			Integer value = ((IntegerMemberValue) pMemberValue).getValue();
			return (T) value;
		}

		if (pMemberValue instanceof ClassMemberValue) {
			try {
				String value = ((ClassMemberValue) pMemberValue).getValue();
				return (T) Class.forName(value);
			} catch (Exception e) {
				// TODO logger.log(Level.SEVERE,
				// "NÃ£o foi possÃ­vel instanciar o Enum !", e);
			}
		}

		throw new RuntimeException(
				"AnotaÃ§Ã£o nÃ£o foi registrado corretamente...");
	}

	public boolean isAnnotationPresent(Class<? extends Annotation> pClass) {

		return annotationsMap.containsKey(pClass.getName());
	}

	@SuppressWarnings("unchecked")
	public <T> T getMemberValue(Class<? extends Annotation> pAnnotationClass,
			Class<T> pMemberType, String pMemberName) {
		String annotationName = pAnnotationClass.getName();
		T value = null;
		if (annotationsMap.containsKey(annotationName)) {

			Map<String, Object> annotationAttributeMap = annotationsMap
					.get(annotationName);
			Object ann = annotationAttributeMap.get(pMemberName);
			if (ann != null) {
				value = (T) ann;

			} else {
				if (pMemberType.isAssignableFrom(Integer.class)) {
					value = (T) new Integer(0);
				}

			}
			return value;

		} else
			throw new MissingAnnotationException(pAnnotationClass, pMemberName);
	}

	public <T> T getSubMemberValue(
			Class<? extends Annotation> pAnnotationClass, Class<T> pMemberType,
			String pMemberName, String pSubMemberName) {
		T value = null;
		String annotationName = pAnnotationClass.getName();
		if (annotationsMap.containsKey(annotationName)) {
			return value;

		} else
			throw new MissingAnnotationException(pAnnotationClass, pMemberName);

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> void getMemberValues(
			Class<? extends Annotation> pAnnotationClass, List<T> pMemberList,
			String pMemberName) {
		String annotationName = pAnnotationClass.getName();
		if (annotationsMap.containsKey(annotationName)) {

			Map<String, Object> annotationAttributeMap = annotationsMap
					.get(annotationName);
			Object memberValues = annotationAttributeMap.get(pMemberName);

			if (memberValues != null && memberValues instanceof List) {
				for (Object object : (List) memberValues) {
					pMemberList.add((T) object);
				}
			} else {
				throw new MissingAnnotationException(pAnnotationClass,
						pMemberName);
			}

		} else
			throw new IllegalAnnotationException(pAnnotationClass,
					"Member not found: '" + pMemberName + "'");
	}

	public <T> int getArrayMemberValueSize(Class<T> pClass, String pString) {
		int size = 0;

		return size;
	}

	public <T> T getArrayMemberValue(
			Class<? extends Annotation> pAnnotationClass, Class<T> pMemberType,
			String pMemberName, int pPosition, String pString) {
		T value = null;

		return value;
	}
}
