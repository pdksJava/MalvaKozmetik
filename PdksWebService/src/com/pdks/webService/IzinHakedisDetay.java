package com.pdks.webService;

import java.util.List;

public class IzinHakedisDetay extends BaseERPObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5551111998947506181L;
	
	protected String hakEdisTarihi;
	protected int kidemYil;
	protected double izinSuresi;
	protected List<IzinERP> kullanilanIzinler;
	
	public String getHakEdisTarihi() {
		return hakEdisTarihi;
	}
	public void setHakEdisTarihi(String hakEdisTarihi) {
		this.hakEdisTarihi = hakEdisTarihi;
	}
	public int getKidemYil() {
		return kidemYil;
	}
	public void setKidemYil(int kidemYil) {
		this.kidemYil = kidemYil;
	}
	public double getIzinSuresi() {
		return izinSuresi;
	}
	public void setIzinSuresi(double izinSuresi) {
		this.izinSuresi = izinSuresi;
	}
	public List<IzinERP> getKullanilanIzinler() {
		return kullanilanIzinler;
	}
	public void setKullanilanIzinler(List<IzinERP> kullanilanIzinler) {
		this.kullanilanIzinler = kullanilanIzinler;
	}

}
