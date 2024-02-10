package org.pdks.quartz.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.scheduling.quartz.QuartzJobBean;

import org.pdks.dao.PdksDAO;
import org.pdks.dao.impl.BaseDAOHibernate;
import org.pdks.entity.HataliPersonel;
import org.pdks.entity.Parameter;
import org.pdks.entity.Personel;
import org.pdks.entity.Sirket;
import org.pdks.genel.model.Constants;
import org.pdks.genel.model.MailManager;
import org.pdks.genel.model.PdksUtil;

import com.pdks.webService.PdksVeriOrtakAktar;

public final class PersonelKontrol extends QuartzJobBean {

	private Logger logger = Logger.getLogger(PersonelKontrol.class);

	private static boolean calisiyor = false;

	// ERROR //
	protected void executeInternal(org.quartz.JobExecutionContext ctx) throws org.quartz.JobExecutionException {
		if (!calisiyor) {
			try {
				calisiyor = true;
				PdksDAO pdksDAO = Constants.pdksDAO;
				if (PdksUtil.zamanKontrolDurum()) {
					HashMap fields = new HashMap();
					fields.put("name", "canliSunucu");
					fields.put("active", Boolean.TRUE);
					Parameter parameter = (Parameter) pdksDAO.getObjectByInnerObject(fields, Parameter.class);
					if (parameter != null && PdksUtil.getCanliSunucuDurum())
						personelKontrol();
				}

				Calendar cal = Calendar.getInstance();
				int gun = cal.get(Calendar.DAY_OF_WEEK);
				boolean haftaIci = gun != Calendar.SATURDAY && gun != Calendar.SUNDAY;
				if (PdksUtil.isSistemDestekVar()) {
					topluUpdate(pdksDAO, "SP_SISTEM_ALL_UPDATE");
					if (haftaIci == false)
						topluUpdate(pdksDAO, "SP_INDEX_REORGANIZE_REBUILD");
				}

			} catch (Exception e) {
				logger.error(e);
			} finally {
				calisiyor = false;
			}

		}

	}

	/**
	 * @param header
	 * @param perList
	 * @param sb
	 * @param dolu
	 */
	private void tabloYaz(String header, List<HataliPersonel> perList, StringBuffer sb, boolean dolu) {
		if (perList != null && !perList.isEmpty()) {
			sb.append("<DIV><H2>" + header + "</H2></BR>");
			sb.append("<TABLE align=\"center\" style=\"border: solid 1px\" cellpadding=\"5\" cellspacing=\"0\"><THEAD><TR>");
			if (dolu)
				sb.append("<TH align=\"center\" style=\"border: 1px solid;\">Personel No</TH>");
			sb.append("<TH align=\"center\" style=\"border: 1px solid;\">Adı</TH>");
			sb.append("<TH align=\"center\" style=\"border: 1px solid;\">Soyadı</TH>");
			sb.append("<TH align=\"center\" style=\"border: 1px solid;\">Kart No</TH>");
			sb.append("<TH align=\"center\" style=\"border: 1px solid;\">İşe Giriş Tarihi</TH>");
			sb.append("<TH align=\"center\" style=\"border: 1px solid;\">Şirket Adı</TH>");
			sb.append("<TH align=\"center\" style=\"border: 1px solid;\">Durum</TH>");
			sb.append("</TR></THEAD><TBODY>");
			for (HataliPersonel hataliPersonel : perList) {
				sb.append("<TR>");
				if (dolu)
					sb.append("<TD align='center' nowrap style=\"border: 1px solid;\">" + hataliPersonel.getPersonelNo() + "</TD>");
				sb.append("<TD  nowrap style=\"border: 1px solid;\">" + hataliPersonel.getAdi() + "</TD>");
				sb.append("<TD  nowrap style=\"border: 1px solid;\">" + hataliPersonel.getSoyadi() + "</TD>");
				sb.append("<TD align='center' nowrap style=\"border: 1px solid;\">" + (hataliPersonel.getKartNo() != null ? hataliPersonel.getKartNo().trim() : "") + "</TD>");
				sb.append("<TD align='center' nowrap style=\"border: 1px solid;\">" + (hataliPersonel.getIseGirisTarhi() != null ? PdksUtil.convertToDateString(hataliPersonel.getIseGirisTarhi(), PdksUtil.getDateFormat()) : "") + "</TD>");
				sb.append("<TD   nowrap style=\"border: 1px solid;\">" + (hataliPersonel.getSirketAdi() != null ? hataliPersonel.getSirketAdi().trim() : "") + "</TD>");
				sb.append("<TD align='center' nowrap style=\"border: 1px solid;\">" + (hataliPersonel.getDurum() != null ? (hataliPersonel.getDurum() ? "Aktif" : "Pasif") : "") + "</TD>");
				sb.append("</TR>");
			}
			sb.append("</TBODY></TABLE></DIV>");
		}

	}

