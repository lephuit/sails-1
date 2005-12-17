package org.opensails.dock.controllers;

import org.opensails.sails.action.oem.PartialActionResult;
import org.opensails.sails.controller.oem.BaseController;

public class PartialsController extends BaseController {
	public PartialActionResult renderPartial() {
		return new PartialActionResult(event);
	}

	public PartialActionResult renderOtherPartial() {
		return new PartialActionResult(event, "otherPartial");
	}
}
