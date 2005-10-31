package org.opensails.sails.html;

import junit.framework.TestCase;

import org.opensails.sails.ISailsEvent;
import org.opensails.sails.oem.SailsEventFixture;
import org.opensails.sails.url.UrlType;

public class ActionLinkTest extends TestCase {
	public void testImage() throws Exception {
		ISailsEvent event = SailsEventFixture.actionGet();
		ActionLink link = new ActionLink(event);
		link.controller("controller").action("action");
		ImageLink imageLink = link.image("myImage");
		assertEquals("<a href=\"" + event.resolve(UrlType.CONTROLLER, "controller/action") + "\"><img src=\"" + event.resolve(UrlType.IMAGE, "myImage")
				+ "\" alt=\"controller/action\" /></a>", imageLink.toString());
	}

	public void testToString() {
		ISailsEvent event = SailsEventFixture.actionGet();
		ActionLink link = new ActionLink(event).controller("controller").action("action");
		assertEquals("<a href=\"" + event.resolve(UrlType.CONTROLLER, "controller/action") + "\">controller/action</a>", link.toString());
	}

	public void testToString_NoAction() {
		ISailsEvent event = SailsEventFixture.actionGet();
		ActionLink link = new ActionLink(event).controller("controller");
		assertEquals("<a href=\"" + event.resolve(UrlType.CONTROLLER, "controller") + "\">controller</a>", link.toString());
	}

	public void testToString_NoController() {
		ISailsEvent event = SailsEventFixture.actionGet("originalController", "originalAction");
		ActionLink link = new ActionLink(event).action("action");
		assertEquals("<a href=\"" + event.resolve(UrlType.CONTROLLER, "originalController/action") + "\">originalController/action</a>", link.toString());
	}

	public void testToString_Secure() {
		ISailsEvent event = SailsEventFixture.actionGet();
		ActionLink link = new ActionLink(event).controller("controller").secure();
		assertTrue(link.toString().contains("https://"));
	}

	public void testToString_Text() {
		ISailsEvent event = SailsEventFixture.actionGet();
		ActionLink link = new ActionLink(event).controller("controller").action("action").text("text");
		assertEquals("<a href=\"" + event.resolve(UrlType.CONTROLLER, "controller/action") + "\">text</a>", link.toString());
	}
}