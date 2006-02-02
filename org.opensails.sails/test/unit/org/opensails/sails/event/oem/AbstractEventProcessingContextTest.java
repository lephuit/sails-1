package org.opensails.sails.event.oem;

import junit.framework.TestCase;

import org.opensails.sails.action.oem.TemplateActionResult;
import org.opensails.sails.controller.IController;
import org.opensails.sails.controller.oem.BaseController;
import org.opensails.sails.form.FormFields;

public class AbstractEventProcessingContextTest extends TestCase {
	public void testFieldAndFields() {
		FormFields formFields = new FormFields();
		formFields.setValue("my.field", "myValue");

		AbstractEventProcessingContext<IController> controller = new BaseController();
		controller.setEventContext(SailsEventFixture.actionPost(formFields), null);
		assertEquals("myValue", controller.field("my.field"));

		controller.setEventContext(SailsEventFixture.actionGet(), null);
		assertNull(controller.field("whatever"));
	}

	public void testGetTemplateResult() throws Exception {
		AbstractEventProcessingContext<IController> controller = new BaseController();
		controller.setEventContext(SailsEventFixture.sham(), null);
		TemplateActionResult templateResult = controller.getTemplateResult();
		assertSame(templateResult, controller.getTemplateResult());
	}

	public void testRenderTemplate() throws Exception {
		AbstractEventProcessingContext<IController> controller = new BaseController();
		GetEvent actionGet = SailsEventFixture.actionGet();
		controller.setEventContext(actionGet, null);
		TemplateActionResult result = controller.renderTemplate("justTheActionName");
		assertEquals(String.format("%s/%s", actionGet.getProcessorName(), "justTheActionName"), result.getIdentifier());
	}
}