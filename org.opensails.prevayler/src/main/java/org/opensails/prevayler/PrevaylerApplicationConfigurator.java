package org.opensails.prevayler;

import org.opensails.sails.ApplicationContainer;
import org.opensails.sails.IConfigurableSailsApplication;
import org.opensails.sails.IEventContextContainer;
import org.opensails.sails.event.ISailsEvent;
import org.opensails.sails.oem.BaseConfigurator;
import org.opensails.sails.persist.IObjectPersister;
import org.prevayler.Prevayler;

public abstract class PrevaylerApplicationConfigurator extends BaseConfigurator {

	// TODO why do I need to implement this method in addition to installObjectPersister()? I don't understand it's usefullness. This should probably be tested.
	// For Database based persistance IObjectPersisters are registered at the Request scope to keep transactions as short lived as possible.
	@Override
	public void configure(ISailsEvent event, IEventContextContainer eventContainer) {
		super.configure(event, eventContainer);
		eventContainer.register(IObjectPersister.class, PrevaylerPersister.class);
	}

	@Override
	protected void installObjectPersister(IConfigurableSailsApplication application, ApplicationContainer container) {
		super.installObjectPersister(application, container);
		container.register(Prevayler.class, getPrevayler());
		container.register(IObjectPersister.class, PrevaylerPersister.class);
	}

	/**
	 * Example implementation:
	 * 
	 * IdentifiableObjectPrevalentSystem prevalantSystem = new IdentifiableObjectPrevalentSystem(); 
	 * Prevayler prevayler = PrevaylerFactory.createTransientPrevayler(prevalantSystem); 
	 * return prevayler;
	 * 
	 * @return a Prevayler instance configured as you desire. See PrevaylerFactory.create methods for more info.
	 */
	protected abstract Class<Prevayler> getPrevayler();
}
