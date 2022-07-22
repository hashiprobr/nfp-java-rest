package br.pro.hashi.nfp.rest;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Iterator;
import java.util.Stack;

import br.pro.hashi.nfp.rest.exception.ReflectionsException;

public class Reflections extends org.reflections.Reflections {
	public static <T, S extends T> Type getTypeParameter(Class<T> superType, S object, int i) {
		Stack<Class<?>> stack = new Stack<>();

		Class<?> subType = object.getClass();
		do {
			stack.push(subType);
			subType = subType.getSuperclass();
		} while (!subType.equals(superType));

		Type type = null;
		while (true) {
			subType = stack.pop();
			ParameterizedType genericType = (ParameterizedType) subType.getGenericSuperclass();
			Type[] types = genericType.getActualTypeArguments();
			type = types[i];
			if (type instanceof TypeVariable) {
				TypeVariable<?>[] subVariables = subType.getTypeParameters();
				TypeVariable<?> variable = (TypeVariable<?>) type;
				i = 0;
				while (!subVariables[i].equals(variable)) {
					i++;
				}
			} else {
				break;
			}
		}
		return type;
	}

	public Reflections(String prefix) {
		super(prefix);
	}

	public <E> Iterable<E> getSubInstancesOf(Class<E> type) {
		return new Iterable<>() {
			@Override
			public Iterator<E> iterator() {
				Iterator<Class<? extends E>> iterator = getSubTypesOf(type).stream()
						.filter((subType) -> !Modifier.isAbstract(subType.getModifiers()))
						.iterator();

				return new Iterator<>() {
					@Override
					public boolean hasNext() {
						return iterator.hasNext();
					}

					@Override
					public E next() {
						Class<? extends E> subType = iterator.next();
						E instance;
						try {
							Constructor<? extends E> constructor = subType.getConstructor();
							instance = constructor.newInstance();
						} catch (NoSuchMethodException exception) {
							throw new ReflectionsException(exception);
						} catch (InvocationTargetException exception) {
							throw new ReflectionsException(exception);
						} catch (IllegalAccessException exception) {
							throw new ReflectionsException(exception);
						} catch (InstantiationException exception) {
							throw new ReflectionsException(exception);
						}
						return instance;
					}
				};
			}
		};
	}
}
