package org.pdks.quartz;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.pdks.security.action.StartupAction;
import org.pdks.session.OrtakIslemler;
import org.pdks.session.PdksUtil;
import org.pdks.session.SSLImport;
import org.hibernate.Session;
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

@Name("sertifikaSSLKontrol")
@AutoCreate
@Scope(ScopeType.APPLICATION)
public class SertifikaSSLKontrol implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6017076879969504465L;

	/**
	 * 
	 */
	static Logger logger = Logger.getLogger(SertifikaSSLKontrol.class);

	OrtakIslemler ortakIslemler;

	@In(required = false, create = true)
	HashMap<String, String> parameterMap;

	@In(required = false, create = true)
	EntityManager entityManager;

	@In(required = false, create = true)
	Zamanlayici zamanlayici;

	@In(required = false, create = true)
	StartupAction startupAction;

	private static boolean calisiyor = Boolean.FALSE;

	@Asynchronous
	@SuppressWarnings("unchecked")
	@Transactional
	// @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public QuartzTriggerHandle sertifikaSSLKontrolTimer(@Expiration Date when, @IntervalCron String interval) {

		if (!isCalisiyor()) {
			setCalisiyor(Boolean.TRUE);
			logger.debug("Sertifika SSL Kontrol in " + new Date());
			Session session = null;
			try {
				SSLImport.setServisURLList(null);
				SSLImport.addCertToKeyStore(null, null, true);
				Date time = new Date();
				int dakika = PdksUtil.getDateField(time, Calendar.MINUTE);
				if (dakika % 30 == 0) {
					session = PdksUtil.getSession(entityManager, Boolean.TRUE);
					startupAction.fillParameter(session);
				}

				logger.debug("Sertifika SSL Kontrol out " + new Date());
			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());

				logger.error("Sertifika SSL Kontrol hata " + e.getMessage() + " " + new Date());

			} finally {
				if (session != null)
					session.close();
				setCalisiyor(Boolean.FALSE);

			}

		}

		return null;
	}

	public static boolean isCalisiyor() {
		return calisiyor;
	}

	public static void setCalisiyor(boolean calisiyor) {
		SertifikaSSLKontrol.calisiyor = calisiyor;
	}

}