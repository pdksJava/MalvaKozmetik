package org.pdks.entity;

import java.io.Serializable;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.apache.log4j.Logger;

public class CertificatePdks implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8677308119096502959L;

	static Logger logger = Logger.getLogger(CertificatePdks.class);

	private X509Certificate certificate;
	private String alias, name, cn;
	private Date notAfter, notBefore;

	private boolean durum;

	public CertificatePdks() {
		super();

	}

	public CertificatePdks(String alias, X509Certificate cer) {
		super();
		this.alias = alias;
		this.certificate = cer;
		this.notAfter = cer.getNotAfter();
		this.notBefore = cer.getNotBefore();
		this.durum = Boolean.FALSE;
		if (cer != null && cer.getSubjectDN() != null) {
			Principal principal = cer.getSubjectDN();
			StringBuffer sb = new StringBuffer();
			String[] names = principal.getName().split(",");
			for (int i = 0; i < names.length; i++) {
				String bilgi = names[i];
				boolean isim = bilgi.indexOf("=") < 0;
				if (!isim) {
					String[] parts = bilgi.split("=");
					isim = !parts[0].equals("CN");
					if (!isim)
						this.cn = parts[1];
				}

				if (isim) {
					if (sb.length() > 0)
						sb.append(", ");
					sb.append(bilgi);
				}
			}

			this.name = sb.toString();
		}
	}

	public X509Certificate getCertificate() {
		return certificate;
	}

	public void setCertificate(X509Certificate certificate) {
		this.certificate = certificate;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public Date getNotAfter() {
		return notAfter;
	}

	public void setNotAfter(Date notAfter) {
		this.notAfter = notAfter;
	}

	public Date getNotBefore() {
		return notBefore;
	}

	public void setNotBefore(Date notBefore) {
		this.notBefore = notBefore;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCn() {
		return cn;
	}

	public void setCn(String cn) {
		this.cn = cn;
	}

	public boolean isDurum() {
		return durum;
	}

	public void setDurum(boolean durum) {
		this.durum = durum;
	}
}
