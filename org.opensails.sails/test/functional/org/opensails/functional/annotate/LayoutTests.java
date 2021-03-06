package org.opensails.functional.annotate;

import junit.framework.TestCase;

import org.opensails.functional.SailsFunctionalTester;
import org.opensails.functional.controllers.LayoutSuperController;
import org.opensails.functional.controllers.LayoutTestController;
import org.opensails.sails.tester.Page;

public class LayoutTests extends TestCase {
	public void testBasic() throws Exception {
		SailsFunctionalTester tester = new SailsFunctionalTester(LayoutSuperController.class);
		Page page = tester.get("one");
		page.assertLayout("layoutSuper/classSuperLayout");

		// Test that NoLayout and Layout handler used is same instance
		for (int i = 0; i < 20; i++) {
			page = tester.get("eight");
			page.assertLayout(null);
		}

		tester = new SailsFunctionalTester(LayoutTestController.class);
		page = tester.get("one");
		page.assertLayout("layoutTest/oneLayout");

		page = tester.get("two");
		page.assertLayout("layoutTest/twoSuperLayout");

		page = tester.get("three");
		page.assertLayout("layoutTest/classSuperLayout");

		page = tester.get("four");
		page.assertLayout("layoutTest/classSuperLayout");

		page = tester.get("five");
		page.assertLayout("layoutTest/renderTemplateLayout");

		page = tester.get("six");
		page.assertLayout(null);

		page = tester.get("seven");
		page.assertLayout(null);
	}

	public void testRealRender() throws Exception {
		SailsFunctionalTester t = new SailsFunctionalTester(LayoutTestController.class);
		t.get().assertEquals("layout top\r\ncontent of index\r\nlayout bottom");
	}
}
