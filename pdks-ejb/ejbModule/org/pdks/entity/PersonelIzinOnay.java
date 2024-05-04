package org.pdks.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.pdks.security.entity.User;
import org.pdks.session.PdksUtil;
import org.pdks.session.OrtakIslemler;
import org.hibernate.Session;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = PersonelIzinOnay.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { PersonelIzinOnay.COLUMN_NAME_PERSONEL_IZIN_ID, PersonelIzinOnay.COLUMN_NAME_ONAYLAYAN_TIPI }) })
public class PersonelIzinOnay extends BaseObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7225587627236187304L;
	public static final String ONAYLAYAN_TIPI_YONETICI1 = "1";
	public static final String ONAYLAYAN_TIPI_YONETICI2 = "2";
	public static final String ONAYLAYAN_TIPI_IK = "3";
	public static final String TABLE_NAME = "PERSONELIZINONAY";
	public static final String COLUMN_NAME_PERSONEL_IZIN_ID = "PERSONEL_IZIN_ID";
	public static final String COLUMN_NAME_ONAYLAYAN_TIPI = "ONAYLAYAN_TIPI";

	public static final int ONAY_DURUM_ISLEM_YAPILMADI = 1;
	public static final int ONAY_DURUM_ONAYLANDI = 2;
	public static final int ONAY_DURUM_RED = 3;

	private int onayDurum;

	private Tanim onaylamamaNeden;

	private String onaylamamaNedenAciklama;

	private Integer version = 0;

	private PersonelIzin personelIzin;

	private String onaylayanTipi;

	private String kayitDurum = "";

	private boolean tekrarOnayla;

	private Personel yeniOnayPersonel = null;

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = COLUMN_NAME_PERSONEL_IZIN_ID, nullable = false)
	@Fetch(FetchMode.JOIN)
	public PersonelIzin getPersonelIzin() {
		return personelIzin;
	}

	public void setPersonelIzin(PersonelIzin personelIzin) {
		this.personelIzin = personelIzin;
	}

	@Column(name = COLUMN_NAME_ONAYLAYAN_TIPI, nullable = false, length = 1)
	public String getOnaylayanTipi() {
		return onaylayanTipi;
	}

	public void setOnaylayanTipi(String onaylayanTipi) {
		this.onaylayanTipi = onaylayanTipi;
	}

	@Column(name = "VERSION")
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@Transient
	public String getOnayDurumAciklama(OrtakIslemler ortakIslemler, Session session) {
		if (getOnayDurum() == PersonelIzinOnay.ONAY_DURUM_ISLEM_YAPILMADI)
			return "Onay Bekliyor";
		else if (getOnayDurum() == PersonelIzinOnay.ONAY_DURUM_ONAYLANDI)
			return "Onaylandı";
		else if (getOnayDurum() == PersonelIzinOnay.ONAY_DURUM_RED) {
			User user = personelIzin.getGuncelleyenUser();
			ortakIslemler.setUserRoller(user, session);
			String durum = user.isIK() || onaylayanTipi.equals(ONAYLAYAN_TIPI_IK) ? "İptal Edildi" : "Red Edildi";
			return durum;
		}

		return "Onay Durum Bilinmiyor";
	}

	@Column(name = "ONAY_DURUM")
	public int getOnayDurum() {
		return onayDurum;
	}

	public void setOnayDurum(int onayDurum) {
		this.onayDurum = onayDurum;
	}

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = "ONAYLAMAMA_NEDEN_ID")
	@Fetch(FetchMode.JOIN)
	public Tanim getOnaylamamaNeden() {
		return onaylamamaNeden;
	}

	public void setOnaylamamaNeden(Tanim onaylamamaNeden) {
		this.onaylamamaNeden = onaylamamaNeden;
	}

	@Column(name = "ONAYLAYAN_NEDEN_ACIKLAMA", length = 256)
	public String getOnaylamamaNedenAciklama() {
		return onaylamamaNedenAciklama;
	}

	public void setOnaylamamaNedenAciklama(String onaylamamaNedenAciklama) {
		this.onaylamamaNedenAciklama = onaylamamaNedenAciklama;
	}

	@Transient
	public User getOnaylayan() {
		return this.getGuncelleyenUser();
	}

	public void setOnaylayan(User guncelleyenUser) {
		this.setGuncelleyenUser(guncelleyenUser);
	}

	@Transient
	public boolean isOnayBekliyor() {
		return onayDurum == ONAY_DURUM_ISLEM_YAPILMADI;

	}

	@Transient
	public Long getIzinId() {
		return personelIzin.getId();
	}

	@Transient
	public String getKayitDurum() {
		return kayitDurum;
	}

	public void setKayitDurum(String kayitDurum) {
		this.kayitDurum = kayitDurum;
	}

	@Transient
	public String getAkisDurum() {
		return PdksUtil.getMessageBundleMessage("izin.etiket.onaylayan" + onaylayanTipi);
	}

	@Transient
	public boolean isBakiyeDurum() {
		return personelIzin != null && personelIzin.getPersonelIzinler() != null && !personelIzin.getPersonelIzinler().isEmpty();
	}

	@Transient
	public boolean isTekrarOnayla() {
		return tekrarOnayla;
	}

	public void setTekrarOnayla(boolean tekrarOnayla) {
		this.tekrarOnayla = tekrarOnayla;
	}

	@Transient
	public Personel getYeniOnayPersonel() {
		if (yeniOnayPersonel == null)
			yeniOnayPersonel = yeniOnayPersonelBul();
		return yeniOnayPersonel;
	}

	public void setYeniOnayPersonel(Personel yeniOnayPersonel) {
		this.yeniOnayPersonel = yeniOnayPersonel;
	}

	@Transient
	public Personel yeniOnayPersonelBul() {
		Personel yeniYonetici = null;
		if (onaylayanTipi != null && guncelleyenUser != null) {
			Personel yonetici = null;
			Personel izinSahibi = personelIzin != null ? personelIzin.getIzinSahibi() : null;
			if (izinSahibi != null) {
				if (onaylayanTipi.equals(ONAYLAYAN_TIPI_YONETICI1)) {
					yonetici = izinSahibi.getPdksYonetici();
				} else if (onaylayanTipi.equals(ONAYLAYAN_TIPI_YONETICI2)) {
					try {
						if (izinSahibi.getYonetici2() != null)
							yonetici = izinSahibi.getYonetici2();
						else if (izinSahibi.getPdksYonetici() != null)
							yonetici = izinSahibi.getPdksYonetici().getPdksYonetici();
					} catch (Exception e) {
					 
					}

				}
			}
			if (yonetici != null && !guncelleyenUser.getPdksPersonel().getId().equals(yonetici.getId()))
				yeniYonetici = yonetici;
		}

		return yeniYonetici;
	}

	public void entityRefresh() {
		// TODO entityRefresh
		
	}
}
