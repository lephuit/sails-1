package org.opensails.sails.oem;

import junit.framework.TestCase;

import org.opensails.rigging.IScopedContainer;
import org.opensails.sails.ApplicationContainer;
import org.opensails.sails.adapter.IAdapter;
import org.opensails.sails.adapter.IAdapterResolver;
import org.opensails.sails.adapter.oem.AdapterResolver;
import org.opensails.sails.controller.IController;
import org.opensails.sails.controller.IControllerImpl;
import org.opensails.sails.controller.oem.ControllerResolver;
import org.opensails.sails.controller.oem.ShamController;
import org.opensails.spyglass.ClassResolverAdapter;

public class ControllerResolverTest extends TestCase {
	protected int resolutionCount = 0;
	ApplicationContainer container;
	ControllerResolver resolver;

	public void testResolve() throws Exception {
		IController controller = resolver.resolve("sham");
		assertEquals(ShamController.class, controller.getImplementation());
		assertEquals(1, resolutionCount);
		resolver.resolve("sham");
		assertEquals("Should be cached", 1, resolutionCount);
	}

	public void testResolve_HandlesNull() throws Exception {
		ControllerResolver resolver = new ControllerResolver(new AdapterResolver());
		resolver.push(new ClassResolverAdapter<IControllerImpl>() {
			@Override
			public Class<IControllerImpl> resolve(String key) {
				return null;
			}
		});
		IController controller = resolver.resolve("sham");
		assertNotNull(controller);
		assertNull(controller.getImplementation());
	}

	@Override
	protected void setUp() throws Exception {
		container = new ApplicationContainer();
		container.register(IAdapterResolver.class, new IAdapterResolver() {
			public IAdapter resolve(Class<?> targetType, IScopedContainer container) {
				return null;
			}
		});

		resolver = new ControllerResolver(new AdapterResolver());
		resolver.push(new ClassResolverAdapter() {
			@Override
			public Class resolve(String key) {
				resolutionCount++;
				return ShamController.class;
			}
		});
	}
}
