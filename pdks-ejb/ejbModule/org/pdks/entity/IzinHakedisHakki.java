package org.pdks.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = IzinHakedisHakki.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { IzinHakedisHakki.COLUMN_NAME_DEPARTMAN, IzinHakedisHakki.COLUMN_NAME_YAS_TIPI, IzinHakedisHakki.COLUMN_NAME_KIDEM_YILI, IzinHakedisHakki.COLUMN_NAME_SUA_DURUM }) })
public class IzinHakedisHakki extends BaseObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4106637401187795084L;
	public static final int YAS_TIPI_COCUK = 0;
	public static final int YAS_TIPI_GENC = 1;
	public static final int YAS_TIPI_YASLI = 2;
	public static final String TABLE_NAME = "IZINHAKEDISHAKKI";
	public static final String COLUMN_NAME_DEPARTMAN = "DEPARTMAN";
	public static final String COLUMN_NAME_YAS_TIPI = "YAS_TIPI";
	public static final String COLUMN_NAME_KIDEM_YILI = "KIDEMYILI";
	public static final String COLUMN_NAME_SUA_DURUM = "SUA_DURUM";

	private int kidemYili, izinSuresi, minGun = 0, maxGun = 0;

	private boolean suaDurum = false;

	private Integer version = 0;

	private Departman departman;

	private Integer yasTipi = YAS_TIPI_GENC;

	@Column(name = "VERSION")
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@Column(name = COLUMN_NAME_KIDEM_YILI, nullable = false)
	public int getKidemYili() {
		return kidemYili;
	}

	public void setKidemYili(int kidemYili) {
		this.kidemYili = kidemYili;
	}

	@Column(name = "IZIN_SURESI")
	public int getIzinSuresi() {
		return izinSuresi;
	}

	public void setIzinSuresi(int izinSuresi) {
		this.izinSuresi = izinSuresi;
	}

	@Column(name = COLUMN_NAME_YAS_TIPI, nullable = false)
	public Integer getYasTipi() {
		return yasTipi;
	}

	public void setYasTipi(Integer yasTipi) {
		this.yasTipi = yasTipi;
	}

	@Column(name = COLUMN_NAME_SUA_DURUM)
	public boolean isSuaDurum() {
		return suaDurum;
	}

	public void setSuaDurum(boolean suaDurum) {
		this.suaDurum = suaDurum;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_DEPARTMAN, nullable = false)
	@Fetch(FetchMode.JOIN)
	public Departman getDepartman() {
		return departman;
	}

	public void setDepartman(Departman departman) {
		this.departman = departman;
	}

	@Transient
	public int getMinGun() {
		return minGun;
	}

	public void setMinGun(int minGun) {
		this.minGun = minGun;
	}

	@Transient
	public int getMaxGun() {
		return maxGun;
	}

	public void setMaxGun(int maxGun) {
		this.maxGun = maxGun;
	}

	@Transient
	public static String getYasTipiStr(int tipi, Departman departman, boolean sua) {
		String mesaj = sua ? "ŞUA " : "";
		switch (tipi) {
		case YAS_TIPI_COCUK:
			mesaj = "0 - " + departman.getCocukYasUstSiniri();
			break;

		case YAS_TIPI_GENC:
			mesaj = (departman.getCocukYasUstSiniri() + 1) + " - " + (departman.getYasliYasAltSiniri() - 1);
			break;

		case YAS_TIPI_YASLI:
			mesaj = (departman.getYasliYasAltSiniri()) + " - ";
			break;
		}
		return mesaj;
	}

	@Transient
	public static String getHakedisKey(int kidemYil, int yasTipi, boolean suaDurum, Departman departman) {
		String hakedisKey = departman.getDepartmanTanim().getKodu() + "_" + (suaDurum ? 1 : 0) + "_" + kidemYil + "_" + yasTipi;
		return hakedisKey;
	}

	@Transient
	public String getHakedisKey() {
		String hakedisKey = getHakedisKey(kidemYili, yasTipi, suaDurum, departman);
		return hakedisKey;
	}

	@Transient
	public String getYasTipiStr() {
		return getYasTipiStr(yasTipi, departman, this.isSuaDurum());
	}

	@Transient
	public String getSort() {
		String sira = departman.getDepartmanTanim().getAciklama() + departman.getId() + "_" + this.isSuaDurum() + (((yasTipi + 1) * 100) + izinSuresi);
		return sira;
	}

	public void entityRefresh() {
		// TODO entityRefresh
		
	}

}
