package org.pdks.security.action;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;
import org.pdks.security.entity.User;
import org.pdks.session.PdksUtil;

public class SessionListener implements HttpSessionListener {

	static final String LIST_NAME = "pdksSession";
	static Logger logger = Logger.getLogger(SessionListener.class);

	public static final String SESSION_USER_NAME = "authenticatedUser";

	public static List<HttpSession> getSessionList(ServletContext servletContext) {

		List sessionList = null;

		if (servletContext.getAttribute(LIST_NAME) != null)
			sessionList = (List<HttpSession>) servletContext.getAttribute(LIST_NAME);
		else {
			sessionList = new ArrayList<HttpSession>();
			servletContext.setAttribute(LIST_NAME, sessionList);
		}
		return sessionList;
	}

	public SessionListener() {

	}

	public void sessionCreated(HttpSessionEvent se) {
		HttpSession session = se.getSession();
		session.setMaxInactiveInterval(1200);
		// Date now = new Date();
		// StringBuffer sb = new StringBuffer();
		// String id = session.getId();
		// sb.append(now.toString()).append("\nID: ").append(id);
		List<HttpSession> sessionList = getSessionList(session.getServletContext());
		synchronized (this) {
			sessionList.add(session);
		}
		// sb.append("\n").append("PDKS are now : " + sessionList.size());
		// String message = sb.toString();
		// logger.info(message);
		// message = null;
		// sb = null;

	}

	public void sessionDestroyed(HttpSessionEvent se) {
		HttpSession session = se.getSession();
		StringBuffer sb = new StringBuffer();
		User authenticatedUser = (User) session.getAttribute(SESSION_USER_NAME);
		if (authenticatedUser != null) {
			PdksUtil.getSessionUser(null, authenticatedUser);
			sb.append(authenticatedUser.getUsername() + " " + PdksUtil.setTurkishStr(authenticatedUser.getAdSoyad() + " kullanıcısı PDKS sisteminden logout oldu."));

		}
		List<HttpSession> sessionList = getSessionList(session.getServletContext());
		synchronized (this) {
			if (sessionList.contains(session))
				sessionList.remove(session);
		}
		if (!sessionList.isEmpty())
			sb.append("PDKS are now : " + sessionList.size());
		String message = sb.toString();
		if (authenticatedUser != null)
			logger.info(message);
		message = null;
		sb = null;

	}

}