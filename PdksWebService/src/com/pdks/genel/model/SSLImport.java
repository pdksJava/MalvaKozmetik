package com.pdks.genel.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import oracle.net.jndi.TrustManager;

import org.apache.log4j.Logger;

public class SSLImport implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1086392924694799114L;

	public static final String KEY_PASS = "changeit";

	static Logger logger = Logger.getLogger(SSLImport.class);

	private static List<String> servisURLList;

	public static boolean isSelfSigned(X509Certificate cert) throws Exception {
		boolean selfSigned = false;
		try {
			PublicKey key = cert.getPublicKey();
			cert.verify(key);
			selfSigned = true;
		} catch (Exception ex) {
			// logger.error(cert.getSerialNumber() + " --> " + ex);

		}
		return selfSigned;
	}

	/**
	 * @param endPoint
	 * @throws Exception
	 */
	public static List<String> getCertificateInputStream(String endPointAdres) throws Exception {
		return getCertificateInputStream(endPointAdres, Boolean.TRUE);
	}

	/**
	 * @param endPointAdres
	 * @param kok
	 * @return
	 * @throws Exception
	 */
	public static List<String> getCertificateInputStream(String endPointAdres, boolean kok) throws Exception {
		List<String> keyList = null;
		if (endPointAdres != null) {
			String adres = endPointAdres.toLowerCase(Locale.ENGLISH);
			if (adres.indexOf("https:") >= 0) {
				boolean sil = servisURLList == null;
				if (sil) {
					servisURLList = new ArrayList<String>();

				}
				kokSertifikaImport();
				String str = endPointAdres.substring(8);
				String endPoint = "https://" + str;
				if (!servisURLList.contains(endPoint)) {

					try {
						URL destinationURL = new URL(endPoint);
						String url = endPoint.substring(endPoint.indexOf("/") + 2);
						String servisName = url.replace("/", "_");

						// System.setProperty("javax.net.ssl.trustStrore", dosyaAdi);
						SSLContext sslctx = SSLContext.getInstance("SSL");
						sslctx.init(null, new X509TrustManager[] { new TrustManager() }, null);
						HttpsURLConnection.setDefaultSSLSocketFactory(sslctx.getSocketFactory());
						HttpsURLConnection connjava = (HttpsURLConnection) destinationURL.openConnection();
						int timeOutSaniye = 15;
						connjava.setReadTimeout(2 * timeOutSaniye * 1000);
						connjava.setConnectTimeout(timeOutSaniye * 1000); // set timeout to 5 seconds
						connjava.setRequestMethod("GET");
						connjava.setDoOutput(true);
						connjava.connect();
						Certificate[] certs = connjava.getServerCertificates();
						boolean root = !kok;
						int sayac = kok ? 0 : 1;
						while (root == false || sayac < 2) {
							for (Certificate cert : certs) {
								if (cert instanceof X509Certificate) {
									try {
										X509Certificate x509Certificate = (X509Certificate) cert;
										x509Certificate.checkValidity();
										String key = PdksUtil.setTurkishStr(servisName + "_" + x509Certificate.getSerialNumber().toString());
										if (kok == false || isSelfSigned(x509Certificate)) {
											addCertToKeyStore(cert, key.toLowerCase(Locale.ENGLISH), sil);
											if (keyList == null)
												keyList = new ArrayList<String>();
											keyList.add(key);
											if (kok)
												++sayac;
										}

									} catch (CertificateExpiredException cee) {
										logger.error("Certificate is expired " + cee.getMessage());
									}
								}

							}
							kok = false;
							root = true;
							sil = false;
							++sayac;
						}
						connjava.disconnect();
						servisURLList.add(endPoint);
					} catch (Exception e) {
						logger.error(endPoint + " " + e.toString());
						e.printStackTrace();
					}
				}
			}
		}
		return keyList;
	}

	public static String getTrustStoreFileName() {
		String dosyaAdi;
		dosyaAdi = System.getProperty("javax.net.ssl.trustStore") != null ? System.getProperty("javax.net.ssl.trustStore") : System.getProperty("java.home") + System.getProperty("file.separator") + "lib/security/cacerts";
		return dosyaAdi;
	}

	/**
	 * @param cert
	 * @param alias
	 * @param sil
	 * @return
	 * @throws Exception
	 */
	public static KeyStore addCertToKeyStore(java.security.cert.Certificate cert, String alias, boolean sil) throws Exception {

		String keystore_pass = KEY_PASS;
		// initializing keystore
		KeyStore ks = createAndLoadKeyStore(keystore_pass);
		boolean guncelle = Boolean.FALSE;
		if (sil) {
			Enumeration enumeration = ks.aliases();

			while (enumeration.hasMoreElements()) {
				String aliasKs = (String) enumeration.nextElement();
				try {
					Certificate certificate = ks.getCertificate(aliasKs);
					X509Certificate x509Certificate = (X509Certificate) certificate;
					x509Certificate.checkValidity();
				} catch (Exception e) {
					try {
						ks.deleteEntry(aliasKs);
						logger.error(aliasKs + " Certificate is expired ");
						guncelle = Boolean.TRUE;
					} catch (Exception e2) {
						logger.error(e2);
					}

				}

			}

		}
		if (alias != null) {
			java.security.cert.Certificate ksCrt = null;
			try {
				ksCrt = ks.getCertificate(alias);
			} catch (Exception e) {
			}
			boolean devam = ksCrt == null;
			if (ksCrt != null) {
				X509Certificate x509Certificate = (X509Certificate) ksCrt;
				try {
					x509Certificate.checkValidity();
				} catch (Exception e) {
					try {
						ks.deleteEntry(alias);
						guncelle = Boolean.TRUE;
						logger.error(alias + " Certificate is expired ");
						devam = true;
					} catch (Exception e2) {
					}
				}
			}
			if (devam) {
				// Add the certificate
				ks.setCertificateEntry(alias, cert);
				logger.info(alias + " Certificate is add ");
				// Save the new keystore contents
				guncelle = Boolean.TRUE;
			}
		}

		if (guncelle)
			try {
				keyStoreKaydet(keystore_pass, ks);
				logger.info(" Certificate is update ");
			} catch (Exception e) {
				logger.error(" Certificate is guncelle hata " + e);
			}

		return ks;
	}

	public static TreeMap<String, Object> getCertToKeyStoreMap() throws Exception {
		TreeMap<String, Object> cerMap = null;
		String keystore_pass = KEY_PASS;
		boolean kaydet = Boolean.FALSE;
		try {
			KeyStore ks = createAndLoadKeyStore(keystore_pass);
			Enumeration enumeration = ks.aliases();

			while (enumeration.hasMoreElements()) {
				String aliasKs = (String) enumeration.nextElement();
				try {
					if (cerMap == null) {
						cerMap = new TreeMap<String, Object>();
						cerMap.put("keyStore", ks);
					}
					Certificate certificate = ks.getCertificate(aliasKs);
					X509Certificate x509Certificate = (X509Certificate) certificate;
					cerMap.put(aliasKs, x509Certificate);
				} catch (Exception ee) {
					try {
						ks.deleteEntry(aliasKs);
						kaydet = Boolean.TRUE;
					} catch (Exception ex) {

					}

				}

			}
			if (kaydet) {
				keyStoreKaydet(keystore_pass, ks);

			}

		} catch (Exception e) {
			//
		}
		// initializing keystore

		return cerMap;
	}

	/**
	 * @param keystoreName
	 * @param keystore_pass
	 * @param ks
	 * @throws Exception
	 */
	public static void keyStoreKaydet(String keystore_pass, KeyStore ks) throws Exception {
		if (keystore_pass == null)
			keystore_pass = KEY_PASS;
		String keystoreName = getTrustStoreFileName();
		FileOutputStream out = new FileOutputStream(keystoreName);
		try {
			ks.store(out, keystore_pass.toCharArray());
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		} finally {
			out.close();
		}
		if (servisURLList != null)
			servisURLList.clear();
	}

	/**
	 * @throws Exception
	 */
	private static void kokSertifikaImport() throws Exception {
		File root = new File("/opt/sertifika");
		if (root.exists() && root.isDirectory()) {
			File[] dirs = root.listFiles();
			if (dirs != null) {
				for (int i = 0; i < dirs.length; i++) {
					File file = dirs[i];
					if (!file.isDirectory()) {
						String dosyaAdi = PdksUtil.replaceAll(file.getName(), ".", "_");
						InputStream in = new ByteArrayInputStream(PdksUtil.getFileByteArray(file));
						try {
							CertificateFactory cf = CertificateFactory.getInstance("X.509");
							Certificate cert = null;
							try {
								cert = (Certificate) cf.generateCertificate(in);
							} catch (Exception e) {
								cert = null;
							}
							if (cert != null) {
								X509Certificate x509Certificate = (X509Certificate) cert;
								x509Certificate.checkValidity();
								String key = PdksUtil.setTurkishStr(dosyaAdi + "_" + x509Certificate.getSerialNumber().toString());
								if (!SSLImport.getServisURLList().contains(key)) {
									addCertToKeyStore(cert, key, false);
									SSLImport.getServisURLList().add(dosyaAdi);
								}
							} else {

							}

						} catch (Exception e) {
							logger.error(dosyaAdi + "\n" + e);
						}
						in.close();
					}
				}
			}
		}

	}

	/**
	 * @param keyStorePath
	 * @param storePass
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws java.security.cert.CertificateException
	 * @throws IOException
	 * @throws KeyStoreException
	 * @throws NoSuchProviderException
	 */
	public static KeyStore createAndLoadKeyStore(String storePass) throws Exception {
		if (storePass == null)
			storePass = KEY_PASS;
		String keyStorePath = getTrustStoreFileName();
		KeyStore ks = KeyStore.getInstance("JKS", "SUN");
		ks.load(null, storePass.toCharArray());
		File storeFile = new File(keyStorePath);
		logger.debug("Create and load key store: " + keyStorePath + " using password: " + storePass);
		if (!storeFile.exists()) {
			FileOutputStream osStorePath = new FileOutputStream(keyStorePath);
			try {
				ks.store(osStorePath, storePass.toCharArray());
				logger.error(keyStorePath + " Certificate is create ");
			} finally {
				osStorePath.close();
			}
		}
		FileInputStream isStorePath = new FileInputStream(keyStorePath);
		try {
			ks.load(isStorePath, storePass.toCharArray());
		} finally {
			isStorePath.close();
		}
		return ks;
	}

	public static List<String> getServisURLList() {
		return servisURLList;
	}

	public static void setServisURLList(List<String> servisURLList) {
		SSLImport.servisURLList = servisURLList;
	}

}
