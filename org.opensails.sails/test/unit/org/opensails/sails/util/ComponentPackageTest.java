/*
 * Created on Mar 2, 2005
 * 
 * (c) 2005 Adam Williams
 */
package org.opensails.sails.util;

import junit.framework.TestCase;

import org.opensails.sails.controller.IController;
import org.opensails.sails.controller.oem.ShamController;

public class ComponentPackageTest extends TestCase {
    protected ComponentPackage<IController> resolver = new ComponentPackage<IController>(ClassHelper.getPackage(ShamController.class), "Controller");

    public void testResolveComponentImplementation_DiscoverInPackage() throws Exception {
        Class<? extends IController> type = resolver.resolve("shamController");
        assertEquals(ShamController.class, type);
    }

    public void testResolveControllerImplementation_DiscoverInPackage_AppendingControllerToName() {
        Class<? extends IController> type = resolver.resolve("sham");
        assertEquals("Controller suffix should be optional", ShamController.class, type);
    }
}