package br.pro.hashi.nfp.rest;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Iterator;

import org.reflections.Reflections;

import br.pro.hashi.nfp.rest.exception.ReflectionsException;

public class SimpleReflections extends Reflections {
	public SimpleReflections(String prefix) {
		super(prefix);
	}

	public <E> Iterable<E> getSubInstancesOf(Class<E> type) {
		return new Iterable<>() {
			@Override
			public Iterator<E> iterator() {
				Iterator<Class<? extends E>> iterator = getSubTypesOf(type).stream()
						.filter((subType) -> !Modifier.isAbstract(subType.getModifiers()))
						.iterator();

				return new Iterator<E>() {
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
							instance = (E) constructor.newInstance();
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
