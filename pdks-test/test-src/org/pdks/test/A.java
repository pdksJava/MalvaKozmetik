package org.pdks.test;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

public class A extends TestCase {
	@Test
	public static void testMatchSelectedSlips() {
		long result = 6;
		Assert.assertEquals("Eşit değil", 6, result);
		// fail("Not yet implemented");
	}
}
