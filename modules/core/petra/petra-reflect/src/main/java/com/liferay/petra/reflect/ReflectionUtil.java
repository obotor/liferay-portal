/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.petra.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author Brian Wing Shun Chan
 * @author Miguel Pastor
 * @author Shuyang Zhou
 */
public class ReflectionUtil {

	public static Object arrayClone(Object array) {
		Class<?> clazz = array.getClass();

		if (!clazz.isArray()) {
			throw new IllegalArgumentException(
				"Input object is not an array: " + array);
		}

		try {
			return _cloneMethod.invoke(array);
		}
		catch (Exception e) {
			return throwException(e);
		}
	}

	public static Field getDeclaredField(Class<?> clazz, String name)
		throws Exception {

		Field field = clazz.getDeclaredField(name);

		field.setAccessible(true);

		return unfinalField(field);
	}

	public static Field[] getDeclaredFields(Class<?> clazz) throws Exception {
		Field[] fields = clazz.getDeclaredFields();

		for (Field field : fields) {
			field.setAccessible(true);

			unfinalField(field);
		}

		return fields;
	}

	public static Method getDeclaredMethod(
			Class<?> clazz, String name, Class<?>... parameterTypes)
		throws Exception {

		Method method = clazz.getDeclaredMethod(name, parameterTypes);

		method.setAccessible(true);

		return method;
	}

	public static Class<?>[] getInterfaces(Object object) {
		return getInterfaces(object, null);
	}

	public static Class<?>[] getInterfaces(
		Object object, ClassLoader classLoader) {

		return getInterfaces(
			object, classLoader,
			cnfe -> {
			});
	}

	public static Class<?>[] getInterfaces(
		Object object, ClassLoader classLoader,
		Consumer<ClassNotFoundException> classNotFoundHandler) {

		Set<Class<?>> interfaceClasses = new LinkedHashSet<>();

		Class<?> superclass = object.getClass();

		while (superclass != null) {
			for (Class<?> interfaceClass : superclass.getInterfaces()) {
				try {
					if (classLoader == null) {
						interfaceClasses.add(interfaceClass);
					}
					else {
						interfaceClasses.add(
							classLoader.loadClass(interfaceClass.getName()));
					}
				}
				catch (ClassNotFoundException cnfe) {
					classNotFoundHandler.accept(cnfe);
				}
			}

			superclass = superclass.getSuperclass();
		}

		return interfaceClasses.toArray(new Class<?>[0]);
	}

	public static <T> T throwException(Throwable throwable) {
		return ReflectionUtil.<T, RuntimeException>_throwException(throwable);
	}

	public static Field unfinalField(Field field) throws Exception {
		int modifiers = field.getModifiers();

		if ((modifiers & _STATIC_FINAL) == _STATIC_FINAL) {
			_modifiersField.setInt(field, modifiers - Modifier.FINAL);
		}

		return field;
	}

	@SuppressWarnings("unchecked")
	private static <T, E extends Throwable> T _throwException(
			Throwable throwable)
		throws E {

		throw (E)throwable;
	}

	private static final int _STATIC_FINAL = Modifier.STATIC + Modifier.FINAL;

	private static final Method _cloneMethod;
	private static final Field _modifiersField;

	static {
		try {
			_cloneMethod = Object.class.getDeclaredMethod("clone");

			_cloneMethod.setAccessible(true);

			_modifiersField = Field.class.getDeclaredField("modifiers");

			_modifiersField.setAccessible(true);
		}
		catch (Exception e) {
			throw new ExceptionInInitializerError(e);
		}
	}

}