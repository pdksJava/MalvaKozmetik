package org.pdks.security.action;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.management.IdentityManager;
import org.jboss.seam.security.permission.PermissionManager;
import org.pdks.entity.Personel;
import org.pdks.erp.action.SapRfcManager;
import org.pdks.sap.entity.SAPSunucu;
import org.pdks.security.entity.Role;
import org.pdks.security.entity.User;
import org.pdks.session.IAuthenticator;
import org.pdks.session.LDAPUserManager;
import org.pdks.session.OrtakIslemler;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;

@Stateless
@Name("authenticator")
public class Authenticator implements IAuthenticator, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5011682752102859161L;
	static Logger logger = Logger.getLogger(Authenticator.class);

	@In
	Identity identity;
	@In
	Credentials credentials;
	@In
	EntityManager entityManager;
	@In
	IdentityManager identityManager;
	@In
	PermissionManager permissionManager;

	@In(create = true)
	OrtakIslemler ortakIslemler;

	@In(required = false, create = true)
	HashMap<String, String> parameterMap;

	/*
	 * @In LDAPUserManager ldapUserManager;
	 */
	@Out(scope = ScopeType.SESSION, required = false)
	User authenticatedUser;
	@Out(scope = ScopeType.SESSION, required = false)
	String kisaKullaniciAdi;

	@In(create = true)
	PdksEntityController pdksEntityController;

	private String adres;
	private Session session;

	@Transactional
	public boolean authenticate() {
		session = PdksUtil.getSession(entityManager, Boolean.FALSE);
		session.clear();
		String username = credentials.getUsername();
		String userName = username.trim();
		String password = credentials.getPassword();
		Map<String, String> map = null;
		try {

			map = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap();

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			authenticatedUser = new User();
			authenticatedUser.setUsername(userName);
			authenticatedUser.setPasswordHash(password);
			return false;
		}
		User ldapUser = null;
		adres = map.containsKey("host") ? map.get("host") : "";
		boolean test = adres.indexOf("localhost:8080") >= 0;
		if (!test)
			test = ortakIslemler.testDurum(password);

		kisaKullaniciAdi = "";
		if (userName.indexOf("@") < 0)
			ldapUser = ortakIslemler.kullaniciBul(userName, LDAPUserManager.USER_ATTRIBUTES_SAM_ACCOUNT_NAME);
		else {
			ldapUser = ortakIslemler.kullaniciBul(userName, LDAPUserManager.USER_ATTRIBUTES_PRINCIPAL_NAME);
			if (ldapUser == null)
				ldapUser = ortakIslemler.kullaniciBul(userName, LDAPUserManager.USER_ATTRIBUTES_MAIL);
		}
		if (ldapUser != null && !ldapUser.isDurum())
			ldapUser = null;
		if (ldapUser != null) {
			kisaKullaniciAdi = ldapUser.getStaffId();
			userName = ldapUser.getUsername();
		}

		boolean sonuc = Boolean.FALSE;

		try {
			authenticatedUser = getKullanici(userName, User.COLUMN_NAME_USERNAME);
			List<String> adminIPList = PdksUtil.getListByString(ortakIslemler.getParameterKey("adminIP"), null);
			String remoteAddr = PdksUtil.getRemoteAddr();
			if (!test)
				test = adminIPList.contains(remoteAddr);
			if (authenticatedUser == null) {
				if (ldapUser != null) {
					authenticatedUser = getKullanici(ldapUser.getShortUsername(), User.COLUMN_NAME_SHORT_USER_NAME);
					if (authenticatedUser != null) {
						authenticatedUser.setUsername(ldapUser.getUsername());
						if (!parameterMap.containsKey("emailBozuk"))
							authenticatedUser.setEmail(ldapUser.getEmail());
						pdksEntityController.saveOrUpdate(session, entityManager, authenticatedUser);
						session.flush();
						// authenticatedUser = entityManager.merge(authenticatedUser);
						// entityManager.flush();
						userName = ldapUser.getUsername();
					}
				}
			} else if (ldapUser != null && authenticatedUser != null) {
				if (authenticatedUser.getShortUsername() == null || !authenticatedUser.getShortUsername().equals(ldapUser.getShortUsername())) {
					authenticatedUser.setShortUsername(ldapUser.getShortUsername());

					pdksEntityController.saveOrUpdate(session, entityManager, authenticatedUser);
					session.flush();
				}
			}

			if (authenticatedUser == null && ldapUser != null) {
				String sicilNo = "900" + ldapUser.getStaffId().substring(3).trim();
				HashMap parametreMap = new HashMap();
				StringBuffer sb = new StringBuffer();
				sb.append("SELECT U.* FROM " + Personel.TABLE_NAME + " P WITH(nolock)");
				sb.append("INNER JOIN " + User.TABLE_NAME + " U ON P." + Personel.COLUMN_NAME_ID + "=U." + User.COLUMN_NAME_PERSONEL + " AND U." + User.COLUMN_NAME_DURUM + "=1 AND U." + User.COLUMN_NAME_DEPARTMAN + " IS NOT NULL ");
				sb.append(" WHERE  P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + "=:sicilNo AND P." + Personel.COLUMN_NAME_DURUM + "=1  ");
				sb.append(" AND  P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + "<=CAST(GETDATE() AS date) AND P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + ">=CAST(GETDATE() AS date)");
				sb.append(" AND  P." + Personel.COLUMN_NAME_DURUM + "=1");
				parametreMap.put("sicilNo", sicilNo);
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				authenticatedUser = (User) pdksEntityController.getObjectBySQL(sb, parametreMap, User.class);

				if (authenticatedUser != null) {
					logger.info(PdksUtil.setTurkishStr(authenticatedUser.getUsername() + " kullanıcı bilgisi okundu."));
					authenticatedUser.setUsername(ldapUser.getUsername());
					if (!parametreMap.containsKey("emailBozuk"))
						authenticatedUser.setEmail(ldapUser.getEmail());

					pdksEntityController.saveOrUpdate(session, entityManager, authenticatedUser);
					session.flush();
				}

			}
			if (!test && parameterMap != null && parameterMap.containsKey("sifreKontrol"))
				test = !parameterMap.get("sifreKontrol").equals("1");
			sonuc = authenticatedUser != null && test;
			String encodePassword = PdksUtil.encodePassword(password);
			if (authenticatedUser != null) {
				authenticatedUser.setLogin(Boolean.FALSE);
				userName = authenticatedUser.getUsername();
				if (!PdksUtil.hasStringValue(kisaKullaniciAdi))
					kisaKullaniciAdi = authenticatedUser.getUsername();
				else
					kisaKullaniciAdi = kisaKullaniciAdi + " <---> " + authenticatedUser.getUsername();

				authenticatedUser.setDurum(authenticatedUser.getPdksPersonel().isCalisiyor());
				if (!authenticatedUser.isDurum())
					PdksUtil.addMessageAvailableWarn(authenticatedUser.getAdSoyad() + " ait işe giriş çıkış tarihinde uyumsuz");
				else {
					if (authenticatedUser.isLdapUse() || sonuc) {
						// ldap kullanıyorsa
						try {
							if (!sonuc)
								sonuc = LDAPUserManager.authenticate(userName, password);
							if (!PdksUtil.hasStringValue(kisaKullaniciAdi)) {
								ldapUser = LDAPUserManager.getLDAPUserAttributes(userName);
								if (ldapUser != null)
									kisaKullaniciAdi = ldapUser.getStaffId();
							}

						} catch (Exception e) {
							logger.error("PDKS hata in : \n");
							e.printStackTrace();
							logger.error("PDKS hata out : " + e.getMessage());
							try {
								String ldapHataDonusKodu = LDAPUserManager.ldapHatasininDetayAciklamasiniGetir(e);
								if (ldapHataDonusKodu != null) {
									String mesaj = PdksUtil.getMessageBundleMessage(LDAPUserManager.LDAP_HATA_KODU_KEY_ON_EK + ldapHataDonusKodu);
									logger.info(mesaj + " ( " + userName + " )");
								}

							} catch (Exception e2) {
								logger.error(e2.getLocalizedMessage());
							}
						}

					} else {

						sonuc = (authenticatedUser.getPasswordHash().equals(encodePassword));
					}
					if (sonuc) {
						username = authenticatedUser.getUsername();
						FacesContext context = FacesContext.getCurrentInstance();
						try {
							if (authenticatedUser.getPdksPersonel().getSirket() != null && authenticatedUser.getPdksPersonel().getSirket().isLdap()) {
								String email = PdksUtil.getMailAdres(userName);
								if (email != null && (authenticatedUser.getEmail() == null || !email.equals(authenticatedUser.getEmail()))) {
									if (!parameterMap.containsKey("emailBozuk"))
										authenticatedUser.setEmail(email);
									pdksEntityController.saveOrUpdate(session, entityManager, authenticatedUser);
									session.flush();
								}

							}

						} catch (Exception e) {
							logger.error("PDKS hata in : \n");
							e.printStackTrace();
							logger.error("PDKS hata out : " + e.getMessage());

						}
						credentials.setUsername(userName);
						boolean browserIE = PdksUtil.isInternetExplorer((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
						ortakIslemler.setUserRoller(authenticatedUser, session);
						ortakIslemler.setUserTesisler(authenticatedUser, session);
						authenticatedUser.setBrowserIE(browserIE);
						if (authenticatedUser.getYetkiliRollerim() != null) {
							for (Role role : authenticatedUser.getYetkiliRollerim())
								identity.addRole(role.getRolename());
						}
						try {
							HashMap map1 = new HashMap();
							map1.put("durum", SAPSunucu.DURUM_AKTIF);
							if (session != null)
								map1.put(PdksEntityController.MAP_KEY_SESSION, session);
							List<SAPSunucu> sapSunucular = pdksEntityController.getObjectByInnerObjectList(map1, SAPSunucu.class);
							if (!sapSunucular.isEmpty())
								logger.info(PdksUtil.setTurkishStr("SAP sunucuları okundu."));

							SapRfcManager.setSapSunucular(sapSunucular);

							if (authenticatedUser.getYetkiliRollerim().isEmpty())
								authenticatedUser = ortakIslemler.personelPdksRolAta(authenticatedUser, Boolean.TRUE, session);
							if (PdksUtil.getBundleName() == null) {

								try {
									PdksUtil.setBundleName(context.getApplication().getMessageBundle());

								} catch (Exception e) {
									logger.error("PDKS hata in : \n");
									e.printStackTrace();
									logger.error("PDKS hata out : " + e.getMessage());
									PdksUtil.setBundleName(null);

								}
							}
							authenticatedUser.setTestLogin(test);
							authenticatedUser.setCalistigiSayfa("anasayfa");
							ortakIslemler.sistemeGirisIslemleri(authenticatedUser, Boolean.TRUE, null, null, session);
							logger.info(authenticatedUser.getUsername()
									+ " "
									+ PdksUtil.setTurkishStr(authenticatedUser.getAdSoyad() + " " + (authenticatedUser.getEmail() != null && !authenticatedUser.getEmail().equals(authenticatedUser.getUsername()) ? authenticatedUser.getEmail() + " E-postali" : "")
											+ " kullanıcısı PDKS sistemine login oldu. " + new Date()));
							authenticatedUser.setSessionSQL(session);
						} catch (Exception e) {
							logger.error("PDKS hata in : \n");
							e.printStackTrace();
							logger.error("PDKS hata out : " + e.getMessage());
							logger.debug("Hata : " + e.getMessage());
						}
						if (!test || adres.startsWith("surum")) {
							try {
								authenticatedUser.setLastLogin(new Date());
								pdksEntityController.saveOrUpdate(session, entityManager, authenticatedUser);
								session.flush();
							} catch (Exception e) {
							}
						}
						authenticatedUser.setLogin(Boolean.TRUE);
					}
				}
			} else
				PdksUtil.addMessageAvailableError(credentials.getUsername().trim() + " kullanıcı adı Zaman Yönetimi-PDKS Sistemi'nde kayıtlı değildir!");

			return sonuc;
		} catch (Exception ex) {
			logger.debug("Hata : " + ex.getMessage());
			HashMap parametreMap = new HashMap();
			try {
				parametreMap.clear();
				parametreMap.put("id", 1);
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				List perList = pdksEntityController.getObjectByInnerObjectList(parametreMap, Personel.class);
				logger.info(PdksUtil.setTurkishStr(authenticatedUser.getUsername() + " kullanıcı bilgisi okundu."));

				if (!perList.isEmpty())
					logger.error(perList.size() + " " + new Date());

			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());
				logger.debug("Hata : " + e.getMessage());
			}
			logger.debug("authenticating  " + username);
			return sonuc;
		}
	}

	/**
	 * @param userName
	 * @param fieldName
	 * @return
	 */
	private User getKullanici(String userName, String fieldName) {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT S.* FROM " + User.TABLE_NAME + " S  WITH(nolock) ");
		sb.append(" INNER JOIN  " + Personel.TABLE_NAME + " P ON  P." + Personel.COLUMN_NAME_ID + "=S." + User.COLUMN_NAME_PERSONEL);
		sb.append(" AND  P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + "<=CAST(GETDATE() AS date) AND P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + ">=CAST(GETDATE() AS date)");
		if (userName.indexOf("%") > 0)
			sb.append(" WHERE S." + fieldName + " LIKE :userName");
		else
			sb.append(" WHERE S." + fieldName + " = :userName");
		HashMap fields = new HashMap();
		fields.put("userName", userName);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		User authenticated = (User) pdksEntityController.getObjectBySQL(sb, fields, User.class);
		fields = null;
		if (authenticated != null) {
			if (authenticated.isDurum() == false || authenticated.getDepartman() == null || authenticated.getPdksPersonel().getDurum().equals(Boolean.FALSE))
				authenticated = null;

		}
		return authenticated;
	}

	public String logout() {
		identity.logout();
		return "login";
	}

	public String getAdres() {
		return adres;
	}

	public void setAdres(String adres) {
		this.adres = adres;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}
}
