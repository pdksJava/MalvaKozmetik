package org.pdks.quartz;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.Expiration;
import org.jboss.seam.annotations.async.IntervalCron;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.pdks.security.action.SessionListener;
import org.pdks.session.PdksUtil;

@Name("acikBaglantiKapat")
@AutoCreate
@Scope(ScopeType.APPLICATION)
public class AcikBaglantiKapat implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3634864061197974008L;

	/**
	 * 
	 */

	static Logger logger = Logger.getLogger(AcikBaglantiKapat.class);
	@In(required = false, create = true)
	HashMap<String, String> parameterMap = new HashMap<String, String>();

	private static boolean calisiyor = Boolean.FALSE;
	private static final String PARAMETER_KEY = "sessionTimeOut";

	@Asynchronous
	@SuppressWarnings("unchecked")
	@Transactional
	// @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public QuartzTriggerHandle acikBaglantiKapatTimer(@Expiration Date when, @IntervalCron String interval) {

		if (!isCalisiyor()) {
			calisiyor = Boolean.TRUE;
			Date simdi = new Date();
			try {
				islemYapanKullanicilariIptalEt(simdi);
			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());

			} finally {
				calisiyor = Boolean.FALSE;
				simdi = null;
			}

		}
		return null;
	}

	public void islemYapanKullanicilariIptalEt(Date simdi) {
		if (simdi == null)
			simdi = new Date();
		if (parameterMap.containsKey(PARAMETER_KEY)) {
			int timeOut = 10;
			try {
				timeOut = Integer.parseInt(parameterMap.get(PARAMETER_KEY));
			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());
				timeOut = 10;
			}
			ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
			for (HttpSession session : SessionListener.getSessionList(servletContext)) {
				try {
					if (session.getAttribute("authenticatedUser") == null)
						continue;
					Date date = PdksUtil.getSession(session, simdi);
					int sure = date != null ? PdksUtil.getDateField(date, Calendar.MINUTE) : -1;
					if (sure >= timeOut)
						try {
							session.invalidate();
						} catch (Exception e) {
							logger.error("PDKS hata in : \n");
							e.printStackTrace();
							logger.error("PDKS hata out : " + e.getMessage());

						}

				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());

				}

			}
		}

	}

	public static boolean isCalisiyor() {
		return calisiyor;
	}

	public static void setCalisiyor(boolean calisiyor) {
		AcikBaglantiKapat.calisiyor = calisiyor;
	}

}