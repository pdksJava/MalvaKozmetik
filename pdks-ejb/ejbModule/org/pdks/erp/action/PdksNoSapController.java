package org.pdks.erp.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.pdks.entity.DenklestirmeAy;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelDenklestirme;
import org.pdks.entity.PersonelIzin;
import org.pdks.security.entity.User;
import org.pdks.session.OrtakIslemler;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;
import org.hibernate.Session;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import com.pdks.webservice.PersonelERP;

@Name("pdksNoSapController")
public class PdksNoSapController implements ERPController, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6003454132581246078L;

	static Logger logger = Logger.getLogger(PdksNoSapController.class);
	@In(create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	HashMap<String, String> parameterMap;
	@In(required = false, create = true)
	User authenticatedUser;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;

	public void setFazlaMesaiUcretRFC(List<PersonelDenklestirme> sapMesaiList, User user, Session session) throws Exception {

		try {
			if (session == null)
				session = PdksUtil.getSession(entityManager, Boolean.FALSE);
			DenklestirmeAy denklestirmeAy = sapMesaiList.get(0).getDenklestirmeAy();
			Calendar cal = Calendar.getInstance();
			Date guncellemeTarihi = (Date) cal.getTime().clone();
			cal.set(denklestirmeAy.getYil(), denklestirmeAy.getAy() - 1, 1);

			for (PersonelDenklestirme personelDenklestirme : sapMesaiList) {

				double odenecekSure = 0d;
				if (personelDenklestirme.getOdenecekSure() != null && personelDenklestirme.getOdenecekSure() > 0)
					odenecekSure = personelDenklestirme.getOdenecekSure();

				personelDenklestirme.setErpAktarildi(Boolean.TRUE);
				if (personelDenklestirme.getId() != null) {
					personelDenklestirme.setOdenenSure(odenecekSure);
					personelDenklestirme.setGuncelleyenUser(user);
					personelDenklestirme.setGuncellemeTarihi(guncellemeTarihi);
					pdksEntityController.saveOrUpdate(session, entityManager, personelDenklestirme);
					session.flush();
				}

			}

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		} finally {

		}

	}

	public String setIzinRFC(PersonelIzin izin) throws Exception {
		 
		return null;
	}

	public LinkedHashMap<String, Personel> topluHaldePersonelBilgisiGetir(Session session, TreeMap bordroAltBirimiMap, TreeMap masrafYeriMap, LinkedHashMap<String, Personel> personelMap, Date baslangicZamani, Date bitisZamani, Object sapRfcManager, Object jcoClient) throws Exception {
		 
		return null;
	}

	public HashMap<String, Personel> topluHaldeYoneticiBulMap(int derinlik, ArrayList<String> personelNumaralariListesi, Date baslangicZamani, Date bitisZamani) throws Exception {
		 
		return null;
	}

	public LinkedHashMap topluHaldeIscileriVeriGetir(Session session, int icDerinlik, boolean yoneticiEkle, ArrayList<String> personelNo, Date baslangicZamani, Date bitisZamani, TreeMap bordroAltBirimiMap, TreeMap masrafYeriMap) throws Exception {
		 
		return null;
	}

	public List<Personel> pdksTanimsizPersonel(List<String> perNoList, String sapKodu) throws Exception {
		 
		return null;
	}

	public TreeMap<Long, String> setRFCIzinList(List<PersonelIzin> izinList) throws Exception {
		 
		return null;
	}

	public List<PersonelERP> topluHaldePersonelBilgisiNoSapDBGetir(Session session, List<String> personelList, Date baslangicZamani, Date bitisZamani) throws Exception {
		// TODO entityRefresh
		return null;
	}

}
