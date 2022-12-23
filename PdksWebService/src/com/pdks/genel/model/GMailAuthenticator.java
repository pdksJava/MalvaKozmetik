package com.pdks.genel.model;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class GMailAuthenticator extends Authenticator {
	String user;
	String password;

	public GMailAuthenticator(String username, String password) {
		super();
		this.user = username;
		this.password = password;
	}

	@Override
	public PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(user, password);
	}
}
