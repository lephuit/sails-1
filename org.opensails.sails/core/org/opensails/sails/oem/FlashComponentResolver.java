package org.opensails.sails.oem;

import org.opensails.rigging.ComponentResolver;
import org.opensails.rigging.SimpleContainer;
import org.opensails.sails.event.ISailsEvent;

public class FlashComponentResolver implements ComponentResolver {
	protected final ISailsEvent event;
	protected Flash flash;

	public FlashComponentResolver(ISailsEvent event) {
		this.event = event;
	}

	public ComponentResolver cloneFor(SimpleContainer container) {
		throw new UnsupportedOperationException();
	}

	public Object instance() {
		if (flash == null) flash = Flash.load(event.getRequest(), event.getSession(false));
		return flash;
	}

	public boolean isInstantiated() {
		return flash != null || Flash.exists(event.getSession(false));
	}

	public Class<?> type() {
		return Flash.class;
	}
}
