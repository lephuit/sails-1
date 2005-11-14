package org.opensails.sails.tester;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opensails.rigging.ScopedContainer;
import org.opensails.sails.ISailsApplication;
import org.opensails.sails.RequestContainer;
import org.opensails.sails.oem.PostEvent;

public class TestPostEvent extends PostEvent {
	public TestPostEvent(ISailsApplication application, TestRequestContainer container, HttpServletRequest req, HttpServletResponse resp) {
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
		container.register(PostEvent.class, this);
	}

	@Override
	protected RequestContainer createContainer(ScopedContainer parentContainer) {
		return container;
	}
}
