package org.pdks.entity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.pdks.security.entity.User;
import org.pdks.session.PdksUtil;

public class PersonelDenklestirmeTasiyici extends BaseObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8351132789218377364L;
	static Logger logger = Logger.getLogger(PersonelDenklestirmeTasiyici.class);

	public static final String MESAI_ODEME_TANIMSIZ = "";
	public static final String MESAI_ODEME_SEKLI_PARA = "P";
	public static final String MESAI_ODEME_SEKLI_IZIN = "I";
	public static final String LGART_FAZLA_MESAI = "1510";
	public static final String LGART_RESMI_TATIL_MESAI = "1530";
	public static final String LGART_AKSAM_GUN_CALISMA = "3265";
	public static final String LGART_AKSAM_SAAT_CALISMA = "3268";
	public static final String MESAI_ODEME_SEKLI_IZIN_PARA = "A";

	private DepartmanDenklestirmeDonemi denklestirmeDonemi;

	private Personel personel;

	private Double normalFazlaMesai = (double) 0, normalFazlaMesaiPara = (double) 0, resmiTatilMesai = (double) 0;

	private double toplamCalisilacakZaman = 0, toplamCalisilanZaman = 0, toplamEgitim = 0, toplamRaporIzni = 0;

	private String mesaiOdemeSekli = MESAI_ODEME_TANIMSIZ, trClass = "";

	private PersonelIzin izin;

	private int denklestirmeHaftasi = 0;

	private Boolean mesaiSapDurum = Boolean.FALSE, partTime = Boolean.FALSE;
	private int yarimYuvarla = PdksUtil.getYarimYuvarlaLast();
	private TreeMap<String, Integer> genelHaftaMap;
	private TreeMap<Integer, TreeMap> vardiyaHaftaMap;
	private TreeMap<String, VardiyaGun> vardiyaGunleriMap;
	private List<VardiyaGun> vardiyalar;
	private ArrayList<PersonelDenklestirmeTasiyici> personelDenklestirmeleri;
	private boolean goster = Boolean.FALSE;
	private Object sapTable;
	private VardiyaGun sonVardiyaGun, oncekiVardiyaGun;
	private CalismaModeli calismaModeli;
	private DenklestirmeAy denklestirmeAy;
	private CalismaModeliAy calismaModeliAy;
	private Integer version = 0;

	public PersonelDenklestirmeTasiyici(AylikPuantaj ap) {
		super();
		PersonelDenklestirme pd = ap.getPersonelDenklestirme();
		if (pd != null) {
			this.setCalismaModeliAy(pd.getCalismaModeliAy());
			this.setDenklestirmeAy(pd.getDenklestirmeAy());
		}
		this.setCalismaModeli(ap.getCalismaModeli());
		this.setPersonel(ap.getPdksPersonel());
	}

	public PersonelDenklestirmeTasiyici() {
		super();
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public DepartmanDenklestirmeDonemi getDenklestirmeDonemi() {
		return denklestirmeDonemi;
	}

	public void setDenklestirmeDonemi(DepartmanDenklestirmeDonemi denklestirmeDonemi) {
		this.denklestirmeDonemi = denklestirmeDonemi;
	}

	public Personel getPersonel() {
		return personel;
	}

	public void setPersonel(Personel personel) {
		this.personel = personel;
	}

	public String getMesaiOdemeSekli() {
		return mesaiOdemeSekli;
	}

	public void setMesaiOdemeSekli(String mesaiOdemeSekli) {
		this.mesaiOdemeSekli = mesaiOdemeSekli;
	}

	public PersonelIzin getIzin() {
		return izin;
	}

	public void setIzin(PersonelIzin izin) {
		this.izin = izin;
	}

	public Boolean getMesaiSapDurum() {
		return mesaiSapDurum;
	}

	public void setMesaiSapDurum(Boolean mesaiSapDurum) {
		this.mesaiSapDurum = mesaiSapDurum;
	}

	public Boolean getPartTime() {
		return partTime;
	}

	public void setPartTime(Boolean partTime) {
		this.partTime = partTime;
	}

	public Double getResmiTatilMesai() {
		return resmiTatilMesai;
	}

	public void setResmiTatilMesai(Double resmiTatilMesai) {
		this.resmiTatilMesai = resmiTatilMesai;
	}

	public Double getNormalFazlaMesai() {
		return normalFazlaMesai;
	}

	public void setNormalFazlaMesai(Double normalFazlaMesai) {
		this.normalFazlaMesai = normalFazlaMesai;
	}

	public Double getNormalFazlaMesaiPara() {
		return normalFazlaMesaiPara;
	}

	public void setNormalFazlaMesaiPara(Double normalFazlaMesaiPara) {
		this.normalFazlaMesaiPara = normalFazlaMesaiPara;
	}

	public String getMesaiOdemeSekliAciklama() {
		String key = "";
		if (mesaiOdemeSekli.equals(MESAI_ODEME_TANIMSIZ))
			key = "puantaj.etiket.mesaiOdemeSekliYok";
		else if (mesaiOdemeSekli.equals(MESAI_ODEME_SEKLI_IZIN))
			key = "puantaj.etiket.mesaiOdemeSekliIzin";
		else if (mesaiOdemeSekli.equals(MESAI_ODEME_SEKLI_PARA))
			key = "puantaj.etiket.mesaiOdemeSekliPara";
		String aciklama = "";
		if (PdksUtil.hasStringValue(key))
			aciklama = PdksUtil.getMessageBundleMessage(key);
		return aciklama;
	}

	public void setVardiyaGun(VardiyaGun vardiyaGun) {
		if (vardiyaGun != null) {
			String tarih = PdksUtil.convertToDateString(vardiyaGun.getVardiyaDate(), "yyyyMMdd");
			vardiyaGunleriMap.put(tarih, vardiyaGun);
			if (genelHaftaMap != null && genelHaftaMap.containsKey(tarih)) {
				int hafta = genelHaftaMap.get(tarih);
				if (vardiyaHaftaMap == null) {
					vardiyaHaftaMap = new TreeMap<Integer, TreeMap>();
					vardiyaHaftaMap.put(hafta, new TreeMap());
				}
				TreeMap vardiyaMap = vardiyaHaftaMap.containsKey(hafta) ? vardiyaHaftaMap.get(hafta) : new TreeMap();
				vardiyaMap.put(tarih, vardiyaGun);
			}

		}
	}

	public TreeMap<String, Integer> getGenelHaftaMap() {
		return genelHaftaMap;
	}

	public void setGenelHaftaMap(TreeMap<String, Integer> genelHaftaMap, TreeMap<String, Tatil> tatilGunleriMap) {
		if (genelHaftaMap != null && !genelHaftaMap.isEmpty()) {
			vardiyaGunleriMap = new TreeMap<String, VardiyaGun>();
			VardiyaSablonu sablonu = personel.getSablon();
			Calendar cal = Calendar.getInstance();
			Date iseGirisTarihi = personel.getIseGirisTarihi(), sonCalismaTarihi = personel.getSonCalismaTarihi();
			TreeMap<Integer, TreeMap> vardiyaHaftaMap = new TreeMap<Integer, TreeMap>();
			for (Iterator<String> iterator = genelHaftaMap.keySet().iterator(); iterator.hasNext();) {
				String gun = (String) iterator.next();
				int hafta = genelHaftaMap.get(gun);
				Date vardiyaDate = PdksUtil.convertToJavaDate(gun, "yyyyMMdd");
				TreeMap vardiyaMap = vardiyaHaftaMap.containsKey(hafta) ? vardiyaHaftaMap.get(hafta) : new TreeMap();
				if (!vardiyaHaftaMap.containsKey(hafta))
					vardiyaHaftaMap.put(hafta, vardiyaMap);
				VardiyaGun pdksVardiyaGun = new VardiyaGun(personel, null, vardiyaDate);
				if (tatilGunleriMap.containsKey(gun))
					pdksVardiyaGun.setTatil(tatilGunleriMap.get(gun));

				if (sablonu != null && vardiyaDate.getTime() >= iseGirisTarihi.getTime() && vardiyaDate.getTime() <= sonCalismaTarihi.getTime()) {
					cal.setTime(vardiyaDate);
					switch (cal.get(Calendar.DAY_OF_WEEK)) {
					case Calendar.MONDAY:
						pdksVardiyaGun.setVardiya(sablonu.getVardiya1());
						break;
					case Calendar.TUESDAY:
						pdksVardiyaGun.setVardiya(sablonu.getVardiya2());
						break;
					case Calendar.WEDNESDAY:
						pdksVardiyaGun.setVardiya(sablonu.getVardiya3());
						break;
					case Calendar.THURSDAY:
						pdksVardiyaGun.setVardiya(sablonu.getVardiya4());
						break;
					case Calendar.FRIDAY:
						pdksVardiyaGun.setVardiya(sablonu.getVardiya5());
						break;
					case Calendar.SATURDAY:
						pdksVardiyaGun.setVardiya(sablonu.getVardiya6());
						break;
					case Calendar.SUNDAY:
						pdksVardiyaGun.setVardiya(sablonu.getVardiya7());
						break;

					default:
						break;
					}

					vardiyaGunleriMap.put(gun, pdksVardiyaGun);

				}

				vardiyaMap.put(gun, pdksVardiyaGun);
			}
			setVardiyaHaftaMap(vardiyaHaftaMap);
		}
		this.genelHaftaMap = genelHaftaMap;
	}

	public TreeMap<Integer, TreeMap> getVardiyaHaftaMap() {
		return vardiyaHaftaMap;
	}

	public void setVardiyaHaftaMap(TreeMap<Integer, TreeMap> vardiyaHaftaMap) {
		this.vardiyaHaftaMap = vardiyaHaftaMap;
	}

	public int getDenklestirmeHaftasi() {
		return denklestirmeHaftasi;
	}

	public void setDenklestirmeHaftasi(int denklestirmeHaftasi) {
		this.denklestirmeHaftasi = denklestirmeHaftasi;
	}

	public ArrayList<PersonelDenklestirmeTasiyici> getPersonelDenklestirmeleri() {
		return personelDenklestirmeleri;
	}

	public void setPersonelDenklestirmeleri(ArrayList<PersonelDenklestirmeTasiyici> personelDenklestirmeleri) {
		this.personelDenklestirmeleri = personelDenklestirmeleri;
	}

	public List<VardiyaGun> getVardiyalar() {
		return vardiyalar;
	}

	public void setVardiyalar(List<VardiyaGun> vardiyalar) {
		this.vardiyalar = vardiyalar;
	}

	public double getToplamCalisilacakZaman() {
		return toplamCalisilacakZaman;
	}

	public void setToplamCalisilacakZaman(double toplamCalisilacakZaman) {
		this.toplamCalisilacakZaman = toplamCalisilacakZaman;
	}

	public void addToplamCalisilacakZaman(double sure) {
		toplamCalisilacakZaman += sure;
	}

	public double getToplamCalisilanZaman() {
		double toplamCalisilanSure = PdksUtil.setSureDoubleTypeRounded(toplamCalisilanZaman, yarimYuvarla);
		return toplamCalisilanSure;
	}

	public void setToplamCalisilanZaman(double toplamCalisilanZaman) {
		this.toplamCalisilanZaman = toplamCalisilanZaman;
	}

	public void addToplamCalisilanZaman(String key, double sure) {
		toplamCalisilanZaman += sure;

	}

	public double getCalisilanFark() {
		double calisilanFark = PdksUtil.setSureDoubleTypeRounded(getToplamCalisilanZaman() - toplamCalisilacakZaman, yarimYuvarla);
		return calisilanFark;
	}

	public String getTrClass() {
		return trClass;
	}

	public void setTrClass(String trClass) {
		this.trClass = trClass;
	}

	public boolean isGoster() {
		return goster;
	}

	public void setGoster(boolean goster) {
		this.goster = goster;
	}

	public TreeMap<String, VardiyaGun> getVardiyaGunleriMap() {
		return vardiyaGunleriMap;
	}

	public void setVardiyaGunleriMap(TreeMap<String, VardiyaGun> vardiyaGunleriMap) {
		this.vardiyaGunleriMap = vardiyaGunleriMap;
	}

	public double getToplamEgitim() {
		return toplamEgitim;
	}

	public void setToplamEgitim(double toplamEgitim) {
		this.toplamEgitim = toplamEgitim;
	}

	public double getToplamRaporIzni() {
		return toplamRaporIzni;
	}

	public void setToplamRaporIzni(double toplamRaporIzni) {
		this.toplamRaporIzni = toplamRaporIzni;
	}

	public String getOdemeSekliAciklama() {
		String aciklama = "";
		if (mesaiOdemeSekli != null) {
			if (mesaiOdemeSekli.equals(MESAI_ODEME_SEKLI_PARA))
				aciklama = "Para";
			else if (mesaiOdemeSekli.equals(MESAI_ODEME_SEKLI_IZIN))
				aciklama = "İzin";
			else if (mesaiOdemeSekli.equals(MESAI_ODEME_SEKLI_IZIN_PARA))
				aciklama = "İzin ve Para";
		}
		return aciklama;
	}

	public long getPersonelId() {
		return personel != null ? personel.getId() : 0;
	}

	public String getAdSoyad() {
		String key = personel != null ? personel.getAdSoyad() : "";
		// CalismaModeli cm = calismaModeliAy != null ? calismaModeliAy.getCalismaModeli() : calismaModeli;
		// key += (cm != null ? "_" + cm.getAciklama() : "");
		return key;
	}

	public boolean isIptal(User user) {
		boolean iptal = (user.isAdmin() || user.isIK()) && (mesaiSapDurum || izin != null);
		return iptal;
	}

	public Object getSapTable() {
		return sapTable;
	}

	public void setSapTable(Object sapTable) {
		this.sapTable = sapTable;
	}

	public VardiyaGun getSonVardiyaGun() {
		return sonVardiyaGun;
	}

	public String getKontratliSortKey() {
		String str = personel != null ? personel.getKontratliSortKey() : (id != null ? id.toString() : "");
		return str;
	}

	public void setSonVardiyaGun(VardiyaGun sonVardiyaGun) {
		this.sonVardiyaGun = sonVardiyaGun;
	}

	public VardiyaGun getOncekiVardiyaGun() {
		return oncekiVardiyaGun;
	}

	public void setOncekiVardiyaGun(VardiyaGun oncekiVardiyaGun) {
		this.oncekiVardiyaGun = oncekiVardiyaGun;
	}

	public DenklestirmeAy getDenklestirmeAy() {
		return denklestirmeAy;
	}

	public void setDenklestirmeAy(DenklestirmeAy denklestirmeAy) {
		this.denklestirmeAy = denklestirmeAy;
	}

	public CalismaModeli getCalismaModeli() {
		return calismaModeli;
	}

	public void setCalismaModeli(CalismaModeli calismaModeli) {
		this.calismaModeli = calismaModeli;
	}

	public int getYarimYuvarla() {
		return yarimYuvarla;
	}

	public void setYarimYuvarla(int yarimYuvarla) {
		this.yarimYuvarla = yarimYuvarla;
	}

	public Personel getPdksPersonel() {
		return personel;
	}

	/**
	 * @return the calismaModeliAy
	 */
	public CalismaModeliAy getCalismaModeliAy() {
		return calismaModeliAy;
	}

	/**
	 * @param calismaModeliAy
	 *            the calismaModeliAy to set
	 */
	public void setCalismaModeliAy(CalismaModeliAy value) {
		if (value != null) {
			this.denklestirmeAy = value.getDenklestirmeAy();
			this.calismaModeli = value.getCalismaModeli();
		}

		this.calismaModeliAy = value;
	}
}
