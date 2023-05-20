package org.pdks.test;

import org.testng.annotations.Test;
import org.jboss.seam.mock.SeamTest;

public class MenuLoaderActionTest extends SeamTest {

	@Test
	public void test_menuLoaderAction() throws Exception {
		new FacesRequest() {
			@Override
			protected void invokeApplication() {
				//call action methods here
				invokeMethod("#{MenuLoaderAction.menuLoaderAction}");
			}
		}.run();
	}
}
