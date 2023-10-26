package org.pdks.session;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.AramaSecenekleri;
import org.pdks.entity.Dosya;
import org.pdks.entity.IzinTipi;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelIzin;
import org.pdks.entity.PersonelIzinDetay;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.TempIzin;
import org.pdks.entity.VardiyaGun;
import org.pdks.pdf.action.PDFUtils;
import org.pdks.quartz.IzinBakiyeGuncelleme;
import org.pdks.security.action.StartupAction;
import org.pdks.security.entity.MenuItemConstant;
import org.pdks.security.entity.User;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.UploadItem;

import com.lowagie.text.Cell;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;

@Name("personelKalanIzinHome")
public class PersonelKalanIzinHome extends EntityHome<PersonelIzin> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4183382367447830720L;
	static Logger logger = Logger.getLogger(PersonelKalanIzinHome.class);

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

	@In(required = false, create = true)
	StartupAction startupAction;

	@In(required = false)
	FacesMessages facesMessages;

	private Dosya izinBakiyeDosya = new Dosya();

	private List<IzinTipi> izinTipiList;
	private TempIzin updateTempIzin;
	private PersonelIzin updateIzin;
	private List<PersonelIzin> personelizinList = new ArrayList<PersonelIzin>();
	private List<TempIzin> pdksPersonelList = new ArrayList<TempIzin>(), personelBakiyeIzinList = new ArrayList<TempIzin>();

	private String kidemYili, bolumAciklama;
	private boolean gecmisYil, gelecekIzinGoster, geciciBakiye, bolumKlasorEkle, suaVar, istenAyrilanEkle = Boolean.FALSE, iptalIzinleriGetir = Boolean.FALSE;
	private Date donemSonu, hakedisTarihi;
	private Double izinSuresi, bakiyeSuresi, bakiyeleriTemizle;
	private List<PersonelIzinDetay> harcananIzinler;

	private int minYil, maxYil, bakiyeYil, updateAdet, baslangicYil;

	private AramaSecenekleri aramaSecenekleri = null;
	private Session session;

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

	public String izinKarti(TempIzin izin) {
		updateTempIzin = izin;
		suaVar = Boolean.FALSE;
		HashMap fields = new HashMap();
		fields.put("id", izin.getIzinler().clone());
		if (izin.getPersonel().getSirket().getDepartman().isAdminMi())
			fields.put("baslangicZamani>", PdksUtil.getBakiyeYil());
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<PersonelIzin> bakiyeIzinler = pdksEntityController.getObjectByInnerObjectListInLogic(fields, PersonelIzin.class);
		fields = null;
		if (gelecekIzinGoster)
			bakiyeIzinler = borcluIzinleriSifirla(bakiyeIzinler);
		for (PersonelIzin personelIzin : bakiyeIzinler) {
			if (personelIzin.getIzinTipi() != null && personelIzin.getIzinTipi().getBakiyeIzinTipi() != null && personelIzin.getIzinTipi().getBakiyeIzinTipi().isSuaIzin()) {
				suaVar = Boolean.TRUE;
				break;
			}
		}

		updateTempIzin.setYillikIzinler((ArrayList<PersonelIzin>) PdksUtil.sortListByAlanAdi(bakiyeIzinler, "baslangicZamani", Boolean.FALSE));
		updateTempIzin.bakiyeHesapla();
		return "";
	}

	public String pdfAktar(TempIzin izin) {
		updateTempIzin = izin;
		suaVar = Boolean.FALSE;

		HashMap fields = new HashMap();
		fields.put("id", izin.getIzinler().clone());
		if (izin.getPersonel().getSirket().getDepartman().isAdminMi() /* || authenticatedUser.isIK() */)
			fields.put("baslangicZamani>", PdksUtil.getBakiyeYil());
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<PersonelIzin> bakiyeIzinler = pdksEntityController.getObjectByInnerObjectListInLogic(fields, PersonelIzin.class);
		if (gelecekIzinGoster)
			bakiyeIzinler = borcluIzinleriSifirla(bakiyeIzinler);
		for (PersonelIzin personelIzin : bakiyeIzinler) {
			if (personelIzin.getIzinTipi() != null && personelIzin.getIzinTipi().getBakiyeIzinTipi() != null && personelIzin.getIzinTipi().getBakiyeIzinTipi().isSuaIzin()) {
				suaVar = Boolean.TRUE;
				break;
			}
		}
		ArrayList<PersonelIzin> yillikIzinler = (ArrayList<PersonelIzin>) PdksUtil.sortListByAlanAdi(bakiyeIzinler, "baslangicZamani", Boolean.FALSE);
		for (Iterator iterator = yillikIzinler.iterator(); iterator.hasNext();) {
			PersonelIzin personelIzin = (PersonelIzin) iterator.next();
			personelIzin.setIslemYapildi(iterator.hasNext());
		}
		updateTempIzin.setYillikIzinler(yillikIzinler);
		updateTempIzin.bakiyeHesapla();
		String sayfa = pdfTekAktar(updateTempIzin);
		return sayfa;
	}

	public Object getImage() {
		InputStream is = startupAction.getProjeHeaderImage() != null ? new ByteArrayInputStream(startupAction.getProjeHeaderImage()) : null;
		StreamedContent content = null;
		try {
			content = new DefaultStreamedContent(is, "/image/jpeg", authenticatedUser.getSession().getId());
		} catch (Exception e) {
			logger.error(e);
		}
		return content;
	}

	public void pdfYaz() {
		ByteArrayOutputStream baosPDF = new ByteArrayOutputStream();
		String fileName = "izinKarti_" + updateTempIzin.getPersonel().getPdksSicilNo() + ".pdf";
		try {
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
			ServletOutputStream sos = response.getOutputStream();
			response.setContentType("application/pdf");
			response.setHeader("Content-Type", "Content-Type: text/plain; charset=ISO-8859-9");
			response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
			/** ***********PDF DOSYASI YARATMA KODLARI************ */
			Document doc = new Document(PageSize.A4.rotate(), -60, -60, 30, 30);
			PdfWriter docWriter = PdfWriter.getInstance(doc, baosPDF);
			FileOutputStream fileOutputStream = new FileOutputStream(fileName);
			PdfWriter.getInstance(doc, fileOutputStream);
			doc.open();
			BaseFont bfArial = BaseFont.createFont("ARIAL.TTF", BaseFont.IDENTITY_H, BaseFont.EMBEDDED); // iste
			Font fontH = new Font(bfArial, 8f, Font.BOLD);
			Font fontBaslik = new Font(bfArial, 14f, Font.BOLD);
			Font font = new Font(bfArial, 7f, Font.NORMAL);

			Table table = null;

			Paragraph paragraph = new Paragraph();
			paragraph.setAlignment(Element.ALIGN_LEFT);

			Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
			NumberFormat nf = DecimalFormat.getNumberInstance(locale);

			for (Iterator iterator = updateTempIzin.getYillikIzinler().iterator(); iterator.hasNext();) {

				PersonelIzin bakiyeIzin = (PersonelIzin) iterator.next();
				try {

					Personel personel = updateTempIzin.getPersonel();
					doc.add(PDFUtils.getParagraph("YILLIK ÜCRETLİ İZİN KARTI", fontBaslik, Element.ALIGN_CENTER));
					table = new Table(15);
					table.setBorder(0);
					table.setWidths(new float[] { 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3 });
					table.addCell(PDFUtils.getCell("Gruba Giriş Tarihi", fontH, Element.ALIGN_CENTER, 2));
					table.addCell(PDFUtils.getCell(PdksUtil.convertToDateString(personel.getGrubaGirisTarihi(), PdksUtil.getDateFormat()), font, Element.ALIGN_CENTER, 2));
					table.addCell(PDFUtils.getCell(ortakIslemler.kidemBasTarihiAciklama(), fontH, Element.ALIGN_CENTER, 2));
					table.addCell(PDFUtils.getCell(PdksUtil.convertToDateString(personel.getIzinHakEdisTarihi(), PdksUtil.getDateFormat()), font, Element.ALIGN_CENTER, 3));
					table.addCell(PDFUtils.getCell("Adı Soyadı", fontH, Element.ALIGN_CENTER, 2));
					table.addCell(PDFUtils.getCell(personel.getAdSoyad(), font, Element.ALIGN_CENTER, 4));
					table.addCell(PDFUtils.getCell("" + ortakIslemler.sirketAciklama() + "e Giriş Tarihi", fontH, Element.ALIGN_CENTER, 2));
					table.addCell(PDFUtils.getCell(PdksUtil.convertToDateString(personel.getIseBaslamaTarihi(), PdksUtil.getDateFormat()), font, Element.ALIGN_CENTER, 2));
					table.addCell(PDFUtils.getCell("Doğum Tarihi", fontH, Element.ALIGN_CENTER, 2));
					table.addCell(PDFUtils.getCell(PdksUtil.convertToDateString(personel.getDogumTarihi(), PdksUtil.getDateFormat()), font, Element.ALIGN_CENTER, 3));
					table.addCell(PDFUtils.getCell("Önceki Soyadı", fontH, Element.ALIGN_CENTER, 2));
					table.addCell(PDFUtils.getCell("", font, Element.ALIGN_CENTER, 2));
					table.addCell(PDFUtils.getCell(ortakIslemler.personelNoAciklama(), fontH, Element.ALIGN_CENTER));
					table.addCell(PDFUtils.getCell(personel.getPdksSicilNo(), font, Element.ALIGN_CENTER));

					table.addCell(PDFUtils.getCell("Yılı", fontH, Element.ALIGN_CENTER));
					table.addCell(PDFUtils.getCell("Bir Yıl Önceki İzin Hakkını Kazandığı Tarih", fontH, Element.ALIGN_CENTER));

					Cell cellDiger = new Cell();

					Table digerTable = new Table(6);
					digerTable.setWidths(new float[] { 2, 2, 2, 2, 2, 2 });
					digerTable.addCell(PDFUtils.getCell("Bir Yıllık Çalışma Süresi İçinde Çalışılmayan Gün Sayısı ve Nedenleri", fontH, Element.ALIGN_CENTER, 6));
					digerTable.addCell(PDFUtils.getCell("Hastalık", fontH, Element.ALIGN_CENTER));
					digerTable.addCell(PDFUtils.getCell("Askerlik", fontH, Element.ALIGN_CENTER));
					digerTable.addCell(PDFUtils.getCell("Zorunluluk Hali", fontH, Element.ALIGN_CENTER));
					digerTable.addCell(PDFUtils.getCell("Devamsızlık", fontH, Element.ALIGN_CENTER));
					digerTable.addCell(PDFUtils.getCell("Hizmete Ara Verme", fontH, Element.ALIGN_CENTER));
					digerTable.addCell(PDFUtils.getCell("Diğer Nedenler", fontH, Element.ALIGN_CENTER));
					cellDiger.setColspan(6);
					// cellDiger.add(digerTable);
					table.addCell(cellDiger);

					table.addCell(PDFUtils.getCell("İzne Hak Kazandığı Tarih", fontH, Element.ALIGN_CENTER));
					table.addCell(PDFUtils.getCell("İşyerindeki Kıdemi (Yıl)", fontH, Element.ALIGN_CENTER));
					table.addCell(PDFUtils.getCell("Hakettiği İzin (işgünü)", fontH, Element.ALIGN_CENTER));
					table.addCell(PDFUtils.getCell("İzin Süresi (İşgünü)", fontH, Element.ALIGN_CENTER));
					table.addCell(PDFUtils.getCell("İzne Başlangıç Tarihi", fontH, Element.ALIGN_CENTER));
					table.addCell(PDFUtils.getCell("İzinden Dönüş Tarihi", fontH, Element.ALIGN_CENTER));
					table.addCell(PDFUtils.getCell("Çalışanın İmzası", fontH, Element.ALIGN_CENTER));

					table.addCell(PDFUtils.getCell(PdksUtil.convertToDateString(bakiyeIzin.getBaslangicZamani(), "yyyy"), font, Element.ALIGN_CENTER));
					table.addCell(PDFUtils.getCell(bakiyeIzin.getBirOncekiHakedisTarih() != null ? PdksUtil.convertToDateString(bakiyeIzin.getBirOncekiHakedisTarih(), PdksUtil.getDateFormat()) : "", font, Element.ALIGN_CENTER));

					Cell cellPersonelDiger = new Cell();

					Table digerPersonelTable = new Table(6);
					digerPersonelTable.setWidths(new float[] { 2, 2, 2, 2, 2, 2 });
					digerPersonelTable.addCell(PDFUtils.getCell("", font, Element.ALIGN_CENTER));
					digerPersonelTable.addCell(PDFUtils.getCell("", font, Element.ALIGN_CENTER));
					digerPersonelTable.addCell(PDFUtils.getCell("", font, Element.ALIGN_CENTER));
					digerPersonelTable.addCell(PDFUtils.getCell("", font, Element.ALIGN_CENTER));
					digerPersonelTable.addCell(PDFUtils.getCell("", font, Element.ALIGN_CENTER));
					digerPersonelTable.addCell(PDFUtils.getCell("", font, Element.ALIGN_CENTER));
					cellPersonelDiger.setColspan(6);
					// cellPersonelDiger.add(digerPersonelTable);
					table.addCell(cellPersonelDiger);

					table.addCell(PDFUtils.getCell(PdksUtil.convertToDateString(bakiyeIzin.getBitisZamani(), PdksUtil.getDateFormat()), font, Element.ALIGN_CENTER));
					table.addCell(PDFUtils.getCell(bakiyeIzin.getAciklama(), font, Element.ALIGN_CENTER));
					table.addCell(PDFUtils.getCell(nf.format(bakiyeIzin.getIzinSuresi()), font, Element.ALIGN_CENTER));

					Cell cellPersonelHarcanan = new Cell();

					Table harcananTable = new Table(3);

					harcananTable.setWidths(new float[] { 2, 2, 2 });
					if (bakiyeIzin.getHarcananDigerIzinler() != null && !bakiyeIzin.getHarcananDigerIzinler().isEmpty()) {
						for (PersonelIzin harcananIzin : bakiyeIzin.getHarcananDigerIzinler()) {
							harcananTable.addCell(PDFUtils.getCell(nf.format(harcananIzin.getIzinSuresi()), font, Element.ALIGN_CENTER));
							harcananTable.addCell(PDFUtils.getCell(PdksUtil.convertToDateString(harcananIzin.getBaslangicZamani(), PdksUtil.getDateFormat()), font, Element.ALIGN_CENTER));
							harcananTable.addCell(PDFUtils.getCell(PdksUtil.convertToDateString(harcananIzin.getBitisZamani(), PdksUtil.getDateFormat()), font, Element.ALIGN_CENTER));
						}
					} else {
						harcananTable.addCell(PDFUtils.getCell("", font, Element.ALIGN_CENTER));
						harcananTable.addCell(PDFUtils.getCell("", font, Element.ALIGN_CENTER));
						harcananTable.addCell(PDFUtils.getCell("", font, Element.ALIGN_CENTER));
					}
					cellPersonelHarcanan.add(harcananTable);
					cellPersonelHarcanan.setColspan(3);
					table.addCell(cellPersonelHarcanan);
					table.addCell(PDFUtils.getCell("", font, Element.ALIGN_CENTER));
					doc.add(table);
					doc.newPage();

				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());

				}

			}

			doc.close();
			docWriter.close();
			response.setContentLength(baosPDF.size());
			baosPDF.writeTo(sos);
			sos.flush();
			sos.close();
			File file = new File(fileName);
			if (file != null && file.exists())
				file.delete();
			FacesContext.getCurrentInstance().responseComplete();
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}

	}

	public String izinKartiPDF() {
		String sayfa = pdfTekAktar(updateTempIzin);

		return sayfa;
	}

	/**
	 * @param tempIzin
	 * @return
	 */
	private String pdfTekAktar(TempIzin tempIzin) {
		String sayfa = "/izin/izinKartiPdf.xhtml";
		if (tempIzin != null) {
			List<TempIzin> list = new ArrayList<TempIzin>();
			list.add(tempIzin);
			ByteArrayOutputStream baosPDF = null;
			/** *********** DOSYASI YARATMA KODLARI************ */
			try {
				baosPDF = ortakIslemler.izinBakiyeTopluITextPDF(-1, list, false, false);
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}
			if (baosPDF != null && baosPDF.size() > 0) {
				try {
					String ek = "";
					Personel personel = list.get(0).getPersonel();
					if (personel != null)
						ek = "_" + personel.getAdSoyad() + "_" + personel.getPdksSicilNo();

					String fileName = "izinKarti" + ek + ".pdf";
					HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
					ServletOutputStream sos = response.getOutputStream();
					String characterEncoding = "ISO-8859-9";
					response.setContentType("application/pdf;charset=" + characterEncoding);
					response.setCharacterEncoding(characterEncoding);
					String fileNameURL = PdksUtil.encoderURL(fileName, characterEncoding);
					response.setHeader("Content-Disposition", "attachment;filename=" + fileNameURL);
					response.setContentLength(baosPDF.size());
					baosPDF.writeTo(sos);
					sos.flush();
					sos.close();
					FacesContext.getCurrentInstance().responseComplete();
					sayfa = null;
				} catch (Exception e) {
					logger.error(e);
				}

			}
			list = null;
		}
		return sayfa;
	}

	public String pdfTopluYazBasla() {
		baslangicYil = 0;
		for (TempIzin tempIzin : pdksPersonelList) {
			if (tempIzin.getToplamBakiyeIzin() == 0.0d)
				continue;
			if (baslangicYil == 0)
				baslangicYil = PdksUtil.getDateField(new Date(), Calendar.YEAR) + 1;
			for (Iterator iterator = tempIzin.getYillikIzinler().iterator(); iterator.hasNext();) {
				PersonelIzin bakiyeIzin = (PersonelIzin) iterator.next();
				String bakiyeYil = PdksUtil.convertToDateString(bakiyeIzin.getBaslangicZamani(), "yyyy");
				bolumKlasorEkle = authenticatedUser.isIK();
				if (Integer.parseInt(bakiyeYil) < baslangicYil)
					baslangicYil = Integer.parseInt(bakiyeYil);
			}
		}
		if (baslangicYil == 0)
			PdksUtil.addMessageAvailableWarn("Bakiye bilgisi yok!");
		return "";

	}

	/**
	 * @param zipDosya
	 * @return
	 */
	public String pdfTopluYaz(boolean zipDosya) {
		if (pdksPersonelList.size() == 1)
			zipDosya = false;
		ByteArrayOutputStream baosPDF = null;
		/** *********** DOSYASI YARATMA KODLARI************ */
		try {
			// baosPDF = ortakIslemler.izinBakiyeTopluLowagiePDF(baslangicYil, pdksPersonelList, zipDosya, bolumKlasorEkle);
			baosPDF = ortakIslemler.izinBakiyeTopluITextPDF(baslangicYil, pdksPersonelList, zipDosya, bolumKlasorEkle);
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		try {
			if (baosPDF != null && baosPDF.size() > 0) {
				String ek = "";
				if (pdksPersonelList.size() > 1) {
					if (aramaSecenekleri.getEkSaha1Id() != null) {
						HashMap fields = new HashMap();
						fields.put("id", aramaSecenekleri.getEkSaha1Id());
						if (session != null)
							fields.put(PdksEntityController.MAP_KEY_SESSION, session);
						Tanim departmanTanim = (Tanim) pdksEntityController.getObjectByInnerObject(fields, Tanim.class);
						if (departmanTanim != null)
							ek = "_" + PdksUtil.replaceAll(PdksUtil.setTurkishStr(departmanTanim.getAciklama()), " ", "_");
					} else if (aramaSecenekleri.getSirketId() != null) {
						HashMap fields = new HashMap();
						fields.put("id", aramaSecenekleri.getSirketId());
						if (session != null)
							fields.put(PdksEntityController.MAP_KEY_SESSION, session);
						Sirket sirket = (Sirket) pdksEntityController.getObjectByInnerObject(fields, Sirket.class);
						if (sirket != null)
							ek = "_" + PdksUtil.replaceAll(PdksUtil.setTurkishStr(sirket.getAd()), " ", "_");
					}
				} else {
					Personel personel = pdksPersonelList.get(0).getPersonel();
					if (personel != null)
						ek = "_" + personel.getAdSoyad() + "_" + personel.getPdksSicilNo();
				}
				String fileName = "izinKarti" + ek + (zipDosya ? ".zip" : ".pdf");
				HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
				ServletOutputStream sos = response.getOutputStream();
				String characterEncoding = "ISO-8859-9";
				if (zipDosya == false)
					response.setContentType("application/pdf;charset=" + characterEncoding);
				else
					response.setContentType("application/zip;charset=" + characterEncoding);
				response.setCharacterEncoding(characterEncoding);
				response.setHeader("Content-Disposition", "attachment;filename=" + PdksUtil.encoderURL(fileName, characterEncoding));
				response.setContentLength(baosPDF.size());
				baosPDF.writeTo(sos);
				sos.flush();
				sos.close();
				File file = new File(fileName);
				if (file != null && file.exists())
					file.delete();
				FacesContext.getCurrentInstance().responseComplete();
			}
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}
		return "";

	}

	public String guncelle(PersonelIzin izin) throws Exception {
		double sure = izin.getKontrolIzin() == null ? izin.getIzinSuresi() : izin.getKontrolIzin().getIzinSuresi();
		setBakiyeSuresi(izin.getKalanIzin());
		setIzinSuresi(sure);
		kidemYili = izin.getAciklama();
		hakedisTarihi = izin.getBitisZamani();
		int bakiyeYil = PdksUtil.getDateField(izin.getBaslangicZamani(), Calendar.YEAR);

		boolean gecmisTarih = PdksUtil.getSistemBaslangicYili() > bakiyeYil;
		if (!gecmisTarih && (authenticatedUser.isAdmin() || authenticatedUser.isIKAdmin())) {
			Calendar cal = Calendar.getInstance();
			Date bugun = PdksUtil.getDate((Date) cal.getTime().clone());
			cal.setTime(izin.getIzinSahibi().getIzinHakEdisTarihi());
			cal.set(Calendar.YEAR, bakiyeYil);
			Date hakedisTarihi = PdksUtil.getDate((Date) cal.getTime().clone());
			boolean haketti = PdksUtil.tarihKarsilastirNumeric(bugun, hakedisTarihi) == 1;
			gecmisTarih = !haketti;
		}
		setGecmisYil(gecmisTarih);
		setUpdateIzin((PersonelIzin) izin.clone());
		return "";

	}

	public void izinleriBakiyeleriniHesapla() throws Exception {
		String sicilNo = aramaSecenekleri.getSicilNo();
		if (PdksUtil.hasStringValue(sicilNo) == false && aramaSecenekleri.getSirketId() == null && (authenticatedUser.isIK() || authenticatedUser.isAdmin()))
			PdksUtil.addMessageError("" + ortakIslemler.sirketAciklama() + " seçiniz!");
		else {
			ArrayList<String> siciller = ortakIslemler.getAramaPersonelSicilNo(aramaSecenekleri, Boolean.TRUE, session);
			if (!siciller.isEmpty()) {
				String hata = null;
				try {

					izinBakiyeGuncelleme.izinleriBakiyeleriniHesapla(session, siciller, aramaSecenekleri.getSirket(), null, Boolean.FALSE, Boolean.TRUE);
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());
					hata = e.getMessage();
				}
				fillIzinList(null);
				if (hata == null)
					PdksUtil.addMessageInfo("İzin bakiyeleri güncellendi.");
				else
					PdksUtil.addMessageError("İzin bakiyeleri güncellenmedi." + hata);

			}
		}

	}

	@Transactional
	public String sifirla(PersonelIzin izin) {
		String durum = "persist";
		try {
			if (izin != null && izin.getIzinKagidiGeldi() == null) {
				izin.setIzinSuresi(0.0D);
				izin.setIzinKagidiGeldi(Boolean.FALSE);
				if (!authenticatedUser.isAdmin()) {
					izin.setGuncellemeTarihi(new Date());
					izin.setGuncelleyenUser(authenticatedUser);
				}
				pdksEntityController.saveOrUpdate(session, entityManager, izin);
				session.flush();
				TempIzin tempIzin = updateTempIzin;
				tempIzin.setToplamBakiyeIzin(0.0d);
				for (Iterator iterator = tempIzin.getYillikIzinler().iterator(); iterator.hasNext();) {
					PersonelIzin personelIzin = (PersonelIzin) iterator.next();
					if (!personelIzin.getId().equals(izin.getId())) {
						tempIzin.setToplamBakiyeIzin(tempIzin.getToplamBakiyeIzin() + personelIzin.getIzinSuresi());
					} else
						iterator.remove();

				}

			}

			// fillIzinList(null);
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			durum = "";
			PdksUtil.addMessageError(e.getMessage());
		}
		return durum;
	}

	@Transactional
	public String sil(PersonelIzin izin) {
		String durum = "persist";
		try {
			ortakIslemler.bakiyeIzinSil(izin, session);
			session.flush();
			fillIzinList(null);
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			durum = "";
			PdksUtil.addMessageError(e.getMessage());
		}
		return durum;
	}

	@Transactional
	public String kaydet(Boolean kayitDurum) {
		String durum = "persist";
		try {
			boolean sureGuncelleme = authenticatedUser.isIK();
			HashMap fields = new HashMap();
			fields.put("id", updateIzin.getId());
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			updateIzin = (PersonelIzin) pdksEntityController.getObjectByInnerObject(fields, PersonelIzin.class);
			updateIzin.setIzinKagidiGeldi(null);
			updateIzin.setAciklama(kidemYili);
			updateIzin.setBitisZamani(hakedisTarihi);
			if (!authenticatedUser.isAdmin() && !updateIzin.isRedmi()) {
				updateIzin.setGuncelleyenUser(authenticatedUser);
				updateIzin.setGuncellemeTarihi(new Date());
			}
			updateIzin.setIzinDurumu(PersonelIzin.IZIN_DURUMU_ONAYLANDI);
			if (sureGuncelleme)
				updateIzin.setIzinSuresi(izinSuresi);
			updateIzin.setIzinKagidiGeldi(kayitDurum.equals(Boolean.FALSE) || izinSuresi > 0.0d ? null : Boolean.FALSE);
			pdksEntityController.saveOrUpdate(session, entityManager, updateIzin);
			session.flush();
			fillIzinList(null);

		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			durum = "";
			PdksUtil.addMessageError(e.getMessage());

		}

		return durum;

	}

	private void fillEkSahaTanim() {

		ortakIslemler.fillEkSahaTanimAramaSecenekAta(session, Boolean.FALSE, null, aramaSecenekleri);
		HashMap sonucMap = ortakIslemler.fillEkSahaTanim(session, Boolean.FALSE, null);
		bolumAciklama = (String) sonucMap.get("bolumAciklama");
		if (aramaSecenekleri.getSirketList() != null) {
			List<SelectItem> sirketIdList = ortakIslemler.getIzinSirketItemList(aramaSecenekleri.getSirketList());
			aramaSecenekleri.setSirketIdList(sirketIdList);
			if (aramaSecenekleri.getSirketIdList().size() == 1)
				aramaSecenekleri.setSirketId((Long) aramaSecenekleri.getSirketIdList().get(0).getValue());
		}

	}

	public void instanceRefresh() {
		if (getInstance().getId() != null)
			session.refresh(getInstance());
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void bakiyeGuncelleSayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		minYil = PdksUtil.getSistemBaslangicYili();
		Calendar cal = Calendar.getInstance();
		maxYil = cal.get(Calendar.YEAR) - 1;
		bakiyeYil = maxYil;
		personelizinList.clear();
		updateAdet = 50;
		geciciBakiye = false;
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public String sayfaGirisAction() throws Exception {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		aramaSecenekleri = null;
		if (authenticatedUser.isAdmin() == false || aramaSecenekleri == null)
			aramaSecenekleri = new AramaSecenekleri(authenticatedUser);

		aramaSecenekleri.setSirketIzinKontrolYok(false);
		fillEkSahaTanim();
		List<IzinTipi> list = sayfaGiris(session);
		String pos = "";
		if (list == null || list.isEmpty()) {
			PdksUtil.addMessageWarn("Bakiye izin takibi yapılmamaktadır!");
			pos = MenuItemConstant.home;
		}

		return pos;

	}

	/**
	 * @param session
	 * @return
	 * @throws Exception
	 */
	private List<IzinTipi> sayfaGiris(Session session) throws Exception {
		session.clear();
		List<IzinTipi> izinTipiList = getYillikIzinTipleri(session);
		if (izinTipiList != null && !izinTipiList.isEmpty()) {
			iptalIzinleriGetir = Boolean.FALSE;
			setGelecekIzinGoster(Boolean.FALSE);
			izinBakiyeDosya.setDosyaIcerik(null);

			setDonemSonu(null);
			if (authenticatedUser.isIK() || authenticatedUser.isAdmin())
				setPdksPersonelList(new ArrayList<TempIzin>());
			else {
				istenAyrilanEkle = Boolean.FALSE;

				gelecekIzinGoster = Boolean.FALSE;
				fillIzinList(null);
			}

		}

		return izinTipiList;
	}

	/**
	 * @param session
	 * @return
	 */
	private List<IzinTipi> getYillikIzinTipleri(Session session) {
		String uygulamaTipi = ortakIslemler.getParameterKey("uygulamaTipi");
		List<String> tipler = null;
		if (PdksUtil.hasStringValue(uygulamaTipi) == false || uygulamaTipi.equalsIgnoreCase("H"))
			tipler = Arrays.asList(new String[] { IzinTipi.YILLIK_UCRETLI_IZIN, IzinTipi.SUA_IZNI });
		else
			tipler = Arrays.asList(new String[] { IzinTipi.YILLIK_UCRETLI_IZIN });
		HashMap fields = new HashMap();
		fields.put("durum=", Boolean.TRUE);
		fields.put("bakiyeIzinTipi.izinTipiTanim.kodu", tipler);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<IzinTipi> list = pdksEntityController.getObjectByInnerObjectListInLogic(fields, IzinTipi.class);
		return list;
	}

	public void fillTarihIzinList() throws Exception {
		Date gecerlilikTarih = donemSonu != null ? donemSonu : new Date();
		fillIzinList(gecerlilikTarih, Boolean.FALSE, Boolean.FALSE);
	}

	public void fillIzinList(Date gecerlilikTarih) throws Exception {
		if (!gelecekIzinGoster)
			gecerlilikTarih = new Date();
		fillIzinList(gecerlilikTarih, gelecekIzinGoster, Boolean.TRUE);
	}

	public void fillIzinList(Date gecerlilikTarih, boolean gelecekIzinGoster, boolean harcananIzinlerHepsi) throws Exception {
		if (istenAyrilanEkle && PdksUtil.hasStringValue(aramaSecenekleri.getSicilNo())) {
			String sicilNo = ortakIslemler.getSicilNo(aramaSecenekleri.getSicilNo().trim());
			HashMap fields = new HashMap();
			fields.put("pdksSicilNo", sicilNo);
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<Personel> personelList = pdksEntityController.getObjectByInnerObjectList(fields, Personel.class);
			if (personelList.size() == 1) {
				Personel personel = personelList.get(0);
				if (personel.isCalisiyor() == false) {
					gecerlilikTarih = personel.getSskCikisTarihi();
					aramaSecenekleri.setSirketId(personel.getSirket().getId());
					aramaSecenekleri.setSicilNo(sicilNo);
					donemSonu = personel.getSskCikisTarihi();
				}
			}
		}
		if (authenticatedUser.isAdmin() || authenticatedUser.isIK()) {
			if (gelecekIzinGoster)
				gecerlilikTarih = null;

		}
		String sicilNo = aramaSecenekleri.getSicilNo();
		setInstance(null);
		HashMap<Long, TempIzin> izinMap = new HashMap<Long, TempIzin>();
		if (PdksUtil.hasStringValue(sicilNo) == false && aramaSecenekleri.getSirketId() == null && (authenticatedUser.isIK() || authenticatedUser.isAdmin()))
			PdksUtil.addMessageError("" + ortakIslemler.sirketAciklama() + " seçiniz!");
		else {
			ArrayList<String> sicilNoList = ortakIslemler.getAramaPersonelSicilNo(aramaSecenekleri, Boolean.TRUE, istenAyrilanEkle, session);
			// ArrayList<String> sicilNoList = ortakIslemler.getPersonelSicilNo(ad, soyad, sicilNo, sirket, seciliEkSaha1, seciliEkSaha2, seciliEkSaha3, seciliEkSaha4, Boolean.TRUE, istenAyrilanEkle, session);
			if (sicilNoList != null) {
				HashMap fields = new HashMap();
				fields.put("id", aramaSecenekleri.getSirketId());
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				Sirket sirket = (Sirket) pdksEntityController.getObjectByInnerObject(fields, Sirket.class);
				izinMap = ortakIslemler.senelikIzinListesiOlustur(sicilNoList, gecerlilikTarih, sirket, harcananIzinlerHepsi, Boolean.TRUE, iptalIzinleriGetir, session);
			}

		}
		personelBakiyeIzinList.clear();
		List<TempIzin> izinList = new ArrayList<TempIzin>(izinMap.values());
		if (istenAyrilanEkle) {
			Date tarih = donemSonu;
			if (tarih == null)
				tarih = PdksUtil.getDate(new Date());
			for (Iterator iterator = izinList.iterator(); iterator.hasNext();) {
				TempIzin tempIzin = (TempIzin) iterator.next();
				if (tarih.after(tempIzin.getPersonel().getSonCalismaTarihi()) || tarih.before(tempIzin.getPersonel().getIseBaslamaTarihi()))
					iterator.remove();

			}
		}
		if (gelecekIzinGoster) {
			Date tarih = PdksUtil.getDate(new Date());
			if (donemSonu != null && donemSonu.after(tarih))
				tarih = donemSonu;
			for (Iterator iterator = izinList.iterator(); iterator.hasNext();) {
				TempIzin tempIzin = (TempIzin) iterator.next();
				if (tempIzin.getYillikIzinler() == null)
					continue;
				ArrayList<PersonelIzin> yillikTemIzinler = new ArrayList<PersonelIzin>(), yillikIzinler = tempIzin.getYillikIzinler();
				boolean guncelle = false;
				boolean sifirla = true;
				if (authenticatedUser.isAdmin() || authenticatedUser.isIK() || authenticatedUser.isSistemYoneticisi())
					sifirla = PersonelIzin.getYillikIzinMaxBakiye() > 0;
				for (PersonelIzin personelIzin : yillikIzinler) {
					PersonelIzin personelIzinNew = (PersonelIzin) personelIzin.clone();
					personelIzinNew.setKontrolIzin(personelIzin);
					if (sifirla && personelIzinNew.getBitisZamani().after(tarih)) {
						guncelle = true;
						double izinSuresi = personelIzinNew.getIzinSuresi(), harcanan = personelIzinNew.getHarcananIzin();
						personelIzinNew.setDonemSonu(tarih);
						harcanan -= personelIzinNew.getHarcananIzin();
						tempIzin.setKullanilanIzin(tempIzin.getKullanilanIzin() - harcanan);
						tempIzin.setToplamBakiyeIzin(tempIzin.getToplamBakiyeIzin() - izinSuresi);
						tempIzin.setToplamKalanIzin(tempIzin.getToplamKalanIzin() - izinSuresi + harcanan);
						personelIzinNew.setIzinSuresi(0D);
					}
					yillikTemIzinler.add(personelIzin);
				}
				if (guncelle) {
					tempIzin.setYillikIzinler(yillikTemIzinler);
				}

			}
		}
		izinMap = null;
		setPdksPersonelList(izinList);
	}

	public ByteArrayOutputStream excelAktarDevam() {
		ByteArrayOutputStream baos = null;
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = ExcelUtil.createSheet(wb, "Izin Karti", Boolean.TRUE);
		CellStyle header = ExcelUtil.getStyleHeader(wb);
		CellStyle styleOdd = ExcelUtil.getStyleOdd(null, wb);
		CellStyle styleOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleOddTutar = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_TUTAR, wb);
		CellStyle styleOddDate = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_DATE, wb);
		CellStyle styleEven = ExcelUtil.getStyleEven(null, wb);
		CellStyle styleEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleEvenTutar = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_TUTAR, wb);
		CellStyle styleEvenDate = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_DATE, wb);

		int row = 0, col = 0;
		boolean ekSaha1 = false, ekSaha2 = false, ekSaha3 = false, ekSaha4 = false;
		HashMap<String, Boolean> map = ortakIslemler.getListEkSahaDurumMap(pdksPersonelList, null);
		if (authenticatedUser.isAdmin() || authenticatedUser.isIKAdmin()) {
			ekSaha1 = aramaSecenekleri.getEkSahaListMap().containsKey("ekSaha1") && map.containsKey("ekSaha1");
			ekSaha2 = aramaSecenekleri.getEkSahaListMap().containsKey("ekSaha2") && map.containsKey("ekSaha2");
			ekSaha3 = aramaSecenekleri.getEkSahaListMap().containsKey("ekSaha3") && map.containsKey("ekSaha3");
			ekSaha4 = aramaSecenekleri.getEkSahaListMap().containsKey("ekSaha4") && map.containsKey("ekSaha4");
		}

		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.sirketAciklama());
		if (ekSaha1)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(aramaSecenekleri.getEkSahaTanimMap().get("ekSaha1").getAciklama());
		if (ekSaha2)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(aramaSecenekleri.getEkSahaTanimMap().get("ekSaha2").getAciklama());
		if (ekSaha3)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(aramaSecenekleri.getEkSahaTanimMap().get("ekSaha3").getAciklama());
		if (ekSaha4)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(aramaSecenekleri.getEkSahaTanimMap().get("ekSaha4").getAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Adı Soyadı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.kidemBasTarihiAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İzin Toplamı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Kullanılan İzin Toplamı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Bakiye İzin Toplamı");
		boolean renk = true;
		for (TempIzin tempIzin : pdksPersonelList) {
			Personel personel = tempIzin.getPersonel();
			CellStyle style = null, styleCenter = null, styleTutar = null, styleDate = null;
			if (renk) {
				styleDate = styleOddDate;
				styleTutar = styleOddTutar;
				style = styleOdd;
				styleCenter = styleOddCenter;
			} else {
				styleDate = styleEvenDate;
				styleTutar = styleEvenTutar;
				style = styleEven;
				styleCenter = styleEvenCenter;
			}
			renk = !renk;
			++row;
			col = 0;
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getSirket().getAd());
			if (ekSaha1)
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha1() != null ? personel.getEkSaha1().getAciklama() : "");
			if (ekSaha2)
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha2() != null ? personel.getEkSaha2().getAciklama() : "");
			if (ekSaha3)
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");
			if (ekSaha4)
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha4() != null ? personel.getEkSaha4().getAciklama() : "");
			ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personel.getPdksSicilNo());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getAdSoyad());
			if (personel.getIzinHakEdisTarihi() != null)
				ExcelUtil.getCell(sheet, row, col++, styleDate).setCellValue(personel.getIzinHakEdisTarihi());
			else
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
			ExcelUtil.getCell(sheet, row, col++, styleTutar).setCellValue(tempIzin.getToplamBakiyeIzin());
			ExcelUtil.getCell(sheet, row, col++, styleTutar).setCellValue(tempIzin.getKullanilanIzin());
			ExcelUtil.getCell(sheet, row, col++, styleTutar).setCellValue(tempIzin.getToplamKalanIzin());
		}

		for (int i = 0; i < col; i++)
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

	public ByteArrayOutputStream izinKartiExcelAktarDevam() {
		ByteArrayOutputStream baos = null;
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = ExcelUtil.createSheet(wb, "Izin Karti", Boolean.TRUE);
		CellStyle header = ExcelUtil.getStyleHeader(wb);
		CellStyle styleOdd = ExcelUtil.getStyleOdd(null, wb);
		CellStyle styleOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleOddDate = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_DATE, wb);
		CellStyle styleOddDateTime = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_DATETIME, wb);
		CellStyle styleEven = ExcelUtil.getStyleEven(null, wb);
		CellStyle styleEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleEvenDate = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_DATE, wb);
		CellStyle styleEvenDateTime = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_DATETIME, wb);
		int row = 0, col = 0;
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("ADI SOYADI");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("GRUBA GİRİŞ");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İŞE GİRİŞ TARİHİ");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İZİN HAKEDİŞ");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("DOĞUM TARİHİ");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İZİN TÜRÜ");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("YIL");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("ÖNCEKİ HAKEDİŞ");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("HAKETTİĞİ TARİH");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("KIDEM");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İZİN GÜN");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İZİN SÜRE");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İZİN BAŞL. TARİHİ");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İZİN BİTİŞ TARİHİ");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İZİN AÇIKLAMA");
		boolean renk = true;
		for (TempIzin tempIzin : pdksPersonelList) {

			boolean durum = tempIzin.getPersonel().getSirket().getDepartman().isAdminMi();
			ArrayList<PersonelIzin> yillikIzinler = (ArrayList<PersonelIzin>) (tempIzin.getYillikIzinler() != null ? PdksUtil.sortListByAlanAdi(tempIzin.getYillikIzinler(), "baslangicZamani", Boolean.FALSE) : null);
			Date oncekiHakedisTarihi = null;
			if (yillikIzinler != null && !yillikIzinler.isEmpty()) {

				for (PersonelIzin personelIzin : yillikIzinler) {
					int yil = PdksUtil.getDateField(personelIzin.getBaslangicZamani(), Calendar.YEAR);
					if (durum && 1900 > yil)
						continue;
					CellStyle style = null, styleCenter = null, styleDateTime = null, styleDate = null;
					if (renk) {
						styleDate = styleOddDate;
						styleDateTime = styleOddDateTime;
						style = styleOdd;
						styleCenter = styleOddCenter;
					} else {
						styleDate = styleEvenDate;
						styleDateTime = styleEvenDateTime;
						style = styleEven;
						styleCenter = styleEvenCenter;
					}
					renk = !renk;
					col = 0;
					List<PersonelIzin> harcananlar = null;
					if (personelIzin.getHarcananIzinler() != null && !personelIzin.getHarcananIzinler().isEmpty()) {
						harcananlar = new ArrayList<PersonelIzin>();
						for (PersonelIzinDetay personelIDetay : personelIzin.getHarcananIzinler()) {
							if (personelIDetay.getPersonelIzin().getIzinDurumu() != PersonelIzin.IZIN_DURUMU_REDEDILDI && personelIDetay.getPersonelIzin().getIzinDurumu() != PersonelIzin.IZIN_DURUMU_SISTEM_IPTAL)
								harcananlar.add(personelIDetay.getPersonelIzin());
						}
						if (!harcananlar.isEmpty()) {
							if (harcananlar.size() > 1)
								harcananlar = PdksUtil.sortListByAlanAdi(harcananlar, "baslangicZamani", Boolean.FALSE);

						} else
							harcananlar = null;
					}

					if (harcananlar != null) {
						for (PersonelIzin personelIzinHarcanan : harcananlar) {
							row++;
							col = izinExcelBakiye(sheet, style, styleCenter, styleDate, row, oncekiHakedisTarihi, personelIzin);
							ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personelIzinHarcanan.getIzinSuresi());
							ExcelUtil.getCell(sheet, row, col++, styleDateTime).setCellValue(PdksUtil.getDate(personelIzinHarcanan.getBaslangicZamani()));
							ExcelUtil.getCell(sheet, row, col++, styleDateTime).setCellValue(PdksUtil.getDate(personelIzinHarcanan.getBitisZamani()));
							ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personelIzinHarcanan.getAciklama());
							col = 0;
						}

					} else {
						row++;
						col = izinExcelBakiye(sheet, style, styleCenter, styleDate, row, oncekiHakedisTarihi, personelIzin);
						ExcelUtil.getCell(sheet, row, col++, styleCenter);
						ExcelUtil.getCell(sheet, row, col++, styleCenter);
						ExcelUtil.getCell(sheet, row, col++, styleCenter);
						ExcelUtil.getCell(sheet, row, col++, styleCenter);

					}
					oncekiHakedisTarihi = (Date) personelIzin.getBitisZamani().clone();
				}

			}

		}
		double katsayi = 3.43;
		int[] dizi = new int[] { 1000, 2500, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 2500 };
		for (int i = 0; i < dizi.length; i++)
			sheet.setColumnWidth(i, (short) (dizi[i] * katsayi));
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

	/**
	 * @param sheet
	 * @param style
	 * @param styleCenter
	 * @param styleDate
	 * @param row
	 * @param oncekiHakedisTarihi
	 * @param personelIzin
	 * @return
	 */
	private int izinExcelBakiye(Sheet sheet, CellStyle style, CellStyle styleCenter, CellStyle styleDate, int row, Date oncekiHakedisTarihi, PersonelIzin personelIzin) {
		int col = 0;
		try {

			Personel personel = personelIzin.getIzinSahibi();
			int yil = PdksUtil.getDateField(personelIzin.getBaslangicZamani(), Calendar.YEAR);
			ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personel.getPdksSicilNo().trim());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getAdSoyad());
			ExcelUtil.getCell(sheet, row, col++, styleDate).setCellValue(personel.getGrubaGirisTarihi());
			ExcelUtil.getCell(sheet, row, col++, styleDate).setCellValue(personel.getIseBaslamaTarihi());
			ExcelUtil.getCell(sheet, row, col++, styleDate).setCellValue(personel.getIzinHakEdisTarihi());
			ExcelUtil.getCell(sheet, row, col++, styleDate).setCellValue(personel.getDogumTarihi());
			ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personelIzin.getIzinTipi().getBakiyeIzinTipi().getKisaAciklama());
			ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(yil);
			if (oncekiHakedisTarihi != null)
				ExcelUtil.getCell(sheet, row, col++, styleDate).setCellValue(oncekiHakedisTarihi);
			else
				ExcelUtil.getCell(sheet, row, col++, styleCenter);
			ExcelUtil.getCell(sheet, row, col++, styleDate).setCellValue(personelIzin.getBitisZamani());
			ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personelIzin.getAciklama());
			ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personelIzin.getIzinSuresi());
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			logger.error(row + " " + col);
		}
		return col;
	}

	public String excelAktar() {

		try {
			ByteArrayOutputStream baosDosya = excelAktarDevam();
			if (baosDosya != null)
				PdksUtil.setExcelHttpServletResponse(baosDosya, "kalanIzin.xlsx");

		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}

		return "";

	}

	public String izinKartiExcelAktar() {
		try {
			ByteArrayOutputStream baosDosya = izinKartiExcelAktarDevam();
			if (baosDosya != null)
				PdksUtil.setExcelHttpServletResponse(baosDosya, "izinKarti.xlsx");

		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}

		return "";
	}

	public String izinBakiyeDosyaOku() throws Exception {

		Workbook wb = ortakIslemler.getWorkbook(izinBakiyeDosya);
		HashMap fields = new HashMap();
		String pattern = "dd.MM.yyyy";
		try {
			if (wb != null) {
				String bakiyeIzinTip = "B";
				String normalIzinTip = "N";
				User sistemAdminUser = ortakIslemler.getSistemAdminUser(session);
				if (sistemAdminUser == null)
					sistemAdminUser = authenticatedUser;
				String sicilNo = "";
				Sheet sheet = wb.getSheetAt(0);
				TempIzin tempIzin = null;
				TreeMap<String, IzinTipi> izinTipiMap = new TreeMap<String, IzinTipi>();
				TreeMap<String, PersonelIzin> izinMap = new TreeMap<String, PersonelIzin>(), personelIzinMap = new TreeMap<String, PersonelIzin>();
				List<String> tipler = Arrays.asList(new String[] { IzinTipi.YILLIK_UCRETLI_IZIN, IzinTipi.SUA_IZNI });
				fields.put("durum=", Boolean.TRUE);
				fields.put("izinTipiTanim.kodu", tipler);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<IzinTipi> izinTipleri = pdksEntityController.getObjectByInnerObjectListInLogic(fields, IzinTipi.class);
				for (Iterator iterator = izinTipleri.iterator(); iterator.hasNext();) {
					IzinTipi izinTipi = (IzinTipi) iterator.next();
					String key = izinTipi.getDepartman().getId() + "_" + (izinTipi.getBakiyeIzinTipi() != null ? bakiyeIzinTip : normalIzinTip) + "_" + izinTipi.getIzinKodu();
					izinTipiMap.put(key, izinTipi);
					if (izinTipi.getBakiyeIzinTipi() == null)
						iterator.remove();
				}
				tipler = null;
				boolean renk = false;
				int COL_SICIL_NO = 0;
				int COL_ADI_SOYADI = 1;
				int COL_GRUBA_BASLAMA_TARIHI = 2;
				int COL_ISE_BASLAMA_TARIHI = 3;
				int COL_IZIN_HAKEDIS_TARIH = 4;
				int COL_DOGUM_TARIHI = 5;
				int COL_IZIN_TURU = 6;
				int COL_IZIN_YILI = 7;
				int COL_IZIN_YIL_HAKEDIS_TARIH = 9;
				int COL_KIDEM_YIL = 10;
				int COL_IZIN_HAKEDIS_GUN = 11;
				int COL_IZIN_SURESI = 12;
				int COL_IZIN_BAS_TARIH = 13;
				int COL_IZIN_BIT_TARIH = 14;
				int COL_IZIN_ACIKLAMA = 15;
				boolean listeEkle = false;
				String perSicilNo = null;
				TreeMap<String, HashMap<Integer, org.apache.poi.ss.usermodel.Cell>> hucreMap = new TreeMap<String, HashMap<Integer, org.apache.poi.ss.usermodel.Cell>>();
				TreeMap<String, Personel> bosPersonelMap = new TreeMap<String, Personel>();
				for (int row = 1; row <= sheet.getLastRowNum(); row++) {
					try {
						perSicilNo = ExcelUtil.getSheetStringValueTry(sheet, row, COL_SICIL_NO);

						if (!PdksUtil.hasStringValue(perSicilNo))
							break;
						String key = null;
						String kod = ExcelUtil.getSheetStringValueTry(sheet, row, COL_IZIN_TURU);
						try {
							Date tarih = ExcelUtil.getSheetDateValueTry(sheet, row, COL_IZIN_BAS_TARIH, pattern);
							key = perSicilNo + "_" + ExcelUtil.getSheetStringValueTry(sheet, row, COL_IZIN_YILI) + "_" + (tarih != null ? PdksUtil.convertToDateString(tarih, "yyyyMMdd") : "99991231") + "_" + kod;
						} catch (Exception e1) {
							// hucreMap.clear();
							// PdksUtil.addMessageWarn((row + 1) + " satırda " + perSicilNo + " sicil no için yılı alanlarında sorun var");
							Personel personel = new Personel();
							personel.setPdksSicilNo(perSicilNo);
							personel.setAd(ExcelUtil.getSheetStringValueTry(sheet, row, COL_ADI_SOYADI));
							personel.setSoyad("");
							Date izinHakEdisTarihi = null, iseBaslamaTarihi = null, grubaGirisTarihi = null, dogumTarihi = null;
							try {
								izinHakEdisTarihi = ExcelUtil.getSheetDateValueTry(sheet, row, COL_IZIN_HAKEDIS_TARIH, pattern);
							} catch (Exception e) {
								PdksUtil.addMessageWarn(personel.getPdksSicilNo() + " " + personel.getAdSoyad() + " " + ortakIslemler.kidemBasTarihiAciklama() + "nde sorun var!");
								break;
							}

							try {
								grubaGirisTarihi = ExcelUtil.getSheetDateValueTry(sheet, row, COL_GRUBA_BASLAMA_TARIHI, pattern);

							} catch (Exception e) {
								PdksUtil.addMessageWarn(personel.getPdksSicilNo() + " " + personel.getAdSoyad() + " gruba giriş tarihinde  sorun var!");
								break;
							}
							try {
								iseBaslamaTarihi = ExcelUtil.getSheetDateValueTry(sheet, row, COL_ISE_BASLAMA_TARIHI, pattern);
							} catch (Exception e) {
								PdksUtil.addMessageWarn(personel.getPdksSicilNo() + " " + personel.getAdSoyad() + " işe giriş tarihinde sorun var!");
								break;
							}
							try {
								dogumTarihi = ExcelUtil.getSheetDateValueTry(sheet, row, COL_DOGUM_TARIHI, pattern);
							} catch (Exception e) {
								PdksUtil.addMessageWarn(personel.getPdksSicilNo() + " " + personel.getAdSoyad() + " doğum tarihinde  sorun var!");
								break;
							}
							personel.setGrubaGirisTarihi(grubaGirisTarihi);
							personel.setIseBaslamaTarihi(iseBaslamaTarihi);
							personel.setDogumTarihi(dogumTarihi);

							personel.setIzinHakEdisTarihi(izinHakEdisTarihi);
							bosPersonelMap.put(perSicilNo, personel);
							continue;
						}
						HashMap<Integer, org.apache.poi.ss.usermodel.Cell> veriMap = new HashMap<Integer, org.apache.poi.ss.usermodel.Cell>();
						for (Integer col = 0; col <= COL_IZIN_ACIKLAMA; col++) {
							org.apache.poi.ss.usermodel.Cell cell = ExcelUtil.getCell(sheet, row, col);
							veriMap.put(col, cell);
						}
						hucreMap.put(key, veriMap);
					} catch (Exception e) {
						logger.error("Pdks hata in : \n");
						e.printStackTrace();
						logger.error("Pdks hata out : " + e.getMessage());

					}

				}
				if (!bosPersonelMap.isEmpty()) {
					Calendar cal = Calendar.getInstance();
					Date guncellemeTarihi = cal.getTime();
					Date tarih = PdksUtil.getDate(guncellemeTarihi);
					cal.add(Calendar.YEAR, 1);
					cal.set(Calendar.MONTH, Calendar.JANUARY);
					cal.set(Calendar.DATE, 1);
					Date yilBasi = PdksUtil.getDate(cal.getTime());
					Boolean flush = Boolean.FALSE;
					for (Iterator iterator = new ArrayList(bosPersonelMap.keySet()).iterator(); iterator.hasNext();) {
						String key = (String) iterator.next();
						Personel personel = null;
						if (!hucreMap.containsKey(key)) {
							HashMap parametreMap = new HashMap();

							parametreMap.put("pdksSicilNo", key);
							if (session != null)
								parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);

							personel = (Personel) pdksEntityController.getObjectByInnerObject(parametreMap, Personel.class);
							if (personel != null) {
								Personel personelExcel = bosPersonelMap.get(key);
								personel.setIzinHakEdisTarihi(personelExcel.getIzinHakEdisTarihi());
								if (!personel.getSirket().isErp()) {
									personel.setGrubaGirisTarihi(personelExcel.getGrubaGirisTarihi());
									personel.setIseBaslamaTarihi(personelExcel.getIseBaslamaTarihi());
									personel.setDogumTarihi(personelExcel.getDogumTarihi());
								}
								fields.clear();
								fields.put("izinSahibi", personel);
								fields.put("izinTipi", izinTipleri);
								if (session != null)
									fields.put(PdksEntityController.MAP_KEY_SESSION, session);
								List<PersonelIzin> izinler = pdksEntityController.getObjectByInnerObjectList(fields, PersonelIzin.class);
								for (Iterator iterator2 = izinler.iterator(); iterator2.hasNext();) {
									PersonelIzin personelIzin = (PersonelIzin) iterator2.next();
									if (personelIzin.getIzinKagidiGeldi() == null && personelIzin.getIzinSuresi() > 0.0d && !personelIzin.isRedmi() && personelIzin.getBaslangicZamani().before(tarih)) {
										if (personelIzin.getBaslangicZamani().before(yilBasi)) {
											personelIzin.setIzinDurumu(PersonelIzin.IZIN_DURUMU_SISTEM_IPTAL);
											personelIzin.setGuncellemeTarihi(guncellemeTarihi);
										} else {
											personelIzin.setIzinKagidiGeldi(Boolean.FALSE);
											personelIzin.setIzinSuresi(0.0d);
										}
										pdksEntityController.saveOrUpdate(session, entityManager, personelIzin);

									}

								}
								pdksEntityController.saveOrUpdate(session, entityManager, personel);
								logger.info(personel.getPdksSicilNo() + " " + personel.getAdSoyad() + " sıfırlandı!");
								flush = Boolean.TRUE;
							}
						}

					}
					if (flush)
						session.flush();

				}
				if (!hucreMap.isEmpty()) {
					List<HashMap<Integer, org.apache.poi.ss.usermodel.Cell>> hucreler = new ArrayList<HashMap<Integer, org.apache.poi.ss.usermodel.Cell>>(hucreMap.values());
					for (Iterator iterator = hucreler.iterator(); iterator.hasNext();) {
						HashMap<Integer, org.apache.poi.ss.usermodel.Cell> veriMap = (HashMap<Integer, org.apache.poi.ss.usermodel.Cell>) iterator.next();
						List<IzinTipi> izinTipiList = getYillikIzinTipleri(session);
						HashMap<String, String> kodMap = new HashMap<String, String>();
						for (IzinTipi izinTipi : izinTipiList)
							kodMap.put(izinTipi.getKisaAciklama(), izinTipi.getIzinTipiTanim().getKodu());

						int bakiyeYil = 0;
						Personel personel = null;
						String kod = ExcelUtil.getSheetStringValue((org.apache.poi.ss.usermodel.Cell) veriMap.get(COL_IZIN_TURU));
						try {
							perSicilNo = ExcelUtil.getSheetStringValue((org.apache.poi.ss.usermodel.Cell) veriMap.get(COL_SICIL_NO));
							if (!perSicilNo.equals(sicilNo)) {
								if (tempIzin != null)
									tempIzin.excelBakiyeHesapla();
								if (personelBakiyeIzinList.size() > 300)
									break;
								sicilNo = perSicilNo;
								izinMap.clear();
								personelIzinMap.clear();
								HashMap parametreMap = new HashMap();

								parametreMap.put("pdksSicilNo", perSicilNo);
								if (session != null)
									parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
								personel = (Personel) pdksEntityController.getObjectByInnerObject(parametreMap, Personel.class);
								fields.clear();
								fields.put("izinSahibi.id", personel.getId());
								fields.put("izinTipi", izinTipleri);
								if (session != null)
									fields.put(PdksEntityController.MAP_KEY_SESSION, session);
								List<PersonelIzin> izinler = pdksEntityController.getObjectByInnerObjectList(fields, PersonelIzin.class);
								for (PersonelIzin personelIzin : izinler) {
									if (personelIzin.getIzinTipi().getBakiyeIzinTipi() != null) {
										int bakiyeDonem = PdksUtil.getDateField(personelIzin.getBaslangicZamani(), Calendar.YEAR);
										personelIzin.setBakiyeSuresi(-1d);
										personelIzin.setHarcananDigerIzinler(new ArrayList<PersonelIzin>());
										izinMap.put(bakiyeDonem + personelIzin.getIzinKodu(), personelIzin);
									}
								}
								if (personel != null) {
									listeEkle = false;
									tempIzin = new TempIzin();
									tempIzin.setPersonel(personel);
									tempIzin.setYillikIzinler(new ArrayList<PersonelIzin>());
									tempIzin.setStyleClass(renk ? VardiyaGun.STYLE_CLASS_ODD : VardiyaGun.STYLE_CLASS_EVEN);
									renk = !renk;
									Date izinHakEdisTarihi = null, iseBaslamaTarihi = null, grubaGirisTarihi = null, dogumTarihi = null;
									try {
										izinHakEdisTarihi = ExcelUtil.getSheetDateValue((org.apache.poi.ss.usermodel.Cell) veriMap.get(COL_IZIN_HAKEDIS_TARIH), pattern);
									} catch (Exception e) {
										logger.error("Pdks hata in : \n");
										e.printStackTrace();
										logger.error("Pdks hata out : " + e.getMessage());
										personelBakiyeIzinList.clear();
										PdksUtil.addMessageWarn(personel.getPdksSicilNo() + " " + personel.getAdSoyad() + " sorun var!");
										break;
									}
									if (!personel.getSirket().isErp()) {
										try {
											grubaGirisTarihi = ExcelUtil.getSheetDateValue((org.apache.poi.ss.usermodel.Cell) veriMap.get(COL_GRUBA_BASLAMA_TARIHI), pattern);
										} catch (Exception e) {
											logger.error("Pdks hata in : \n");
											e.printStackTrace();
											logger.error("Pdks hata out : " + e.getMessage());
										}
										try {
											iseBaslamaTarihi = ExcelUtil.getSheetDateValue((org.apache.poi.ss.usermodel.Cell) veriMap.get(COL_ISE_BASLAMA_TARIHI), pattern);
										} catch (Exception e) {
											logger.error("Pdks hata in : \n");
											e.printStackTrace();
											logger.error("Pdks hata out : " + e.getMessage());
										}
										try {
											dogumTarihi = ExcelUtil.getSheetDateValue((org.apache.poi.ss.usermodel.Cell) veriMap.get(COL_DOGUM_TARIHI), pattern);
										} catch (Exception e) {
											logger.error("Pdks hata in : \n");
											e.printStackTrace();
											logger.error("Pdks hata out : " + e.getMessage());

										}

										tempIzin.setIseBaslamaTarihi(iseBaslamaTarihi);
										tempIzin.setGrubaGirisTarihi(grubaGirisTarihi);
										tempIzin.setDogumTarihi(dogumTarihi);
									}
									tempIzin.setIzinHakEdisTarihi(izinHakEdisTarihi);
								}
							}
							try {
								bakiyeYil = ExcelUtil.getSheetDoubleValue((org.apache.poi.ss.usermodel.Cell) veriMap.get(COL_IZIN_YILI)).intValue();
							} catch (Exception e) {
								logger.error("Pdks hata in : \n");
								e.printStackTrace();
								logger.error("Pdks hata out : " + e.getMessage());
								try {
									bakiyeYil = Integer.parseInt(ExcelUtil.getSheetStringValue((org.apache.poi.ss.usermodel.Cell) veriMap.get(COL_IZIN_YILI)));
								} catch (Exception e2) {
									bakiyeYil = 0;
								}
							}
							if (bakiyeYil <= 0 || tempIzin == null) {
								PdksUtil.addMessageWarn(personel.getPdksSicilNo() + " " + personel.getAdSoyad() + " sorun var!");
								personelBakiyeIzinList.clear();
								break;
							}

							Date hakedisTarih = null;
							try {
								hakedisTarih = ExcelUtil.getSheetDateValue((org.apache.poi.ss.usermodel.Cell) veriMap.get(COL_IZIN_YIL_HAKEDIS_TARIH), pattern);
							} catch (Exception e) {
								logger.error("Pdks hata in : \n");
								e.printStackTrace();
								logger.error("Pdks hata out : " + e.getMessage());

								personelBakiyeIzinList.clear();
								break;

							}
							String bakiyeIzinTipiKodu = "";
							if (kodMap.containsKey(kod))
								bakiyeIzinTipiKodu = kodMap.get(kod);
							PersonelIzin bakiyeIzin = izinMap.containsKey(bakiyeYil + "" + bakiyeIzinTipiKodu) ? izinMap.get(bakiyeYil + "" + bakiyeIzinTipiKodu) : null;
							List<PersonelIzin> harcananDigerIzinler = null;
							String key = tempIzin.getPersonel().getSirket().getDepartman().getId() + "_" + bakiyeIzinTip + "_" + bakiyeIzinTipiKodu;
							IzinTipi izinTipi = izinTipiMap.containsKey(key) ? izinTipiMap.get(key) : null;
							if (bakiyeIzin == null) {
								if (izinTipi != null) {
									bakiyeIzin = new PersonelIzin();
									bakiyeIzin.setIzinSahibi(tempIzin.getPersonel());
									bakiyeIzin.setIzinTipi(izinTipi);
									bakiyeIzin.setBaslangicZamani(PdksUtil.convertToJavaDate(bakiyeYil + "0101", "yyyyMMdd"));
									bakiyeIzin.setBitisZamani(PdksUtil.convertToJavaDate(bakiyeYil + "1231", "yyyyMMdd"));
									bakiyeIzin.setDurum(Boolean.TRUE);
									bakiyeIzin.setKullanilanIzinSuresi(0D);
									bakiyeIzin.setIzinDurumu(PersonelIzin.IZIN_DURUMU_ONAYLANDI);
									bakiyeIzin.setOlusturanUser(sistemAdminUser);
								}
								izinMap.put(bakiyeYil + "" + bakiyeIzin.getIzinKodu(), bakiyeIzin);
								harcananDigerIzinler = new ArrayList<PersonelIzin>();
								bakiyeIzin.setHarcananDigerIzinler(harcananDigerIzinler);
							} else {
								if (!izinTipi.getId().equals(bakiyeIzin.getIzinTipi().getId()))
									bakiyeIzin.setIzinTipi(izinTipi);
								bakiyeIzinTipiKodu = bakiyeIzin.getIzinKodu();
								harcananDigerIzinler = bakiyeIzin.getHarcananDigerIzinler();
							}
							// int kidemYil = 0;
							String kidemYil = "";
							try {
								// kidemYil = Integer.parseInt(ExcelUtil.getSheetStringValue((org.apache.poi.ss.usermodel.Cell) veriMap.get(COL_KIDEM_YIL)));
								kidemYil = ExcelUtil.getSheetStringValue((org.apache.poi.ss.usermodel.Cell) veriMap.get(COL_KIDEM_YIL));
							} catch (Exception e) {
								logger.error("Pdks hata in : \n");
								e.printStackTrace();
								logger.error("Pdks hata out : " + e.getMessage());
								PdksUtil.addMessageWarn(personel.getPdksSicilNo() + " " + personel.getAdSoyad() + " sorun var!");
								personelBakiyeIzinList.clear();
								break;

							}
							double bakiyeSuresi = 0;
							try {
								bakiyeSuresi = ExcelUtil.getSheetDoubleValue((org.apache.poi.ss.usermodel.Cell) veriMap.get(COL_IZIN_HAKEDIS_GUN)).doubleValue();
							} catch (Exception e) {
								logger.error("Pdks hata in : \n");
								e.printStackTrace();
								logger.error("Pdks hata out : " + e.getMessage());
								bakiyeIzin = null;
							}

							if (bakiyeIzin == null) {
								PdksUtil.addMessageWarn(personel.getPdksSicilNo() + " " + personel.getAdSoyad() + " sorun var!");
								personelBakiyeIzinList.clear();
								break;
							}
							bakiyeIzin.setAciklama(String.valueOf(kidemYil));
							bakiyeIzin.setBakiyeSuresi(bakiyeSuresi);
							bakiyeIzin.setHakedisTarih(hakedisTarih);
							key = tempIzin.getPersonel().getSirket().getDepartman().getId() + "_" + normalIzinTip + "_" + bakiyeIzinTipiKodu;
							IzinTipi izinTipiKullanilan = izinTipiMap.containsKey(key) ? izinTipiMap.get(key) : null;
							if (izinTipiKullanilan != null) {
								if (!listeEkle) {
									listeEkle = true;
									personelBakiyeIzinList.add(tempIzin);
								}
								if (!personelIzinMap.containsKey(bakiyeYil + bakiyeIzin.getIzinKodu())) {
									personelIzinMap.put(bakiyeYil + bakiyeIzin.getIzinKodu(), bakiyeIzin);
									tempIzin.getYillikIzinler().add(bakiyeIzin);
								}
								double kullanilanIzinSuresi = 0;
								try {
									if (veriMap.containsKey(COL_IZIN_SURESI))
										kullanilanIzinSuresi = ExcelUtil.getSheetDoubleValue((org.apache.poi.ss.usermodel.Cell) veriMap.get(COL_IZIN_SURESI)).doubleValue();
									else
										continue;
								} catch (Exception e) {
									continue;
								}
								Date baslangicZamani = null;
								try {
									baslangicZamani = ExcelUtil.getSheetDateValue((org.apache.poi.ss.usermodel.Cell) veriMap.get(COL_IZIN_BAS_TARIH), pattern);
								} catch (Exception e) {
									logger.error("Pdks hata in : \n");
									e.printStackTrace();
									logger.error("Pdks hata out : " + e.getMessage());

								}
								Date bitisZamani = null;
								try {
									bitisZamani = ExcelUtil.getSheetDateValue((org.apache.poi.ss.usermodel.Cell) veriMap.get(COL_IZIN_BIT_TARIH), pattern);
								} catch (Exception e) {
									logger.error("Pdks hata in : \n");
									e.printStackTrace();
									logger.error("Pdks hata out : " + e.getMessage());
								}
								String aciklama = "";
								try {
									aciklama = ExcelUtil.getSheetStringValue((org.apache.poi.ss.usermodel.Cell) veriMap.get(COL_IZIN_ACIKLAMA));
								} catch (Exception e) {
									aciklama = "";
								}
								if (baslangicZamani != null && bitisZamani != null) {
									PersonelIzin personelKullanilanIzin = new PersonelIzin();
									personelKullanilanIzin.setBaslangicZamani(baslangicZamani);
									personelKullanilanIzin.setBitisZamani(bitisZamani);
									personelKullanilanIzin.setIzinTipi(izinTipiKullanilan);
									personelKullanilanIzin.setIzinSahibi(tempIzin.getPersonel());
									personelKullanilanIzin.setIzinDurumu(PersonelIzin.IZIN_DURUMU_ONAYLANDI);
									personelKullanilanIzin.setOlusturanUser(sistemAdminUser);
									personelKullanilanIzin.setIzinSuresi(kullanilanIzinSuresi);
									personelKullanilanIzin.setAciklama(aciklama);
									harcananDigerIzinler.add(personelKullanilanIzin);
								}

							}
						} catch (Exception e) {
							logger.error("Pdks hata in : \n");
							e.printStackTrace();
							logger.error("Pdks hata out : " + e.getMessage());
							if (perSicilNo != null) {
								PdksUtil.addMessageWarn(perSicilNo + " " + bakiyeYil + " hata ");
								personelBakiyeIzinList.clear();
							}
							break;
						}
						veriMap.clear();
					}
					hucreler = null;
				}
				hucreMap = null;

				if (!personelBakiyeIzinList.isEmpty()) {
					if (personelBakiyeIzinList.size() > 1)
						personelBakiyeIzinList = PdksUtil.sortListByAlanAdi(personelBakiyeIzinList, "sicilNo", Boolean.FALSE);
					PdksUtil.addMessageWarn("Dosya okuma tamamlandı ");
				}

				if (tempIzin != null)
					tempIzin.excelBakiyeHesapla();
			}
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			PdksUtil.addMessageWarn(e.getMessage());
		} finally {

		}

		return "";
	}

	public String izinDosyaSifirla() throws Exception {
		personelBakiyeIzinList.clear();
		izinBakiyeDosya.setDosyaIcerik(null);
		return "";
	}

	public String izinBakiyeDosyaYaz() throws Exception {
		TreeMap<String, PersonelIzin> izinMap = new TreeMap<String, PersonelIzin>();
		List<String> tipler = Arrays.asList(new String[] { IzinTipi.YILLIK_UCRETLI_IZIN, IzinTipi.SUA_IZNI });
		User guncelleyenUser = authenticatedUser.isIK() ? authenticatedUser : ortakIslemler.getSistemAdminUser(session);
		HashMap fields = new HashMap();
		fields.put("durum=", Boolean.TRUE);
		fields.put("izinTipiTanim.kodu", tipler);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<IzinTipi> izinTipleri = pdksEntityController.getObjectByInnerObjectListInLogic(fields, IzinTipi.class);
		List bakiyeIzinTipleri = new ArrayList<IzinTipi>();

		for (Iterator iterator = izinTipleri.iterator(); iterator.hasNext();) {
			IzinTipi izinTipi = (IzinTipi) iterator.next();
			if (izinTipi.getBakiyeIzinTipi() != null) {
				bakiyeIzinTipleri.add(izinTipi);
				iterator.remove();
			}

		}
		Calendar cal = Calendar.getInstance();

		List<Integer> izinDurumlari = Arrays.asList(new Integer[] { PersonelIzin.IZIN_DURUMU_REDEDILDI, PersonelIzin.IZIN_DURUMU_SISTEM_IPTAL });
		int sayi = personelBakiyeIzinList.size();
		TreeMap<Long, PersonelIzin> bayiIzinMap = new TreeMap<Long, PersonelIzin>();
		for (TempIzin tempIzin : personelBakiyeIzinList) {
			Personel pdksPersonel = tempIzin.getPersonel();
			Date tarih = new Date();
			logger.info((sayi--) + " " + pdksPersonel.getPdksSicilNo() + " " + PdksUtil.setTurkishStr(pdksPersonel.getAdSoyad()));

			// session.clear();
			fields.clear();
			bayiIzinMap.clear();
			izinMap.clear();
			fields.put("izinSahibi", tempIzin.getPersonel());
			fields.put("izinTipi", bakiyeIzinTipleri);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<PersonelIzin> bakiyeIzinler = pdksEntityController.getObjectByInnerObjectList(fields, PersonelIzin.class);
			for (PersonelIzin hakEdisIzin : tempIzin.getYillikIzinler()) {
				if (hakEdisIzin.getId() != null)
					bayiIzinMap.put(hakEdisIzin.getId(), hakEdisIzin);
			}
			int sayac = 0;
			for (PersonelIzin personelIzin : bakiyeIzinler) {
				if (personelIzin.getIzinTipi().getBakiyeIzinTipi() != null) {
					int bakiyeYil = PdksUtil.getDateField(personelIzin.getBaslangicZamani(), Calendar.YEAR);
					if (bayiIzinMap.containsKey(personelIzin.getId()) && !izinMap.containsKey(bakiyeYil + personelIzin.getIzinKodu()))
						izinMap.put(bakiyeYil + personelIzin.getIzinKodu(), personelIzin);
					else
						izinMap.put(--sayac + personelIzin.getIzinKodu(), personelIzin);
				}
			}

			fields.clear();
			fields.put("izinSahibi=", tempIzin.getPersonel());
			fields.put("izinTipi", izinTipleri);
			fields.put("izinDurumu not ", izinDurumlari);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<PersonelIzin> izinler = pdksEntityController.getObjectByInnerObjectListInLogic(fields, PersonelIzin.class);
			for (Iterator iterator = izinler.iterator(); iterator.hasNext();) {
				PersonelIzin personelIzin = (PersonelIzin) iterator.next();
				personelIzin.setIzinDurumu(PersonelIzin.IZIN_DURUMU_SISTEM_IPTAL);
				if (!authenticatedUser.isAdmin()) {
					personelIzin.setGuncellemeTarihi(tarih);
					personelIzin.setGuncelleyenUser(guncelleyenUser);
				}
				pdksEntityController.saveOrUpdate(session, entityManager, personelIzin);
			}

			for (PersonelIzin hakEdisIzin : tempIzin.getYillikIzinler()) {
				int bakiyeYil = PdksUtil.getDateField(hakEdisIzin.getBaslangicZamani(), Calendar.YEAR);
				if (izinMap.containsKey(bakiyeYil + hakEdisIzin.getIzinKodu()))
					izinMap.remove(bakiyeYil + hakEdisIzin.getIzinKodu());
				hakEdisIzin.setIzinDurumu(PersonelIzin.IZIN_DURUMU_ONAYLANDI);
				hakEdisIzin.setIzinSuresi(hakEdisIzin.getBakiyeSuresi());
				if (hakEdisIzin.getBakiyeSuresi().doubleValue() == 0.0d)
					hakEdisIzin.setIzinKagidiGeldi(Boolean.FALSE);
				else
					hakEdisIzin.setIzinKagidiGeldi(null);
				hakEdisIzin.setBitisZamani(hakEdisIzin.getHakedisTarih());
				if (hakEdisIzin.getId() != null) {
					hakEdisIzin.setGuncellemeTarihi(tarih);
					hakEdisIzin.setGuncelleyenUser(guncelleyenUser);
				} else {
					hakEdisIzin.setOlusturanUser(guncelleyenUser);
				}

				hakEdisIzin.setKullanilanIzinSuresi(null);
				hakEdisIzin.setHesapTipi(PersonelIzin.HESAP_TIPI_GUN);
				pdksEntityController.saveOrUpdate(session, entityManager, hakEdisIzin);
				for (PersonelIzin personelIzin : hakEdisIzin.getHarcananDigerIzinler()) {

					personelIzin.setHesapTipi(PersonelIzin.HESAP_TIPI_GUN);
					personelIzin.setOlusturanUser(guncelleyenUser);
					cal.setTime(personelIzin.getBaslangicZamani());
					if (!PdksUtil.hasStringValue(personelIzin.getAciklama()))
						personelIzin.setAciklama(personelIzin.getIzinTipi().getMesaj());
					personelIzin.setIzinKagidiGeldi(Boolean.TRUE);
					personelIzin.setIzinDurumu(PersonelIzin.IZIN_DURUMU_ONAYLANDI);
					PersonelIzinDetay izinDetay = new PersonelIzinDetay();
					izinDetay.setPersonelIzin(personelIzin);
					izinDetay.setHakEdisIzin(hakEdisIzin);
					izinDetay.setIzinMiktari(personelIzin.getIzinSuresi());
					pdksEntityController.saveOrUpdate(session, entityManager, personelIzin);
					pdksEntityController.saveOrUpdate(session, entityManager, izinDetay);
				}
				// session.refresh(hakEdisIzin);

			}
			if (!izinMap.isEmpty()) {
				bakiyeIzinler = new ArrayList<PersonelIzin>(izinMap.values());
				for (Iterator iterator = bakiyeIzinler.iterator(); iterator.hasNext();) {
					PersonelIzin personelIzin = (PersonelIzin) iterator.next();
					personelIzin.setIzinDurumu(PersonelIzin.IZIN_DURUMU_SISTEM_IPTAL);
					if (!authenticatedUser.isAdmin()) {
						personelIzin.setGuncellemeTarihi(tarih);
						personelIzin.setGuncelleyenUser(guncelleyenUser);
					}

					personelIzin.setIzinSuresi(0d);
					personelIzin.setKullanilanIzinSuresi(null);
					pdksEntityController.saveOrUpdate(session, entityManager, personelIzin);
				}
			}

			pdksPersonel.setIzinHakEdisTarihi(tempIzin.getIzinHakEdisTarihi());
			if (!pdksPersonel.getSirket().isErp()) {
				pdksPersonel.setIseBaslamaTarihi(tempIzin.getIseBaslamaTarihi());
				pdksPersonel.setGrubaGirisTarihi(tempIzin.getGrubaGirisTarihi());
				pdksPersonel.setDogumTarihi(tempIzin.getDogumTarihi());
			}
			pdksEntityController.saveOrUpdate(session, entityManager, pdksPersonel);
			session.flush();
			bakiyeIzinler = null;
		}

		personelBakiyeIzinList.clear();
		izinBakiyeDosya.setDosyaIcerik(null);
		sayfaGiris(session);
		PdksUtil.addMessageInfo("Bilgiler güncellenmiştir.");

		return "";

	}

	/**
	 * @param izinler
	 * @return
	 */
	private List<PersonelIzin> borcluIzinleriSifirla(List<PersonelIzin> izinler) {
		List<PersonelIzin> yillikTemIzinler = new ArrayList<PersonelIzin>();
		boolean guncelle = false;
		Date tarih = PdksUtil.getDate(new Date());
		if (donemSonu != null && donemSonu.after(tarih))
			tarih = donemSonu;
		boolean sifirla = true;
		if (authenticatedUser.isAdmin() || authenticatedUser.isIK() || authenticatedUser.isSistemYoneticisi())
			sifirla = PersonelIzin.getYillikIzinMaxBakiye() > 0;

		for (PersonelIzin personelIzin : izinler) {
			PersonelIzin personelIzinNew = (PersonelIzin) personelIzin.clone();
			personelIzinNew.setKontrolIzin(personelIzin);
			personelIzinNew.setDonemSonu(tarih);
			if (sifirla && personelIzinNew.getBitisZamani().after(tarih)) {
				guncelle = true;
				personelIzinNew.setIzinSuresi(0D);
			}
			yillikTemIzinler.add(personelIzinNew);
		}
		if (guncelle) {
			izinler = null;
			izinler = yillikTemIzinler;
		} else
			yillikTemIzinler = null;
		return izinler;
	}

	public void listenerIzinBakiyeDosya(UploadEvent event) throws Exception {
		UploadItem item = event.getUploadItem();
		PdksUtil.getDosya(item, izinBakiyeDosya);
		if (personelBakiyeIzinList == null)
			personelBakiyeIzinList = new ArrayList<TempIzin>();
		else
			personelBakiyeIzinList.clear();

	}

	public void izinGoster(TempIzin tempIzin) {
		izinTipiList = null;
		Personel pdksPersonel = tempIzin.getPersonel();

		setUpdateTempIzin(tempIzin);
		HashMap fields = new HashMap();
		fields.put("id", tempIzin.getIzinler().clone());
		if (pdksPersonel.getSirket().getDepartman().isAdminMi())
			fields.put("baslangicZamani>", PdksUtil.getBakiyeYil());
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<PersonelIzin> izinList1 = tempIzin.getIzinler().isEmpty() ? new ArrayList<PersonelIzin>() : pdksEntityController.getObjectByInnerObjectListInLogic(fields, PersonelIzin.class);

		suaVar = Boolean.FALSE;
		if (gelecekIzinGoster)
			izinList1 = borcluIzinleriSifirla(izinList1);
		for (PersonelIzin personelIzin : izinList1) {
			if (personelIzin.getIzinTipi() != null && personelIzin.getIzinTipi().getBakiyeIzinTipi() != null && personelIzin.getIzinTipi().getBakiyeIzinTipi().isSuaIzin()) {
				suaVar = Boolean.TRUE;
				break;
			}
		}
		if (izinList1.size() > 1)
			izinList1 = PdksUtil.sortListByAlanAdi(izinList1, "baslangicZamani", Boolean.FALSE);
		if (authenticatedUser.isAdmin()) {
			if (pdksPersonel.isSuaOlur()) {
				if (pdksPersonel.getSirket().getDepartman().isAdminMi()) {
					List<String> tipler = new ArrayList<String>();
					tipler.add(IzinTipi.SUA_IZNI);
					tipler.add(IzinTipi.YILLIK_UCRETLI_IZIN);
					fields.clear();
					fields.put("izinTipiTanim.kodu", tipler);
					fields.put("bakiyeIzinTipi<>", null);
					fields.put("departman.id=", pdksPersonel.getSirket().getDepartman().getId());
					fields.put("durum=", Boolean.TRUE);
					// fields.put("bakiyeIzinTipi.personelGirisTipi<>", IzinTipi.GIRIS_TIPI_YOK);
					if (session != null)
						fields.put(PdksEntityController.MAP_KEY_SESSION, session);
					izinTipiList = pdksEntityController.getObjectByInnerObjectListInLogic(fields, IzinTipi.class);
					if (izinTipiList.size() > 1)
						izinTipiList = PdksUtil.sortListByAlanAdi(izinTipiList, "id", Boolean.FALSE);
					else
						izinTipiList = null;
				}
			} else {
				HashMap<Long, IzinTipi> tipMap = new HashMap<Long, IzinTipi>();
				for (PersonelIzin personelIzin : izinList1)
					tipMap.put(personelIzin.getIzinTipi().getId(), personelIzin.getIzinTipi());
				if (tipMap.size() > 1)
					izinTipiList = PdksUtil.sortListByAlanAdi(new ArrayList(tipMap.values()), "id", Boolean.FALSE);
				tipMap = null;
			}
		}
		setPersonelizinList(izinList1);
		fields = null;
	}

	public List<PersonelIzin> getPersonelizinList() {
		return personelizinList;
	}

	public void setPersonelizinList(List<PersonelIzin> personelizinList) {
		this.personelizinList = personelizinList;
	}

	public boolean isGecmisYil() {
		return gecmisYil;
	}

	public void setGecmisYil(boolean gecmisYil) {
		this.gecmisYil = gecmisYil;
	}

	public Double getIzinSuresi() {
		return izinSuresi;
	}

	public void setIzinSuresi(Double izinSuresi) {
		this.izinSuresi = izinSuresi;
	}

	public boolean isGelecekIzinGoster() {
		return gelecekIzinGoster;
	}

	public void setGelecekIzinGoster(boolean gelecekIzinGoster) {
		this.gelecekIzinGoster = gelecekIzinGoster;
	}

	public Double getBakiyeSuresi() {
		return bakiyeSuresi;
	}

	public void setBakiyeSuresi(Double bakiyeSuresi) {
		this.bakiyeSuresi = bakiyeSuresi;
	}

	public TempIzin getUpdateTempIzin() {
		return updateTempIzin;
	}

	public void setUpdateTempIzin(TempIzin updateTempIzin) {
		this.updateTempIzin = updateTempIzin;
	}

	public List<TempIzin> getPdksPersonelList() {
		return pdksPersonelList;
	}

	public void setPdksPersonelList(List<TempIzin> pdksPersonelList) {
		this.pdksPersonelList = pdksPersonelList;
	}

	public PersonelIzin getUpdateIzin() {
		return updateIzin;
	}

	public void setUpdateIzin(PersonelIzin updateIzin) {
		this.updateIzin = updateIzin;
	}

	public Date getDonemSonu() {
		return donemSonu;
	}

	public void setDonemSonu(Date donemSonu) {
		this.donemSonu = donemSonu;
	}

	public List<PersonelIzinDetay> getHarcananIzinler() {
		return harcananIzinler;
	}

	public void setHarcananIzinler(List<PersonelIzinDetay> harcananIzinler) {
		this.harcananIzinler = harcananIzinler;
	}

	public int getMinYil() {
		return minYil;
	}

	public void setMinYil(int minYil) {
		this.minYil = minYil;
	}

	public int getMaxYil() {
		return maxYil;
	}

	public void setMaxYil(int maxYil) {
		this.maxYil = maxYil;
	}

	public int getBakiyeYil() {
		return bakiyeYil;
	}

	public void setBakiyeYil(int bakiyeYil) {
		this.bakiyeYil = bakiyeYil;
	}

	public int getUpdateAdet() {
		return updateAdet;
	}

	public void setUpdateAdet(int updateAdet) {
		this.updateAdet = updateAdet;
	}

	public boolean isGeciciBakiye() {
		return geciciBakiye;
	}

	public void setGeciciBakiye(boolean geciciBakiye) {
		this.geciciBakiye = geciciBakiye;
	}

	public Dosya getIzinBakiyeDosya() {
		return izinBakiyeDosya;
	}

	public void setIzinBakiyeDosya(Dosya izinBakiyeDosya) {
		this.izinBakiyeDosya = izinBakiyeDosya;
	}

	public List<TempIzin> getPersonelBakiyeIzinList() {
		return personelBakiyeIzinList;
	}

	public void setPersonelBakiyeIzinList(List<TempIzin> personelBakiyeIzinList) {
		this.personelBakiyeIzinList = personelBakiyeIzinList;
	}

	public Double getBakiyeleriTemizle() {
		return bakiyeleriTemizle;
	}

	public void setBakiyeleriTemizle(Double bakiyeleriTemizle) {
		this.bakiyeleriTemizle = bakiyeleriTemizle;
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

	public boolean isSuaVar() {
		return suaVar;
	}

	public void setSuaVar(boolean suaVar) {
		this.suaVar = suaVar;
	}

	public boolean isIstenAyrilanEkle() {
		return istenAyrilanEkle;
	}

	public void setIstenAyrilanEkle(boolean istenAyrilanEkle) {
		this.istenAyrilanEkle = istenAyrilanEkle;
	}

	public List<IzinTipi> getIzinTipiList() {
		return izinTipiList;
	}

	public void setIzinTipiList(List<IzinTipi> izinTipiList) {
		this.izinTipiList = izinTipiList;
	}

	public boolean isIptalIzinleriGetir() {
		return iptalIzinleriGetir;
	}

	public void setIptalIzinleriGetir(boolean iptalIzinleriGetir) {
		this.iptalIzinleriGetir = iptalIzinleriGetir;
	}

	public User getAuthenticatedUser() {
		return authenticatedUser;
	}

	public void setAuthenticatedUser(User authenticatedUser) {
		this.authenticatedUser = authenticatedUser;
	}

	public AramaSecenekleri getAramaSecenekleri() {
		return aramaSecenekleri;
	}

	public void setAramaSecenekleri(AramaSecenekleri aramaSecenekleri) {
		this.aramaSecenekleri = aramaSecenekleri;
	}

	public int getBaslangicYil() {
		return baslangicYil;
	}

	public void setBaslangicYil(int baslangicYil) {
		this.baslangicYil = baslangicYil;
	}

	public boolean isBolumKlasorEkle() {
		return bolumKlasorEkle;
	}

	public void setBolumKlasorEkle(boolean bolumKlasorEkle) {
		this.bolumKlasorEkle = bolumKlasorEkle;
	}

	public String getBolumAciklama() {
		return bolumAciklama;
	}

	public void setBolumAciklama(String bolumAciklama) {
		this.bolumAciklama = bolumAciklama;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

}
