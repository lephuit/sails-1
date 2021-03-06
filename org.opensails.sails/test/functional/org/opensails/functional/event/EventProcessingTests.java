package org.opensails.functional.event;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.lang.ArrayUtils;
import org.opensails.functional.SailsFunctionalTester;
import org.opensails.functional.ShamApplicationStartable;
import org.opensails.functional.controllers.EventTestController;
import org.opensails.functional.controllers.EventTestSubclassController;
import org.opensails.functional.controllers.ExampleEnum;
import org.opensails.rigging.Startable;
import org.opensails.sails.action.IAction;
import org.opensails.sails.action.IActionListener;
import org.opensails.sails.adapter.AbstractAdapter;
import org.opensails.sails.adapter.AdaptationException;
import org.opensails.sails.controller.oem.BaseController;
import org.opensails.sails.event.ISailsEvent;
import org.opensails.sails.form.FormMeta;
import org.opensails.sails.http.ContentType;
import org.opensails.sails.tester.Page;
import org.opensails.sails.tester.browser.Browser;
import org.opensails.sails.tester.browser.SailsTestApplication;
import org.opensails.sails.tester.browser.ShamFormFields;

public class EventProcessingTests extends TestCase implements IActionListener {
	private int beginExecutionCallCount = 0;
	private int endExecutionCallCount;

	public void beginExecution(IAction action) {
		beginExecutionCallCount++;
	}

	public void endExecution(IAction action) {
		endExecutionCallCount++;
	}

	public void testActionReturnsResult() {
		SailsFunctionalTester tester = new SailsFunctionalTester(EventTestController.class);
		Page page = tester.get("actionReturnsResult");
		page.assertContains("string rendered");
	}

	/**
	 * This tests the
	 * {@link BaseConfigurator#configure(ISailsEvent, org.opensails.viento.IBinding)}
	 * method. It makes sure that the Text type mixin works.
	 */
	public void testConfigureBinding_Mixins() throws Exception {
		SailsFunctionalTester t = new SailsFunctionalTester();
		Page page = t.getTemplated("$form.text('thename').decorated");
		page.assertContains("<span class=\"decorated\"><input");
	}

	@SuppressWarnings("unused")
	public void testExceptions() throws Exception {
		SailsFunctionalTester t = new SailsFunctionalTester();

		final Map<Class<? extends Throwable>, Throwable> exceptions = new HashMap<Class<? extends Throwable>, Throwable>();
		BaseController controller = new BaseController() {
					public void indexBounds() {
						throw new IndexOutOfBoundsException();
					}
		
					public void nullPointer() {
						throw new NullPointerException();
					}
		
					public void runtime() {
						throw new RuntimeException();
					}
		
					protected void handle(IndexOutOfBoundsException e) {
						exceptions.put(IndexOutOfBoundsException.class, e);
					}
		
					protected void handle(NullPointerException e) {
						exceptions.put(NullPointerException.class, e);
					}
				};

		t.getApplication().registerController("errorTest", controller);
		t.get("errorTest", "nullPointer");
		
		t.getApplication().registerController("errorTest", controller);
		t.get("errorTest", "indexBounds");

		t.getApplication().registerController("errorTest", controller);
		try {
			t.get("errorTest", "runtime");
		} catch (Exception e) {
			exceptions.put(Exception.class, e);
		}

		assertNotNull(exceptions.get(NullPointerException.class));
		assertNotNull(exceptions.get(IndexOutOfBoundsException.class));
		assertNotNull(exceptions.get(Exception.class));
	}

	public void testFlash() throws Exception {
		Browser browserOne = new SailsFunctionalTester(Flasher.class);
		SailsTestApplication application = browserOne.getApplication();
		Browser browserTwo = application.openBrowser(Flasher.class);

		application.registerController(Flasher.class);

		browserOne.get("flashInRequest");
		browserOne.getSession().assertNull();
		Page pageBrowserOne = browserOne.get("flashInSession");
		browserOne.getSession().assertExists();
		pageBrowserOne.flash().assertContains("something");

		browserTwo.get("flashInRequest");
		browserTwo.getSession().assertNull();
		pageBrowserOne.flash().assertContains("something");

		// clincher - make sure browser one still has flash
		browserTwo.get("flashInRequest");
		pageBrowserOne.flash().assertContains("something");
	}

	public void testGet() {
		SailsFunctionalTester tester = new SailsFunctionalTester(EventTestController.class);
		registerAsActionListener(tester);
		Page page = tester.get("simpleGet");
		page.assertTemplate("eventTest/simpleGet");
		page.assertContentType(ContentType.TEXT_HTML);
		assertHeardActionEvents();
	}

	public void testGet_ActionsInSuperclass() {
		SailsFunctionalTester tester = new SailsFunctionalTester(EventTestSubclassController.class);
		registerAsActionListener(tester);
		Page page = tester.get("simpleGet");
		page.assertTemplate("eventTestSubclass/simpleGet");
		assertHeardActionEvents();
	}

	public void testGet_DifferentTemplateRendered() {
		SailsFunctionalTester tester = new SailsFunctionalTester(EventTestController.class);
		Page page = tester.get("differentTemplate");
		page.assertContains("rendered different/template");
	}

	public void testGet_NoCodeBehind() {
		SailsFunctionalTester tester = new SailsFunctionalTester(EventTestController.class);
		Page page = tester.get("noCodeBehind");
		page.assertContains("noCodeBehind");
	}

