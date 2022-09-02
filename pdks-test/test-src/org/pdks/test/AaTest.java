package org.pdks.test;

import org.testng.annotations.Test;
import org.jboss.seam.mock.SeamTest;

public class AaTest extends SeamTest {

	@Test
	public void test_aa() throws Exception {
		new FacesRequest() {
			@Override
			protected void invokeApplication() {
				//call action methods here
				invokeMethod("#{aa.aa}");
			}
		}.run();
	}
}
