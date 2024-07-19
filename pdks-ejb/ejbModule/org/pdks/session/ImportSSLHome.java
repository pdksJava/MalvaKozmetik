package org.pdks.session;

import java.io.Serializable;
import java.net.URL;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.net.ssl.HttpsURLConnection;

import org.apache.log4j.Logger;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.CertificatePdks;
import org.pdks.entity.Parameter;
import org.pdks.security.entity.User;

import sun.security.x509.X509CertImpl;

@Name("importSSLHome")
public class ImportSSLHome extends EntityHome<Parameter> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1168388552213694100L;
	static Logger logger = Logger.getLogger(ImportSSLHome.class);

	@In(required = false, create = true)
	User authenticatedUser;

	@RequestParameter
	Long doktorId;
	@In(required = true, create = true)
	OrtakIslemler ortakIslemler;

	@In(required = false)
	FacesMessages facesMessages;

	private List<CertificatePdks> cerList;

	private String urlSSL;

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		PdksUtil.getSessionUser(null, authenticatedUser);
		List<CertificatePdks> list = null;
		try {
			logger.info("Sertifika SSL Kontrol in " + PdksUtil.getCurrentTimeStampStr());
			SSLImport.setServisURLList(null);
			SSLImport.addCertToKeyStore(null, null, true);
			list = getCertificaList();
			logger.info("Sertifika SSL Kontrol out " + PdksUtil.getCurrentTimeStampStr());
		} catch (Exception e) {
		}
		setCerList(list);
	}

	public String importURLCertifica() throws Exception {
		Integer responseCode = null;
		String endPoint = null;
		try {
			endPoint = (urlSSL.indexOf("https") < 0 ? "https://" : "") + urlSSL;
			URL url = new URL(endPoint);
			HttpsURLConnection connjava = (HttpsURLConnection) url.openConnection();
			int timeOutSaniye = 15;
			connjava.setReadTimeout(2 * timeOutSaniye * 1000);
			connjava.setConnectTimeout(timeOutSaniye * 1000); // set timeout to 5 seconds
			responseCode = connjava.getResponseCode();
		} catch (Exception e) {

		}
		if (responseCode == null || responseCode < 400) {
			if (SSLImport.getServisURLList() != null)
				SSLImport.getServisURLList().clear();
			SSLImport.getCertificateInputStream(endPoint);
			certificaUpdate();
		}

		return "";

	}

	private void certificaUpdate() {
		List<CertificatePdks> list = null;
		try {
			list = getCertificaList();
		} catch (Exception e) {
		}
		setCerList(list);
	}

	public String deleteCerticalar() throws Exception {

		boolean getir = deleteAliasCertifica(cerList);
		if (getir)
			cerList = getCertificaList();

		return "";

	}

	/**
	 * @param alias
	 * @return
	 * @throws Exception
	 */
	private boolean deleteAliasCertifica(List<CertificatePdks> list) throws Exception {
		boolean oku = Boolean.FALSE;
		TreeMap<String, Object> cerMap = SSLImport.getCertToKeyStoreMap();
		if (cerMap != null && cerMap.containsKey("keyStore")) {
			KeyStore ks = (KeyStore) cerMap.get("keyStore");
			cerMap.remove("keyStore");
			for (CertificatePdks liste : list) {
				String alias = (String) liste.getAlias();
				if (liste.isDurum() && cerMap.containsKey(alias)) {
					try {
						ks.deleteEntry(alias);
						oku = Boolean.TRUE;
					} catch (Exception e) {
					}

				}
			}

			if (oku)
				SSLImport.keyStoreKaydet(SSLImport.KEY_PASS, ks);
		}

		return oku;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	private List<CertificatePdks> getCertificaList() throws Exception {
		List<CertificatePdks> cerList = null;
		TreeMap<String, Object> cerMap = SSLImport.getCertToKeyStoreMap();
		if (cerMap != null && !cerMap.isEmpty()) {
			cerList = new ArrayList<CertificatePdks>();
			if (cerMap.containsKey("keyStore"))
				cerMap.remove("keyStore");
			for (String alias : cerMap.keySet()) {
				try {
					Object object = cerMap.get(alias);
					if (object instanceof X509CertImpl) {
						X509CertImpl im = (X509CertImpl) object;
						CertificatePdks certificatePdks = new CertificatePdks(alias, im);
						cerList.add(certificatePdks);
					} else if (object instanceof CertificatePdks)

						cerList.add((CertificatePdks) cerMap.get(alias));

				} catch (Exception e) {
					logger.error(e);
					e.printStackTrace();
					break;
				}

			}

		}
		cerMap = null;

		return cerList;
	}

	public List<CertificatePdks> getCerList() {
		return cerList;
	}

	public void setCerList(List<CertificatePdks> cerList) {
		this.cerList = cerList;
	}

	public String getUrlSSL() {
		return urlSSL;
	}

	public void setUrlSSL(String urlSSL) {
		this.urlSSL = urlSSL;
	}

}
