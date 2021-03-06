package org.opensails.sails.tester.browser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opensails.rigging.IScopedContainer;
import org.opensails.sails.IEventContextContainer;
import org.opensails.sails.ISailsApplication;
import org.opensails.sails.event.oem.GetEvent;

public class TesterGetEvent extends GetEvent {
	public TesterGetEvent(ISailsApplication application, TesterRequestContainer container, HttpServletRequest req, HttpServletResponse resp) {
		super(req, resp);
		this.application = application;
		this.container = container;
		initialize(application.getContainer());
	}

	public void setActionParameters(String[] parameters) {
		url.setParameters(parameters);
	}

	@Override
	protected void containerSet() {
		super.containerSet();
		container.register(GetEvent.class, this);
	}

	@Override
	protected IEventContextContainer createContainer(IScopedContainer parentContainer) {
		return container;
	}
}
