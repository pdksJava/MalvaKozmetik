package org.pdks.test;

import org.jboss.seam.util.Base64;
import org.jboss.security.Base64Encoder;

public class Test {

	public static void main(String[] args) throws Exception {
		byte[] en = new byte[] { 'a' };
		String pass = Base64.encodeBytes(en);
		System.out.println("pass: " + pass + " " + Base64.decode(pass));
		pass = Base64.encodeBytes(en);
		System.out.println("pass: " + pass + " " + Base64.decode(pass));
		System.out.println("pass: " + Base64Encoder.encode("a") + " " + Base64.decode(Base64Encoder.encode("a")));

	}

}
