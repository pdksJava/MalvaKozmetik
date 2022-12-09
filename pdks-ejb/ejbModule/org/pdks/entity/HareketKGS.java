package org.pdks.entity;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.pdks.session.PdksUtil;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = HareketKGS.TABLE_NAME)
@Immutable
public class HareketKGS implements Serializable, Cloneable {

	private static final long serialVersionUID = -7614576632219960146L;
	static Logger logger = Logger.getLogger(HareketKGS.class);

	public static final String TABLE_NAME = "KGS_HAREKET_VIEW";
	public static final String COLUMN_NAME_ID = "ID";
	public static final String COLUMN_NAME_PERSONEL = "USERID";
	public static final String COLUMN_NAME_KAPI = "KAPIID";
	public static final String COLUMN_NAME_ZAMAN = "HAREKET_ZAMANI";
	public static final String COLUMN_NAME_OLUSTURMA_ZAMANI = "OLUSTURMA_ZAMANI";
	public static final String COLUMN_NAME_ORJ_ZAMAN = "ORJ_ZAMAN";
	public static final String COLUMN_NAME_ISLEM = "ISLEM_ID";
	public static final String COLUMN_NAME_DURUM = "DURUM";
	public static final String COLUMN_NAME_SICIL_NO = "SICIL_NO";
	public static final String COLUMN_NAME_SIRKET = "SIRKET";
	public static final String COLUMN_NAME_TABLE_ID = "TABLE_ID";

	public static final String AYRIK_HAREKET = "M";

	public static final int DURUM_BLOKE = 2;
	public static final int DURUM_AKTIF = 1;
	public static final int DURUM_PASIF = 0;
	public static final int DURUM_TASLAK = -1;
	public static final String GIRIS_ISLEM_YAPAN_SIRKET_KGS = "K";
	public static final String GIRIS_ISLEM_YAPAN_SIRKET_PDKS = "A";

	private String id;
	private String sirket;
	private Long hareketTableId, islemId;
	private PersonelView personel;
	private PersonelKGS personelKGS;
	private KapiKGS kapiKGS;
	private KapiView kapiView, terminalKapi;
	private Date zaman, olusturmaZamani, orjinalZaman, girisZaman, cikisZaman, girisOrjinalZaman, cikisOrjinalZaman, yemekTeloreansZamani, oncekiYemekZamani;
	private Double fazlaMesai;
	private PersonelHareketIslem islem;
	private PersonelFazlaMesai personelFazlaMesai;
	private VardiyaGun vardiyaGun;
	private int durum;
	private int yemekYiyenSayisi, gecerliYemekAdet = 0;
	private int toplam;
	private Boolean gecerliYemek;
	private Long kapiId;
	private Long personelId;
	private boolean tatil = Boolean.FALSE, puantajOnayDurum, cokluOgun = Boolean.FALSE, gecerliDegil = Boolean.FALSE;
	private HareketKGS cikisHareket, girisHareket;
	private YemekOgun yemekOgun;
	private List<HareketKGS> yemekList;
	protected boolean checkBoxDurum;
	protected String style = VardiyaGun.STYLE_CLASS_ODD, islemYapan = "";

	@Id
	@Column(name = COLUMN_NAME_ID)
	public String getId() {

		return id;
	}

	public void setId(String id) {
		this.id = id;

	}

	@Column(name = COLUMN_NAME_SIRKET, length = 1)
	public String getSirket() {
		return this.sirket;
	}

	public void setSirket(String sirket) {
		this.sirket = sirket;
	}

	@Column(name = COLUMN_NAME_TABLE_ID)
	public Long getHareketTableId() {
		return this.hareketTableId;
	}

	public void setHareketTableId(Long hareketTableId) {
		this.hareketTableId = hareketTableId;
	}

