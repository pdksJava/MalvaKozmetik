package org.pdks.session;

import javax.ejb.Local;

@Local
public interface IAuthenticator {
	boolean authenticate();
}