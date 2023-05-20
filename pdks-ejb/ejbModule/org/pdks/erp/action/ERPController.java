package org.pdks.erp.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

import org.pdks.entity.Personel;
import org.pdks.entity.PersonelDenklestirme;
import org.pdks.entity.PersonelIzin;
import org.pdks.security.entity.User;
import org.hibernate.Session;

public interface ERPController {

	/**
	 * @param sapMesaiList
	 * @param user
	 * @throws Exception
	 */
	void setFazlaMesaiUcretRFC(List<PersonelDenklestirme> sapMesaiList,User user, Session session) throws Exception;

	/**
	 * @param izin
	 * @return
	 * @throws Exception
	 */
	String setIzinRFC(PersonelIzin izin) throws Exception;

	/**
	 * @param session
	 * @param bordroAltBirimiMap
	 * @param masrafYeriMap
	 * @param personelMap
	 * @param baslangicZamani
	 * @param bitisZamani
	 * @param sapRfcManager
	 * @param jcoClient
	 * @return
	 * @throws Exception
	 */
	LinkedHashMap<String, Personel> topluHaldePersonelBilgisiGetir(Session session, TreeMap bordroAltBirimiMap, TreeMap masrafYeriMap, LinkedHashMap<String, Personel> personelMap, Date baslangicZamani, Date bitisZamani, Object sapRfcManager, Object jcoClient) throws Exception;

	/**
	 * @param derinlik
	 * @param personelNumaralariListesi
	 * @param baslangicZamani
	 * @param bitisZamani
	 * @return
	 * @throws Exception
	 */
	HashMap<String, Personel> topluHaldeYoneticiBulMap(int derinlik, ArrayList<String> personelNumaralariListesi, Date baslangicZamani, Date bitisZamani) throws Exception;

	/**
	 * @param session
	 * @param icDerinlik
	 * @param yoneticiEkle
	 * @param personelNo
	 * @param baslangicZamani
	 * @param bitisZamani
	 * @param bordroAltBirimiMap
	 * @param masrafYeriMap
	 * @return
	 * @throws Exception
	 */
	LinkedHashMap topluHaldeIscileriVeriGetir(Session session, int icDerinlik, boolean yoneticiEkle, ArrayList<String> personelNo, Date baslangicZamani, Date bitisZamani, TreeMap bordroAltBirimiMap, TreeMap masrafYeriMap) throws Exception;

	
	/**
	 * @param perNoList
	 * @param sapKodu
	 * @return
	 * @throws Exception
	 */
	public List<Personel> pdksTanimsizPersonel(List<String> perNoList, String sapKodu) throws Exception;
	
	/**
	 * @param izinList
	 * @return
	 * @throws Exception
	 */
	public TreeMap<Long, String> setRFCIzinList(List<PersonelIzin> izinList) throws Exception;
	
}
