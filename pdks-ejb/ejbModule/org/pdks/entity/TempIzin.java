package org.pdks.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.pdks.session.PdksUtil;

public class TempIzin implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 544794133884390627L;
	static Logger logger = Logger.getLogger(TempIzin.class);

	private Personel personel, yeniPersonel;

	private ArrayList<Long> izinler;

	private ArrayList<PersonelIzin> yillikIzinler;

	private PersonelIzin personelIzin;

	private double toplamBakiyeIzin = 0, toplamKalanIzin = 0, kullanilanIzin = 0;

	private Date izinHakEdisTarihi, iseBaslamaTarihi, grubaGirisTarihi, dogumTarihi;

	private int sayfaNo = 0;

	private String styleClass;

	private Boolean secim = Boolean.FALSE;

	public Personel getPersonel() {
		return personel;
	}

	public void setPersonel(Personel personel) {
		this.personel = personel;
	}

	public ArrayList<Long> getIzinler() {
		return izinler;
	}

	public void setIzinler(ArrayList<Long> izinler) {
		this.izinler = izinler;
	}

	public double getToplamKalanIzin() {
		return toplamKalanIzin;
	}

	public void setToplamKalanIzin(double toplamKalanIzin) {
		this.toplamKalanIzin = toplamKalanIzin;
	}

	public PersonelIzin getPersonelIzin() {
		return personelIzin;
	}

	public void setPersonelIzin(PersonelIzin personelIzin) {
		this.personelIzin = personelIzin;
	}

	public double getKullanilanIzin() {
		return kullanilanIzin;
	}

	public void setKullanilanIzin(double kullanilanIzin) {
		this.kullanilanIzin = kullanilanIzin;
	}

	public double getToplamBakiyeIzin() {
		return toplamBakiyeIzin;
	}

	public void setToplamBakiyeIzin(double toplamBakiyeIzin) {
		this.toplamBakiyeIzin = toplamBakiyeIzin;
	}

	public ArrayList<PersonelIzin> getYillikIzinler() {
		return yillikIzinler;
	}

	public void setYillikIzinler(ArrayList<PersonelIzin> yillikIzinler) {
		this.yillikIzinler = yillikIzinler;
	}

	public void bakiyeHesapla() {
		if (yillikIzinler != null) {
			Date onceki = null;
			sayfaNo = 0;
			for (PersonelIzin bakiyeIzin : yillikIzinler) {
				++sayfaNo;
				bakiyeIzin.setSayfaNo(sayfaNo);
				bakiyeIzin.setBirOncekiHakedisTarih(onceki);
				onceki = bakiyeIzin.getBitisZamani();
				List<PersonelIzin> harcananDigerIzinler = new ArrayList<PersonelIzin>();
				try {
					if (bakiyeIzin.getHakEdisIzinler() != null) {
						for (PersonelIzinDetay detay : bakiyeIzin.getHakEdisIzinler()) {

							if (!detay.getPersonelIzin().isRedmi())
								if (bakiyeIzin.getDonemSonu() == null || detay.getPersonelIzin().getBaslangicZamani().getTime() <= bakiyeIzin.getDonemSonu().getTime())
									harcananDigerIzinler.add(detay.getPersonelIzin());
						}
						if (harcananDigerIzinler.size() > 1)
							harcananDigerIzinler = PdksUtil.sortListByAlanAdi(harcananDigerIzinler, "baslangicZamani", Boolean.FALSE);
					}
				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());

				}
				bakiyeIzin.setHarcananDigerIzinler(harcananDigerIzinler);
			}
		}

	}

	public void excelBakiyeHesapla() {
		toplamBakiyeIzin = 0d;
		kullanilanIzin = 0d;
		sayfaNo = 0;
		if (yillikIzinler != null) {
			for (PersonelIzin bakiyeIzin : yillikIzinler) {
				++sayfaNo;
				bakiyeIzin.setSayfaNo(sayfaNo);
				toplamBakiyeIzin += bakiyeIzin.getBakiyeSuresi();
				if (bakiyeIzin.getHarcananDigerIzinler() != null) {
					List<PersonelIzin> harcananDigerIzinler = bakiyeIzin.getHarcananDigerIzinler();
					if (harcananDigerIzinler.size() > 1) {
						harcananDigerIzinler = PdksUtil.sortListByAlanAdi(harcananDigerIzinler, "baslangicZamani", Boolean.FALSE);
						bakiyeIzin.setHarcananDigerIzinler(harcananDigerIzinler);
					}
					for (PersonelIzin harcananIzin : harcananDigerIzinler) {
						kullanilanIzin += harcananIzin.getIzinSuresi();
					}

				}
			}
		}
	}

	public String getStyleClass() {
		return styleClass;
	}

	public void setStyleClass(String styleClass) {
		this.styleClass = styleClass;
	}

	public Date getIzinHakEdisTarihi() {
		return izinHakEdisTarihi;
	}

	public void setIzinHakEdisTarihi(Date izinHakEdisTarihi) {
		this.izinHakEdisTarihi = izinHakEdisTarihi;
	}

	public Date getIseBaslamaTarihi() {
		return iseBaslamaTarihi;
	}

	public void setIseBaslamaTarihi(Date iseBaslamaTarihi) {
		this.iseBaslamaTarihi = iseBaslamaTarihi;
	}

	public Date getGrubaGirisTarihi() {
		return grubaGirisTarihi;
	}

	public void setGrubaGirisTarihi(Date grubaGirisTarihi) {
		this.grubaGirisTarihi = grubaGirisTarihi;
	}

	public Date getDogumTarihi() {
		return dogumTarihi;
	}

	public void setDogumTarihi(Date dogumTarihi) {
		this.dogumTarihi = dogumTarihi;
	}

	public int getSayfaNo() {
		return sayfaNo;
	}

	public void setSayfaNo(int sayfaNo) {
		this.sayfaNo = sayfaNo;
	}

	public Long getSicilNo() {
		Long sicilNo = personel != null ? Long.parseLong(personel.getPdksSicilNo()) : 0L;
		return sicilNo;
	}

	public Personel getYeniPersonel() {
		return yeniPersonel;
	}

	public void setYeniPersonel(Personel yeniPersonel) {
		this.yeniPersonel = yeniPersonel;
	}

	public Boolean getSecim() {
		return secim;
	}

	public void setSecim(Boolean secim) {
		this.secim = secim;
	}

	public Personel getPdksPersonel() {
		Personel pdksPersonel = personel != null ? personel : yeniPersonel;
		return pdksPersonel;
	}
}
