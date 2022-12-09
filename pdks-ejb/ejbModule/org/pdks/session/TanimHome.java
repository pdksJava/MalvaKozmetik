package org.pdks.session;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.Tanim;
import org.pdks.security.entity.User;

@Name("tanimHome")
public class TanimHome extends EntityHome<Tanim> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2932981173320724234L;
	static Logger logger = Logger.getLogger(TanimHome.class);

	@RequestParameter
	Long tanimId;
	@In(create = true)
	PdksEntityController pdksEntityController;
	@In(required = false)
	User authenticatedUser;
	@In(required = false, create = true)
	EntityManager entityManager;

	private Tanim genelTanim = new Tanim();
	private List<Tanim> genelTanimList = new ArrayList<Tanim>();
	private List<Tanim> tanimList = new ArrayList<Tanim>();
	private List<Tanim> childTanimList = new ArrayList<Tanim>();
	private Tanim selectedParentTanim = new Tanim();
	private Session session;

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	@Override
	public Object getId() {
		if (tanimId == null) {
			return super.getId();
		} else {
			return tanimId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	public Tanim getGenelTanim() {
		/*
		 * HashMap parametreMap = new HashMap(); parametreMap.put("id",new Long("4")); List<Tanim> genelTanimList = pdksEntityController.getObjectByInnerObjectList( parametreMap, Tanim.class, ""); genelTanim=genelTanimList.get(0);
		 */
		return genelTanim;
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		setGenelTanim(new Tanim());
		setSelectedParentTanim(new Tanim());
		setTanimList(new ArrayList<Tanim>());
		setChildTanimList(new ArrayList<Tanim>());
		fiilGenelTanimList();
	}

	public void setGenelTanim(Tanim genelTanim) {
		this.genelTanim = genelTanim;

		if (genelTanim != null)
			getTanimByGenelTanim(genelTanim);
		else
			setTanimList(new ArrayList<Tanim>());
	}

	/**
	 * @param list
	 * @param sayfaAdi
	 * @return
	 */
	public String tanimExcel(String tip, String sayfaAdi) {
		ByteArrayOutputStream baosDosya = null;
		try {
			List<Tanim> excelList = tip != null && tip.equalsIgnoreCase("P") ? tanimList : childTanimList;
			if (excelList != null && !excelList.isEmpty())
				baosDosya = excelDevam(excelList, sayfaAdi);
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		if (baosDosya != null) {
			String dosyaAdi = sayfaAdi + ".xlsx";
			PdksUtil.setExcelHttpServletResponse(baosDosya, dosyaAdi);

		}
		return null;
	}

	/**
	 * @param list
	 * @param sayfaAdi
	 * @return
	 */
	private ByteArrayOutputStream excelDevam(List<Tanim> list, String sayfaAdi) {
		ByteArrayOutputStream baos = null;
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = ExcelUtil.createSheet(wb, sayfaAdi, Boolean.TRUE);
		XSSFCellStyle styleCenter = (XSSFCellStyle) ExcelUtil.getStyleData(wb);
		styleCenter.setAlignment(CellStyle.ALIGN_CENTER);
		XSSFCellStyle styleGenel = (XSSFCellStyle) ExcelUtil.getStyleData(wb);
		XSSFCellStyle styleOddCenter = (XSSFCellStyle) ExcelUtil.getStyleData(wb);
		styleOddCenter.setAlignment(CellStyle.ALIGN_CENTER);
		XSSFCellStyle styleEvenCenter = (XSSFCellStyle) ExcelUtil.getStyleData(wb);
		styleEvenCenter.setAlignment(CellStyle.ALIGN_CENTER);
		XSSFCellStyle styleTatil = (XSSFCellStyle) ExcelUtil.getStyleData(wb);
		styleTatil.setAlignment(CellStyle.ALIGN_CENTER);
		XSSFCellStyle header = (XSSFCellStyle) ExcelUtil.getStyleHeader(wb);
		int col = 0, row = 0;
		ExcelUtil.getCell(sheet, row, col, header).setCellValue("Kodu");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("ERP Kodu");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Türkçe");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İngilizce");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Durum");
		if (authenticatedUser.isAdmin())
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("ID");
		for (Tanim tanim : list) {
			++row;
			col = 0;
			ExcelUtil.getCell(sheet, row, col, styleCenter).setCellValue(tanim.getKodu());
			ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(tanim.getErpKodu());
			ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(tanim.getAciklamatr());
			ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(tanim.getAciklamaen());
			ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(User.getDurumAciklamaAna(tanim.getDurum()));
			if (authenticatedUser.isAdmin())
				ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(tanim.getId());
		}
		try {

			for (int i = 0; i < col; i++)
				sheet.autoSizeColumn(i);

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

	private void getTanimByGenelTanim(Tanim genelTanim) {

		if (genelTanim.getKodu() != null) {
			HashMap fields = new HashMap();
			fields.put("tipi", genelTanim.getKodu());
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			try {
				tanimList = pdksEntityController.getObjectByInnerObjectList(fields, Tanim.class);
				if (tanimList.size() > 1)
					tanimList = PdksUtil.sortObjectStringAlanList(tanimList, "getKodu", null);
			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());

			}

			fields = null;
		}
		fillChildTanimList(null);
	}

	public Tanim getSelectedParentTanim() {
		return selectedParentTanim;
	}

	public void setSelectedParentTanim(Tanim selectedParentTanim) {
		this.selectedParentTanim = selectedParentTanim;
	}

	public String tanimEkle(Tanim anaTanim) {
		Tanim tanim = new Tanim();
		if (anaTanim == null)
			tanim.setTipi(genelTanim.getKodu());
		else {
			tanim.setTipi(genelTanim.getChildGenelTanim().getKodu());
			tanim.setParentTanim(anaTanim);
		}
		tanim.setGuncelle(!authenticatedUser.isAdmin());
		setInstance(tanim);
		return "";
	}

	public String guncelle(Tanim tanim) {
		setInstance(tanim);
		return "";

	}

	@Transactional
	public String save() {
		Tanim tanim = getInstance();
		tanimKaydet(tanim);
		getTanimByGenelTanim(genelTanim);
		return "";

	}

	@Transactional
	public String saveChild() {
		Tanim tanim = getInstance();
		tanimKaydet(tanim);
		fillChildTanimList(tanim.getParentTanim());
		return "";

	}

	private void tanimKaydet(Tanim tanim) {
		tanim.setIslemTarihi(new Date());
		tanim.setIslemYapan(authenticatedUser);
		pdksEntityController.saveOrUpdate(session, entityManager, tanim);
		session.flush();
	}

	public void fiilGenelTanimList() {
		List<Tanim> list = null;
		HashMap parametreMap = new HashMap();
		parametreMap.put("tipi=", Tanim.TIPI_GENEL_TANIM);
		if (!authenticatedUser.isAdmin()) {
			parametreMap.put("kodu<>", Tanim.TIPI_GENEL_TANIM);
			parametreMap.put("guncelle=", Boolean.TRUE);
			parametreMap.put("durum=", Boolean.TRUE);

		}
		try {
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			list = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, Tanim.class);

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}
		HashMap<Long, Tanim> bagliOlmayanGenelTanimMap = new HashMap<Long, Tanim>();
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			Tanim tanim = (Tanim) iterator.next();
			if (tanim.getParentTanim() == null) {
				bagliOlmayanGenelTanimMap.put(tanim.getId(), tanim);
				iterator.remove();
			}
		}
		for (Tanim childGenelTanim : list) {
			if (bagliOlmayanGenelTanimMap.containsKey(childGenelTanim.getParentTanim().getId()))
				bagliOlmayanGenelTanimMap.get(childGenelTanim.getParentTanim().getId()).setChildGenelTanim(childGenelTanim);
		}
		list = PdksUtil.sortObjectStringAlanList(new ArrayList<Tanim>(bagliOlmayanGenelTanimMap.values()), "getAciklama", null);
		setGenelTanimList(list);
	}

	public List<Tanim> getTanimList() {
		return tanimList;
	}

	public String fillChildTanimList(Tanim parentTanim) {

		if (parentTanim != null && parentTanim.getId() != null) {
			HashMap parametreMap = new HashMap();
			parametreMap.put("parentTanim.kodu", parentTanim.getTipi());
			parametreMap.put("parentTanim.tipi", Tanim.TIPI_GENEL_TANIM);
			parametreMap.put("tipi", Tanim.TIPI_GENEL_TANIM);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			Tanim parentGenelTanim = (Tanim) pdksEntityController.getObjectByInnerObject(parametreMap, Tanim.class);
			parametreMap.clear();
			parametreMap.put("parentTanim.id", parentTanim.getId());
			if (parentGenelTanim != null)
				parametreMap.put("tipi", parentGenelTanim.getKodu());
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			childTanimList = pdksEntityController.getObjectByInnerObjectList(parametreMap, Tanim.class);
			if (childTanimList.size() > 1)
				childTanimList = PdksUtil.sortListByAlanAdi(childTanimList, "koduLong", Boolean.FALSE);
			setSelectedParentTanim(parentTanim);
		} else {
			childTanimList = new ArrayList<Tanim>();
		}

		return "";

	}

	public List<Tanim> getChildTanimList() {
		return childTanimList;
	}

	public void selectTanim(Tanim id) {
		logger.debug(id.getAciklama());
	}

	public void setGenelTanimList(List<Tanim> genelTanimList) {
		this.genelTanimList = genelTanimList;
	}

	public List<Tanim> getGenelTanimList() {

		return genelTanimList;
	}

	public void setTanimList(List<Tanim> tanimList) {
		this.tanimList = tanimList;
	}

	public void setChildTanimList(List<Tanim> childTanimList) {
		this.childTanimList = childTanimList;
	}

	public String getDurumAciklama() {
		return getInstance().getId() != null ? " Güncelle" : " Ekle";
	}
}
