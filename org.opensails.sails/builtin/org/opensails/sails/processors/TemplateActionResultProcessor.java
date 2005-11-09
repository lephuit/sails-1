package org.opensails.sails.processors;

import org.opensails.rigging.ScopedContainer;
import org.opensails.sails.IActionResultProcessor;
import org.opensails.sails.controller.IControllerImpl;
import org.opensails.sails.controller.oem.TemplateActionResult;
import org.opensails.sails.template.IMixinResolver;
import org.opensails.sails.template.ITemplateBinding;
import org.opensails.sails.template.ITemplateRenderer;

public class TemplateActionResultProcessor implements IActionResultProcessor<TemplateActionResult> {
	protected final ITemplateRenderer renderer;

	public TemplateActionResultProcessor(ITemplateRenderer renderer) {
		this.renderer = renderer;
	}

	@SuppressWarnings("unchecked")
	public void process(TemplateActionResult result) {
		ITemplateBinding binding = result.getBinding();

		IControllerImpl controllerImpl = result.getController();
		if (controllerImpl != null)
			binding.mixin(controllerImpl);

		ScopedContainer container = result.getContainer();
		IMixinResolver resolver = container.instance(IMixinResolver.class);
		binding.mixin(resolver);

		StringBuilder content = new StringBuilder();
		renderer.render(result.getIdentifier(), binding, content);
		if (result.hasLayout()) {
			binding.put("contentForLayout", content);
			content = new StringBuilder();
			renderer.render(result.getLayout(), binding, content);
		}
		result.write(content.toString());
	}
}