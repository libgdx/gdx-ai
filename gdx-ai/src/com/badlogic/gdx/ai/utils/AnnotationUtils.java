/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.ai.utils;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.utils.reflect.ClassReflection;

/** General utility methods for working with annotations.
 *
 * @author davebaol */
public final class AnnotationUtils {

	private AnnotationUtils () {
	}

	/** Find a single {@link Annotation} of {@code annotationType} on the supplied {@link Class}, traversing its interfaces,
	 * annotations, and superclasses if the annotation is not <em>directly present</em> on the given class itself.
	 * <p>
	 * This method explicitly handles class-level annotations which are not declared as {@link java.lang.annotation.Inherited
	 * inherited} <em>as well
	 * as meta-annotations and annotations on interfaces</em>.
	 * <p>
	 * The algorithm operates as follows:
	 * <ol>
	 * <li>Search for the annotation on the given class and return it if found.
	 * <li>Recursively search through all annotations that the given class declares.
	 * <li>Recursively search through all interfaces that the given class declares (Skipped, see note 2 below).
	 * <li>Recursively search through the superclass hierarchy of the given class.
	 * </ol>
	 * <p>
	 * Note 1: in this context, the term <em>recursively</em> means that the search process continues by returning to step #1 with
	 * the current interface, annotation, or superclass as the class to look for annotations on.
	 * <p>
	 * Note 2: <b>step #2 is actually skipped in order to keep GWT compatibility.</b>
	 * @param clazz the class to look for annotations on
	 * @param annotationType the type of annotation to look for
	 * @return the first matching annotation, or {@code null} if not found */
	public static <A extends Annotation> A findAnnotation (Class<?> clazz, Class<A> annotationType) {
		return findAnnotation(clazz, annotationType, new HashSet<com.badlogic.gdx.utils.reflect.Annotation>());
	}

	/** Perform the search algorithm for {@link #findAnnotation(Class, Class)}, avoiding endless recursion by tracking which
	 * annotations have already been <em>visited</em>.
	 * @param clazz the class to look for annotations on
	 * @param annotationType the type of annotation to look for
	 * @param visited the set of annotations that have already been visited
	 * @return the first matching annotation, or {@code null} if not found */
	@SuppressWarnings("unchecked")
	private static <A extends Annotation> A findAnnotation (Class<?> clazz, Class<A> annotationType,
		Set<com.badlogic.gdx.utils.reflect.Annotation> visited) {
		try {
			com.badlogic.gdx.utils.reflect.Annotation[] anns = ClassReflection.getDeclaredAnnotations(clazz);
			for (com.badlogic.gdx.utils.reflect.Annotation ann : anns) {
				Annotation a = ann.getAnnotation(annotationType);
				if (a != null) return (A)a;
			}
			for (com.badlogic.gdx.utils.reflect.Annotation ann : anns) {
				if (!isInJavaLangAnnotationPackage(ann) && visited.add(ann)) {
					A annotation = findAnnotation(ann.getAnnotationType(), annotationType, visited);
					if (annotation != null) {
						return annotation;
					}
				}
			}
		} catch (Exception ex) {
			return null;
		}

		// TODO Unfortunately clazz.getInterfaces() breaks GWT compatibility :(
		// @off - disable libgdx formatter
		//for (Class<?> ifc : clazz.getInterfaces()) {
		//	A annotation = findAnnotation(ifc, annotationType, visited);
		//	if (annotation != null) {
		//		return annotation;
		//	}
		//}
		// @on - enable libgdx formatter

		Class<?> superclass = clazz.getSuperclass();
		if (superclass == null || Object.class == superclass) {
			return null;
		}

		return findAnnotation(superclass, annotationType, visited);
	}

	/** Determine if the supplied {@link Annotation} is defined in the core JDK {@code java.lang.annotation} package.
	 * @param annotation the annotation to check (can not be {@code null})
	 * @return {@code true} if the annotation is in the {@code java.lang.annotation} package */
	public static boolean isInJavaLangAnnotationPackage (com.badlogic.gdx.utils.reflect.Annotation annotation) {
		return isInJavaLangAnnotationPackage(annotation.getAnnotationType().getName());
	}

	/** Determine if the {@link Annotation} with the supplied name is defined in the core JDK {@code java.lang.annotation} package.
	 * @param annotationType the name of the annotation type to check (never {@code null} or empty)
	 * @return {@code true} if the annotation is in the {@code java.lang.annotation} package */
	public static boolean isInJavaLangAnnotationPackage (String annotationType) {
		return annotationType.startsWith("java.lang.annotation");
	}
}
