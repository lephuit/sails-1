package org.opensails.sails.controller.oem;

import java.util.List;

import org.opensails.sails.action.oem.RedirectActionResult;
import org.opensails.sails.action.oem.TemplateActionResult;
import org.opensails.sails.controller.IController;
import org.opensails.sails.controller.IControllerImpl;
import org.opensails.sails.event.oem.AbstractEventProcessingContext;

public class BaseController extends AbstractEventProcessingContext<IController> implements IControllerImpl {
	protected void layout(String templateIdentifier) {
		getTemplateResult().setLayout(templateIdentifier);
	}

	protected RedirectActionResult redirectAction(Class<? extends IControllerImpl> controller, String action) {
		return setResult(new RedirectActionResult(event, controller, action));
	}

	protected RedirectActionResult redirectAction(Class<? extends IControllerImpl> controller, String action, List<?> parameters) {
		return setResult(new RedirectActionResult(event, controller, action, parameters));
	}

	protected TemplateActionResult renderIndex() {
		return renderTemplate("index");
	}
}
