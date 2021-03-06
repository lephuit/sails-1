package org.opensails.sails.form.html;

import static org.opensails.sails.form.FormMeta.CHECKBOX_PREFIX;

import java.io.IOException;
import java.io.Writer;

import org.opensails.sails.html.HtmlConstants;
import org.opensails.sails.html.HtmlGenerator;

/**
 * An HTML INPUT of type CHECKBOX.
 * <p>
 * Checkboxes can be rendered in groups of one or more by name. If there is more
 * than one with the same name, the are submitted as a String[]. If they are by
 * themselves, they likely represent a boolean state, though they may not. One
 * of the challenges in making the display of a domain model work is knowing
 * whether, in the case of the boolean checkbox, the absence of a value on the
 * server side means that the box was displayed and then unchecked - the model
 * should have it's property set to false - or was never exposed at all. Of
 * course, if you are exposing every property for an object, then there is no
 * problem to solve.
 * <p>
 * Anywho, this will render a hidden 'meta' field that the
 * {@link org.opensails.sails.form.HtmlForm} class uses when binding an HTTP
 * form post to a model. If the checkbox is bound to a boolean property,
 * unchecking will remove that field from the posted form, but the hidden will
 * come back, indicating the the checkbox was rendered, but unchecked.
 */
public class Checkbox extends LabelableInputElement<Checkbox> {
	public static final String CHECKBOX = "checkbox";

	protected boolean checked;
	protected Hidden hiddenForBoolean;

	private final boolean includeHidden;

	public Checkbox(String name) {
		this(name, true);
	}
	
	public Checkbox(String name, boolean includeHidden) {
		this(name, "1", "0", includeHidden);
	}

	public Checkbox(String name, String checkedValue, String uncheckedValue) {
		this(name, checkedValue, uncheckedValue, true);
	}
	
	public Checkbox(String name, String checkedValue, String uncheckedValue, boolean includeHidden) {
		super(RENDER_LABEL_AFTER, CHECKBOX, name);
		this.includeHidden = includeHidden;
		value(checkedValue);
		hiddenForBoolean = new Hidden(CHECKBOX_PREFIX + name);
		hiddenForBoolean.value(uncheckedValue);
	}

	/**
	 * @see #checked(boolean)
	 */
	public Checkbox checked() {
		return checked(true);
	}

	/**
	 * Sets the checked attribute
	 * <p>
	 * This is ignored if the checkedValues is not null and has the value of
	 * this checkbox in it
	 * 
	 * @param b
	 * @return
	 */
	public Checkbox checked(boolean b) {
		checked = b;
		return this;
	}

	/**
	 * @param writer
	 * @throws IOException
	 */
	@Override
	public void renderThyself(Writer writer) throws IOException {
		super.renderThyself(writer);
		if (includeHidden) hiddenForBoolean.renderThyself(writer);
	}

	@Override
	public Checkbox value(Object value) {
		return super.value(value);
	}

	@Override
	protected void writeAttributes(HtmlGenerator generator) throws IOException {
		super.writeAttributes(generator);
		if (checked) generator.attribute(HtmlConstants.CHECKED, HtmlConstants.CHECKED);
	}
}
