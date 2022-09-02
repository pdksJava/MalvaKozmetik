package com.pdks.webService;

import java.util.List;

public class IzinHakedis extends BaseERPObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -338414968530949769L;

	protected String personelNo;

	protected String kidemBaslangicTarihi;

	protected List<IzinHakedisDetay> hakedisList;

	public String getPersonelNo() {
		return personelNo;
	}

	public void setPersonelNo(String personelNo) {
		this.personelNo = personelNo;
	}

	public String getKidemBaslangicTarihi() {
		return kidemBaslangicTarihi;
	}

	public void setKidemBaslangicTarihi(String kidemBaslangicTarihi) {
		this.kidemBaslangicTarihi = kidemBaslangicTarihi;
	}

	public List<IzinHakedisDetay> getHakedisList() {
		return hakedisList;
	}

	public void setHakedisList(List<IzinHakedisDetay> hakedisList) {
		this.hakedisList = hakedisList;
	}

}
