package org.opensails.sails.form;

import org.opensails.sails.adapter.AdaptationTarget;
import org.opensails.sails.adapter.ContainerAdapterResolver;
import org.opensails.sails.adapter.IAdapter;
import org.opensails.sails.adapter.oem.PrimitiveAdapter;
import org.opensails.sails.model.IPropertyAccessor;
import org.opensails.sails.model.IPropertyFactory;
import org.opensails.sails.model.IPropertyPath;
import org.opensails.sails.model.ModelContext;

/**
 * An html form post processor.
 * <p>
 * This encapsulates the behavior of:
 * <ul>
 * <li>transferring values into the model</li>
 * <li>validating the models using an ValidationContext</li>
 * </ul>
 * 
 * @author aiwilliams
 */
public class HtmlForm {
	protected FormFields formFields;
	protected ValidationContext validationContext;
	protected ModelContext modelContext;
	protected IPropertyFactory propertyFactory;
	protected ContainerAdapterResolver adapterResolver;

	/**
	 * @param validationContext used to validate the models after their values
	 *        have been transferred from the form fields into the models
	 * @param valueModel used to read and write the model during both render and
	 *        form post processing
	 * 
	 */
	public HtmlForm(ValidationContext validationContext, ModelContext modelContext, IPropertyFactory propertyFactory, ContainerAdapterResolver adapterResolver) {
		this.modelContext = modelContext;
		this.validationContext = validationContext;
		this.propertyFactory = propertyFactory;
		this.adapterResolver = adapterResolver;
	}

	public ValidationContext getValidationContext() {
		return validationContext;
	}

	/**
	 * @return true if the ValidationContext has no errors
	 */
	public boolean isValid() {
		return !validationContext.hasErrors();
	}

	/**
	 * @param formFields the fields for updating the models
	 */
	public boolean updateModels(FormFields formFields) {
		this.formFields = formFields;
		transferFormIntoModels(formFields);
		return validateModels();
	}

	public boolean validateModels() {
		return validationContext.validate();
	}

	@SuppressWarnings("unchecked")
	public <M, W> W value(String propertyPath) {
		IPropertyPath path = propertyFactory.createPath(propertyPath);
		Object model = modelContext.getModel(path);
		if (model == null) return null;

		IPropertyAccessor accessor = propertyFactory.createAccessor(path);
		AdaptationTarget adaptationTarget = accessor.getAdaptationTarget(model);
		if (!adaptationTarget.isReadable()) return null;

		IAdapter<M, W> adapter = adapter(path, model, adaptationTarget);
		return adapter.forWeb(adaptationTarget, accessor.<Object, M> get(model));
	}

	public <M, W> M valueModel(String propertyPath) {
		IPropertyPath path = propertyFactory.createPath(propertyPath);
		Object model = modelContext.getModel(path);
		if (model == null) return null;
		return propertyFactory.createAccessor(path).<Object, M> get(model);
	}

	protected IAdapter adapter(IPropertyPath path, Object model, AdaptationTarget adaptationTarget) {
		if (adaptationTarget.getTargetClass() == void.class) return new PrimitiveAdapter.StringAdapter();
		else return adapterResolver.resolve(adaptationTarget.getTargetClass());
	}

	@SuppressWarnings("unchecked")
	protected void transferFormIntoModels(FormFields formFields) {
		for (String name : formFields.getNames()) {
			IPropertyPath path = propertyFactory.createPath(name);
			Object model = modelContext.getModel(path);
			if (model == null) continue;

			IPropertyAccessor accessor = propertyFactory.createAccessor(path);
			AdaptationTarget adaptationTarget = accessor.getAdaptationTarget(model);
			if (!adaptationTarget.exists()) continue;
			IAdapter adapter = adapter(path, model, adaptationTarget);
			accessor.set(model, adapter.forModel(adaptationTarget, adapter.getFieldType().getValue(formFields, name)));
		}
	}
}
