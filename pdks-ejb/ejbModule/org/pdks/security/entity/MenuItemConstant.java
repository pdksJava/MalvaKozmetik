package org.pdks.security.entity;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;

@Startup
@Scope(ScopeType.APPLICATION)
@Name("menuItemConstant")
public class MenuItemConstant implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8417215681954625626L;
	public static String admin = "admin";
	public static String guest = "guest";
	public static String kullaniciIslemleri = "kullaniciIslemleri";
	public static String menuIslemleri = "menuIslemleri";
	public static String izinIslemleri = "izinIslemleri";
	public static String puantajIslemleri = "puantajIslemleri";
	public static String raporIslemleri = "raporIslemleri";
	public static String izinRaporlari = "izinRaporlari";
	public static String puantajRaporlari = "puantajRaporlari";
	public static String yemekHaneRaporlari = "yemekHaneRaporlari";

	public static String login = "/login.xhtml";
	public static String home = "/home.xhtml";
	public static String role = "/Role.xhtml";
	public static String roleList = "/RoleList.xhtml";

	public static String importSSL = "/general/importSSL.xhtml";
	public static String notice = "/general/notice.xhtml";
	public static String parameter = "/general/parameter.xhtml";
	public static String tanim = "/general/tanim.xhtml";
	public static String sapSunucuTanimlama = "/general/sapSunucuTanimlama.xhtml";
	public static String uygulamaVersiyon = "/general/uygulamaVersiyon.xhtml";

	public static String bakiyeGuncelle = "/izin/bakiyeGuncelle.xhtml";
	public static String excelOkuma = "/izin/excelOkuma.xhtml";
	public static String izinERPAktarim = "/izin/izinERPAktarim.xhtml";
	public static String izinKarti = "/izin/izinKarti.xhtml";
	public static String izinKartiSayfa = "/izin/izin/izinKartiSayfa.xhtml";
	public static String izinPdf = "/izin/izinPdf.xhtml";
	public static String onayimaGelenIzinler = "/izin/onayimaGelenIzinler.xhtml";
	public static String personelIzinGirisi = "/izin/personelIzinGirisi.xhtml";
	public static String personelIzinKopyala = "/izin/personelIzinKopyala.xhtml";
	public static String sskIzinGirisi = "/izin/sskIzinGirisi.xhtml";

	public static String denklestirmeBordroRaporu = "/puantaj/denklestirmeBordroRaporu.xhtml";
	public static String fazlaMesaiERPAktarim = "/puantaj/fazlaMesaiERPAktarim.xhtml";
	public static String fazlaMesaiHesapla = "/puantaj/fazlaMesaiHesapla.xhtml";
	public static String fazlaMesaiTalep = "/puantaj/fazlaMesaiTalep.xhtml";
	public static String hareketGiris = "/puantaj/hareketGiris.xhtml";
	public static String isKurVardiyaPlani = "/puantaj/isKurVardiyaPlani.xhtml";
	public static String kgsHareket = "/puantaj/kgsHareket.xhtml";
	public static String mesaiTalepLinkOnay = "/puantaj/mesaiTalepLinkOnay.xhtml";
	public static String mesaiTalepListesi = "/puantaj/mesaiTalepListesi.xhtml";
	public static String pdksVardiyaTanimlama = "/puantaj/pdksVardiyaTanimlama.xhtml";
	public static String personelFazlaMesai = "/puantaj/personelFazlaMesai.xhtml";
	public static String personelHareket = "/puantaj/personelHareket.xhtml";
	public static String planGuncellemeTalepLinkOnay = "/puantaj/planGuncellemeTalepLinkOnay.xhtml";
	public static String vardiyaPlani = "/puantaj/vardiyaPlani.xhtml";

	public static String aylikIzinRapor = "/rapor/izin/aylikIzinRapor.xhtml";
	public static String bakiyeIzin = "/rapor/izin/bakiyeIzin.xhtml";
	public static String fazlaMesaiIzin = "/rapor/izin/fazlaMesaiIzin.xhtml";
	public static String fazlaMesaiIzinRaporu = "/rapor/izin/fazlaMesaiIzinRaporu.xhtml";
	public static String gunlukIzinRapor = "/rapor/izin/gunlukIzinRapor.xhtml";
	public static String hekimCalisanRaporu = "/rapor/izin/hekimCalisanRaporu.xhtml";
	public static String hekimIzinRaporu = "/rapor/izin/hekimIzinRaporu.xhtml";
	public static String holdingKalanIzin = "/rapor/izin/holdingKalanIzin.xhtml";
	public static String izinKartiPdf = "/izin/izin/izinKartiPdf.xhtml";
	public static String izinOnay = "/rapor/izin/izinOnay.xhtml";
	public static String kullanilanIzinler = "/rapor/izin/kullanilanIzinler.xhtml";
	public static String personelKalanIzin = "/rapor/izin/personelKalanIzin.xhtml";
	public static String sskIstirahatIzinleri = "/rapor/izin/sskIstirahatIzinleri.xhtml";

	public static String aylikPlanRapor = "/rapor/puantaj/aylikPlanRapor.xhtml";
	public static String binadaKalanPersoneller = "/rapor/puantaj/binadaKalanPersoneller.xhtml";
	public static String calismaSaatleri = "/rapor/puantaj/calismaSaatleri.xhtml";
	public static String devamsizlikRaporu = "/rapor/puantaj/devamsizlikRaporu.xhtml";
	public static String fazlaCalismaRapor = "/rapor/puantaj/fazlaCalismaRapor.xhtml";
	public static String fazlaMesaiDonemselPuantajRapor = "/rapor/puantaj/fazlaMesaiDonemselPuantajRapor.xhtml";
	public static String fazlaMesaiDonemselRapor = "/rapor/puantaj/fazlaMesaiDonemselRapor.xhtml";
	public static String fazlaMesaiKontrolRapor = "/rapor/puantaj/fazlaMesaiKontrolRapor.xhtml";
	public static String fazlaMesaiOnayRapor = "/rapor/puantaj/fazlaMesaiOnayRapor.xhtml";
	public static String fazlaMesaiOzetRapor = "/rapor/puantaj/fazlaMesaiOzetRapor.xhtml";
	public static String fazlaMesaiRapor = "/rapor/puantaj/fazlaMesaiRapor.xhtml";
	public static String girisCikisKontrol = "/rapor/puantaj/girisCikisKontrol.xhtml";
	public static String hareketlerText = "/rapor/puantaj/hareketlerText.xhtml";
	public static String iseGelmeyenPersonelDagilimi = "/rapor/puantaj/iseGelmeyenPersonelDagilimi.xhtml";
	public static String vardiyaOzetRaporu = "/rapor/puantaj/vardiyaOzetRaporu.xhtml";
	public static String vardiyaPlaniTopluRapor = "/rapor/puantaj/vardiyaPlaniTopluRapor.xhtml";

	public static String yemekYiyenler = "/rapor/yemek/yemekYiyenler.xhtml";
	public static String yemekYiyenSayisi = "/rapor/yemek/yemekYiyenSayisi.xhtml";
	public static String yemekSirketTanimsiz = "/rapor/yemek/yemekSirketTanimsiz.xhtml";
	public static String yemekCiftBasanRapor = "/rapor/yemek/yemekCiftBasanRapor.xhtml";

	public static String tumHareketler = "/rapor/tumHareketler.xhtml";
	public static String organizasyonSemasi = "/rapor/organizasyonSemasi.xhtml";
	public static String personelListesi = "/rapor/personelListesi.xhtml";

	public static String menuItemPermissionTanimlama = "/security/menuItemPermissionTanimlama.xhtml";
	public static String menuItemTanimlama = "/security/menuItemTanimlama.xhtml";
	public static String openSession = "/security/openSession.xhtml";
	public static String user = "/security/user.xhtml";
	public static String sifreDegistirme = "/security/sifreDegistirme.xhtml";
	public static String superVisorDegistir = "/security/superVisorDegistir.xhtml";
	public static String yoneticiDegistir = "/security/yoneticiDegistir.xhtml";

	public static String calismaModeliTanimlama = "/tanimlama/calismaModeliTanimlama.xhtml";
	public static String departmanMail = "/tanimlama/departmanMail.xhtml";
	public static String departmanTanimlama = "/tanimlama/departmanTanimlama.xhtml";
	public static String detaysizPersonelTanimlama = "/tanimlama/detaysizPersonelTanimlama.xhtml";
	public static String izinHakedisHakkiTanimlama = "/tanimlama/izinHakedisHakkiTanimlama.xhtml";
	public static String izinTipiTanimlama = "/tanimlama/izinTipiTanimlama.xhtml";
	public static String kapiTanimlama = "/tanimlama/kapiTanimlama.xhtml";
	public static String personelGeciciYonetici = "/tanimlama/personelGeciciYonetici.xhtml";
	public static String personelKullaniciExcelGuncelle = "/tanimlama/personelKullaniciExcelGuncelle.xhtml";
	public static String personelRoleTanimlama = "/tanimlama/personelRoleTanimlama.xhtml";
	public static String personelTanimlama = "/tanimlama/personelTanimlama.xhtml";
	public static String personelYoneticiGuncelle = "/tanimlama/personelYoneticiGuncelle.xhtml";
	public static String sirketTanimlama = "/tanimlama/sirketTanimlama.xhtml";
	public static String taseronPersonelKopyala = "/tanimlama/taseronPersonelKopyala.xhtml";
	public static String tatilTanimlama = "/tanimlama/tatilTanimlama.xhtml";
	public static String sablonTanimlama = "/tanimlama/sablonTanimlama.xhtml";
	public static String vardiyaSablonTanimlama = "/tanimlama/vardiyaSablonTanimlama.xhtml";
	public static String vekilTanimlama = "/tanimlama/vekilTanimlama.xhtml";
	public static String vardiyaTanimlama = "/tanimlama/vardiyaTanimlama.xhtml";
	public static String yemekIzinTanimlama = "/tanimlama/yemekIzinTanimlama.xhtml";
	public static String yemekKartsizTanimlama = "/tanimlama/yemekKartsizTanimlama.xhtml";
	public static String yemekTanimlama = "/tanimlama/yemekTanimlama.xhtml";

	public String getKgsHareket() {
		return kgsHareket;
	}

	public void setKgsHareket(String kgsHareket) {
		MenuItemConstant.kgsHareket = kgsHareket;
	}

	public String getVardiyaPlani() {
		return vardiyaPlani;
	}

	public void setVardiyaPlani(String vardiyaPlani) {
		MenuItemConstant.vardiyaPlani = vardiyaPlani;
	}

	public String getVekilTanimlama() {
		return vekilTanimlama;
	}

	public void setVekilTanimlama(String vekilTanimlama) {
		MenuItemConstant.vekilTanimlama = vekilTanimlama;
	}

	public String getVardiyaSablonTanimlama() {
		return vardiyaSablonTanimlama;
	}

	public void setVardiyaSablonTanimlama(String vardiyaSablonTanimlama) {
		MenuItemConstant.vardiyaSablonTanimlama = vardiyaSablonTanimlama;
	}

	public String getPersonelTanimlama() {
		return personelTanimlama;
	}

	public void setPersonelTanimlama(String personelTanimlama) {
		MenuItemConstant.personelTanimlama = personelTanimlama;
	}

	public String getTatilTanimlama() {
		return tatilTanimlama;
	}

	public void setTatilTanimlama(String tatilTanimlama) {
		MenuItemConstant.tatilTanimlama = tatilTanimlama;
	}

	public String getSablonTanimlama() {
		return sablonTanimlama;
	}

	public void setSablonTanimlama(String sablonTanimlama) {
		MenuItemConstant.sablonTanimlama = sablonTanimlama;
	}

	public String getIzinTipiTanimlama() {
		return izinTipiTanimlama;
	}

	public void setIzinTipiTanimlama(String izinTipiTanimlama) {
		MenuItemConstant.izinTipiTanimlama = izinTipiTanimlama;
	}

	public String getKapiTanimlama() {
		return kapiTanimlama;
	}

	public void setKapiTanimlama(String kapi) {
		MenuItemConstant.kapiTanimlama = kapi;
	}

	public String getSirketTanimlama() {
		return sirketTanimlama;
	}

	public void setSirketTanimlama(String sirketTanimlama) {
		MenuItemConstant.sirketTanimlama = sirketTanimlama;
	}

	public String getNotice() {
		return notice;
	}

	public void setNotice(String notice) {
		MenuItemConstant.notice = notice;
	}

	public String getKullaniciIslemleri() {
		return kullaniciIslemleri;
	}

	public void setKullaniciIslemleri(String kullaniciIslemleri) {
		MenuItemConstant.kullaniciIslemleri = kullaniciIslemleri;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		MenuItemConstant.user = user;
	}

	public String getAdmin() {
		return admin;
	}

	public void setAdmin(String admin) {
		MenuItemConstant.admin = admin;
	}

	public String getGuest() {
		return guest;
	}

	public void setGuest(String guest) {
		MenuItemConstant.guest = guest;
	}

	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		MenuItemConstant.parameter = parameter;
	}

	public String getTanim() {
		return tanim;
	}

	public void setTanim(String tanim) {
		MenuItemConstant.tanim = tanim;
	}

	public String getMenuIslemleri() {
		return menuIslemleri;
	}

	public void setMenuIslemleri(String menuIslemleri) {
		MenuItemConstant.menuIslemleri = menuIslemleri;
	}

	public String getMenuItemTanimlama() {
		return menuItemTanimlama;
	}

	public void setMenuItemTanimlama(String menuItemTanimlama) {
		MenuItemConstant.menuItemTanimlama = menuItemTanimlama;
	}

	public String getMenuItemPermissionTanimlama() {
		return menuItemPermissionTanimlama;
	}

	public void setMenuItemPermissionTanimlama(String menuItemPermissionTanimlama) {
		MenuItemConstant.menuItemPermissionTanimlama = menuItemPermissionTanimlama;
	}

	public String getVardiyaTanimlama() {
		return vardiyaTanimlama;
	}

	public void setVardiyaTanimlama(String vardiyaTanimlama) {
		MenuItemConstant.vardiyaTanimlama = vardiyaTanimlama;
	}

	public String getIzinHakedisHakkiTanimlama() {
		return izinHakedisHakkiTanimlama;
	}

	public void setIzinHakedisHakkiTanimlama(String izinHakedisHakkiTanimlama) {
		MenuItemConstant.izinHakedisHakkiTanimlama = izinHakedisHakkiTanimlama;
	}

	public String getPersonelGeciciYonetici() {
		return personelGeciciYonetici;
	}

	public void setPersonelGeciciYonetici(String personelGeciciYonetici) {
		MenuItemConstant.personelGeciciYonetici = personelGeciciYonetici;
	}

	public String getSifreDegistirme() {
		return sifreDegistirme;
	}

	public void setSifreDegistirme(String sifreDegistirme) {
		MenuItemConstant.sifreDegistirme = sifreDegistirme;
	}

	public String getSuperVisorDegistir() {
		return superVisorDegistir;
	}

	public void setSuperVisorDegistir(String superVisorDegistir) {
		MenuItemConstant.superVisorDegistir = superVisorDegistir;
	}

	public String getDepartmanTanimlama() {
		return departmanTanimlama;
	}

	public void setDepartmanTanimlama(String departmanTanimlama) {
		MenuItemConstant.departmanTanimlama = departmanTanimlama;
	}

	public String getPersonelIzinGirisi() {
		return personelIzinGirisi;
	}

	public void setPersonelIzinGirisi(String personelIzinGirisi) {
		MenuItemConstant.personelIzinGirisi = personelIzinGirisi;
	}

	public String getOnayimaGelenIzinler() {
		return onayimaGelenIzinler;
	}

	public void setOnayimaGelenIzinler(String onayimaGelenIzinler) {
		MenuItemConstant.onayimaGelenIzinler = onayimaGelenIzinler;
	}

	public String getIzinIslemleri() {
		return izinIslemleri;
	}

	public void setIzinIslemleri(String izinIslemleri) {
		MenuItemConstant.izinIslemleri = izinIslemleri;
	}

	public String getPuantajIslemleri() {
		return puantajIslemleri;
	}

	public void setPuantajIslemleri(String puantajIslemleri) {
		MenuItemConstant.puantajIslemleri = puantajIslemleri;
	}

	public String getPersonelHareket() {
		return personelHareket;
	}

	public void setPersonelHareket(String personelHareket) {
		MenuItemConstant.personelHareket = personelHareket;
	}

	public String getGirisCikisKontrol() {
		return girisCikisKontrol;
	}

	public void setGirisCikisKontrol(String girisCikisKontrol) {
		MenuItemConstant.girisCikisKontrol = girisCikisKontrol;
	}

	public String getPersonelFazlaMesai() {
		return personelFazlaMesai;
	}

	public void setPersonelFazlaMesai(String personelFazlaMesai) {
		MenuItemConstant.personelFazlaMesai = personelFazlaMesai;
	}

	public String getCalismaSaatleri() {
		return calismaSaatleri;
	}

	public void setCalismaSaatleri(String calismaSaatleri) {
		MenuItemConstant.calismaSaatleri = calismaSaatleri;
	}

	public String getBinadaKalanPersoneller() {
		return binadaKalanPersoneller;
	}

	public void setBinadaKalanPersoneller(String binadaKalanPersoneller) {
		MenuItemConstant.binadaKalanPersoneller = binadaKalanPersoneller;
	}

	public String getYemekYiyenler() {
		return yemekYiyenler;
	}

	public void setYemekYiyenler(String yemekYiyenler) {
		MenuItemConstant.yemekYiyenler = yemekYiyenler;
	}

	public String getYemekYiyenSayisi() {
		return yemekYiyenSayisi;
	}

	public void setYemekYiyenSayisi(String yemekYiyenSayisi) {
		MenuItemConstant.yemekYiyenSayisi = yemekYiyenSayisi;
	}

	public String getRaporIslemleri() {
		return raporIslemleri;
	}

	public void setRaporIslemleri(String raporIslemleri) {
		MenuItemConstant.raporIslemleri = raporIslemleri;
	}

	public String getIzinRaporlari() {
		return izinRaporlari;
	}

	public void setIzinRaporlari(String izinRaporlari) {
		MenuItemConstant.izinRaporlari = izinRaporlari;
	}

	public String getPuantajRaporlari() {
		return puantajRaporlari;
	}

	public void setPuantajRaporlari(String puantajRaporlari) {
		MenuItemConstant.puantajRaporlari = puantajRaporlari;
	}

	public String getYemekHaneRaporlari() {
		return yemekHaneRaporlari;
	}

	public void setYemekHaneRaporlari(String yemekHaneRaporlari) {
		MenuItemConstant.yemekHaneRaporlari = yemekHaneRaporlari;
	}

	public String getPersonelKalanIzin() {
		return personelKalanIzin;
	}

	public void setPersonelKalanIzin(String personelKalanIzin) {
		MenuItemConstant.personelKalanIzin = personelKalanIzin;
	}

	public String getIzinOnay() {
		return izinOnay;
	}

	public void setIzinOnay(String izinOnay) {
		MenuItemConstant.izinOnay = izinOnay;
	}

	public String getIseGelmeyenPersonelDagilimi() {
		return iseGelmeyenPersonelDagilimi;
	}

	public void setIseGelmeyenPersonelDagilimi(String iseGelmeyenPersonelDagilimi) {
		MenuItemConstant.iseGelmeyenPersonelDagilimi = iseGelmeyenPersonelDagilimi;
	}

	public String getDevamsizlikRaporu() {
		return devamsizlikRaporu;
	}

	public void setDevamsizlikRaporu(String devamsizlikRaporu) {
		MenuItemConstant.devamsizlikRaporu = devamsizlikRaporu;
	}

	public String getHareketlerText() {
		return hareketlerText;
	}

	public void setHareketlerText(String hareketlerText) {
		MenuItemConstant.hareketlerText = hareketlerText;
	}

	public String getTumHareketler() {
		return tumHareketler;
	}

	public void setTumHareketler(String tumHareketler) {
		MenuItemConstant.tumHareketler = tumHareketler;
	}

	public String getIzinPdf() {
		return izinPdf;
	}

	public void setIzinPdf(String izinPdf) {
		MenuItemConstant.izinPdf = izinPdf;
	}

	public String getIzinERPAktarim() {
		return izinERPAktarim;
	}

	public void setIzinERPAktarim(String izinERPAktarim) {
		MenuItemConstant.izinERPAktarim = izinERPAktarim;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		MenuItemConstant.role = role;
	}

	public String getRoleList() {
		return roleList;
	}

	public void setRoleList(String roleList) {
		MenuItemConstant.roleList = roleList;
	}

	public String getHome() {
		return home;
	}

	public void setHome(String value) {
		MenuItemConstant.home = value;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		MenuItemConstant.login = login;
	}

	public String getYoneticiDegistir() {
		return yoneticiDegistir;
	}

	public void setYoneticiDegistir(String yoneticiDegistir) {
		MenuItemConstant.yoneticiDegistir = yoneticiDegistir;
	}

	public String getBakiyeIzin() {
		return bakiyeIzin;
	}

	public void setBakiyeIzin(String bakiyeIzin) {
		MenuItemConstant.bakiyeIzin = bakiyeIzin;
	}

	public String getFazlaMesaiIzin() {
		return fazlaMesaiIzin;
	}

	public void setFazlaMesaiIzin(String fazlaMesaiIzin) {
		MenuItemConstant.fazlaMesaiIzin = fazlaMesaiIzin;
	}

	public String getYemekTanimlama() {
		return yemekTanimlama;
	}

	public void setYemekTanimlama(String yemekTanimlama) {
		MenuItemConstant.yemekTanimlama = yemekTanimlama;
	}

	public String getHareketGiris() {
		return hareketGiris;
	}

	public void setHareketGiris(String hareketGiris) {
		MenuItemConstant.hareketGiris = hareketGiris;
	}

	public String getExcelOkuma() {
		return excelOkuma;
	}

	public void setExcelOkuma(String excelOkuma) {
		MenuItemConstant.excelOkuma = excelOkuma;
	}

	public String getPersonelListesi() {
		return personelListesi;
	}

	public void setPersonelListesi(String personelListesi) {
		MenuItemConstant.personelListesi = personelListesi;
	}

	public String getDetaysizPersonelTanimlama() {
		return detaysizPersonelTanimlama;
	}

	public void setDetaysizPersonelTanimlama(String detaysizPersonelTanimlama) {
		MenuItemConstant.detaysizPersonelTanimlama = detaysizPersonelTanimlama;
	}

	public String getUygulamaVersiyon() {
		return uygulamaVersiyon;
	}

	public void setUygulamaVersiyon(String uygulamaVersiyon) {
		MenuItemConstant.uygulamaVersiyon = uygulamaVersiyon;
	}

	public String getYemekSirketTanimsiz() {
		return yemekSirketTanimsiz;
	}

	public void setYemekSirketTanimsiz(String yemekSirketTanimsiz) {
		MenuItemConstant.yemekSirketTanimsiz = yemekSirketTanimsiz;
	}

	public String getYemekIzinTanimlama() {
		return yemekIzinTanimlama;
	}

	public void setYemekIzinTanimlama(String yemekIzinTanimlama) {
		MenuItemConstant.yemekIzinTanimlama = yemekIzinTanimlama;
	}

	public String getGunlukIzinRapor() {
		return gunlukIzinRapor;
	}

	public void setGunlukIzinRapor(String gunlukIzinRapor) {
		MenuItemConstant.gunlukIzinRapor = gunlukIzinRapor;
	}

	public String getKullanilanIzinler() {
		return kullanilanIzinler;
	}

	public void setKullanilanIzinler(String kullanilanIzinler) {
		MenuItemConstant.kullanilanIzinler = kullanilanIzinler;
	}

	public String getSskIstirahatIzinleri() {
		return sskIstirahatIzinleri;
	}

	public void setSskIstirahatIzinleri(String sskIstirahatIzinleri) {
		MenuItemConstant.sskIstirahatIzinleri = sskIstirahatIzinleri;
	}

	public String getSskIzinGirisi() {
		return sskIzinGirisi;
	}

	public void setSskIzinGirisi(String sskIzinGirisi) {
		MenuItemConstant.sskIzinGirisi = sskIzinGirisi;
	}

	public String getAylikIzinRapor() {
		return aylikIzinRapor;
	}

	public void setAylikIzinRapor(String aylikIzinRapor) {
		MenuItemConstant.aylikIzinRapor = aylikIzinRapor;
	}

	public String getHekimIzinRaporu() {
		return hekimIzinRaporu;
	}

	public void setHekimIzinRaporu(String hekimIzinRaporu) {
		MenuItemConstant.hekimIzinRaporu = hekimIzinRaporu;
	}

	public String getHekimCalisanRaporu() {
		return hekimCalisanRaporu;
	}

	public void setHekimCalisanRaporu(String hekimCalisanRaporu) {
		MenuItemConstant.hekimCalisanRaporu = hekimCalisanRaporu;
	}

	public String getOpenSession() {
		return openSession;
	}

	public void setOpenSession(String openSession) {
		MenuItemConstant.openSession = openSession;
	}

	public static String getPersonelYoneticiGuncelle() {
		return personelYoneticiGuncelle;
	}

	public static void setPersonelYoneticiGuncelle(String personelYoneticiGuncelle) {
		MenuItemConstant.personelYoneticiGuncelle = personelYoneticiGuncelle;
	}

	public static String getBakiyeGuncelle() {
		return bakiyeGuncelle;
	}

	public static void setBakiyeGuncelle(String bakiyeGuncelle) {
		MenuItemConstant.bakiyeGuncelle = bakiyeGuncelle;
	}

	public static String getPersonelKullaniciExcelGuncelle() {
		return personelKullaniciExcelGuncelle;
	}

	public static void setPersonelKullaniciExcelGuncelle(String personelKullaniciExcelGuncelle) {
		MenuItemConstant.personelKullaniciExcelGuncelle = personelKullaniciExcelGuncelle;
	}

	public static String getPersonelRoleTanimlama() {
		return personelRoleTanimlama;
	}

	public static void setPersonelRoleTanimlama(String personelRoleTanimlama) {
		MenuItemConstant.personelRoleTanimlama = personelRoleTanimlama;
	}

	public static String getIzinKartiPdf() {
		return izinKartiPdf;
	}

	public static void setIzinKartiPdf(String izinKartiPdf) {
		MenuItemConstant.izinKartiPdf = izinKartiPdf;
	}

	public static String getIzinKarti() {
		return izinKarti;
	}

	public static void setIzinKarti(String izinKarti) {
		MenuItemConstant.izinKarti = izinKarti;
	}

	public static String getIzinKartiSayfa() {
		return izinKartiSayfa;
	}

	public static void setIzinKartiSayfa(String izinKartiSayfa) {
		MenuItemConstant.izinKartiSayfa = izinKartiSayfa;
	}

	public static String getFazlaMesaiHesapla() {
		return fazlaMesaiHesapla;
	}

	public static void setFazlaMesaiHesapla(String fazlaMesaiHesapla) {
		MenuItemConstant.fazlaMesaiHesapla = fazlaMesaiHesapla;
	}

	public static String getDepartmanMail() {
		return departmanMail;
	}

	public static void setDepartmanMail(String departmanMail) {
		MenuItemConstant.departmanMail = departmanMail;
	}

	public static String getAylikPlanRapor() {
		return aylikPlanRapor;
	}

	public static void setAylikPlanRapor(String aylikPlanRapor) {
		MenuItemConstant.aylikPlanRapor = aylikPlanRapor;
	}

	public static String getYemekKartsizTanimlama() {
		return yemekKartsizTanimlama;
	}

	public static void setYemekKartsizTanimlama(String yemekKartsizTanimlama) {
		MenuItemConstant.yemekKartsizTanimlama = yemekKartsizTanimlama;
	}

	public static String getYemekCiftBasanRapor() {
		return yemekCiftBasanRapor;
	}

	public static void setYemekCiftBasanRapor(String yemekCiftBasanRapor) {
		MenuItemConstant.yemekCiftBasanRapor = yemekCiftBasanRapor;
	}

	public static String getImportSSL() {
		return importSSL;
	}

	public static void setImportSSL(String importSSL) {
		MenuItemConstant.importSSL = importSSL;
	}

	public static String getSapSunucuTanimlama() {
		return sapSunucuTanimlama;
	}

	public static void setSapSunucuTanimlama(String sapSunucuTanimlama) {
		MenuItemConstant.sapSunucuTanimlama = sapSunucuTanimlama;
	}

	public static String getPersonelIzinKopyala() {
		return personelIzinKopyala;
	}

	public static void setPersonelIzinKopyala(String personelIzinKopyala) {
		MenuItemConstant.personelIzinKopyala = personelIzinKopyala;
	}

	public static String getHoldingKalanIzin() {
		return holdingKalanIzin;
	}

	public static void setHoldingKalanIzin(String holdingKalanIzin) {
		MenuItemConstant.holdingKalanIzin = holdingKalanIzin;
	}

	public static String getPdksVardiyaTanimlama() {
		return pdksVardiyaTanimlama;
	}

	public static void setPdksVardiyaTanimlama(String pdksVardiyaTanimlama) {
		MenuItemConstant.pdksVardiyaTanimlama = pdksVardiyaTanimlama;
	}

	public static String getFazlaMesaiERPAktarim() {
		return fazlaMesaiERPAktarim;
	}

	public static void setFazlaMesaiERPAktarim(String fazlaMesaiERPAktarim) {
		MenuItemConstant.fazlaMesaiERPAktarim = fazlaMesaiERPAktarim;
	}

	public static String getMesaiTalepLinkOnay() {
		return mesaiTalepLinkOnay;
	}

	public static void setMesaiTalepLinkOnay(String mesaiTalepLinkOnay) {
		MenuItemConstant.mesaiTalepLinkOnay = mesaiTalepLinkOnay;
	}

	public static String getMesaiTalepListesi() {
		return mesaiTalepListesi;
	}

	public static void setMesaiTalepListesi(String mesaiTalepListesi) {
		MenuItemConstant.mesaiTalepListesi = mesaiTalepListesi;
	}

	public static String getCalismaModeliTanimlama() {
		return calismaModeliTanimlama;
	}

	public static void setCalismaModeliTanimlama(String calismaModeliTanimlama) {
		MenuItemConstant.calismaModeliTanimlama = calismaModeliTanimlama;
	}

	public static String getFazlaMesaiTalep() {
		return fazlaMesaiTalep;
	}

	public static void setFazlaMesaiTalep(String fazlaMesaiTalep) {
		MenuItemConstant.fazlaMesaiTalep = fazlaMesaiTalep;
	}

	public static String getVardiyaOzetRaporu() {
		return vardiyaOzetRaporu;
	}

	public static void setVardiyaOzetRaporu(String vardiyaOzetRaporu) {
		MenuItemConstant.vardiyaOzetRaporu = vardiyaOzetRaporu;
	}

	public static String getFazlaMesaiRapor() {
		return fazlaMesaiRapor;
	}

	public static void setFazlaMesaiRapor(String fazlaMesaiRapor) {
		MenuItemConstant.fazlaMesaiRapor = fazlaMesaiRapor;
	}

	public static String getFazlaMesaiOzetRapor() {
		return fazlaMesaiOzetRapor;
	}

	public static void setFazlaMesaiOzetRapor(String fazlaMesaiOzetRapor) {
		MenuItemConstant.fazlaMesaiOzetRapor = fazlaMesaiOzetRapor;
	}

	public static String getFazlaMesaiDonemselRapor() {
		return fazlaMesaiDonemselRapor;
	}

	public static void setFazlaMesaiDonemselRapor(String fazlaMesaiDonemselRapor) {
		MenuItemConstant.fazlaMesaiDonemselRapor = fazlaMesaiDonemselRapor;
	}

	public static String getIsKurVardiyaPlani() {
		return isKurVardiyaPlani;
	}

	public static void setIsKurVardiyaPlani(String isKurVardiyaPlani) {
		MenuItemConstant.isKurVardiyaPlani = isKurVardiyaPlani;
	}

	public static String getFazlaMesaiIzinRaporu() {
		return fazlaMesaiIzinRaporu;
	}

	public static void setFazlaMesaiIzinRaporu(String fazlaMesaiIzinRaporu) {
		MenuItemConstant.fazlaMesaiIzinRaporu = fazlaMesaiIzinRaporu;
	}

	public static String getFazlaMesaiKontrolRapor() {
		return fazlaMesaiKontrolRapor;
	}

	public static void setFazlaMesaiKontrolRapor(String fazlaMesaiKontrolRapor) {
		MenuItemConstant.fazlaMesaiKontrolRapor = fazlaMesaiKontrolRapor;
	}

	public static String getDenklestirmeBordroRaporu() {
		return denklestirmeBordroRaporu;
	}

	public static void setDenklestirmeBordroRaporu(String denklestirmeBordroRaporu) {
		MenuItemConstant.denklestirmeBordroRaporu = denklestirmeBordroRaporu;
	}

	public static String getFazlaMesaiOnayRapor() {
		return fazlaMesaiOnayRapor;
	}

	public static void setFazlaMesaiOnayRapor(String fazlaMesaiOnayRapor) {
		MenuItemConstant.fazlaMesaiOnayRapor = fazlaMesaiOnayRapor;
	}

	public static String getTaseronPersonelKopyala() {
		return taseronPersonelKopyala;
	}

	public static void setTaseronPersonelKopyala(String taseronPersonelKopyala) {
		MenuItemConstant.taseronPersonelKopyala = taseronPersonelKopyala;
	}

	public static String getFazlaCalismaRapor() {
		return fazlaCalismaRapor;
	}

	public static void setFazlaCalismaRapor(String fazlaCalismaRapor) {
		MenuItemConstant.fazlaCalismaRapor = fazlaCalismaRapor;
	}

	public static String getOrganizasyonSemasi() {
		return organizasyonSemasi;
	}

	public static void setOrganizasyonSemasi(String organizasyonSemasi) {
		MenuItemConstant.organizasyonSemasi = organizasyonSemasi;
	}

	public static String getFazlaMesaiDonemselPuantajRapor() {
		return fazlaMesaiDonemselPuantajRapor;
	}

	public static void setFazlaMesaiDonemselPuantajRapor(String fazlaMesaiDonemselPuantajRapor) {
		MenuItemConstant.fazlaMesaiDonemselPuantajRapor = fazlaMesaiDonemselPuantajRapor;
	}

	public static String getPlanGuncellemeTalepLinkOnay() {
		return planGuncellemeTalepLinkOnay;
	}

	public static void setPlanGuncellemeTalepLinkOnay(String planGuncellemeTalepLinkOnay) {
		MenuItemConstant.planGuncellemeTalepLinkOnay = planGuncellemeTalepLinkOnay;
	}

	public static String getVardiyaPlaniTopluRapor() {
		return vardiyaPlaniTopluRapor;
	}

	public static void setVardiyaPlaniTopluRapor(String vardiyaPlaniTopluRapor) {
		MenuItemConstant.vardiyaPlaniTopluRapor = vardiyaPlaniTopluRapor;
	}

}
