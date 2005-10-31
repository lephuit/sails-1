package org.opensails.sails.controller.oem;

import java.lang.reflect.Method;
import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.commons.lang.ArrayUtils;
import org.opensails.sails.controller.IActionResult;
import org.opensails.sails.util.CollectionAssert;

public class ActionTest extends TestCase {
	Method actionMethodNoParams;
	Method actionMethodOneParam;
	Method actionMethodTwoParams;
	Method voidActionParams;

	public ActionTest() throws Exception, Exception {
		voidActionParams = ShamController.class.getMethod("voidActionParams", new Class[] { String.class });
		actionMethodNoParams = ShamController.class.getMethod("voidActionMultiple", ArrayUtils.EMPTY_CLASS_ARRAY);
		actionMethodOneParam = ShamController.class.getMethod("voidActionMultiple", new Class[] { int.class });
		actionMethodTwoParams = ShamController.class.getMethod("voidActionMultiple", new Class[] { String.class, Object.class });
	}

	public void testConstructor_MultipleMethods() throws Exception {
		Action action = ActionFixture.defaultAdapters("voidActionMultiple", ShamController.class);
		CollectionAssert.containsOnlyOrdered(new Method[] { actionMethodTwoParams, actionMethodOneParam, actionMethodNoParams }, action.actionMethods);
	}

	public void testConstructor_NoImplementation() throws Exception {
		Action action = ActionFixture.defaultAdapters("voidActionMultiple", null);
		CollectionAssert.containsOnlyOrdered(new Method[] {}, action.actionMethods);
	}

	/**
	 * It is important to maintain knowledge about the number of arguments for
	 * each method that matches our action name. This allows the action to
	 * execute the method that most closely resembles our URL and it's quantity
	 * of parameters.
	 */
	public void testConstructor_SingleMethod() throws Exception {
		Action voidActionNoParams = ActionFixture.defaultAdapters("voidActionNoParams", ShamController.class);
		Method voidActionNoParamsMethod = ShamController.class.getMethod("voidActionNoParams", ArrayUtils.EMPTY_CLASS_ARRAY);
		CollectionAssert.containsOnly(voidActionNoParamsMethod, voidActionNoParams.actionMethods);
	}

	public void testExecute() throws Exception {
		Action action = ActionFixture.defaultAdapters("voidActionMultiple", ShamController.class);
		ShamController controller = new ShamController();
		ActionFixture.execute(action, controller);
		assertEquals("voidActionMultiple()", controller.actionInvoked);

		try {
			ActionFixture.execute(action, new BaseController());
			fail("Can't execute this action against a different controller type");
		} catch (IllegalArgumentException expected) {}
	}

	public void testExecute_AdaptersUsed() {
		Action action = ActionFixture.defaultAdapters("voidActionMultiple", ShamController.class);
		ShamController controller = new ShamController();
		ActionFixture.execute(action, controller, new String[] { "1" });
		assertEquals(1, controller.intArgumentValue);
	}

	/**
	 * If the url has zero parameters, and there is not a zero argument method,
	 * don't call any code
	 */
	public void testExecute_MostNumberOfArguments() throws Exception {
		Action action = ActionFixture.defaultAdapters("voidActionParams", ShamController.class);
		ShamController controller = new ShamController();
		ActionFixture.execute(action, controller);
		assertNull(controller.actionInvoked);
	}

	public void testExecute_NullControllerInstance() {
		Action action = ActionFixture.defaultAdapters("voidActionMultiple", null);
		assertNotNull(ActionFixture.execute(action, null, new String[] { "1" }));
	}

	public void testExecute_ResultDefault() {
		Action action = ActionFixture.defaultAdapters("voidActionMultiple", ShamController.class);
		ShamController controller = new ShamController();
		IActionResult result = ActionFixture.execute(action, controller);
		assertEquals(TemplateActionResult.class, result.getClass());
	}

	public void testExecute_ResultFromActionMethod() throws Exception {
		Action action = ActionFixture.defaultAdapters("resultAction", ShamController.class);
		ShamController controller = new ShamController();
		IActionResult result = ActionFixture.execute(action, controller);
		assertSame(controller.resultReturned, result);
	}

	public void testExecute_ResultInContainer() {
		Action action = ActionFixture.defaultAdapters("voidActionContainerResult", ShamController.class);
		ShamController controller = new ShamController();
		IActionResult result = ActionFixture.execute(action, controller);
		assertSame("make sure it is the same one placed in the container by the action", controller.resultReturned, result);
	}

	public void testGetParameterTypes() throws Exception {
		Action action = ActionFixture.defaultAdapters("voidActionMultiple", ShamController.class);
		assertTrue(Arrays.equals(actionMethodOneParam.getParameterTypes(), action.getParameterTypes(1)));
		assertEquals(0, action.getParameterTypes(0).length);
	}

	public void testMethodHavingArgCount() throws Exception {
		Action action = ActionFixture.defaultAdapters("voidActionMultiple", ShamController.class);
		assertEquals(actionMethodTwoParams, action.methodHavingArgCount(2));
		assertEquals(actionMethodTwoParams, action.methodHavingArgCount(5));
		assertEquals(actionMethodOneParam, action.methodHavingArgCount(1));
		assertEquals(actionMethodNoParams, action.methodHavingArgCount(0));

		action = ActionFixture.defaultAdapters("voidActionParams", ShamController.class);
		assertNull(action.methodHavingArgCount(0));
	}
}