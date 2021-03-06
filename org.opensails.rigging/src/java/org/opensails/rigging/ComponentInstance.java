package org.opensails.rigging;

public class ComponentInstance implements ComponentResolver {
	protected Object instance;

	public ComponentInstance(Object instance) {
		this.instance = instance;
	}

	public ComponentResolver cloneFor(SimpleContainer container) {
		return new ComponentInstance(instance);
	}

	public Object instance() {
		return instance;
	}

	public boolean isInstantiated() {
		return true;
	}

	public Class<?> type() {
		return instance.getClass();
	}
}
