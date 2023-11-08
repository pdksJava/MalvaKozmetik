package org.pdks.entity;

import java.util.Date;
import java.util.TreeMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.pdks.security.entity.User;
import org.pdks.session.PdksUtil;

@Entity(name = YemekIzin.TABLE_NAME)
public class YemekIzin extends BaseObject {
	// seam-gen attributes (you should probably edit these)

	/**
	 * 
	 */
	private static final long serialVersionUID = -2645411245304870287L;
	public static final String TABLE_NAME = "YEMEKIZIN";
	public static final String COLUMN_NAME_BAS_TARIHI = "BAS_TARIHI";
	public static final String COLUMN_NAME_BIT_TARIHI = "BIT_TARIHI";
	private String yemekAciklama = "";
	private int baslangicSaat, baslangicDakika, bitisSaat, bitisDakika, maxSure = 30;
	private Boolean ozelMola = Boolean.FALSE;
	private Integer version = 0;
	private Date basTarih = PdksUtil.convertToJavaDate((PdksUtil.getSistemBaslangicYili() * 100 + 1) + "01", "yyyyMMdd"), bitTarih = PdksUtil.getSonSistemTarih();
	// private Set<VardiyaYemekIzin> vardiyaList;
	private TreeMap<Long, Vardiya> vardiyaMap;

	@Column(name = "VERSION")
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@Column(name = "ACIKLAMA")
	public String getYemekAciklama() {
		return yemekAciklama;
	}

	public void setYemekAciklama(String yemekAciklama) {
		this.yemekAciklama = yemekAciklama;
	}

	@Column(name = "BAS_SAAT")
	public int getBaslangicSaat() {
		return baslangicSaat;
	}

	public void setBaslangicSaat(int baslangicSaat) {
		this.baslangicSaat = baslangicSaat;
	}

	@Column(name = "BAS_DAKIKA")
	public int getBaslangicDakika() {
		return baslangicDakika;
	}

	public void setBaslangicDakika(int baslangicDakika) {
		this.baslangicDakika = baslangicDakika;
	}

	@Column(name = "BIT_SAAT")
	public int getBitisSaat() {
		return bitisSaat;
	}

	public void setBitisSaat(int bitisSaat) {
		this.bitisSaat = bitisSaat;
	}

	@Column(name = "BIT_DAKIKA")
	public int getBitisDakika() {
		return bitisDakika;
	}

	public void setBitisDakika(int bitisDakika) {
		this.bitisDakika = bitisDakika;
	}

	@Column(name = "MAX_SURE")
	public int getMaxSure() {
		return maxSure;
	}

	public void setMaxSure(int maxSure) {
		this.maxSure = maxSure;
	}

	@Column(name = "OZEL_MOLA")
	public Boolean getOzelMola() {
		return ozelMola;
	}

	public void setOzelMola(Boolean ozelMola) {
		this.ozelMola = ozelMola;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = COLUMN_NAME_BAS_TARIHI)
	public Date getBasTarih() {
		return basTarih;
	}

	public void setBasTarih(Date basTarih) {
		this.basTarih = basTarih;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = COLUMN_NAME_BIT_TARIHI)
	public Date getBitTarih() {
		return bitTarih;
	}

	public void setBitTarih(Date bitTarih) {
		this.bitTarih = bitTarih;
	}

	// @OneToMany(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER, mappedBy = "yemekIzin", targetEntity = VardiyaYemekIzin.class)
	// public Set<VardiyaYemekIzin> getVardiyaList() {
	// return vardiyaList;
	// }

	// public void setVardiyaList(Set<VardiyaYemekIzin> tags) {
	// if (this.vardiyaList != null) {
	// this.vardiyaList.clear();
	// if (tags != null) {
	// for (VardiyaYemekIzin vardiyaYemekIzin : tags) {
	// this.vardiyaList.add(vardiyaYemekIzin);
	// }
	// }
	// if (vardiyaList != null && !vardiyaList.isEmpty())
	// this.vardiyaMap = null;
	// } else
	// this.vardiyaList = tags;
	// }

	@Transient
	public String getBasKey() {
		int deger = 10000 + (baslangicSaat * 100) + baslangicDakika;
		String key = String.valueOf(deger).substring(1);
		return key;
	}

	@Transient
	public long getYemekNumeric() {
		long yemekNumeric = (baslangicSaat * 1000000) + (baslangicDakika * 10000) + (bitisSaat * 100) + bitisDakika;
		return yemekNumeric;
	}

	@Transient
	public String getBitKey() {
		int deger = 10000 + (bitisSaat * 100) + bitisDakika;
		String key = String.valueOf(deger).substring(1);
		return key;
	}

	@Transient
	public Boolean containsKey(Long vardiyaId) {
		boolean vardiyaKontrol = vardiyaId == null || vardiyaMap == null;
		if (!vardiyaKontrol)
			vardiyaKontrol = vardiyaMap.containsKey(vardiyaId);
		return vardiyaKontrol;
	}

	@Transient
	public Date getBasZaman() {
		Date zaman = User.getTime(baslangicSaat, baslangicDakika);
		return zaman;
	}

	@Transient
	public Date getBitZaman() {
		Date zaman = User.getTime(bitisSaat, bitisDakika);
		return zaman;
	}

	@Transient
	public boolean isOzelMolaVarmi() {
		boolean var = ozelMola != null && ozelMola;
		if (var)
			var = vardiyaMap != null;

		return var;
	}

	@Transient
	public TreeMap<Long, Vardiya> getVardiyaMap() {
		return vardiyaMap;
	}

	public void setVardiyaMap(TreeMap<Long, Vardiya> vardiyaMap) {
		this.vardiyaMap = vardiyaMap;
	}

}