	public void testGet_NoImplementation() {
		SailsFunctionalTester tester = new SailsFunctionalTester();
		tester.registerTemplate("noImplementation/index", "index $event.url");
		Page page = tester.get("noImplementation", "index", ArrayUtils.EMPTY_OBJECT_ARRAY);
		page.assertContains("index");
	}

	public void testGet_Parameters() {
		SailsFunctionalTester tester = new SailsFunctionalTester(EventTestController.class);
		Page page = tester.get("parameterGet", new Object[] { "true", "two", ExampleEnum.ENUM_EXAMPLE_TWO, "four" });
		page.assertTemplate("eventTest/parameterGet");
		page.assertContains("true");
		page.assertContains("two");
		page.assertContains(ExampleEnum.ENUM_EXAMPLE_TWO.name());

		page = tester.get("parameterGet");
		page.assertRenderFails("Arguments weren't exposed: zero parameter event for parameterized action, code doesn't get executed");
	}

	public void testPost() {
		SailsFunctionalTester t = new SailsFunctionalTester(EventTestController.class);
		registerAsActionListener(t);
		Page page = t.post("simplePost", t.getFormFields().quickSet("postedField", "postedValue"));
		page.assertTemplate("eventTest/simplePost");
		page.assertContains("postedValue");
		assertHeardActionEvents();
	}

	public void testPost_FieldsAdaptedAndSet() {
		SailsFunctionalTester tester = new SailsFunctionalTester(EventTestController.class);
		tester.getApplication().registerAdapter(MyDomainModel.class, MyAdapter.class);

		ShamFormFields formFields = tester.getFormFields();
		formFields.setValue("stringField", "postedStringFieldValue");
		formFields.setValue("intField", 3);
		formFields.setValue("floatField", 5.4);
		formFields.setValue("enumField", ExampleEnum.ENUM_EXAMPLE_ONE);
		formFields.setValues("stringArrayField", "one", "two");
		formFields.setValues("objectArrayField", "hello", "there");

		Page page = tester.post("simplePost", formFields);
		page.assertContains("postedStringFieldValue");
		page.assertContains("3");
		page.assertContains(ExampleEnum.ENUM_EXAMPLE_ONE.name());
		page.assertContains("[one, two]");
		page.assertContains("[hello, there]");
		page.assertExcludes("5.4");
	}

	public void testPost_MetaAction_ImageSubmit_Parameters() {
		SailsFunctionalTester t = new SailsFunctionalTester(EventTestController.class);

		ShamFormFields fields = t.getFormFields();
		fields.setValue("postedField", "postedValue");
		fields.setValue(FormMeta.action("parameterPost", "one", "2"), "Submit Label");
		fields.setValue(FormMeta.action("parameterPost.x"), "45");
		fields.setValue(FormMeta.action("parameterPost.y"), "23");

		Page page = t.post("notTheActionInTheMeta", t.getFormFields().quickSet("postedField", "postedValue", FormMeta.action("parameterPost", "one", "2"), "Submit Label"), "three", "four", "areIgnored");
		page.assertContains("postedValue");
		page.assertContains("one");
		page.assertContains("2");
	}

	public void testPost_MetaAction_Parameters() {
		SailsFunctionalTester t = new SailsFunctionalTester(EventTestController.class);
		Page page = t.post("notTheActionInTheMeta", t.getFormFields().quickSet("postedField", "postedValue", FormMeta.action("parameterPost", "one", "2"), "Submit Label"), "three", "four", "areIgnored");
		page.assertContains("postedValue");
		page.assertContains("one");
		page.assertContains("2");
	}

	public void testPost_Parameters() {
		SailsFunctionalTester t = new SailsFunctionalTester(EventTestController.class);
		Page page = t.post("parameterPost", t.getFormFields().quickSet("postedField", "postedValue"), "one", "2");
		page.assertContains("postedValue");
		page.assertContains("one");
		page.assertContains("2");
	}

	public void testStartables() throws Exception {
		SailsFunctionalTester tester = new SailsFunctionalTester();
		tester.instance(ShamApplicationStartable.class).assertStarted();

		tester.inject(MyStartableThing.class);
		tester.get();
		tester.instance(MyStartableThing.class).assertStarted();
	}

	private void assertHeardActionEvents() {
		// Once for application container, once for event container
		assertEquals(2, beginExecutionCallCount);
		assertEquals(2, endExecutionCallCount);
	}

	private void registerAsActionListener(SailsFunctionalTester tester) {
		tester.getContainer().register(this);
		tester.getApplicationContainer().register(this);
	}

	public static class Flasher extends BaseController {
		public void flashInRequest() {
			flash("something", "inthere");
		}

		public void flashInSession() {
			flashSession("something", "inthere");
		}
	}

	public static class MyAdapter extends AbstractAdapter<MyDomainModel, String> {
		public MyDomainModel forModel(Class<? extends MyDomainModel> modelType, String fromWeb) throws AdaptationException {
			return new MyDomainModel(fromWeb);
		}

		public String forWeb(Class<? extends MyDomainModel> modelType, MyDomainModel fromModel) throws AdaptationException {
			return fromModel.web;
		}
	}

	public static class MyDomainModel {
		public final String web;

		public MyDomainModel(String fromWeb) {
			this.web = fromWeb;
		}
	}

	public static class MyStartableThing implements Startable {

		private boolean wasStarted;

		public void assertStarted() {
			assertTrue(wasStarted);
		}

		public void start() {
			wasStarted = true;
		}

	}
}
