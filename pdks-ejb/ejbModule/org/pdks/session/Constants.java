package org.pdks.session;

import java.io.Serializable;
import java.util.Locale;

 

/**
 * @author Hasan Sayar
 */
public class Constants  implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 5790790563808952131L;

	public static final Locale TR_LOCALE = new Locale("tr", "TR");

    public static final Locale RU_LOCALE = new Locale("ru", "RU");
 

    public static final String LOCALE_KEY = "langLocale";

    public static final String KULLANICI = "kullanici";

 

    // ServletContext
    public static final String SERVLET_CONTEXT_UYGULAMA_DILLERI = "servletContextUygulamaDilleri";

    public static final String SERVLET_CONTEXT_UYGULAMA_DB = "servletContextUygulamaDb";

    //genel islem tipleri
    public static final int GENEL_ISLEM_TIPI_KURUMSAL_KULLANICI_ARAMA = 101;

    //cookie'ler
    public static final String COOKIE_KULLANICI_ADI1 = "PDKSUsername1";

    public static final String COOKIE_KULLANICI_ADI2 = "PDKSUsername2";

    public static final String COOKIE_KULLANICI_DILI = "kullaniciDili";

    public static final int COOKIE_KULLANICI_DILI_SURE = 7 * 24 * 60 * 60;

    //	firma'lar
    public static final int ISLEM_TIPI_FIRMA_ILISKILERI_DUZENLE = 111105;

    public static final int SECIM_EVET = 1;

    public static final int SECIM_HAYIR = 0;

    public static final int TAKIP_NO_UZUNLUK = 8;
 
    public static final long GUN_ZAMAN = 24 * 60 * 60 * 1000;

 

   

}