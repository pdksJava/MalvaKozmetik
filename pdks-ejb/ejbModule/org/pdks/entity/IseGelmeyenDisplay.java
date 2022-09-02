package org.pdks.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.pdks.session.PdksUtil;


public class IseGelmeyenDisplay implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8706812169473847748L;
	private IzinTipi izinTipi;
	private int personelSayisi;
	private Departman departman;
	private String mesaj;
	private Date date;
	private List<Personel> personelList = new ArrayList<Personel>();
	
	public Departman getDepartman() {
		return departman;
	}

	public void setDepartman(Departman departman) {
		this.departman = departman;
	}

	

	public String getMesaj() {
		return mesaj;
	}

	public void setMesaj(String mesaj) {
		mesaj = PdksUtil.convertUTF8(mesaj);
		this.mesaj = mesaj;
	}

	public IzinTipi getIzinTipi() {
		return izinTipi;
	}

	public void setIzinTipi(IzinTipi izinTipi) {
		this.izinTipi = izinTipi;
	}

	public int getPersonelSayisi() {
		return personelSayisi;
	}

	public void setPersonelSayisi(int personelSayisi) {
		this.personelSayisi = personelSayisi;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public List<Personel> getPersonelList() {
		return personelList;
	}

	public void setPersonelList(List<Personel> personelList) {
		this.personelList = personelList;
	}

	

	
}
