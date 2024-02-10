package org.pdks.genel.model;

import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;

import org.pdks.dao.PdksDAO;

/**
 * @author Hasan Sayar
 */
public class Constants implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6098113295381802911L;

	public static final String UYGULAMA_VERSION = "PdksWebService_20230930_1225_1.0.0.ear";
	public static final Locale TR_LOCALE = new Locale("tr", "TR");
	public static final Locale RU_LOCALE = new Locale("ru", "RU");
	public static final Locale EN_LOCALE = new Locale("en", "EN");
	public static final String MENU_REPOSITORY = "dbRepositoryEIG_";
	public static String webServisHataAc = "<EXCEPTION><DESCRIPTION>";
	public static String webServisHataKapa = "</DESCRIPTION></EXCEPTION>";
	public static String JSON_AYRAC = "XXX";
	public static final String WEB_DATASOURCE = "webDS";

	public static final String LOCALE_KEY = "langLocale";
	public static final String KULLANICI = "kullanici";

	// ServletContext
	public static final String SERVLET_CONTEXT_UYGULAMA_DILLERI = "servletContextUygulamaDilleri";

	public static final String SERVLET_CONTEXT_UYGULAMA_ITHALATCI_DILLERI = "servletContextIthalatciUygulamaDilleri";
	// genel islem tipleri
	public static final int GENEL_ISLEM_TIPI_KURUMSAL_KULLANICI_ARAMA = 101;
	public static final String SERVLET_CONTEXT_DOSYA_FORMAT_HASHMAP = "dosyaFormatHashMap";
	// cookie'ler
	public static final String COOKIE_KULLANICI_DILI = "kullaniciDili";
	public static final int COOKIE_KULLANICI_DILI_SURE = 7 * 24 * 60 * 60;

	// firma'lar
	public static final int ISLEM_TIPI_FIRMA_ILISKILERI_DUZENLE = 111105;

	public static final int SECIM_EVET = 1;
	public static final int SECIM_HAYIR = 0;
	public static final int TAKIP_NO_UZUNLUK = 8;

	public static final String TARIH_FORMAT_MEDULA = "dd.MM.yyyy";

	public static final long GUN_ZAMAN = 24 * 60 * 60 * 1000;

	public static int sayfaSayisi = 100;
	public static ResourceBundle rscBundle;

	public static String COMBO_BOS = "-1";

	public static String DOSYA_AYRAC;

	public static String ICERIK_USER_NAME = "";

	public static String ICERIK_PASSWORD = "";

	public static String ICERIK_URL = "";

	public static final String ENCRYPTION_OPTION = "on";

	public static String AES_KEY = "AES";

	public static String webServiceUrl = "";

	public static String sistemDurum = "1";

	public static final String COOKIE_KULLANICI_ADI1 = "eig_username1";
	public static final String COOKIE_KULLANICI_ADI2 = "eig_username2";
	public static final String TEST_SONUC_ONAYLI = "Onaylı Sonuç";
	public static final String TEST_SONUC_ONAYSIZ = "Onaylanmamıştır";
	public static final String TEST_SONUC_DUZELTILMIS = "Düzeltilmiş Sonuç";
	

	public static final String EFESGLOBAL_ACEGI_SECURITY_CONTEXT_KULLANICI_DEGISTIR = "EFESGLOBAL_ACEGI_SECURITY_CONTEXT_KULLANICI_DEGISTIR-";
	public static PdksDAO pdksDAO = null;

}
