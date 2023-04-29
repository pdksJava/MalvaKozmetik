package org.pdks.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import javax.faces.model.SelectItem;
import javax.persistence.Transient;

import org.pdks.security.entity.User;

public class AramaSecenekleri implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2081976296129705305L;

	private String ad = "", soyad = "", sicilNo = "";

	private Long sirketId, departmanId, tesisId, ekSaha1Id, ekSaha2Id, ekSaha3Id, ekSaha4Id;

	private List<Sirket> sirketList;

	private List<Tanim> tesisTanimList;

	private List<SelectItem> sirketIdList, departmanIdList, gorevYeriList, tesisList, altBolumIdList;

	private HashMap<String, List<SelectItem>> ekSahaSelectListMap;

	private HashMap<String, List<Tanim>> ekSahaListMap;

	private TreeMap<String, Tanim> ekSahaTanimMap;

	private Boolean stajyerOlmayanSirket = Boolean.TRUE, sessionClear = Boolean.TRUE, sirketIzinKontrolYok = Boolean.TRUE;

	private Tanim ekSaha1, ekSaha2, ekSaha3, ekSaha4;

	private User user;

	private Sirket sirket;

	private Departman departman;

	private boolean yetkiliPersoneller = false;

	public AramaSecenekleri() {
		super();

	}

	public AramaSecenekleri(User user, boolean yetki) {
		super();
		this.user = user;
		this.yetkiliPersoneller = yetki;
		if (user != null) {
			if (!(user.isAdmin() || user.isIKAdmin())) {
				departmanId = user.getDepartman().getId();
				if (!user.isIK())
					sirketId = user.getPdksPersonel().getSirket().getId();

			}
			if (user.isYoneticiKontratli()) {
				if (!(user.isIK() || user.isAdmin()))
					sirketId = null;
			}
		}

	}

	public AramaSecenekleri(User user) {
		super();
		this.user = user;
		if (user != null) {
			if (!(user.isAdmin() || user.isIKAdmin())) {
				departmanId = user.getDepartman().getId();
				if (!user.isIK())
					sirketId = user.getPdksPersonel().getSirket().getId();

			}
			if (user.isYoneticiKontratli()) {
				if (!(user.isIK() || user.isAdmin()))
					sirketId = null;
			}
		}

	}

	public String getAd() {
		return ad;
	}

	public void setAd(String ad) {
		this.ad = ad;
	}

	public String getSoyad() {
		return soyad;
	}

	public void setSoyad(String soyad) {
		this.soyad = soyad;
	}

	public String getSicilNo() {
		return sicilNo;
	}

	public void setSicilNo(String sicilNo) {
		this.sicilNo = sicilNo;
	}

	public Long getSirketId() {
		return sirketId;
	}

	public void setSirketId(Long sirketId) {
		this.sirketId = sirketId;
	}

	public Long getDepartmanId() {
		return departmanId;
	}

	public void setDepartmanId(Long departmanId) {
		this.departmanId = departmanId;
	}

	public Long getEkSaha1Id() {
		return ekSaha1Id;
	}

	public void setEkSaha1Id(Long ekSaha1Id) {
		this.ekSaha1Id = ekSaha1Id;
	}

	public Long getEkSaha2Id() {
		return ekSaha2Id;
	}

	public void setEkSaha2Id(Long ekSaha2Id) {
		this.ekSaha2Id = ekSaha2Id;
	}

	public Long getEkSaha3Id() {
		return ekSaha3Id;
	}

	public void setEkSaha3Id(Long ekSaha3Id) {
		this.ekSaha3Id = ekSaha3Id;
	}

	public Long getEkSaha4Id() {
		return ekSaha4Id;
	}

	public void setEkSaha4Id(Long ekSaha4Id) {
		this.ekSaha4Id = ekSaha4Id;
	}

	public List<SelectItem> getSirketIdList() {
		return sirketIdList;
	}

	public void setSirketIdList(List<SelectItem> sirketIdList) {
		this.sirketIdList = sirketIdList;
	}

	public List<SelectItem> getDepartmanIdList() {
		return departmanIdList;
	}

	public void setDepartmanIdList(List<SelectItem> departmanIdList) {
		this.departmanIdList = departmanIdList;
	}

	public HashMap<String, List<SelectItem>> getEkSahaSelectListMap() {
		return ekSahaSelectListMap;
	}

	public void setEkSahaSelectListMap(HashMap<String, List<SelectItem>> ekSahaSelectListMap) {
		this.ekSahaSelectListMap = ekSahaSelectListMap;
	}

	public TreeMap<String, Tanim> getEkSahaTanimMap() {
		return ekSahaTanimMap;
	}

	public void setEkSahaTanimMap(TreeMap<String, Tanim> ekSahaTanimMap) {
		this.ekSahaTanimMap = ekSahaTanimMap;
	}

	public HashMap<String, List<Tanim>> getEkSahaListMap() {
		return ekSahaListMap;
	}

	public void setEkSahaListMap(HashMap<String, List<Tanim>> ekSahaListMap) {
		this.ekSahaListMap = ekSahaListMap;
	}

	public List<Sirket> getSirketList() {
		return sirketList;
	}

	public void setSirketList(List<Sirket> value) {
		this.sirketList = value;
	}

	public Boolean getStajyerOlmayanSirket() {
		return stajyerOlmayanSirket;
	}

	public void setStajyerOlmayanSirket(Boolean stajyerOlmayanSirket) {
		this.stajyerOlmayanSirket = stajyerOlmayanSirket;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Boolean getSessionClear() {
		return sessionClear;
	}

	public void setSessionClear(Boolean sessionClear) {
		this.sessionClear = sessionClear;
	}

	public Sirket getSirket() {
		return sirket;
	}

	public void setSirket(Sirket value) {
		if (value != null)
			this.departman = value.getDepartman();
		this.sirket = value;
	}

	public Departman getDepartman() {
		return departman;
	}

	public void setDepartman(Departman departman) {
		this.departman = departman;
	}

	public List<SelectItem> getGorevYeriList() {
		return gorevYeriList;
	}

	public void setGorevYeriList(List<SelectItem> gorevYeriList) {
		this.gorevYeriList = gorevYeriList;
	}

	@Transient
	public Object clone() {
		try {

			return super.clone();
		} catch (CloneNotSupportedException e) {
			// bu class cloneable oldugu icin buraya girilmemeli...
			throw new InternalError();
		}
	}

	public Tanim getEkSaha1() {
		if (ekSaha1 == null && ekSahaTanimMap != null && ekSahaTanimMap.containsKey("ekSaha1"))
			ekSaha1 = ekSahaTanimMap.get("ekSaha1");
		return ekSaha1;
	}

	public void setEkSaha1(Tanim ekSaha1) {
		this.ekSaha1 = ekSaha1;
	}

	public Tanim getEkSaha2() {
		if (ekSaha2 == null && ekSahaTanimMap != null && ekSahaTanimMap.containsKey("ekSaha2"))
			ekSaha2 = ekSahaTanimMap.get("ekSaha2");
		return ekSaha2;
	}

	public void setEkSaha2(Tanim ekSaha2) {
		this.ekSaha2 = ekSaha2;
	}

	public Tanim getEkSaha3() {
		if (ekSaha3 == null && ekSahaTanimMap != null && ekSahaTanimMap.containsKey("ekSaha3"))
			ekSaha3 = ekSahaTanimMap.get("ekSaha3");
		return ekSaha3;
	}

	public void setEkSaha3(Tanim ekSaha3) {
		this.ekSaha3 = ekSaha3;
	}

	public Tanim getEkSaha4() {
		if (ekSaha4 == null && ekSahaTanimMap != null && ekSahaTanimMap.containsKey("ekSaha4"))
			ekSaha4 = ekSahaTanimMap.get("ekSaha4");

		return ekSaha4;
	}

	public void setEkSaha4(Tanim ekSaha4) {
		this.ekSaha4 = ekSaha4;
	}

	public List<SelectItem> getTesisList() {
		return tesisList;
	}

	public void setTesisList(List<SelectItem> tesisList) {
		this.tesisList = tesisList;
	}

	public Long getTesisId() {
		return tesisId;
	}

	public void setTesisId(Long tesisId) {
		this.tesisId = tesisId;
	}

	public List<Tanim> getTesisTanimList() {
		return tesisTanimList;
	}

	public void setTesisTanimList(List<Tanim> tesisTanimList) {
		this.tesisTanimList = tesisTanimList;
	}

	public List<SelectItem> getSelectEkSahaList(String ekSaha) {
		List<SelectItem> list = null;
		if (ekSaha != null && ekSahaSelectListMap != null && ekSahaSelectListMap.containsKey(ekSaha))
			list = ekSahaSelectListMap.get(ekSaha);
		if (list == null)
			list = new ArrayList<SelectItem>();
		return list;
	}

	public Boolean getSirketIzinKontrolYok() {
		return sirketIzinKontrolYok;
	}

	public void setSirketIzinKontrolYok(Boolean sirketIzinKontrolYok) {
		this.sirketIzinKontrolYok = sirketIzinKontrolYok;
	}

	public List<SelectItem> getAltBolumIdList() {
		return altBolumIdList;
	}

	public void setAltBolumIdList(List<SelectItem> altBolumIdList) {
		this.altBolumIdList = altBolumIdList;
	}

	public boolean isYetkiliPersoneller() {
		return yetkiliPersoneller;
	}

	public void setYetkiliPersoneller(boolean yetkiliPersoneller) {
		this.yetkiliPersoneller = yetkiliPersoneller;
	}

}