	@ManyToOne(cascade = { javax.persistence.CascadeType.REFRESH })
	@JoinColumn(name = COLUMN_NAME_PERSONEL, nullable = false, insertable = false, updatable = false)
	@Fetch(FetchMode.JOIN)
	public PersonelKGS getPersonelKGS() {
		return personelKGS;
	}

	public void setPersonelKGS(PersonelKGS value) {
		if (value != null) {
			this.setPersonel(value.getPersonelView());
		}
		this.personelKGS = value;
	}

	@Transient
	public PersonelView getPersonel() {
		if (personel == null && personelKGS != null) {
			this.setPersonel(personelKGS.getPersonelView());
		}
		return this.personel;
	}

	public void setPersonel(PersonelView personel) {
		this.personel = personel;
	}

	@Transient
	public KapiView getKapiView() {
		if (kapiView == null && kapiKGS != null) {
			this.kapiView = kapiKGS.getKapiView();
		}
		return this.kapiView;
	}

	public void setKapiView(KapiView value) {
		if (value != null && kapiKGS == null)
			kapiKGS = value.getKapiKGS();
		this.kapiView = value;
	}

	@Column(name = COLUMN_NAME_KAPI, nullable = false)
	public Long getKapiId() {
		return this.kapiId;
	}

	public void setKapiId(Long kapiId) {
		this.kapiId = kapiId;
	}

	@Column(name = COLUMN_NAME_PERSONEL, nullable = false)
	public Long getPersonelId() {
		return this.personelId;
	}

	public void setPersonelId(Long personelId) {
		this.personelId = personelId;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = COLUMN_NAME_ZAMAN, nullable = false)
	public Date getZaman() {
		return this.zaman;
	}

	public void setZaman(Date vaue) {
		if (vaue != null && this.orjinalZaman == null)
			this.orjinalZaman = vaue;
		this.zaman = vaue;
	}

	@Temporal(value = TemporalType.TIMESTAMP)
	@Column(name = HareketKGS.COLUMN_NAME_OLUSTURMA_ZAMANI, nullable = false)
	public Date getOlusturmaZamani() {
		return olusturmaZamani;
	}

	public void setOlusturmaZamani(Date olusturmaZamani) {
		this.olusturmaZamani = olusturmaZamani;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = COLUMN_NAME_ORJ_ZAMAN, nullable = false)
	public Date getOrjinalZaman() {
		return this.orjinalZaman;
	}

	public void setOrjinalZaman(Date orjinalZaman) {
		this.orjinalZaman = orjinalZaman;
	}

	@Column(name = COLUMN_NAME_ISLEM, updatable = false, insertable = false)
	public Long getIslemId() {
		return islemId;
	}

	public void setIslemId(Long islemId) {
		this.islemId = islemId;
	}

	@ManyToOne(cascade = { javax.persistence.CascadeType.REFRESH })
	@JoinColumn(name = COLUMN_NAME_KAPI, nullable = false, insertable = false, updatable = false)
	@Fetch(FetchMode.JOIN)
	public KapiKGS getKapiKGS() {
		return kapiKGS;
	}

	public void setKapiKGS(KapiKGS value) {
		if (value != null)
			this.kapiView = value.getKapiView();
		this.kapiKGS = value;
	}

	@ManyToOne(cascade = { javax.persistence.CascadeType.REFRESH })
	@JoinColumn(name = COLUMN_NAME_ISLEM)
	@Fetch(FetchMode.JOIN)
	public PersonelHareketIslem getIslem() {
		return this.islem;
	}

	public void setIslem(PersonelHareketIslem islem) {
		this.islem = islem;
	}

	@Column(name = COLUMN_NAME_DURUM)
	public int getDurum() {
		return this.durum;
	}

	public void setDurum(int durum) {
		this.durum = durum;
	}

	@Transient
	public VardiyaGun getVardiyaGun() {
		return this.vardiyaGun;
	}

	public void setVardiyaGun(VardiyaGun vardiyaGun) {
		this.vardiyaGun = vardiyaGun;
	}

