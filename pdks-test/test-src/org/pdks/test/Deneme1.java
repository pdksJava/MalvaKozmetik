package org.pdks.test;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

public class Deneme1 extends TestCase {

	@Test
	public static void testMatchSelectedSlips() {
		long result = 2;
		Assert.assertEquals("Eşit değil", 6, result);
		// fail("Not yet implemented");
	}
}
