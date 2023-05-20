package org.pdks.test;

import junit.framework.TestCase;

import org.junit.Test;

public class ConfirmMatch extends TestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();

	}

	@Test
	public void testMatchSelectedSlips() {
		long result = 2;
		assertEquals("7 girilmesi gerekir", 7, result);
		// fail("Not yet implemented");
	}

}
