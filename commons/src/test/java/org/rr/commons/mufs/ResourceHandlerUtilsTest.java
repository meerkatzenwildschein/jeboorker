package org.rr.commons.mufs;

import java.util.List;

import junit.framework.TestCase;

public class ResourceHandlerUtilsTest extends TestCase {

	public void testListUSB() {
		List<IResourceHandler> usbResources = ResourceHandlerUtils.getExternalDriveResources();
		for(IResourceHandler handler : usbResources) {
			System.out.println(handler);
		}
		assertNotNull(usbResources);
	}
}