	/**
	 * @param pdksDAO
	 * @param spName
	 */
	private void topluUpdate(PdksDAO pdksDAO, String spName) {
		logger.info(spName + "  in " + PdksUtil.getCurrentTimeStampStr());
		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		map.put(BaseDAOHibernate.MAP_KEY_SELECT, spName);
		try {

		} catch (Exception e) {
			logger.error(e);
		}
		pdksDAO.execSP(map);
		logger.info(spName + "  out " + PdksUtil.getCurrentTimeStampStr());
	}

	/**
	 * 
	 */
	private void yoneticiKontrol() {
		PdksDAO pdksDAO = Constants.pdksDAO;
		HashMap fields = new HashMap();
		StringBuffer sql = new StringBuffer();
		sql.append(" select P.* from " + Personel.TABLE_NAME + " P ");
		sql.append(" INNER JOIN " + Sirket.TABLE_NAME + " S ON S." + Sirket.COLUMN_NAME_ID + "=P." + Personel.COLUMN_NAME_SIRKET + " AND S." + Sirket.COLUMN_NAME_DURUM + "=1 AND S." + Sirket.COLUMN_NAME_ERP_DURUM + "=1 ");
		sql.append(" INNER JOIN " + Personel.TABLE_NAME + " Y ON Y." + Personel.COLUMN_NAME_ID + "=P." + Personel.COLUMN_NAME_YONETICI + " AND Y." + Personel.COLUMN_NAME_ISTEN_AYRILIS_TARIHI + "<GETDATE() ");
		sql.append(" WHERE P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + ">=CAST(GETDATE() AS date) AND P." + Personel.COLUMN_NAME_DURUM + "=1 ");
		sql.append(" AND P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + ">=GETDATE() ");
		sql.append(" ORDER BY Y." + Personel.COLUMN_NAME_AD + ",Y." + Personel.COLUMN_NAME_SOYAD + ",Y." + Personel.COLUMN_NAME_ID);
		List<Personel> hataliPersonelList = pdksDAO.getNativeSQLList(fields, sql, Personel.class);
		if (!hataliPersonelList.isEmpty()) {

			StringBuffer sb = new StringBuffer();
			sb.append("<DIV><H2>Yönetcisi Çalışmayan Personel Listesi</H2></BR>");
			sb.append("<TABLE align=\"center\" style=\"border: solid 1px\" cellpadding=\"5\" cellspacing=\"0\"><THEAD><TR>");

			sb.append("<TH align=\"center\" style=\"border: 1px solid;\">Personel No</TH>");
			sb.append("<TH align=\"center\" style=\"border: 1px solid;\">Adı Soyadı</TH>");
			sb.append("<TH align=\"center\" style=\"border: 1px solid;\">Yönetici</TH>");
			sb.append("<TH align=\"center\" style=\"border: 1px solid;\">İşe Giriş Tarihi</TH>");
			sb.append("<TH align=\"center\" style=\"border: 1px solid;\">Şirket Adı</TH>");

			sb.append("</TR></THEAD><TBODY>");
			for (Personel hataliPersonel : hataliPersonelList) {
				sb.append("<TR>");
				sb.append("<TD align='center' nowrap style=\"border: 1px solid;\">" + hataliPersonel.getPdksSicilNo() + "</TD>");
				sb.append("<TD  nowrap style=\"border: 1px solid;\">" + hataliPersonel.getAdSoyad() + "</TD>");
				sb.append("<TD align='center' nowrap style=\"border: 1px solid;\">" + (hataliPersonel.getYoneticisi().getPdksSicilNo().trim() + " " + hataliPersonel.getYoneticisi().getAdSoyad()) + "</TD>");
				sb.append("<TD align='center' nowrap style=\"border: 1px solid;\">" + (hataliPersonel.getIseBaslamaTarihi() != null ? PdksUtil.convertToDateString(hataliPersonel.getIseBaslamaTarihi(), PdksUtil.getDateFormat()) : "") + "</TD>");
				sb.append("<TD   nowrap style=\"border: 1px solid;\">" + (hataliPersonel.getSirket() != null ? hataliPersonel.getSirket().getAd().trim() : "") + "</TD>");
				sb.append("</TR>");
			}
			sb.append("</TBODY></TABLE></DIV>");
			HashMap<String, Object> mailMap = new HashMap<String, Object>();
			fields.clear();
			fields.put("active", Boolean.TRUE);
			List<Parameter> list = pdksDAO.getObjectByInnerObjectList(fields, Parameter.class);
			for (Parameter parameter2 : list)
				mailMap.put(parameter2.getName(), parameter2.getValue());
			mailMap.put("konu", " Yönetici çalışmayan personeller");
			sb.append("</br><P>Saygılarımla</P>");
			mailMap.put("konu", "Yönetici çalışmayan personeller");
			mailMap.put("mailIcerik", sb.toString());
			try {
				PdksVeriOrtakAktar ortakAktar = new PdksVeriOrtakAktar();
				// mailMap.put("ikMailIptal", "");
				ortakAktar.kullaniciIKYukle(mailMap, pdksDAO);
				MailManager.ePostaGonder(mailMap);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void personelKontrol() {
		PdksDAO pdksDAO = Constants.pdksDAO;
		HashMap fields = new HashMap();
		fields.put("name", "kgsMasterUpdate");
		fields.put("active", Boolean.TRUE);
		Parameter parameter = (Parameter) pdksDAO.getObjectByInnerObject(fields, Parameter.class);
		HashMap<String, Object> mailMap = null;
		if (parameter != null && parameter.getValue().equals("2")) {
			logger.info("Personel Kontrol start " + PdksUtil.getCurrentTimeStampStr());
			fields.clear();
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT D.* FROM " + HataliPersonel.VIEW_NAME + " D WITH(nolock)");
			sb.append(" ORDER BY D." + HataliPersonel.COLUMN_NAME_TIP + ",D." + HataliPersonel.COLUMN_NAME_PERSONEL_NO + ", D." + HataliPersonel.COLUMN_NAME_ID);
			List<HataliPersonel> hataliPersonelList = pdksDAO.getNativeSQLList(fields, sb, HataliPersonel.class);
			if (hataliPersonelList != null && !hataliPersonelList.isEmpty()) {
				fields.clear();
				fields.put("active", Boolean.TRUE);
				List<Parameter> list = pdksDAO.getObjectByInnerObjectList(fields, Parameter.class);
				if (mailMap == null)
					mailMap = new HashMap<String, Object>();

				for (Parameter parameter2 : list)
					mailMap.put(parameter2.getName(), parameter2.getValue());
				List<HataliPersonel> bosPersonelList = new ArrayList<HataliPersonel>();
				for (Iterator iterator = hataliPersonelList.iterator(); iterator.hasNext();) {
					HataliPersonel hataliPersonel = (HataliPersonel) iterator.next();
					if (!PdksUtil.hasStringValue(hataliPersonel.getPersonelNo())) {
						bosPersonelList.add(hataliPersonel);
						iterator.remove();
					}
				}
				sb = new StringBuffer();
				String header = null;
				String kapiGirisUygulama = mailMap.containsKey("kapiGirisUygulama") ? (String) mailMap.get("kapiGirisUygulama") : "";
				if (!bosPersonelList.isEmpty()) {
					header = "Sicil numarası boş " + kapiGirisUygulama + " kapı giriş personel bilgileri";
					tabloYaz(header, bosPersonelList, sb, false);
				}
				if (!hataliPersonelList.isEmpty()) {
					header = "Sicil numarası tekrar eden " + kapiGirisUygulama + " kapı giriş personel bilgileri";
					tabloYaz(header, hataliPersonelList, sb, true);
				}

				mailMap.put("konu", (!PdksUtil.hasStringValue(kapiGirisUygulama) ? "Kapı" : kapiGirisUygulama + " kapi") + " girişi personel bilgi kontrol problem");
				sb.append("</br><P>Saygılarımla</P>");
				mailMap.put("mailIcerik", sb.toString());
				// mailMap.put("bccTestMailAdres", "hasansayar58@gmail.com");
				try {
					PdksVeriOrtakAktar ortakAktar = new PdksVeriOrtakAktar();
					ortakAktar.kullaniciIKYukle(mailMap, pdksDAO);
					MailManager.ePostaGonder(mailMap);
				} catch (Exception e) {

					e.printStackTrace();
				}
			}
			logger.info("Personel Kontrol stop " + PdksUtil.getCurrentTimeStampStr());
		}
		yoneticiKontrol();
	}

}
