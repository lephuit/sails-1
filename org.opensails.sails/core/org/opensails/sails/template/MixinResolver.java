package org.opensails.sails.template;

import java.util.ArrayList;
import java.util.List;

import org.opensails.sails.ISailsEvent;
import org.opensails.sails.controller.IControllerImpl;
import org.opensails.sails.util.IClassResolver;

public class MixinResolver implements IMixinResolver {
	protected IControllerImpl controller;
	protected final ISailsEvent event;
	protected final List<IClassResolver> resolvers;

	public MixinResolver(ISailsEvent event) {
		this.event = event;
		this.resolvers = new ArrayList<IClassResolver>();
	}

	@SuppressWarnings("unchecked")
	public Object methodMissing(String methodName, Object[] args) throws NoSuchMethodException {
		for (IClassResolver resolver : resolvers) {
			Class clazz = resolver.resolve(methodName);
			if (clazz != null) {
				Object instance = event.getContainer().instance(clazz, clazz);
				if (instance instanceof IMixinMethod) return ((IMixinMethod) instance).invoke(args);
				return instance;
			}
		}
		throw new NoSuchMethodException("Could not resolve a mixin for " + methodName);
	}

	public void push(IClassResolver resolver) {
		resolvers.add(0, resolver);
	}
}