	@Transient
	public Double getFazlaMesai() {
		return this.fazlaMesai;
	}

	public void setFazlaMesai(Double fazlaMesai) {
		this.fazlaMesai = fazlaMesai;
	}

	@Transient
	public PersonelFazlaMesai getPersonelFazlaMesai() {
		return this.personelFazlaMesai;
	}

	public void setPersonelFazlaMesai(PersonelFazlaMesai value) {

		this.personelFazlaMesai = value;
	}

	@Transient
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
		}
		throw new InternalError();
	}

	@Transient
	public Date getGirisZaman() {
		return this.girisZaman;
	}

	public void setGirisZaman(Date girisZaman) {
		this.girisZaman = girisZaman;
	}

	@Transient
	public Date getCikisZaman() {
		return this.cikisZaman;
	}

	public void setCikisZaman(Date cikisZaman) {
		this.cikisZaman = cikisZaman;
	}

	@Transient
	public Date getGirisOrjinalZaman() {
		return this.girisOrjinalZaman;
	}

	public void setGirisOrjinalZaman(Date girisOrjinalZaman) {
		this.girisOrjinalZaman = girisOrjinalZaman;
	}

	@Transient
	public Date getCikisOrjinalZaman() {
		return this.cikisOrjinalZaman;
	}

	public void setCikisOrjinalZaman(Date cikisOrjinalZaman) {
		this.cikisOrjinalZaman = cikisOrjinalZaman;
	}

	@Transient
	public int getYemekYiyenSayisi() {
		return this.yemekYiyenSayisi;
	}

	public void setYemekYiyenSayisi(int yemekYiyenSayisi) {
		this.yemekYiyenSayisi = yemekYiyenSayisi;
	}

	@Transient
	public int getToplam() {
		return this.toplam;
	}

	public void setToplam(int toplam) {
		this.toplam = toplam;
	}

	@Transient
	public boolean isTatil() {
		return this.tatil;
	}

	public void setTatil(boolean tatil) {
		this.tatil = tatil;
	}

	@Transient
	public List<HareketKGS> getYemekList() {
		return this.yemekList;
	}

	public void setYemekList(List<HareketKGS> yemekList) {
		this.yemekList = yemekList;
	}

	@Transient
	public boolean isCheckBoxDurum() {
		return this.checkBoxDurum;
	}

	public void setCheckBoxDurum(boolean checkBoxDurum) {
		this.checkBoxDurum = checkBoxDurum;
	}

	@Transient
	public String getStyle() {
		return this.style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	@Transient
	public YemekOgun getYemekOgun() {
		return this.yemekOgun;
	}

	public void setYemekOgun(YemekOgun value) {
		this.yemekOgun = value;
	}

	@Transient
	public String getDurumu() {
		return this.checkBoxDurum ? "Mükerrer" : (cokluOgun ? "Çoklu Öğün" : "");
	}

	@Transient
	public boolean isHareketDurum() {
		boolean hareketDurum = durum == 1;
		return hareketDurum;
	}

	@Transient
	public Boolean getHareketEkleDurum() {
		Boolean hareketEkleDurum = null;
		try {
			hareketEkleDurum = puantajOnayDurum == false && (islem == null || islem.getIslemTipi() == null || !islem.getIslemTipi().equalsIgnoreCase("D"));
		} catch (Exception e) {

		}

		return hareketEkleDurum;
	}

	@Transient
	public String getSicilNo() {
		String sicilNo = "";
		try {
			sicilNo = this.personel != null ? this.personel.getSicilNo() : "";
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			sicilNo = "";
			logger.error(getId());
		}
		return sicilNo;
	}

	@Transient
	public HareketKGS getYeniHareket(Date yeniZaman, KapiView yeniYapiView) {
		HareketKGS hareket = new HareketKGS();
		Date zamanGiris = yeniZaman != null ? yeniZaman : this.zaman;
		hareket.setZaman(zamanGiris);
		hareket.setKapiView(yeniYapiView != null ? yeniYapiView : this.kapiView);
		hareket.setKapiId(hareket.getKapiView().getId());
		hareket.setPersonel(this.personel);
		hareket.setPersonelId(this.personelId);
		hareket.setDurum(this.durum);
		hareket.setOlusturmaZamani(zamanGiris);
		return hareket;
	}

	@Transient
	public String getAdSoyad() {
		String adSoyad = "";
		try {
			if (this.personel != null)
				adSoyad = this.personel.getAdSoyad();
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			adSoyad = "";
			logger.error(getId());
		}
		return adSoyad;
	}

	@Transient
	public boolean isPuantajOnayDurum() {
		return puantajOnayDurum;
	}

	public void setPuantajOnayDurum(boolean puantajOnayDurum) {
		this.puantajOnayDurum = puantajOnayDurum;
	}

	@Transient
	public void addYemekTeloreansZamani(int yemekMukerrerAraligi) {
		Date tarihDegeri = yemekMukerrerAraligi != 0 ? PdksUtil.addTarih(zaman, Calendar.MINUTE, yemekMukerrerAraligi) : zaman;
		yemekTeloreansZamani = tarihDegeri;
	}

	@Transient
	public Date getYemekTeloreansZamani() {
		return yemekTeloreansZamani;
	}

	public void setYemekTeloreansZamani(Date yemekTeloreansZamani) {
		this.yemekTeloreansZamani = yemekTeloreansZamani;
	}

	@Transient
	public Boolean getGecerliYemek() {
		return gecerliYemek;
	}

	public void setGecerliYemek(Boolean gecerliYemek) {
		this.gecerliYemek = gecerliYemek;
	}

	@Transient
	public int getGecerliYemekAdet() {
		return gecerliYemekAdet;
	}

	public void setGecerliYemekAdet(int gecerliYemekAdet) {
		this.gecerliYemekAdet = gecerliYemekAdet;
	}

	@Transient
	public Date getOncekiYemekZamani() {
		return oncekiYemekZamani;
	}

	public void setOncekiYemekZamani(Date oncekiYemekZamani) {
		this.oncekiYemekZamani = oncekiYemekZamani;
	}

	@Transient
	public HareketKGS getCikisHareket() {
		return cikisHareket;
	}

	public void setCikisHareket(HareketKGS cikisHareket) {
		this.cikisHareket = cikisHareket;
	}

	@Transient
	public HareketKGS getGirisHareket() {
		return girisHareket;
	}

	public void setGirisHareket(HareketKGS girisHareket) {
		this.girisHareket = girisHareket;
	}

	@Transient
	public boolean isCokluOgun() {
		return cokluOgun;
	}

	public void setCokluOgun(boolean cokluOgun) {
		this.cokluOgun = cokluOgun;
	}

	@Transient
	public boolean isGecerliDegil() {
		return gecerliDegil;
	}

	public void setGecerliDegil(boolean gecerliDegil) {
		this.gecerliDegil = gecerliDegil;
	}

	@Transient
	public boolean isManuelGiris() {
		boolean giris = false;
		if (kapiKGS == null && kapiView != null)
			kapiKGS = kapiView.getKapiKGS();
		if (kapiKGS != null && kapiKGS.isManuel()) {
			try {
				giris = islem == null || (islem.getNeden() != null && islem.getNeden().getDurum());
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		return giris;
	}

	@Transient
	public KapiView getTerminalKapi() {
		return terminalKapi;
	}

	public void setTerminalKapi(KapiView terminalKapi) {
		this.terminalKapi = terminalKapi;
	}

	@Transient
	public String getIslemYapan() {
		return islemYapan;
	}

	public void setIslemYapan(String islemYapan) {
		this.islemYapan = islemYapan;
	}

}