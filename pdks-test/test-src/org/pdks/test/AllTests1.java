package org.pdks.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests1 {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for com.abh.test");
		//$JUnit-BEGIN$
		suite.addTestSuite(A.class);
		suite.addTestSuite(ConfirmMatch.class);
		suite.addTestSuite(Deneme1.class);
		//$JUnit-END$
		return suite;
	}

}
