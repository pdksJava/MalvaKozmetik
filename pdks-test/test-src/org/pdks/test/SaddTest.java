package org.pdks.test;

import org.testng.annotations.Test;
import org.jboss.seam.mock.SeamTest;

public class SaddTest extends SeamTest {

	@Test
	public void test_sadd() throws Exception {
		new FacesRequest() {
			@Override
			protected void invokeApplication() {
				//call action methods here
				invokeMethod("#{sadd.sadd}");
			}
		}.run();
	}
}
