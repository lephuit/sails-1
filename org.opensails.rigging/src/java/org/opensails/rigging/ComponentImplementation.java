package org.opensails.rigging;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

public class ComponentImplementation implements ComponentResolver {
	protected IContainer container;
	protected Object instance;
	protected Class theImplementation;
	protected Class theKey;

	public <T> ComponentImplementation(Class<T> theKey, Class<? extends T> theImplementation, IContainer container) {
		this.theKey = theKey;
		this.theImplementation = theImplementation;
		this.container = container;
	}

	@SuppressWarnings("unchecked")
	public ComponentResolver cloneFor(SimpleContainer container) {
		return new ComponentImplementation(theKey, theImplementation, container);
	}

	public Object instance() {
		if (instance == null) return instance = instantiate();
		return instance;
	}

	public boolean isInstantiated() {
		return instance != null;
	}

	public Class<?> type() {
		return theImplementation;
	}

	@SuppressWarnings("unchecked")
	protected <T extends Annotation> T annotation(Class<T> annotationType, Constructor constructor, int parameterIndex) {
		try { // quick fix for Ticket #161
			for (Annotation annotation : constructor.getParameterAnnotations()[parameterIndex])
				if (annotation.annotationType() == annotationType) return (T) annotation;
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
		return null;
	}

	protected boolean canSatisfy(Constructor constructor) {
		boolean canSatisfy = true;
		Class[] parameterTypes = constructor.getParameterTypes();
		for (int i = 0; i < parameterTypes.length; i++) {
			Class parameterType = parameterTypes[i];
			if (isCircularReference(parameterType)) return false;
			WhenNotInstantiated whenNotInstantiated = annotation(WhenNotInstantiated.class, constructor, i);
			if (!container.contains(parameterType) && (whenNotInstantiated == null || findConstructor(whenNotInstantiated.value()) == null)) canSatisfy = false;
		}
		return canSatisfy;
	}

	protected boolean isCircularReference(Class parameterType) {
		return parameterType == theKey;
	}

	protected Constructor findConstructor() {
		Constructor[] constructors = theImplementation.getConstructors();
		Constructor greedyless = null;
		Constructor greediest = null;
		for (Constructor constructor : constructors) {
			if (greedyless == null || constructor.getParameterTypes().length < greedyless.getParameterTypes().length) greedyless = constructor;
			if ((greediest == null || constructor.getParameterTypes().length > greediest.getParameterTypes().length) && canSatisfy(constructor)) greediest = constructor;
		}
		if (greediest == null) throw new UnsatisfiableDependenciesException(greedyless);
		return greediest;
	}

	// Duplication. Can't return two things. Please refactor.
	protected Constructor findConstructor(Class theClass) {
		Constructor[] constructors = theClass.getConstructors();
		Constructor greedyless = null;
		Constructor greediest = null;
		for (Constructor constructor : constructors) {
			if (greedyless == null || constructor.getParameterTypes().length < greedyless.getParameterTypes().length) greedyless = constructor;
			if ((greediest == null || constructor.getParameterTypes().length > greediest.getParameterTypes().length) && canSatisfy(constructor)) greediest = constructor;
		}
		return greediest;
	}

	@SuppressWarnings("unchecked")
	protected Object instantiate() {
		Constructor constructor = findConstructor();
		if (constructor == null) return null;
		Class[] parameterTypes = constructor.getParameterTypes();
		Object[] parameters = new Object[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			WhenNotInstantiated whenNotInstantiated = annotation(WhenNotInstantiated.class, constructor, i);
			if (whenNotInstantiated != null && !container.resolver(parameterTypes[i]).isInstantiated()) parameters[i] = container.instance(whenNotInstantiated.value(), whenNotInstantiated.value());
			else parameters[i] = container.instance(parameterTypes[i]);
		}
		try {
			Object newInstance = constructor.newInstance(parameters);
			container.notifyInstantiationListeners(theKey, newInstance);
			container.notifyInstantiationListeners(theImplementation, newInstance);
			return newInstance;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
