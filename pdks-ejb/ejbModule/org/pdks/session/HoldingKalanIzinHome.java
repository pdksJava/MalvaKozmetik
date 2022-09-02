package org.pdks.session;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pdks.entity.AramaSecenekleri;
import org.pdks.entity.HoldingIzin;
import org.pdks.entity.Personel;
import org.pdks.entity.Sirket;
import org.pdks.quartz.IzinBakiyeGuncelleme;
import org.pdks.security.entity.MenuItemConstant;
import org.pdks.security.entity.User;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;

@Name("holdingKalanIzinHome")
public class HoldingKalanIzinHome extends EntityHome<HoldingIzin> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4183382367447830720L;
	static Logger logger = Logger.getLogger(HoldingKalanIzinHome.class);

	@RequestParameter
	Long personelIzinId;

	@In(create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false)
	User authenticatedUser;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;
	@In(required = false, create = true)
	IzinBakiyeGuncelleme izinBakiyeGuncelleme;

	private String kidemYili, bolumAciklama;

	private Date hakedisTarihi;

	private List<HoldingIzin> holdingIzinList;

	private Session session;

	private AramaSecenekleri aramaSecenekleri = null;

	private boolean istenAyrilanEkle = Boolean.FALSE, suaVar = Boolean.FALSE;

	@Override
	public Object getId() {
		if (personelIzinId == null) {
			return super.getId();
		} else {
			return personelIzinId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	private void fillEkSahaTanim() {
		ortakIslemler.fillEkSahaTanimAramaSecenekAta(session, Boolean.FALSE, null, aramaSecenekleri);
		HashMap sonucMap = ortakIslemler.fillEkSahaTanim(session, Boolean.FALSE, null);
		bolumAciklama = (String) sonucMap.get("bolumAciklama");
		if (aramaSecenekleri.getSirketIdList().size() == 1)
			aramaSecenekleri.setSirketId((Long) aramaSecenekleri.getSirketIdList().get(0).getValue());

	}

	public void instanceRefresh() {
		if (getInstance().getId() != null)
			session.refresh(getInstance());
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public String sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		istenAyrilanEkle = Boolean.FALSE;
		if (authenticatedUser.isAdmin() == false || aramaSecenekleri == null)
			aramaSecenekleri = new AramaSecenekleri(authenticatedUser);
		boolean yetkili = authenticatedUser.isAdmin() || authenticatedUser.isIK();
		String pos = "";
		aramaSecenekleri.setSirketIzinKontrolYok(Boolean.FALSE);
		if (!yetkili) {
			PdksUtil.addMessageAvailableWarn("Giriş yetkiniz yoktur!");
			pos = MenuItemConstant.home;
		} else

			sayfaGiris(session);

		return pos;
	}

	private void sayfaGiris(Session session) {
		session.clear();
		fillEkSahaTanim();
		suaVar = Boolean.FALSE;
		if (hakedisTarihi == null) {
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.MONTH, Calendar.DECEMBER);
			cal.set(Calendar.DATE, 31);
			hakedisTarihi = PdksUtil.getDate(cal.getTime());
		}
		if (authenticatedUser.isIK() || authenticatedUser.isAdmin())
			setHoldingIzinList(new ArrayList<HoldingIzin>());
	}

	public String fillIzinList() {
		suaVar = Boolean.FALSE;
		aramaSecenekleri.setSirket(null);
		session.clear();
		if (holdingIzinList != null)
			holdingIzinList.clear();
		else
			holdingIzinList = new ArrayList<HoldingIzin>();
		HashMap fields = new HashMap();
		ArrayList<String> sicilNoList = ortakIslemler.getAramaPersonelSicilNo(aramaSecenekleri, Boolean.TRUE, istenAyrilanEkle, session);
		if (!sicilNoList.isEmpty()) {
			String spName = ortakIslemler.getParameterKey("holdingIzinSPName");
			if (istenAyrilanEkle)
				spName = "SP_HOLDING_IZIN_RAPOR_AYRILANLARLA";
			else if (spName == null || spName.trim().equals(""))
				spName = "SP_HOLDING_IZIN_RAPOR_ALL";
			String hakedisTarihiStr = hakedisTarihi != null ? PdksUtil.convertToDateString(hakedisTarihi, "yyyy-MM-dd") : "";
			fields.clear();
			fields.put("id", aramaSecenekleri.getSirketId());
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			Sirket sirket = (Sirket) pdksEntityController.getObjectByInnerObject(fields, Sirket.class);

			aramaSecenekleri.setSirket(sirket);

			Calendar cal = Calendar.getInstance();
			cal.setTime(hakedisTarihi);
			cal.set(Calendar.MONTH, Calendar.JANUARY);
			cal.set(Calendar.DATE, 1);
			Date istenAyrilmaTarih = PdksUtil.getDate(cal.getTime());

			TreeMap<String, HoldingIzin> map = new TreeMap<String, HoldingIzin>();
			StringBuffer sb = null;

			LinkedHashMap<Long, String> dataMap = new LinkedHashMap<Long, String>();
			while (!sicilNoList.isEmpty()) {
				List<String> sorguList = new ArrayList<String>();
				for (Iterator iterator = sicilNoList.iterator(); iterator.hasNext();) {
					String string = (String) iterator.next();
					sorguList.add(string);
					iterator.remove();
					if (sorguList.size() >= 1500)
						break;
				}
				fields.clear();
				sb = new StringBuffer();
				sb.append("SELECT P." + Personel.COLUMN_NAME_ID + " AS PER_ID," + Personel.COLUMN_NAME_IZIN_HAKEDIS_TARIHI + ", P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + " from " + Personel.TABLE_NAME + " P WITH(nolock) ");
				sb.append(" WHERE P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + " :p");
				sb.append(" AND P." + Personel.COLUMN_NAME_SIRKET + "=:t");
				fields.put("t", sirket.getId());
				if (istenAyrilanEkle) {
					sb.append(" AND P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + " >=:ia");
					fields.put("ia", istenAyrilmaTarih);
				}
				sb.append(" ORDER BY  " + Personel.COLUMN_NAME_IZIN_HAKEDIS_TARIHI + ", P." + Personel.COLUMN_NAME_PDKS_SICIL_NO);

				fields.put("p", sorguList);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<Object[]> idList = pdksEntityController.getObjectBySQLList(sb, fields, null);
				for (Iterator iterator = idList.iterator(); iterator.hasNext();) {
					Object[] objects = (Object[]) iterator.next();
					dataMap.put(((BigDecimal) objects[0]).longValue(), (String) objects[2]);
				}

				idList = null;
				sorguList.clear();
			}
			List list = new ArrayList(dataMap.keySet());

			while (!list.isEmpty()) {
				try {
					int i = 0;
					sb = new StringBuffer();
					for (Iterator iterator = list.iterator(); iterator.hasNext();) {
						Object object = (Object) iterator.next();
						sb.append(object.toString());
						iterator.remove();
						if (++i >= 1500)
							break;
						if (iterator.hasNext())
							sb.append(",");

					}
					String personelList = sb.toString();
					sb = new StringBuffer();
					sb.append(spName);
					LinkedHashMap<String, Object> veriMap = new LinkedHashMap<String, Object>();
					veriMap.put("personelList", personelList);
					veriMap.put("sirketId", String.valueOf(aramaSecenekleri.getSirketId()));
					veriMap.put("tarih", hakedisTarihiStr);
					veriMap.put("format", "120");
					veriMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<HoldingIzin> izinList = pdksEntityController.execSPList(veriMap, sb, HoldingIzin.class);

					if (!izinList.isEmpty()) {
						for (HoldingIzin holdingIzin : izinList) {
							if (!suaVar)
								suaVar = holdingIzin.getBuYilHakedilenSua() != null && holdingIzin.getBuYilHakedilenSua().intValue() > 0;
							dataMap.remove(holdingIzin.getPersonel().getId());
						}

						if (!list.isEmpty() || !map.isEmpty()) {
							for (HoldingIzin holdingIzin : izinList) {
								Personel personel = holdingIzin.getPersonel();
								String key = PdksUtil.convertToDateString(personel.getIzinHakEdisTarihi(), "yyyyMMdd") + "_" + personel.getPdksSicilNo() + "_" + personel.getId();
								map.put(key, holdingIzin);
							}
						} else
							holdingIzinList.addAll(izinList);

					}

					izinList = null;
				} catch (Exception e) {
					logger.error(e);
				}

			}
			if (!map.isEmpty())
				holdingIzinList.addAll(new ArrayList<HoldingIzin>(map.values()));
			if (!dataMap.isEmpty()) {
				fields.clear();
				sb = new StringBuffer();
				sb.append("SELECT P.* from " + Personel.TABLE_NAME + " P WITH(nolock) ");
				sb.append(" WHERE P." + Personel.COLUMN_NAME_ID + " :p  AND P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + " <=:ia2");
				fields.put("ia1", istenAyrilmaTarih);
				if (hakedisTarihi != null) {
					sb.append(" AND  P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + " >=:ia1");
					fields.put("ia2", hakedisTarihi);
				}
				sb.append(" ORDER BY  " + Personel.COLUMN_NAME_IZIN_HAKEDIS_TARIHI + ", P." + Personel.COLUMN_NAME_PDKS_SICIL_NO);
				fields.put("p", new ArrayList(dataMap.keySet()));
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<Personel> idList = pdksEntityController.getObjectBySQLList(sb, fields, Personel.class);
				for (Iterator iterator = idList.iterator(); iterator.hasNext();) {
					Personel personel = (Personel) iterator.next();
					holdingIzinList.add(new HoldingIzin(personel));

				}
			}
			map = null;
			sb = null;
		}
		return "";
	}

	private ByteArrayOutputStream izinKartiExcelAktarDevam() {
		ByteArrayOutputStream baos = null;
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = ExcelUtil.createSheet(wb, "Izin Rapor", Boolean.TRUE);
		CellStyle style = ExcelUtil.getStyleData(wb);
		CellStyle styleCenter = ExcelUtil.getStyleData(wb);
		CellStyle styleNumber = ExcelUtil.getCellStyleTutar(wb);
		styleCenter.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		CellStyle header = ExcelUtil.getStyleHeader(wb);
		CellStyle timeStamp = ExcelUtil.getCellStyleDate(wb);
		int row = 0, col = 0;
		boolean ekSaha1 = false, ekSaha2 = false, ekSaha3 = true, ekSaha4 = false;
		HashMap fields = new HashMap();
		fields.put("id", aramaSecenekleri.getSirketId());
		fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		Sirket sirket = (Sirket) pdksEntityController.getObjectByInnerObject(fields, Sirket.class);
		boolean bolumYok = sirket.getDepartman().isAdminMi();
		if (bolumYok) {
			ekSaha1 = (authenticatedUser.isAdmin() || authenticatedUser.isIKAdmin()) && aramaSecenekleri.getEkSahaTanimMap().containsKey("ekSaha1");
			ekSaha2 = (authenticatedUser.isAdmin() || authenticatedUser.isIKAdmin()) && aramaSecenekleri.getEkSahaTanimMap().containsKey("ekSaha2");
			ekSaha3 = (authenticatedUser.isAdmin() || authenticatedUser.isIKAdmin()) && aramaSecenekleri.getEkSahaTanimMap().containsKey("ekSaha3");
			ekSaha4 = (authenticatedUser.isAdmin() || authenticatedUser.isIKAdmin()) && aramaSecenekleri.getEkSahaTanimMap().containsKey("ekSaha4");
		}

		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.sirketAciklama());

		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Adı Soyadı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.kidemBasTarihiAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Doğum Tarihi");
		if (istenAyrilanEkle)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İşten Ayrılma Tarihi");
		if (ekSaha1)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(aramaSecenekleri.getEkSahaTanimMap().get("ekSaha1").getAciklama());
		if (ekSaha2)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(aramaSecenekleri.getEkSahaTanimMap().get("ekSaha2").getAciklama());
		if (ekSaha3)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(bolumYok ? aramaSecenekleri.getEkSahaTanimMap().get("ekSaha3").getAciklama() : "Bölüm");
		if (ekSaha4)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(aramaSecenekleri.getEkSahaTanimMap().get("ekSaha4").getAciklama());

		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.yoneticiAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Geçen Yıl Bakiye");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Bu Yıl Hakkedilen");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Bu Yıl Harcanan İzin Toplamı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Bakiye İzin Toplamı");
		if (suaVar)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Bu Yıl Hakkedilen ŞUA");

		for (HoldingIzin tempIzin : holdingIzinList) {
			Personel personel = tempIzin.getPersonel();
			row++;
			col = 0;
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getSirket().getAd());
			ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personel.getPdksSicilNo());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getAdSoyad());
			ExcelUtil.getCell(sheet, row, col++, timeStamp).setCellValue(personel.getIzinHakEdisTarihi());
			if (personel.getDogumTarihi() != null)
				ExcelUtil.getCell(sheet, row, col++, timeStamp).setCellValue(personel.getDogumTarihi());
			else
				ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue("");
			if (istenAyrilanEkle) {
				boolean calisiyor = personel.isCalisiyorGun(hakedisTarihi);
				if (calisiyor)
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
				else
					ExcelUtil.getCell(sheet, row, col++, timeStamp).setCellValue(personel.getIstenAyrilisTarihi());
			}
			if (ekSaha1)
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha1() != null ? personel.getEkSaha1().getAciklama() : "");
			if (ekSaha2)
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha2() != null ? personel.getEkSaha2().getAciklama() : "");
			if (ekSaha3)
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");
			if (ekSaha4)
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha4() != null ? personel.getEkSaha4().getAciklama() : "");
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getPdksYonetici() != null ? personel.getPdksYonetici().getAdSoyad() : "");
			ExcelUtil.getCell(sheet, row, col++, styleNumber).setCellValue(tempIzin.getGecenYilBakiye());
			ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(tempIzin.getBuYilHakedilen());
			ExcelUtil.getCell(sheet, row, col++, styleNumber).setCellValue(tempIzin.getHarcananIzin());
			ExcelUtil.getCell(sheet, row, col++, styleNumber).setCellValue(tempIzin.getBakiye());
			if (suaVar) {
				if (tempIzin.getBuYilHakedilenSua() != null && tempIzin.getBuYilHakedilenSua().intValue() > 0)
					ExcelUtil.getCell(sheet, row, col++, styleNumber).setCellValue(tempIzin.getBuYilHakedilenSua());
				else
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
			}
		}

		for (int i = 0; i <= col; i++)
			sheet.autoSizeColumn(i);

		try {
			baos = new ByteArrayOutputStream();
			wb.write(baos);
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			baos = null;
		}
		return baos;
	}

	public String izinKartiExcelAktar() {
		try {
			ByteArrayOutputStream baos = izinKartiExcelAktarDevam();
			if (baos != null) {
				HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
				ServletOutputStream sos = response.getOutputStream();
				response.setContentType("application/vnd.ms-excel");
				response.setHeader("Expires", "0");
				response.setHeader("Pragma", "cache");
				response.setHeader("Cache-Control", "cache");
				response.setHeader("Content-Disposition", "attachment;filename=holdingIzinRapor.xlsx");
				if (baos != null) {
					response.setContentLength(baos.size());
					byte[] bytes = baos.toByteArray();
					sos.write(bytes, 0, bytes.length);
					sos.flush();
					sos.close();
					FacesContext.getCurrentInstance().responseComplete();
				}
			}
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}

		return "";
	}

	public String getKidemYili() {
		return kidemYili;
	}

	public void setKidemYili(String kidemYili) {
		this.kidemYili = kidemYili;
	}

	public Date getHakedisTarihi() {
		return hakedisTarihi;
	}

	public void setHakedisTarihi(Date hakedisTarihi) {
		this.hakedisTarihi = hakedisTarihi;
	}

	public List<HoldingIzin> getHoldingIzinList() {
		return holdingIzinList;
	}

	public void setHoldingIzinList(List<HoldingIzin> holdingIzinList) {
		this.holdingIzinList = holdingIzinList;
	}

	public AramaSecenekleri getAramaSecenekleri() {
		return aramaSecenekleri;
	}

	public void setAramaSecenekleri(AramaSecenekleri aramaSecenekleri) {
		this.aramaSecenekleri = aramaSecenekleri;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public boolean isIstenAyrilanEkle() {
		return istenAyrilanEkle;
	}

	public void setIstenAyrilanEkle(boolean istenAyrilanEkle) {
		this.istenAyrilanEkle = istenAyrilanEkle;
	}

	public boolean isSuaVar() {
		return suaVar;
	}

	public void setSuaVar(boolean suaVar) {
		this.suaVar = suaVar;
	}

	public String getBolumAciklama() {
		return bolumAciklama;
	}

	public void setBolumAciklama(String bolumAciklama) {
		this.bolumAciklama = bolumAciklama;
	}

}
