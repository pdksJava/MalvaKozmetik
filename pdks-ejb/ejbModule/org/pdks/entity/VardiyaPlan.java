package org.pdks.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.log4j.Logger;

public class VardiyaPlan extends BaseObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3733637996158688725L;
	static Logger logger = Logger.getLogger(VardiyaPlan.class);

	private Personel personel;

	private VardiyaHafta vardiyaHafta1, vardiyaHafta2, vardiyaHafta3, vardiyaHafta4, vardiyaHafta5, vardiyaHafta6;

	private DepartmanDenklestirmeDonemi denklestirmeDonemi;

	private List<VardiyaHafta> vardiyaHaftaList = new ArrayList<VardiyaHafta>();

	private TreeMap<String, VardiyaGun> vardiyaGunMap = new TreeMap<String, VardiyaGun>();

	private List<PersonelDenklestirmeTasiyici> personelDenklestirmeList;

	private double hesaplananSure = 0;

	public void planVardiyaPlanSifirla() {
		personelDenklestirmeList = new ArrayList<PersonelDenklestirmeTasiyici>();
		for (int i = 0; i < 3; i++)
			personelDenklestirmeList.add(new PersonelDenklestirmeTasiyici());
	}

	public Personel getPersonel() {
		return personel;
	}

	public void setPersonel(Personel personel) {
		this.personel = personel;
	}

	public VardiyaHafta getVardiyaHafta1() {
		return vardiyaHafta1;
	}

	public void setVardiyaHafta1(VardiyaHafta vardiyaHafta1) {
		this.vardiyaHafta1 = vardiyaHafta1;
	}

	public VardiyaHafta getVardiyaHafta2() {
		return vardiyaHafta2;
	}

	public void setVardiyaHafta2(VardiyaHafta vardiyaHafta2) {
		this.vardiyaHafta2 = vardiyaHafta2;
	}

	public List<VardiyaHafta> getVardiyaHaftaList() {
		return vardiyaHaftaList;
	}

	public void setVardiyaHaftaList(List<VardiyaHafta> vardiyaHaftaList) {
		this.vardiyaHaftaList = vardiyaHaftaList;
	}

	public boolean isDurum() {
		return denklestirmeDonemi == null || denklestirmeDonemi.isAcik();
	}

	public double getHesaplananSure() {
		return hesaplananSure;
	}

	public void setHesaplananSure(double hesaplananSure) {
		this.hesaplananSure = hesaplananSure;
	}

	public DepartmanDenklestirmeDonemi getDenklestirmeDonemi() {
		return denklestirmeDonemi;
	}

	public void setDenklestirmeDonemi(DepartmanDenklestirmeDonemi denklestirmeDonemi) {
		this.denklestirmeDonemi = denklestirmeDonemi;
	}

	public TreeMap<String, VardiyaGun> getVardiyaGunMap() {
		return vardiyaGunMap;
	}

	public void setVardiyaGunMap(TreeMap<String, VardiyaGun> vardiyaGunMap) {
		this.vardiyaGunMap = vardiyaGunMap;
	}

	public String getSortKey() {
		return personel != null ? personel.getAdSoyad() + "_" + personel.getSicilNo() : "";
	}

	public Double getPersonelDenklestirme(int index) {
		Double denklestirmeTutar = null;
		try {
			if (index <= personelDenklestirmeList.size())
				denklestirmeTutar = personelDenklestirmeList.get(index).getCalisilanFark();
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}
		return denklestirmeTutar;
	}

	public Double getPersonelDenklestirmeToplam(int index) {
		Double denklestirmeTutar = null;
		try {
			if (index <= personelDenklestirmeList.size()) {
				denklestirmeTutar = 0D;
				for (int i = 0; i <= index; i++)
					denklestirmeTutar += personelDenklestirmeList.get(i).getCalisilanFark();
			}
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}
		return denklestirmeTutar;
	}

	public void setPersonelDenklestirme(PersonelDenklestirmeTasiyici denklestirme, int index) {
		try {
			personelDenklestirmeList.set(index, denklestirme);
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}
	}

	public List<PersonelDenklestirmeTasiyici> getPersonelDenklestirmeList() {
		return personelDenklestirmeList;
	}

	public void setPersonelDenklestirmeList(List<PersonelDenklestirmeTasiyici> personelDenklestirmeList) {
		this.personelDenklestirmeList = personelDenklestirmeList;
	}

	public VardiyaHafta getVardiyaHafta3() {
		return vardiyaHafta3;
	}

	public void setVardiyaHafta3(VardiyaHafta vardiyaHafta3) {
		this.vardiyaHafta3 = vardiyaHafta3;
	}

	public VardiyaHafta getVardiyaHafta4() {
		return vardiyaHafta4;
	}

	public void setVardiyaHafta4(VardiyaHafta vardiyaHafta4) {
		this.vardiyaHafta4 = vardiyaHafta4;
	}

	public VardiyaHafta getVardiyaHafta5() {
		return vardiyaHafta5;
	}

	public void setVardiyaHafta5(VardiyaHafta vardiyaHafta5) {
		this.vardiyaHafta5 = vardiyaHafta5;
	}

	public VardiyaHafta getVardiyaHafta6() {
		return vardiyaHafta6;
	}

	public void setVardiyaHafta6(VardiyaHafta vardiyaHafta6) {
		this.vardiyaHafta6 = vardiyaHafta6;
	}

}
