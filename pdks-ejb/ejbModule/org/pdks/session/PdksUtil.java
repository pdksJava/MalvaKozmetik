package org.pdks.session;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.Collator;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;
import javax.mail.internet.InternetAddress;
import javax.persistence.EntityManager;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.catalina.util.Base64;
import org.apache.catalina.util.MD5Encoder;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.persistence.HibernateSessionProxy;
import org.json.JSONObject;
import org.json.XML;
import org.pdks.entity.Dosya;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelDenklestirme;
import org.pdks.entity.Tanim;
import org.pdks.security.entity.DefaultPasswordGenerator;
import org.pdks.security.entity.Role;
import org.pdks.security.entity.User;
import org.richfaces.model.UploadItem;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sap.conn.jco.JCoAttributes;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.JCoTable;
import com.sap.mw.jco.JCO;
import com.sap.mw.jco.JCO.Attributes;

@Name("pdksUtil")
public class PdksUtil implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7171523343222649407L;
	static Logger logger = Logger.getLogger(PdksUtil.class);

	public static final Locale RU_LOCALE = new Locale("ru", "RU");
	public static final Locale TR_LOCALE = new Locale("tr", "TR");
	public static final String SEPARATOR_MAIL = ";";
	public static final String SEPARATOR_KOD_ACIKLAMA = "_";
	public static final boolean isUTF8 = Boolean.TRUE;
	private static HttpServletRequest httpServletRequest;

	private static int MESAI_YUVARLAMA_KATSAYI = 40;

	private static int sistemBaslangicYili, izinOffAdet, izinHaftaAdet, planOffAdet;

	private static String url, bundleName, canliSunucu, testSunucu, dateFormat = "dd/MM/yyyy", dateTimeFormat, dateTimeLongFormat, saatFormat = "H:mm";;

	private static Date sonSistemTarih, helpDeskLastDate;

	private static double odenenFazlaMesaiSaati = 30d;

	private static Integer yarimYuvarlaLast = 1;

	private static boolean sistemDestekVar = false, puantajSorguAltBolumGir = false;

	/**
	 * @param dosyaAdi
	 * @param dosyaIcerik
	 * @return
	 */
	public static byte[] getFileZip(String dosyaAdi, byte[] dosyaIcerik) {
		byte[] bytesZip = null;
		if (dosyaIcerik != null && PdksUtil.hasStringValue(dosyaAdi)) {
			Dosya dosya = new Dosya();
			dosya.setDosyaAdi(dosyaAdi);
			dosya.setDosyaIcerik(dosyaIcerik);
			List<Dosya> list = new ArrayList<Dosya>();
			list.add(dosya);
			OrtakIslemler ortakIslemler = new OrtakIslemler();
			try {
				String zipDosyaAdi = "tmp" + DefaultPasswordGenerator.generate(8) + ".zip";
				File file = ortakIslemler.dosyaZipFileOlustur(zipDosyaAdi, list);
				if (file != null && file.exists()) {
					dosya = ortakIslemler.dosyaFileOlustur(zipDosyaAdi, file, Boolean.TRUE);
					file.deleteOnExit();
					bytesZip = dosya.getDosyaIcerik();
				}

			} catch (Exception e) {
			}
			dosya = null;
			ortakIslemler = null;
		}
		return bytesZip;
	}

	/**
	 * @param deger
	 */
	public static LinkedHashMap<String, String> parametreAyikla(String deger) {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		StringTokenizer st = new StringTokenizer(deger, ";");
		while (st.hasMoreTokens()) {
			String str = st.nextToken();
			if (str.indexOf(":") > 0) {
				String veri[] = str.split(":");
				if (veri.length == 2)
					map.put(replaceAll(veri[0], " ", ""), replaceAll(veri[1], " ", ""));

			}
		}
		return map;
	}

	/**
	 * @param ad
	 * @param soyad
	 * @return
	 */
	public static String getAdSoyad(String ad, String soyad) {
		String str = (ad != null ? ad.trim() + " " : "") + (soyad != null ? soyad.trim() + " " : "");
		if (str.indexOf("  ") >= 0)
			str = replaceAllManuel(str, "  ", " ");
		while (str.startsWith(" ")) {
			str = str.substring(1);
		}
		return str.trim();
	}

	/**
	 * @param jsonStr
	 * @param rootName
	 * @param arrayTag
	 * @return
	 */
	public static String getJsonToXML(String jsonStr, String rootName, String arrayTag) {
		String str = "";
		try {
			if (hasStringValue(arrayTag)) {
				if (jsonStr.startsWith("["))
					jsonStr = "{\"" + arrayTag + "\":" + jsonStr + "}";
				else if (jsonStr.startsWith("{"))
					jsonStr = "{\"" + arrayTag + "\":" + jsonStr.substring(1);
			}
			JSONObject jsonObject = new JSONObject(jsonStr);
			str = XML.toString(jsonObject);
		} catch (Exception e) {
			str = jsonStr;
		}
		String xml = formatXML("<?xml version=\"1.0\" encoding=\"UTF-8\"?><" + rootName + ">" + str + "</" + rootName + ">");
		return xml;
	}

	/**
	 * @param aciklama
	 * @return
	 */
	public static boolean hasStringValue(String aciklama) {
		boolean strDolu = aciklama != null && aciklama.trim().length() > 0;
		return strDolu;
	}

	/**
	 * @param aciklama
	 * @return
	 */
	public static String getHtmlAciklama(String aciklama) {
		String str = aciklama;
		if (aciklama != null) {
			if (str.length() > 0) {
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < str.length(); i++) {
					char c = str.charAt(i);
					int j = (int) c;
					switch (j) {
					case 10:
						sb.append("\n");
						break;
					default:
						if (j >= 32)
							sb.append(String.valueOf(c));
						else
							sb.append("");
						break;
					}
				}
				str = sb.toString();
				sb = null;
			}
			HashMap<String, String> map = getSpecialMap();
			for (String pattern : map.keySet()) {
				if (str.indexOf(pattern) >= 0) {
					String replace = map.get(pattern);
					str = replaceAllManuel(str, pattern, replace);
				}
			}
			str = convertUTF8(str);
		}

		return str;
	}

	/**
	 * @return
	 */
	private static HashMap<String, String> getSpecialMap() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("&Agrave;", "À");
		map.put("&Aacute;", "Á");
		map.put("&Acirc;", "Â");
		map.put("&Atilde;", "Ã");
		map.put("&Auml;", "Ä");
		map.put("&Aring;", "Å");
		map.put("&agrave;", "à");
		map.put("&aacute;", "á");
		map.put("&acirc;", "â");
		map.put("&atilde;", "ã");
		map.put("&auml;", "ä");
		map.put("&aring;", "å");
		map.put("&AElig;", "Æ");
		map.put("&aelig;", "æ");
		map.put("&szlig;", "ß");
		map.put("&Ccedil;", "Ç");
		map.put("&ccedil;", "ç");
		map.put("&Egrave;", "È");
		map.put("&Eacute;", "É");
		map.put("&Ecirc;", "Ê");
		map.put("&Euml;", "Ë");
		map.put("&egrave;", "è");
		map.put("&eacute;", "é");
		map.put("&ecirc;", "ê");
		map.put("&euml;", "ë");
		map.put("&#131;", "ƒ");
		map.put("&Igrave;", "Ì");
		map.put("&Iacute;", "Í");
		map.put("&Icirc;", "Î");
		map.put("&Iuml;", "Ï");
		map.put("&igrave;", "ì");
		map.put("&iacute;", "í");
		map.put("&icirc;", "î");
		map.put("&iuml;", "ï");
		map.put("&Ntilde;", "Ñ");
		map.put("&ntilde;", "ñ");
		map.put("&Ograve;", "Ò");
		map.put("&Oacute;", "Ó");
		map.put("&Ocirc;", "Ô");
		map.put("&Otilde;", "Õ");
		map.put("&Ouml;", "Ö");
		map.put("&ograve;", "ò");
		map.put("&oacute;", "ó");
		map.put("&ocirc;", "ô");
		map.put("&otilde;", "õ");
		map.put("&ouml;", "ö");
		map.put("&Oslash;", "Ø");
		map.put("&oslash;", "ø");
		map.put("&#140;", "Œ");
		map.put("&#156;", "œ");
		map.put("&#138;", "Š");
		map.put("&#154;", "š");
		map.put("&Ugrave;", "Ù");
		map.put("&Uacute;", "Ú");
		map.put("&Ucirc;", "Û");
		map.put("&Uuml;", "Ü");
		map.put("&ugrave;", "ù");
		map.put("&uacute;", "ú");
		map.put("&ucirc;", "û");
		map.put("&uuml;", "ü");
		map.put("&#181;", "µ");
		map.put("&#215;", "×");
		map.put("&Yacute;", "Ý");
		map.put("&#159;", "Ÿ");
		map.put("&yacute;", "ý");
		map.put("&yuml;", "ÿ");
		map.put("&#176;", "°");
		map.put("&#134;", "†");
		map.put("&#135;", "‡");
		map.put("&lt;", "<");
		map.put("&gt;", ">");
		map.put("&#177;", "±");
		map.put("&#171;", "«");
		map.put("&#187;", "»");
		map.put("&#191;", "¿");
		map.put("&#161;", "¡");
		map.put("&#183;", "·");
		map.put("&#149;", "•");
		map.put("&#153;", "™");
		map.put("&copy;", "©");
		map.put("&reg;", "®");
		map.put("&#167;", "§");
		map.put("&#182;", "¶");
		return map;
	}

	/**
	 * @param value
	 * @return
	 */
	public static String getCutFirstSpaces(String value) {
		String str = value;
		if (value != null) {
			while (str.startsWith(" "))
				str = str.substring(1);
		}
		return str;
	}

	/**
	 * @param value
	 * @param list
	 * @return
	 */
	public static String getSelectItemLabel(Object value, List<SelectItem> list) {
		String label = "";
		if (list != null && value != null) {
			for (SelectItem selectItem : list) {
				if (selectItem.getValue().equals(value))
					try {
						label = selectItem.getLabel();
						break;
					} catch (Exception e) {
					}

			}
		}
		return label;
	}

	/**
	 * @param value
	 * @return
	 */
	public static Date getDateFromString(String value) {
		Date date = null;
		if (value != null) {
			int len = value.length();
			String pattern = null;
			switch (len) {
			case 8:
				pattern = "yyyyMMdd";
				break;
			case 10:
				String[] dizi = new String[] { "-", ".", "/" };
				for (int i = 0; i < dizi.length; i++) {
					String delemiter = dizi[i];
					int pos = value.indexOf(delemiter);
					switch (pos) {
					case 2:
						pattern = "dd" + delemiter + "MM" + delemiter + "yyyy";
						break;
					case 4:
						pattern = "yyyy" + delemiter + "MM" + delemiter + "dd";
						break;

					default:
						break;
					}
					if (pattern != null)
						break;
				}

				break;
			default:
				break;
			}
			if (pattern != null)
				date = convertToJavaDate(value, pattern);

		}
		return date;
	}

	/**
	 * @param email
	 * @return
	 */
	public static boolean isValidEmail(String email) {
		boolean matches = false;
		if (email != null && email.indexOf("@") > 1) {
			String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
			Pattern pattern = Pattern.compile(emailRegex);
			Matcher matcher = pattern.matcher(email);
			matches = matcher.matches();
		}
		return matches;
	}

	public static Date getDateTime(Date date) {
		Date tarih = null;
		if (date != null) {
			String pattern = "yyyyMMddHHmm";
			String dateStr = convertToDateString(date, pattern);
			tarih = convertToJavaDate(dateStr, pattern);
		}
		return tarih;
	}

	/**
	 * @param xml
	 * @return
	 */
	public static String formatXML(String xml) {
		String formatXML = null;
		try {
			// final InputSource src = new InputSource(new StringReader(xml));
			InputStream is = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
			final InputSource src = new InputSource(is);
			final Node document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(src).getDocumentElement();
			final Boolean keepDeclaration = Boolean.valueOf(xml.startsWith("<?xml"));
			final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
			final DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
			final LSSerializer writer = impl.createLSSerializer();
			writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE); // Set this to true if the output needs to be beautified.
			writer.getDomConfig().setParameter("xml-declaration", keepDeclaration); // Set this to true if the declaration is needed to be outputted.
			formatXML = writer.writeToString(document);
		} catch (Exception e) {
			formatXML = xml;
		}
		if (formatXML != null && formatXML.indexOf("UTF-16") > 0)
			formatXML = replaceAll(formatXML, "UTF-16", "UTF-8");
		return formatXML;
	}

	public Locale getLocale_TR() {
		return TR_LOCALE;

	}

	public Locale getLocale_RU() {
		return RU_LOCALE;

	}

	public Locale getLocale_EN() {
		return Locale.ENGLISH;

	}

	/**
	 * @param jsonString
	 * @return
	 */
	public static String toPrettyFormat(String jsonString) {
		String prettyJson = null;
		try {
			JsonParser parser = new JsonParser();
			JsonObject json = parser.parse(jsonString).getAsJsonObject();
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			prettyJson = gson.toJson(json);
			if (prettyJson.lastIndexOf("\\u0026") > 0)
				prettyJson = replaceAll(prettyJson, "\\u0026", "&");
			if (prettyJson.lastIndexOf("\\u003d") > 0)
				prettyJson = replaceAll(prettyJson, "\\u003d", "=");
		} catch (Exception e) {
			prettyJson = jsonString;
		}

		return prettyJson;
	}

	public static String getValidValue(String key) {
		String deger = null;

		if (key != null) {
			key = encode(key);
			int i = 0, j = 0;
			StringBuffer sb = new StringBuffer();
			// logger.info(key);
			for (i = 0; i < key.length(); i++) {

				char current = key.charAt(i);
				j = (int) current;
				// logger.info(j+" "+current);
				if (j == 44 || j == 58 || j == 59)
					sb.append(String.valueOf(current));
				else if (j == 9 || j == 10 || j == 13 || j == 26 || j == 30 || j == 160)
					sb.append("");
				else if (j == 304 || j == 351 || j == 287 || j == 305 || j == 350 || j == 286)
					sb.append(String.valueOf(current));
				else if (j >= 32 && j <= 126)
					sb.append(String.valueOf(current));
				else if (j > 160 && j <= 255)
					sb.append(String.valueOf(current));

			}
			deger = sb.toString();

		}

		return deger;

	}

	/**
	 * @param in
	 * @return
	 */
	public static String StringToByInputStream(InputStream in) {
		String read = null;
		if (in != null) {
			StringBuilder sb = new StringBuilder();
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(in, "utf-8"));
				while ((read = br.readLine()) != null) {
					sb.append(read);
				}
				br.close();
			} catch (Exception e) {
			}
			read = sb.toString();
			sb = null;
		}
		return read;
	}

	/**
	 * @param ip
	 * @return
	 */
	private static boolean isLocalIp(String ip) {
		boolean localIp = ip != null && (ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1"));
		return localIp;
	}

	/**
	 * @return
	 */
	public static String getRemoteAddr() {
		String remoteAddr = null, hostAddress = null;
		try {
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			if (request != null)
				remoteAddr = request.getHeader("X-FORWARDED-FOR") != null ? request.getHeader("X-FORWARDED-FOR") : request.getRemoteAddr();
			if (remoteAddr != null && isLocalIp(remoteAddr)) {
				InetAddress thisIp = InetAddress.getLocalHost();
				if (thisIp != null) {
					hostAddress = thisIp.getHostAddress();
					remoteAddr = hostAddress;
				}

			}

		} catch (Exception e) {
		}
		if (remoteAddr != null)
			logger.debug(remoteAddr + (hostAddress != null && !remoteAddr.equals(hostAddress) ? " -- " + hostAddress : ""));
		return remoteAddr;
	}

	/**
	 * @return
	 */
	public static boolean getTestDurum() {
		String hostName = getHostName(false);
		String sunucu = canliSunucu != null ? canliSunucu.toLowerCase(Locale.ENGLISH) : "srvglf";
		boolean test = !hostName.toLowerCase(Locale.ENGLISH).startsWith(sunucu);
		return test;
	}

	/**
	 * @return
	 */
	public static boolean getCanliSunucuDurum() {
		String hostName = getHostName(false);
		String sunucu = canliSunucu != null ? canliSunucu.toLowerCase(Locale.ENGLISH) : "srvglf";
		boolean canli = hostName.toLowerCase(Locale.ENGLISH).startsWith(sunucu);
		return canli;
	}

	/**
	 * @return
	 */
	public static boolean getTestSunucuDurum() {
		String hostName = getHostName(false);
		String sunucu = testSunucu != null ? testSunucu.toLowerCase(Locale.ENGLISH) : "srvglf";
		boolean test = hostName.toLowerCase(Locale.ENGLISH).startsWith(sunucu);
		return test;
	}

	/**
	 * @param orjinalName
	 * @return
	 */
	public static String getHostName(boolean orjinalName) {
		String hostName = null;
		try {
			InetAddress thisIp = InetAddress.getLocalHost();
			hostName = orjinalName ? thisIp.getHostName() : thisIp.getHostName().toLowerCase(Locale.ENGLISH);
			logger.debug("hostName : " + hostName);
		} catch (Exception e) {
			hostName = "";
		}

		return hostName;
	}

	public static String getEncodeStringByBase64(String string) {
		return new String(Base64.encode((string).getBytes()));
	}

	public static String getDecodeStringByBase64(String string) {
		return new String(Base64.decode((string).getBytes()));
	}

	public static boolean isInternetExplorer(HttpServletRequest req) {
		boolean ie9 = Boolean.FALSE;
		if (req.getHeader("user-agent") != null) {
			String userAgent = req.getHeader("user-agent").toLowerCase();
			logger.debug(userAgent);
			if (userAgent != null && (userAgent.indexOf("msie") >= 0)) {
				ie9 = Boolean.TRUE;

			}
			userAgent = null;
		}
		return ie9;
	}

	public static HashMap<String, String> getDecodeMapByBase64(String key) {
		String planKey = getDecodeStringByBase64(key);
		StringTokenizer st = new StringTokenizer(planKey, "&");
		HashMap<String, String> veriMap = new HashMap<String, String>();
		while (st.hasMoreTokens()) {
			String veri = st.nextToken();
			String[] deger = veri.split("=");
			if (deger != null && deger.length == 2)
				veriMap.put(deger[0], deger[1]);
		}
		st = null;
		return veriMap;
	}

	public static void setTimeZome() {
		String system = System.getProperty("os.name");
		if (system == null)
			system = "";
		else
			system = system.toLowerCase(Locale.ENGLISH);
		if (system.indexOf("windows") >= 0) {
			String timeZone = "Asia/Istanbul";
			TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
		}
	}

	public static ValidatorException setMesajYaz(List<String> mesajList) {

		FacesContext context = FacesContext.getCurrentInstance();

		for (String mesaj : mesajList)
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, mesaj, mesaj));
		return new ValidatorException(new FacesMessage(""));
	}

	public static byte[] getFileByteArray(File file) throws Exception {
		byte[] dosyaIcerik = new byte[(int) file.length()];
		InputStream ios = null;
		try {
			ios = new FileInputStream(file);
			if (ios.read(dosyaIcerik) == -1) {
				throw new IOException("EOF reached while trying to read the whole file");
			}
		} finally {
			try {
				if (ios != null)
					ios.close();
			} catch (IOException e) {
			}
		}
		return dosyaIcerik;
	}

	public static File dosyaOlustur(String fileName, byte[] s) {

		try {
			FileOutputStream fos = new FileOutputStream(fileName);
			fos.write(s);
			fos.close();
		} catch (Exception e) {
			logger.error("PDKS out : \n");
			e.printStackTrace();
			logger.error("PDKS out : " + e.getMessage());

		}
		File file = new File(fileName);
		return file;
	}

	public static ValidatorException setMesajYaz(String mesaj) {
		if (mesaj == null)
			mesaj = "";
		FacesMessage facesMessage = new FacesMessage(mesaj, "message");
		// facesMessage.setDetail(mesaj);
		// facesMessage.setSummary(mesaj);
		// facesMessage.setSeverity(FacesMessage.SEVERITY_ERROR);
		return new ValidatorException(facesMessage);

	}

	public static Object getFormValue(FacesContext context, UIComponent component, String objectId) throws Exception {
		String clientId = (String) component.getAttributes().get(objectId);

		// Find the actual JSF component for the client ID.
		UIComponent uiComponent = context.getViewRoot().findComponent(clientId);
		UIOutput objectOutput = (UIOutput) uiComponent;
		// Get its value, the entered objectId of the first field.
		Object deger = objectOutput.getValue();

		return deger;

	}

	/**
	 * @param baos
	 * @param fileName
	 * @return
	 */
	public static void setExcelHttpServletResponse(ByteArrayOutputStream baos, String fileName) {
		try {
			if (baos != null) {
				HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
				httpServletResponse.setContentType("application/vnd.ms-excel; charset=UTF-8");
				httpServletResponse.setCharacterEncoding("UTF-8");
				httpServletResponse.setHeader("Expires", "0");
				httpServletResponse.setHeader("Pragma", "cache");
				httpServletResponse.setHeader("Cache-Control", "cache");
				String dosyaAdi = encoderURL(fileName, "UTF-8");

				httpServletResponse.setHeader("Content-Disposition", "attachment;filename=" + dosyaAdi);
				writeByteArrayOutputStream(httpServletResponse, baos);
			}
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}

	/**
	 * @param httpServletResponse
	 * @param baos
	 */
	public static void writeByteArrayOutputStream(HttpServletResponse httpServletResponse, ByteArrayOutputStream baos) {
		try {
			if (baos != null && httpServletResponse != null) {
				ServletOutputStream sos = httpServletResponse.getOutputStream();
				httpServletResponse.setContentLength(baos.size());
				byte[] bytes = baos.toByteArray();
				sos.write(bytes, 0, bytes.length);
				sos.flush();
				sos.close();
				FacesContext.getCurrentInstance().responseComplete();
			}
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}

	// String yeniString=new
	// String(gelenString.getBytes("ISO-8859-1"),"ISO-8859-9");

	public static String convertUTF8(String sonuc) {
		if (sonuc != null) {
			if (sonuc.indexOf('İ') >= 0) {// I için 1
				sonuc = replaceAllManuel(sonuc, String.valueOf('İ'), "\u0130");
			}
			if (sonuc.indexOf('ı') >= 0) {// i için 2
				sonuc = replaceAllManuel(sonuc, String.valueOf('ı'), "\u0131");
			}
			if (sonuc.indexOf('Ş') >= 0) {// i için 2
				sonuc = replaceAllManuel(sonuc, String.valueOf('Ş'), "\u015E");
			}
			if (sonuc.indexOf('ş') >= 0) {// i için 2
				sonuc = replaceAllManuel(sonuc, String.valueOf('ş'), "\u015F");
			}
			if (sonuc.indexOf('Ğ') >= 0) {// i için 2
				sonuc = replaceAllManuel(sonuc, String.valueOf('Ğ'), "\u011E");
			}
			if (sonuc.indexOf('ğ') >= 0) {// i için 2
				sonuc = replaceAllManuel(sonuc, String.valueOf('ğ'), "\u011F");
			}
		}
		return sonuc;
	}

	public static String encode(String sa) {

		if (sa != null) {
			StringBuilder sb = new StringBuilder();
			int i = 0, j = 0;
			for (i = 0; i < sa.length(); i++) {
				j = (int) sa.charAt(i);
				if (j == 221)
					sb.append("İ");
				else if (j == 253)
					sb.append("ı");
				else if (j == 222 || j == 94)
					sb.append("Ş");
				else if (j == 254)
					sb.append("ş");
				else if (j == 208)
					sb.append("Ğ");
				else if (j == 240)
					sb.append("ğ");
				else
					sb.append(sa.charAt(i));
			}
			logger.debug("Önceki hali " + sa + "  Sonraki hali: " + sb.toString());
			sa = sb.toString();
			sb = null;
		}
		return sa;
	}

	public static List<String> getSayilar(int bas, int son, int artis) {

		List<String> saatListesi = new ArrayList<String>();
		for (int i = bas; i < son; i = i + artis) {
			String saatStr = "" + i;
			if (saatStr.length() == 1)
				saatStr = "0" + saatStr;
			saatListesi.add(saatStr);
		}

		return saatListesi;
	}

	/**
	 * Gelen text'in başına maximum text uzunluğuna ulaşıncaya dek gelen karakteri ekler.
	 * 
	 * @param txt
	 * @param karakter
	 * @param maxTextLength
	 * @return
	 */
	public static String textBaslangicinaKarakterEkle(String txt, char karakter, int maxTextLength) {
		StringBuilder karakterStrBuf = new StringBuilder(maxTextLength);
		if (txt != null && txt.length() < maxTextLength) {
			for (int i = 0; i < maxTextLength - txt.length(); i++)
				karakterStrBuf.append(karakter);
		}
		String str = karakterStrBuf.append(txt).toString();
		karakterStrBuf = null;
		return str;
	}

	/**
	 * Gelen text'in sonuna maximum text uzunluğuna ulaşıncaya dek gelen karakteri ekler.
	 * 
	 * @param txt
	 * @param karakter
	 * @param maxTextLength
	 * @return
	 */
	public static String textSonunaKarakterEkle(String txt, char karakter, int maxTextLength) {
		StringBuilder karakterStrBuf = new StringBuilder(maxTextLength).append(txt);
		if (txt != null && txt.length() < maxTextLength) {
			for (int i = 0; i < maxTextLength - txt.length(); i++)
				karakterStrBuf.append(karakter);
		}
		String str = karakterStrBuf.toString();
		karakterStrBuf = null;
		return str;
	}

	/**
	 * @param value
	 * @return
	 */
	public static boolean isDoubleValueNotLong(Double value) {
		boolean doubleValue = false;
		if (value != null)
			doubleValue = value.doubleValue() - value.longValue() != 0.0d;
		return doubleValue;

	}

	/**
	 * @param tarih1
	 * @param tarih2
	 * @return
	 */
	public static Long tarihFarki(Date tarih1, Date tarih2) {
		Long fark = null;
		if (tarih1 != null && tarih2 != null) {
			if (tarih2.after(tarih1)) {
				Calendar cal = Calendar.getInstance();
				int sayi = 0;
				String pattern = "yyyyMMddHHmm";
				long deger2 = Long.parseLong(convertToDateString(tarih2, pattern));
				long deger1 = Long.parseLong(convertToDateString(tarih1, pattern));
				while (deger2 > deger1) {
					cal.setTime(tarih1);
					cal.add(Calendar.DATE, sayi + 1);
					deger1 = Long.parseLong(convertToDateString(cal.getTime(), pattern));
					if (deger2 >= deger1)
						sayi++;
					// logger.info(deger1 + " " + deger2 + " " + sayi);
				}
				fark = (long) sayi;

				// fark = tarih1 != null && tarih2 != null ? new Long((tarih2.getTime() - tarih1.getTime()) / (1000 * 60 * 60 * 24)) : null;
			} else
				fark = 0l;

		}

		return fark;
	}

	/**
	 * @param tarihBit
	 * @param tarihBas
	 * @return
	 */
	public static Double getSaatFarkiHesapla(Date tarihBit, Date tarihBas) {
		double fark = 0.0d;
		if (tarihBit != null && tarihBas != null) {
			long zoneDSTFark = getTimeZoneDSTFark(tarihBit, tarihBas);
			fark = setDoubleRounded((double) (tarihBit.getTime() - tarihBas.getTime() + zoneDSTFark) / ((double) 1000 * 60 * 60), 2, BigDecimal.ROUND_HALF_DOWN);
		}
		return fark;
	}

	/**
	 * @param tarihBit
	 * @param tarihBas
	 * @return
	 */
	public static Double getDakikaFarkiHesapla(Date tarihBit, Date tarihBas) {
		double fark = 0.0d;
		if (tarihBit != null && tarihBas != null) {
			long zoneDSTFark = getTimeZoneDSTFark(tarihBit, tarihBas);
			fark = setDoubleRounded((double) (tarihBit.getTime() - tarihBas.getTime() + zoneDSTFark) / ((double) 1000 * 60), 2, BigDecimal.ROUND_HALF_DOWN);
		}
		return fark;
	}

	/**
	 * @param tarihBit
	 * @param tarihBas
	 * @return
	 */
	public static long getTimeZoneDSTFark(Date tarihBit, Date tarihBas) {
		long fark = 0l;
		if (tarihBit != null && tarihBas != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(tarihBit);
			long zone1 = cal.get(Calendar.ZONE_OFFSET), dts1 = cal.get(Calendar.DST_OFFSET);
			cal.setTime(tarihBas);
			long zone2 = cal.get(Calendar.ZONE_OFFSET), dts2 = cal.get(Calendar.DST_OFFSET);
			fark = (zone2 - dts2) - (zone1 - dts1);
		}
		return fark;
	}

	/**
	 * @param tarihBit
	 * @param tarihBas
	 * @return
	 */
	public static Double getSaatFarki(Date tarihBit, Date tarihBas) {
		double fark = 0.0d;
		if (tarihBit != null && tarihBas != null) {
			fark = getSaatFarkiHesapla(tarihBit, tarihBas);
			long zoneDSTFark = getTimeZoneDSTFark(tarihBit, tarihBas);
			if (zoneDSTFark != 0) {
				logger.debug(fark + " " + tarihBit + "  " + tarihBas + " " + zoneDSTFark);
				double fark1 = getSaatFarkiHesapla(tariheAyEkleCikar((Date) tarihBit.clone(), 1), tariheAyEkleCikar((Date) tarihBas.clone(), 1));
				if (fark1 != fark) {
					double fark2 = getSaatFarkiHesapla(tariheAyEkleCikar((Date) tarihBit.clone(), 2), tariheAyEkleCikar((Date) tarihBas.clone(), 2));
					if (fark2 == fark1)
						fark = fark1;
				}

			}
		} else
			logger.debug("");

		return fark;

	}

	/**
	 * @param tarihBit
	 * @param tarihBas
	 * @return
	 */
	public static Double getDakikaFarki(Date tarihBit, Date tarihBas) {
		double fark = 0.0d;
		if (tarihBit != null && tarihBas != null) {
			String pattern = "yyyyMMdd HH:mm:ss";
			String str1 = convertToDateString(tarihBit, pattern), str2 = convertToDateString(tarihBas, pattern);
			Date tarih1 = convertToJavaDate(str1, pattern), tarih2 = convertToJavaDate(str2, pattern);
			long zoneDSTFark = getTimeZoneDSTFark(tarihBit, tarihBas);
			fark = setDoubleRounded((double) (tarih1.getTime() - tarih2.getTime() + zoneDSTFark) / ((double) 1000 * 60), 2, BigDecimal.ROUND_HALF_DOWN);
		}
		return fark;

	}

	/**
	 * java.util.Date objesinden tarihi, SQL sorgusunda kullanilabilecek formata dönüştürür.
	 * 
	 * @param date
	 * @return
	 */
	public static String convertToDateString(Date date, String pattern) {
		Locale locale = getLocale();

		SimpleDateFormat sdf = locale != null ? new SimpleDateFormat(pattern, locale) : new SimpleDateFormat(pattern);
		return date != null ? sdf.format(date) : "";
	}

	/**
	 * Verilen tarihe, verilen gün sayisi eklenir ve yeni olusan java.util.Date objesi döndürülür.
	 * 
	 * @param date
	 * @param gunSayisi
	 * @return
	 */
	public static Date tariheGunEkleCikar(Date date, int gunSayisi) {
		return addTarih(date, Calendar.DATE, gunSayisi);

	}

	/**
	 * Verilen tarihe, verilen ay sayisi eklenir ve yeni olusan java.util.Date objesi döndürülür.
	 * 
	 * @param date
	 * @param aySayisi
	 * @return
	 */
	public static Date tariheAyEkleCikar(Date date, int aySayisi) {
		return addTarih(date, Calendar.MONTH, aySayisi);
	}

	/**
	 * @param str
	 * @param delim
	 * @return
	 */
	public static List<String> getListStringTokenizer(String str, String delim) {
		List<String> list = new ArrayList();
		if (delim == null) {
			delim = "";
			if (str.indexOf(",") > 0)
				delim = ",";
			else if (str.indexOf(";") > 0)
				delim = ";";
			else if (str.indexOf("|") > 0)
				delim = "|";
			else if (str.indexOf(" ") > 0)
				delim = " ";
		}
		StringTokenizer st = new StringTokenizer(str, delim);
		while (st.hasMoreTokens()) {
			String part = st.nextToken();
			list.add(part);
		}
		return list;
	}

	/**
	 * @param str
	 * @param delim
	 * @return
	 */
	public static List<String> getListByString(String str, String delim) {
		List<String> lList = null;
		if (str != null) {
			if (delim == null) {
				if (str.indexOf(",") > 0)
					delim = ",";
				else if (str.indexOf(";") > 0)
					delim = ";";
				else if (str.indexOf("|") > 0)
					delim = "|";
				else if (str.indexOf(" ") > 0)
					delim = " ";
			}
			if (delim != null) {
				lList = getListStringTokenizer(str, delim);

			}

			else {
				lList = new ArrayList();
				lList.add(str);

			}
		}
		return lList;
	}

	public static List<String> getListFromString(String str, String delim) {
		List<String> list = new ArrayList<String>();
		List<String> mailList = null;
		if (str != null) {
			mailList = getListByString(str, delim);

			if (mailList != null && !mailList.isEmpty()) {
				for (Iterator iterator = mailList.iterator(); iterator.hasNext();) {
					String token = (String) iterator.next();
					try {
						String parca = getInternetAdres(token.trim());
						if (hasStringValue(parca))
							list.add(parca);
					} catch (Exception e) {
						logger.error("PDKS hata in : \n");
						e.printStackTrace();
						logger.error("PDKS hata out : " + e.getMessage());

					}

				}
			}

		}
		return list;
	}

	/**
	 * Verilen tarih stringini belirtilen paterne göre java.util.Date objesine donusturur.
	 * 
	 * @param dateStr
	 * @param pattern
	 * @return
	 */
	public static java.util.Date convertToJavaDate(String dateStr, String pattern) {
		Date date = null;
		try {
			if (dateStr != null && pattern != null)
				date = new SimpleDateFormat(pattern).parse(dateStr);
		} catch (ParseException e) {

		}
		return date;
	}

	/**
	 * Girilen float de�eri parse eder.
	 * 
	 * @return
	 */
	public static double parseDouble(String val) {
		double parsedValue = 0.0;
		try {
			if ((val != null) && (val.length() > 0)) {
				parsedValue = Double.parseDouble(val);
				if (parsedValue < 0) {
					parsedValue = 0;
				}
			}
		} catch (NumberFormatException e) {

		}

		return parsedValue;
	}

	/**
	 * Verilen String objesinin numerik değerden oluşup oluşmadığını kontrol eder. request parametresi olarak alınan id değerlerinin kontrolünde kullanılır.
	 * 
	 * @param val
	 * @return
	 */
	public static boolean isNumeric(String val) {
		try {
			Integer.parseInt(val);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	/**
	 * Verilen String objesinin decimal değer olup olmadığı kontrol edilir.
	 * 
	 * @param val
	 * @return
	 */
	public static boolean isDecimal(String val) {
		try {
			Double.parseDouble(val);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;

	}

	/**
	 * @param oldDate
	 * @param newDate
	 * @return
	 */
	public static boolean isDateDegisti(Date oldDate, Date newDate) {
		Long oldId = oldDate != null ? oldDate.getTime() : null;
		Long newId = newDate != null ? newDate.getTime() : null;
		boolean degisti = isLongDegisti(oldId, newId);
		return degisti;
	}

	/**
	 * @param eskiTanim
	 * @param yeniTanim
	 * @return
	 */
	public static boolean isTanimDegisti(Tanim eskiTanim, Tanim yeniTanim) {
		boolean degisti = false;
		try {
			Long oldId = eskiTanim != null && eskiTanim.getId() != null ? eskiTanim.getId() : 0L;
			Long newId = yeniTanim != null && yeniTanim.getId() != null ? yeniTanim.getId() : 0L;
			degisti = isLongDegisti(oldId, newId);
		} catch (Exception e) {
			logger.error(e);
			degisti = true;
			e.printStackTrace();
		}

		return degisti;
	}

	/**
	 * @param oldId
	 * @param newId
	 * @return
	 */
	public static boolean isLongDegisti(Long oldId, Long newId) {
		if (oldId == null)
			oldId = 0L;
		if (newId == null)
			newId = 0L;
		boolean degisti = newId.longValue() != oldId.longValue();
		return degisti;
	}

	/**
	 * @param oldId
	 * @param newId
	 * @return
	 */
	public static boolean isDoubleDegisti(Double oldId, Double newId) {
		if (oldId == null)
			oldId = 0.0d;
		if (newId == null)
			newId = 0.0d;
		boolean degisti = newId.doubleValue() != oldId.doubleValue();
		return degisti;
	}

	/**
	 * @param oldBoolean
	 * @param newBoolean
	 * @return
	 */
	public static boolean isBooleanDegisti(Boolean oldBoolean, Boolean newBoolean) {
		if (oldBoolean == null)
			oldBoolean = Boolean.FALSE;
		if (newBoolean == null)
			newBoolean = Boolean.FALSE;
		boolean degisti = !newBoolean.equals(oldBoolean);
		return degisti;
	}

	/**
	 * @param oldStr
	 * @param newStr
	 * @return
	 */
	public static boolean isStrDegisti(String oldStr, String newStr) {
		if (oldStr == null)
			oldStr = "";
		if (newStr == null)
			newStr = "";
		boolean degisti = !newStr.trim().equals(oldStr.trim());
		return degisti;
	}

	/**
	 * Date tipinde verilen iki tarih değerini alarak yıl,ay ve gün bazında eşit olup olmadıklarını kontrol eder.
	 * 
	 * @param tarih1
	 * @param tarih2
	 * @return iki değeri yil/ay/gün bazında eşitse true, değilse false döner.
	 */
	public static boolean tarihKarsilastir(Date tarih1, Date tarih2) {
		Calendar tarihCal1 = Calendar.getInstance();
		Calendar tarihCal2 = Calendar.getInstance();
		tarihCal1.setTime(tarih1);
		tarihCal2.setTime(tarih2);
		if (tarihCal1.get(Calendar.YEAR) == tarihCal2.get(Calendar.YEAR)) {
			if (tarihCal1.get(Calendar.MONTH) == tarihCal2.get(Calendar.MONTH)) {
				if (tarihCal1.get(Calendar.DATE) == tarihCal2.get(Calendar.DATE)) {
					return true;
				}
			}
		}
		return false;
	}

	public static int tarihKarsilastirNumeric(Date tarih1, Date tarih2) {
		long sayi1 = Long.parseLong(convertToDateString(tarih1, "yyyyMMdd"));
		long sayi2 = Long.parseLong(convertToDateString(tarih2, "yyyyMMdd"));
		int sonuc = 0;
		if (sayi1 > sayi2)
			sonuc = 1;
		else if (sayi1 < sayi2)
			sonuc = -1;
		return sonuc;
	}

	public static Date setTarih(Date tarih, int field, int value) {
		if (tarih != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(tarih);
			try {
				cal.set(field, value);
				tarih = cal.getTime();
			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());

			}
		}
		return tarih;
	}

	public static Date addTarih(Date tarih, int field, int value) {
		if (tarih != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime((Date) tarih.clone());
			try {
				cal.add(field, value);
				tarih = cal.getTime();
			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());

			}
		}
		return tarih;
	}

	public static int getDateField(Date date, int field) {
		int dateField = -1;
		if (date != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime((Date) date.clone());
			try {
				dateField = cal.get(field);

			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());
				dateField = -1;
			}
		}
		return dateField;
	}

	public static Date getYilAyBirinciGun(Integer year, Integer month) {
		Date date = null;
		try {
			Calendar cal = Calendar.getInstance();
			cal.set(year, month - 1, 1);
			date = getDate(cal.getTime());
		} catch (Exception e) {
		}
		return date;
	}

	public static Date getYilAySonGun(Integer yil, Integer ay) {
		Date birinciGun = getYilAyBirinciGun(yil, ay), date = null;
		try {
			if (birinciGun != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(birinciGun);
				int gun = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
				cal.set(Calendar.DATE, gun);
				date = getDate(cal.getTime());
			}
		} catch (Exception e) {
		}
		return date;
	}

	public static Date getDate(Date date) {
		String pattern = "yyyyMMdd";
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		try {
			if (date != null)
				date = sdf.parse(sdf.format(date));
		} catch (ParseException e) {

		}

		return date;
	}

	public static Date toDay() {
		Calendar date = Calendar.getInstance();
		Date tarih = date.getTime();
		return tarih;
	}

	public static Date buGun() {
		return getDate(toDay());
	}

	public static String getStringKes(String str, int uzunluk) {
		if (str == null)
			str = "";
		return str != null && str.length() > uzunluk ? str.substring(0, uzunluk) : str;
	}

	/**
	 * Verilen para değerini(string) double değere dönüştürür.
	 * 
	 * @param value
	 *            double'a dönüştürülmesi istenen string değeri
	 * @return
	 */
	public static double parseCurrency(String value) {
		StringBuilder parsedValue = new StringBuilder();
		char comma = ',';
		for (int i = 0; i < value.length(); i++)
			if (value.charAt(i) != comma)
				parsedValue.append(value.charAt(i));
		double deger = parseDouble(parsedValue.toString());
		parsedValue = null;
		return deger;
	}

	/**
	 * Text alanın null veya empty string olup olmadığını kontrol eder.
	 * 
	 * @param value
	 * @return null veya empty ise false, aksi halde true döner
	 */
	public static boolean textAlanGecerliMi(String value) {
		if (value == null || value.trim().length() <= 0)
			return false;
		return true;
	}

	/**
	 * Türkçe sıralama için karakter kuralı tanımlanmaktadır.
	 * 
	 * @return
	 */
	public static RuleBasedCollator getTrRuleBasedCollator() {
		RuleBasedCollator tr_Collator = (RuleBasedCollator) Collator.getInstance(Constants.TR_LOCALE);
		try {
			tr_Collator = new RuleBasedCollator("<a,A<b,B<c,C<ç,Ç<d,D<e,E<f,F<g,G<\u011f,\u011e<ğ,Ğ<h,H<ı=\u0131;I<i,\u0130=İ<j,J" + "<k,K<l,L<m,M<n,N<o,O<ö,Ö<p,P<q,Q<r,R<s,S<\u015f=ş;\u015e=Ş<t,T<u,U<ü,Ü<v,V<x,X<w,W<y,Y<z,Z<'-'<' '");
		} catch (ParseException ex) {
			ex.printStackTrace();
		}
		tr_Collator.setStrength(Collator.SECONDARY | Collator.CANONICAL_DECOMPOSITION);//
		tr_Collator.setStrength(Collator.TERTIARY);
		return tr_Collator;

	}

	/**
	 * Ruşça sıralama için karakter kuralı tanımlanmaktadır.
	 * 
	 * @return
	 */
	public static RuleBasedCollator getRuRuleBasedCollator() {
		RuleBasedCollator collator = (RuleBasedCollator) Collator.getInstance(Constants.RU_LOCALE);
		try {
			String ruHarf = "< \u0430=?; \u0410=?" + "< \u0431=?; \u0411=?" + "< \u0432=?; \u0412=?" + "< \u0433=?; \u0413=?" + "< \u0434=?; \u0414=?" + "< \u0435=?; \u0415=?" + "< \u0451=?; \u0401=?" + "< \u0436=?; \u0416=?" + "< \u0437=?; \u0417=?" + "< \u0438=?; \u0418=?"
					+ "< \u0439=?; \u0419=?" + "< \u043A=?; \u041A=?" + "< \u043B=?; \u041B=?" + "< \u043C=?; \u041C=?" + "< \u043D=?; \u041D=?" + "< \u043E=?; \u041E=?" + "< \u043F=?; \u041F=?" + "< \u0440=?; \u0420=?" + "< \u0441=?; \u0421=?" + "< \u0442=?; \u0422=?" + "< \u0443=?; \u0423=?"
					+ "< \u0444=?; \u0424=?" + "< \u0445=?; \u0425=?" + "< \u0446=?; \u0426=?" + "< \u0447=?; \u0427=?" + "< \u0448=?; \u0428=?" + "< \u0449=?; \u0429=?" + "< \u044A=?; \u042A=?" + "< \u044B=?; \u042B=?" + "< \u044C=?; \u042C=?" + "< \u044D=?; \u042D=?" + "< \u044E=?; \u042E=?"
					+ "< \u044F=?; \u042F=?" + "<'-'<' '";
			collator = new RuleBasedCollator(ruHarf);
		} catch (ParseException ex) {
			ex.printStackTrace();
		}
		collator.setStrength(Collator.SECONDARY | Collator.CANONICAL_DECOMPOSITION);//
		collator.setStrength(Collator.TERTIARY);
		return collator;

	}

	/**
	 * @param deger
	 * @param ayrac
	 * @return
	 */
	public static ArrayList<String> parseCharToArrayList(String deger, char ayrac) {
		ArrayList<String> list = new ArrayList<String>();
		deger = deger.trim();
		char charArray[] = new char[deger.length()];
		deger.getChars(0, deger.length(), charArray, 0);
		for (int i = 0; i < charArray.length; i++) {
			if (ayrac != charArray[i]) {
				list.add(String.valueOf(charArray[i]));
			}
		}

		return list;
	}

	/**
	 * List objesini objenin istenilen alanına göre sıralar
	 * 
	 * @param izinHakedisHakkiList
	 * @param alanAdi
	 * @param buyukdenKucuge
	 * @return
	 */
	public static List sortListByAlanAdi(List list, String alanAdi, boolean buyukdenKucuge) {
		ArrayList yeniList = new ArrayList();
		if (list != null && !list.isEmpty())
			for (Object object : list)
				if (object != null)
					yeniList.add(object);
		try {
			if (yeniList.size() > 1) {
				Comparator<Object> comparator = new BeanPropertyComparator(alanAdi);
				Collections.sort(yeniList, comparator);
				if (buyukdenKucuge)
					Collections.reverse(yeniList);
			}

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			yeniList = (ArrayList) ((ArrayList) list).clone();

		}

		return yeniList;
	}

	public static ArrayList getSayfalaList(String sonEk, ArrayList veriList, int satirSayisi, HttpServletRequest request) {
		if (sonEk == null)
			sonEk = "";
		int sayac = 0;
		List sayfaList = new ArrayList();
		while (veriList.size() > sayac) {
			sayac += satirSayisi;
			sayfaList.add(String.valueOf(sayac / satirSayisi));
		}
		request.setAttribute("sayfaList" + sonEk, sayfaList);
		ArrayList list = (ArrayList) veriList.clone();
		veriList.clear();
		int sayfaNo = 0;
		if (hasStringValue(request.getParameter("sayfaNo" + sonEk)))
			sayfaNo = Integer.parseInt(request.getParameter("sayfaNo" + sonEk)) - 1;

		request.setAttribute("sayfaNo" + sonEk, String.valueOf(sayfaNo + 1));
		if (sayfaList.size() == sayfaNo)
			--sayfaNo;
		int ilkIndis = ((satirSayisi * sayfaNo) + 1);
		int sonIndis = (satirSayisi * (sayfaNo + 1));
		if (sonIndis > list.size())
			sonIndis = list.size();
		for (int i = ilkIndis - 1; i < sonIndis; i++) {
			Object element = list.get(i);
			veriList.add(element);

		}
		request.setAttribute("sayfaStr" + sonEk, String.valueOf(sayfaNo + 1) + " ( " + ilkIndis + " - " + sonIndis + " )");
		return veriList;

	}

	/**
	 * @param email
	 * @return
	 */
	public static boolean isValidEMail(String email) {
		boolean matches = false;
		if (email != null && email.indexOf("@") > 1) {
			String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
			Pattern pattern = Pattern.compile(emailRegex);
			Matcher matcher = pattern.matcher(email);
			matches = matcher.matches();
		}
		return matches;
	}

	/**
	 * @param fileName
	 * @param characterEncoding
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String encoderURL(String fileName, String characterEncoding) throws Exception {
		if (characterEncoding == null)
			characterEncoding = "UTF-8";
		String url = URLEncoder.encode(fileName, characterEncoding);
		if (url != null)
			url = replaceAllManuel(url, "+", " ");
		return url;
	}

	/**
	 * @param str
	 * @param pattern
	 * @param replace
	 * @return
	 */
	public static String replaceAll(String str, String pattern, String replace) {
		if (str != null && pattern != null && replace != null)
			str = str.replaceAll(pattern, replace);
		return str;
	}

	public static String replaceAllManuel(String str, String pattern, String replace) {
		StringBuffer lSb = new StringBuffer();
		if ((str != null) && (pattern != null) && (pattern.length() > 0) && (replace != null)) {
			int i = 0;
			int j = str.indexOf(pattern, i);
			int l = pattern.length();
			int m = str.length();
			if (j > -1) {
				while (j > -1) {
					if (i != j) {
						lSb.append(str.substring(i, j));
					}
					lSb.append(replace);
					i = j + l;
					j = (i > m) ? -1 : str.indexOf(pattern, i);
				}
				if (i < m) {
					lSb.append(str.substring(i));
				}
			} else {
				lSb.append(str);
			}
			str = lSb.toString();

		}

		return str;
	}

	/**
	 * @param object
	 * @param method
	 * @param parametre
	 * @return
	 */
	public static Object getMethodObject(Object object, String method, Object[] parametre) {
		Object str = null;
		Class[] classes = null;
		if (parametre != null) {
			classes = new Class[parametre.length];
			for (int i = 0; i < classes.length; i++)
				classes[i] = parametre[i].getClass();
		}
		try {
			Method run = object.getClass().getMethod(method, classes);
			str = run.invoke(object, parametre);
		} catch (Exception e) {
			str = null;
		}
		return str;

	}

	/**
	 * @param object
	 * @param method
	 * @param parametre
	 */
	public static void runMethodObject(Object object, String method, Object[] parametre) {
		Class[] classes = null;
		if (parametre != null) {
			classes = new Class[parametre.length];
			for (int i = 0; i < classes.length; i++)
				classes[i] = parametre[i].getClass();
		}
		try {
			Method run = object.getClass().getMethod(method, classes);
			run.invoke(object, parametre);
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}
	}

	public static List sortObjectStringAlanList(List list, String method, Object[] parametre) {
		Locale locale = null;
		try {
			if (FacesContext.getCurrentInstance() != null)
				locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
			else
				locale = Constants.TR_LOCALE;
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			locale = Constants.TR_LOCALE;
		}
		List sortList = sortObjectStringAlanList(locale, list, method, parametre);
		return sortList;
	}

	/**
	 * @param locale
	 * @param list
	 * @param method
	 * @param parametre
	 * @return
	 */
	public static List sortObjectStringAlanList(Locale locale, List listOrj, String method, Object[] parametre) {
		if (listOrj != null && listOrj.size() > 1) {
			if (locale == null)
				locale = Constants.TR_LOCALE;
			TreeMap sortMap = new TreeMap();
			String alan1 = "";
			String alan2 = "";
			TreeMap<String, Object> siraMap = new TreeMap<String, Object>();
			long kk = 0;
			for (Iterator iterator = listOrj.iterator(); iterator.hasNext();) {
				Object object = (Object) iterator.next();
				siraMap.put(getMethodObject(object, method, parametre) + "_" + (++kk), object);
			}
			List list = new ArrayList(siraMap.values());
			if (list.size() < 200) {

				siraMap = null;
				HashMap degerMap = new HashMap();
				for (int k = 0; k < list.size(); k++) {
					sortMap.put(new Long(k), list.get(k));
					degerMap.put(new Long(k), getMethodObject(list.get(k), method, parametre));
				}
				list = new ArrayList(sortMap.values());
				RuleBasedCollator collator = null;
				if (locale.getLanguage().equals(Constants.TR_LOCALE.getLanguage()))
					collator = getTrRuleBasedCollator();
				else if (locale.getLanguage().equals(Constants.RU_LOCALE.getLanguage()))
					collator = getRuRuleBasedCollator();
				Object object1 = null;
				Object object2 = null;
				if (collator != null) {
					for (int j = 0; j < list.size() - 1; j++) {
						object1 = sortMap.get(new Long(j));
						alan1 = (String) degerMap.get(new Long(j));
						for (int k = j + 1; k < list.size(); k++) {
							object2 = sortMap.get(new Long(k));
							alan2 = (String) degerMap.get(new Long(k));
							if (alan1 != null && alan2 != null && collator.compare(alan1, alan2) == 1) {
								sortMap.put(new Long(j), object2);
								sortMap.put(new Long(k), object1);
								degerMap.put(new Long(j), alan2);
								degerMap.put(new Long(k), alan1);
								alan1 = alan2;
								object1 = object2;
							}
						}
					}
				} else {
					Collator coll = Collator.getInstance(locale);
					for (int j = 0; j < list.size() - 1; j++) {
						object1 = sortMap.get(new Long(j));
						alan1 = (String) degerMap.get(new Long(j));
						for (int k = j + 1; k < list.size(); k++) {
							object2 = sortMap.get(new Long(k));
							alan2 = (String) degerMap.get(new Long(k));
							if (alan1 != null && alan2 != null && coll.compare(alan1, alan2) == 1) {
								sortMap.put(new Long(j), object2);
								sortMap.put(new Long(k), object1);
								degerMap.put(new Long(j), alan2);
								degerMap.put(new Long(k), alan1);
								alan1 = alan2;
								object1 = object2;
							}
						}
					}
				}
				listOrj = new ArrayList(sortMap.values());
				list = null;
				degerMap = null;
				sortMap = null;

			} else
				listOrj = list;
		}
		return listOrj;
	}

	/**
	 * Tanim obje listesi sıralanması yapılmaktadır.
	 * 
	 * @param locale
	 * @param list
	 * @return
	 */
	public static List sortTanimList(Locale locale, List list) {
		if (locale == null)
			locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
		if (list != null && list.size() > 0)
			list = sortObjectStringAlanList(locale, list, "getAciklama", null);
		return list;

	}

	public static ArrayList getArrayList(String[] dizi, String ayrac) {
		ArrayList arrayList = null;
		if (dizi != null && dizi.length > 0) {
			arrayList = new ArrayList();
			for (int i = 0; i < dizi.length; i++)
				arrayList.add(ayrac + dizi[i].trim() + ayrac);
		}
		return arrayList;
	}

	public static ArrayList getNumericArrayList(String[] dizi) {
		return getArrayList(dizi, "");
	}

	public static ArrayList getStringArrayList(String[] dizi) {
		return getArrayList(dizi, "'");
	}

	public static StringBuilder getObjectXmlString(Object object) {
		StringBuilder xml = new StringBuilder();
		xml.append("<?xml version=\"1.0\" encoding=\"ISO-8859-9\"?>");
		xml.append(getObjectXml(object));
		return xml;
	}

	public static StringBuilder getObjectXml(Object deger, String fieldAdi) {
		StringBuilder xml = new StringBuilder();
		if (deger instanceof String)
			xml.append("<" + fieldAdi + ">" + deger + "</" + fieldAdi + ">");
		else if (deger instanceof Integer)
			xml.append("<" + fieldAdi + ">" + ((Integer) deger).intValue() + "</" + fieldAdi + ">");
		else if (deger instanceof Double)
			xml.append("<" + fieldAdi + ">" + ((Double) deger).doubleValue() + "</" + fieldAdi + ">");
		else if (deger instanceof Long)
			xml.append("<" + fieldAdi + ">" + ((Long) deger).longValue() + "</" + fieldAdi + ">");
		else if (deger instanceof Float)
			xml.append("<" + fieldAdi + ">" + ((Float) deger).floatValue() + "</" + fieldAdi + ">");
		else if (deger instanceof Boolean)
			xml.append("<" + fieldAdi + ">" + ((Boolean) deger).booleanValue() + "</" + fieldAdi + ">");
		else {
			StringBuilder xml1 = getObjectXml(deger);
			if (xml1 != null && xml1.length() > 0)
				xml.append(xml1.toString());
		}
		return xml;
	}

	public static StringBuilder getObjectXml(Object object) {
		String objectAdi = object.getClass().getName();
		if (objectAdi.indexOf("java.lang.Class") == 0)
			return null;
		logger.error(objectAdi);
		StringBuilder xml = new StringBuilder();
		objectAdi = objectAdi.substring(objectAdi.lastIndexOf(".") + 1);
		objectAdi = objectAdi.substring(0, 1).toLowerCase(Locale.ENGLISH) + objectAdi.substring(1);
		xml.append("<" + objectAdi + ">");
		Class[] class1 = null;
		if (object != null) {
			StringBuilder xml1 = null;
			Method[] methods = object.getClass().getMethods();
			if (methods != null && methods.length > 0) {
				for (int i = 0; i < methods.length; i++) {
					Method method = methods[i];
					String methodAdi = method.getName();
					class1 = method.getParameterTypes();
					if (methodAdi.indexOf("get") == 0 && (class1 == null || class1.length == 0)) {
						Object deger = getMethodObject(object, methodAdi, null);
						String fieldAdi = methodAdi.substring(3, 4).toLowerCase(Locale.ENGLISH) + methodAdi.substring(4);
						xml1 = null;
						if (deger == null) {
							xml.append("<" + fieldAdi + "></" + fieldAdi + ">");
							continue;
						}
						if (deger instanceof Object[]) {
							for (int j = 0; j < ((Object[]) deger).length; j++) {
								Object arrayItem = ((Object[]) deger)[j];
								fieldAdi = arrayItem.getClass().getName();
								fieldAdi = fieldAdi.substring(fieldAdi.lastIndexOf(".") + 1);
								fieldAdi = fieldAdi.substring(0, 1).toLowerCase(Locale.ENGLISH) + fieldAdi.substring(1);
								xml1 = getObjectXml(arrayItem, fieldAdi);
								if (xml1 != null && xml1.length() > 0)
									xml.append(xml1.toString());
							}
						} else {
							if (deger instanceof Map || deger instanceof HashMap || deger instanceof TreeMap)
								deger = new ArrayList(((Map) deger).values());
							if (deger instanceof List || deger instanceof ArrayList) {
								List list = (List) deger;
								for (Iterator iter = list.iterator(); iter.hasNext();) {
									Object arrayItem = iter.next();
									fieldAdi = arrayItem.getClass().getName();
									fieldAdi = fieldAdi.substring(fieldAdi.lastIndexOf(".") + 1);
									fieldAdi = fieldAdi.substring(0, 1).toLowerCase(Locale.ENGLISH) + fieldAdi.substring(1);
									xml1 = getObjectXml(arrayItem, fieldAdi);
									if (xml1 != null && xml1.length() > 0)
										xml.append(xml1.toString());
								}
							} else {
								xml1 = getObjectXml(deger, fieldAdi);
								if (xml1 != null && xml1.length() > 0)
									xml.append(xml1.toString());
							}

						}

					}
				}
			}

		}
		xml.append("</" + objectAdi + ">");
		return xml;
	}

	public static void removeSession(HttpSession session, String name) {
		if (session.getAttribute(name) != null)
			session.removeAttribute(name);
	}

	public static void mesajYazOld(String mesaj) {
		FacesMessage facesMessage = new FacesMessage();
		facesMessage.setDetail(mesaj);
		facesMessage.setSummary(mesaj);
		facesMessage.setSeverity(FacesMessage.SEVERITY_ERROR);
		FacesContext.getCurrentInstance().addMessage(null, facesMessage);
	}

	public static FacesContext getFacesContext() {
		return FacesContext.getCurrentInstance();
	}

	@SuppressWarnings("unchecked")
	public static Map getRequestParam() {
		return getFacesContext().getExternalContext().getRequestParameterMap();
	}

	@SuppressWarnings("unchecked")
	public static Map getRequestScope() {
		return getFacesContext().getExternalContext().getRequestMap();
	}

	@SuppressWarnings("unchecked")
	public static Map getSessionScope() {
		return getFacesContext().getExternalContext().getSessionMap();
	}

	private static void addMessage(String message, Severity severity, Boolean yeni) {
		Object object = Component.getInstance("facesMessages");
		if (object != null) {
			FacesMessages facesMessages = (FacesMessages) object;
			if (yeni)
				facesMessages.clear();
			facesMessages.add(severity, " " + message, "");
		}

	}

	public static void addMessageError(String message) {
		addMessage(message, Severity.ERROR, Boolean.TRUE);
	}

	public static void addMessageWarn(String message) {
		addMessage(message, Severity.WARN, Boolean.TRUE);
	}

	public static void addMessageInfo(String message) {
		addMessage(message, Severity.INFO, Boolean.TRUE);
	}

	public static void addMessageFatal(String message) {
		addMessage(message, Severity.FATAL, Boolean.TRUE);
	}

	public static void addMessageAvailableError(String message) {
		addMessage(message, Severity.ERROR, Boolean.FALSE);
	}

	public static void addMessageAvailableWarn(String message) {
		addMessage(message, Severity.WARN, Boolean.FALSE);
	}

	public static void addMessageAvailableInfo(String message) {
		addMessage(message, Severity.INFO, Boolean.FALSE);
	}

	public static void addMessageAvailableFatal(String message) {
		addMessage(message, Severity.FATAL, Boolean.FALSE);
	}

	public static void addMessageFor(String compId, String messageText) {
		FacesMessage message = new FacesMessage();
		message.setDetail(messageText);
		message.setSeverity(FacesMessage.SEVERITY_ERROR);
		message.setSummary(messageText);
		getFacesContext().addMessage(compId, message);
	}

	public static String getContextPath() {
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();

		return request.getContextPath();
	}

	/**
	 * <p>
	 * Return the {@link UIComponent} (if any) with the specified <code>id</code>, searching recursively starting at the specified <code>base</code>, and examining the base component itself, followed by examining all the base component's facets and children. Unlike findComponent method of
	 * {@link UIComponentBase}, which skips recursive scan each time it finds a {@link NamingContainer}, this method examines all components, regardless of their namespace (assuming IDs are unique).
	 * 
	 * @param base
	 *            Base {@link UIComponent} from which to search
	 * @param id
	 *            Component identifier to be matched
	 */
	@SuppressWarnings("unchecked")
	public static UIComponent findComponent(UIComponent base, String id) {

		// Is the "base" component itself the match we are looking for?
		if (id.equals(base.getId())) {
			return base;
		}

		// Search through our facets and children
		UIComponent kid = null;
		UIComponent result = null;
		Iterator kids = base.getFacetsAndChildren();
		while (kids.hasNext() && (result == null)) {
			kid = (UIComponent) kids.next();
			if (id.equals(kid.getId())) {
				result = kid;
				break;
			}
			result = findComponent(kid, id);
			if (result != null) {
				break;
			}
		}
		return result;
	}

	public static UIComponent findComponentInRoot(String id) {
		UIComponent ret = null;

		FacesContext context = FacesContext.getCurrentInstance();
		if (context != null) {
			UIComponent root = context.getViewRoot();
			ret = findComponent(root, id);
		}

		return ret;
	}

	public static HttpServletRequest getHttpServletRequest() {
		return httpServletRequest;
	}

	public static void setHttpServletRequest(HttpServletRequest httpServletRequest) {
		PdksUtil.httpServletRequest = httpServletRequest;
	}

	public static String getUrl() {
		return url;
	}

	public static void setUrl(String url) {
		PdksUtil.url = url;
	}

	public static String encodePassword(String str) {
		String lockToken = "";
		try {
			MD5Encoder md5Encoder = new MD5Encoder();
			MessageDigest md5Helper = MessageDigest.getInstance("MD5");
			lockToken = md5Encoder.encode(md5Helper.digest(str.getBytes()));
		} catch (NoSuchAlgorithmException e) {

			lockToken = str;
			logger.error("encodePassword : " + e.getMessage());
		}
		return lockToken;

	}

	public static Date getSonSistemTarih() {
		if (sonSistemTarih == null) {
			Calendar calendar = Calendar.getInstance();
			calendar.set(2999, 11, 31);
			sonSistemTarih = getDate(calendar.getTime());
		}
		return sonSistemTarih;

	}

	public static void setSonSistemTarih(Date sonSistemTarih) {
		PdksUtil.sonSistemTarih = sonSistemTarih;
	}

	public static String getBundleName() {

		return bundleName;

	}

	public static String getMessageBundleMessage(Locale locale, String key) {
		String message = "";
		String bundleAdi = bundleName;
		if (key != null)
			try {
				if (locale == null)
					locale = Constants.TR_LOCALE;
				if (bundleAdi == null)
					bundleAdi = "messages";
				message = getMessageBundleMessage(locale, key, bundleAdi);
			} catch (Exception e) {
				try {
					if (bundleAdi.equals("messages"))
						bundleAdi = "org.jboss.seam.core.SeamResourceBundle";
					else
						bundleAdi = "messages";
					message = getMessageBundleMessage(locale, key, bundleAdi);
				} catch (Exception e2) {
					message = key + " ? ";
				}

			}
		return message;

	}

	/**
	 * @param locale
	 * @param key
	 * @param bundleAdi
	 * @return
	 * @throws Exception
	 */
	private static String getMessageBundleMessage(Locale locale, String key, String bundleAdi) throws Exception {
		String message = null;
		ResourceBundle bundle = ResourceBundle.getBundle(bundleAdi, locale);
		if (bundle == null || bundle.containsKey(key))
			message = bundle.getString(key);
		else
			throw new Exception(message = key + " ?  ");

		return message;

	}

	public static String getMessageBundleMessage(String key) {
		Locale locale = Constants.TR_LOCALE;
		return getMessageBundleMessage(locale, key);
	}

	public static Date ayinIlkGunu() {
		Calendar cal = Calendar.getInstance();
		GregorianCalendar calendar = new GregorianCalendar();
		cal.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1);
		return getDate(cal.getTime());
	}

	public static Date ayinSonGunu() {
		Calendar cal = Calendar.getInstance();
		GregorianCalendar calendar = new GregorianCalendar();
		int yil = calendar.get(Calendar.YEAR);
		int ay = calendar.get(Calendar.MONTH) + 1;
		if (ay > 11) {
			ay = 0;
			++yil;
		}
		cal.set(yil, ay, 1);
		cal.add(Calendar.DATE, -1);
		return getDate(cal.getTime());
	}

	public static void setUserYetki(User user) {
		if (user != null) {
			user.setAdmin(Boolean.FALSE);
			user.setGenelMudur(Boolean.FALSE);
			user.setIKDirektor(Boolean.FALSE);
			user.setAnahtarKullanici(Boolean.FALSE);
			user.setIK(Boolean.FALSE);
			user.setMudur(Boolean.FALSE);
			user.setYonetici(Boolean.FALSE);
			user.setSekreter(Boolean.FALSE);
			user.setSuperVisor(Boolean.FALSE);
			user.setProjeMuduru(Boolean.FALSE);
			user.setOperatorSSK(Boolean.FALSE);
			user.setYoneticiKontratli(Boolean.FALSE);
			user.setDirektorSuperVisor(Boolean.FALSE);
			user.setIK_Tesis(Boolean.FALSE);
			user.setSistemYoneticisi(Boolean.FALSE);
			user.setPersonel(Boolean.FALSE);
			user.setTaseronAdmin(Boolean.FALSE);
			user.setTesisYonetici(Boolean.FALSE);
			user.setRaporKullanici(Boolean.FALSE);
			Personel pdksPersonel = user.getPdksPersonel();
			if (user.getYetkiliRollerim() != null)
				for (Role role : user.getYetkiliRollerim()) {
					if (role.getRolename().equals(Role.TIPI_IK_YETKILI_RAPOR_KULLANICI))
						user.setRaporKullanici(Boolean.TRUE);
					else if (role.getRolename().equals(Role.TIPI_ADMIN))
						user.setAdmin(Boolean.TRUE);
					else if (role.getRolename().equals(Role.TIPI_ANAHTAR_KULLANICI)) {
						user.setIK(Boolean.TRUE);
						user.setAnahtarKullanici(Boolean.TRUE);
					} else if (role.getRolename().equals(Role.TIPI_SISTEM_YONETICI)) {
						user.setIK(Boolean.TRUE);
						user.setSistemYoneticisi(Boolean.TRUE);
					} else if (role.getRolename().equals(Role.TIPI_IK))
						user.setIK(Boolean.TRUE);
					else if (role.getRolename().equals(Role.TIPI_IK_Tesis)) {
						user.setIK_Tesis(Boolean.TRUE);
						user.setIK(Boolean.TRUE);
					} else if (role.getRolename().equals(Role.TIPI_IK_DIREKTOR)) {
						user.setIK(Boolean.TRUE);
						user.setIKDirektor(Boolean.TRUE);
					} else if (role.getRolename().equals(Role.TIPI_YONETICI))
						user.setYonetici(Boolean.TRUE);
					else if (role.getRolename().equals(Role.TIPI_YONETICI_KONTRATLI)) {
						user.setYonetici(Boolean.TRUE);
						user.setYoneticiKontratli(Boolean.TRUE);
					} else if (role.getRolename().equals(Role.TIPI_GENEL_MUDUR)) {
						user.setIK(Boolean.TRUE);
						user.setGenelMudur(Boolean.TRUE);
					} else if (role.getRolename().equals(Role.TIPI_SEKRETER))
						user.setSekreter(Boolean.TRUE);
					else if (role.getRolename().equals(Role.TIPI_TASERON_ADMIN))
						user.setTaseronAdmin(Boolean.TRUE);
					else if (role.getRolename().equals(Role.TIPI_SUPER_VISOR)) {
						user.setSuperVisor(Boolean.TRUE);
						user.setYonetici(Boolean.TRUE);
					} else if (role.getRolename().equals(Role.TIPI_DEPARTMAN_SUPER_VISOR)) {
						user.setDirektorSuperVisor(pdksPersonel != null && pdksPersonel.getEkSaha1() != null && pdksPersonel.getEkSaha1().getDurum());
						user.setYonetici(Boolean.TRUE);
					} else if (role.getRolename().equals(Role.TIPI_PROJE_MUDURU))
						user.setProjeMuduru(Boolean.TRUE);
					else if (role.getRolename().equals(Role.TIPI_MUDUR))
						user.setMudur(Boolean.TRUE);
					else if (role.getRolename().equals(Role.TIPI_OPERATOR_SSK_IZIN))
						user.setOperatorSSK(Boolean.TRUE);
					else if (role.getRolename().equals(Role.TIPI_PERSONEL))
						user.setPersonel(Boolean.TRUE);
					else if (role.getRolename().equals(Role.TIPI_TESIS_YONETICI)) {
						user.setYonetici(Boolean.TRUE);
						user.setTesisYonetici(Boolean.TRUE);
					}

				}

		}
		user.setYetkiSet(Boolean.TRUE);
	}

	public static Date tarihiGetir(Date date) {
		if (date != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.clear(Calendar.MILLISECOND);
			cal.clear(Calendar.SECOND);
			cal.clear(Calendar.MINUTE);
			cal.clear(Calendar.HOUR_OF_DAY);
			cal.clear(Calendar.HOUR);
			date = cal.getTime();
		}
		return date;
	}

	/**
	 * @param deger
	 * @param yarimYuvarla
	 * @return
	 */
	public static double setSureDoubleTypeRounded(Double doubleValue, int yarimYuvarla) {
		Double yuvarlanmisDeger = null;
		if (doubleValue != null) {
			double d = doubleValue.doubleValue();
			long l = doubleValue.longValue();
			if (d != 0.0d && d != l) {
				double fark = 0.0d;
				switch (yarimYuvarla) {
				case 1:
					yuvarlanmisDeger = doubleValue.doubleValue() * 2;
					fark = yuvarlanmisDeger.doubleValue() - yuvarlanmisDeger.longValue();
					if (fark > 0) {
						if (fark > 0.5)
							yuvarlanmisDeger = yuvarlanmisDeger.longValue() + (1.0d);
						else
							yuvarlanmisDeger = yuvarlanmisDeger.longValue() + (0.5d);
					}
					yuvarlanmisDeger = yuvarlanmisDeger / 2;
					break;

				case 2:
					fark = doubleValue.doubleValue() - doubleValue.longValue();
					double arti = 0;
					if (fark > 0.0d) {
						for (int i = 3; i >= 0; i--) {
							arti = i * 0.25;
							if (fark >= arti)
								break;
						}
					} else if (fark < 0.0d) {
						for (int i = 3; i >= 0; i--) {
							arti = -i * 0.25;
							if (fark < arti) {
								arti -= 0.25;
								break;
							}

						}
					}
					yuvarlanmisDeger = doubleValue.longValue() + arti;
					break;

				default:
					yuvarlanmisDeger = doubleValue;
					break;
				}

			} else
				yuvarlanmisDeger = doubleValue;
		}

		return yuvarlanmisDeger;

	}

	/**
	 * @param deger
	 * @return
	 */
	public static double setSureDoubleRounded(Double deger) {
		Double yuvarlanmisDeger = null;
		if (deger != null)
			yuvarlanmisDeger = setSureDoubleTypeRounded(deger, yarimYuvarlaLast);
		return yuvarlanmisDeger;

	}

	/**
	 * @param deger
	 * @param digit
	 * @param yuvarmaTipi
	 * @return
	 */
	public static double setDoubleRounded(double deger, int digit, int yuvarmaTipi) {
		double yuvarlanmisDeger = new BigDecimal(deger).doubleValue();
		if (deger != 0)
			try {
				BigDecimal decimal = new BigDecimal(deger).setScale(digit, yuvarmaTipi);
				yuvarlanmisDeger = decimal.doubleValue();
			} catch (Exception e) {

				yuvarlanmisDeger = deger;
			}
		return yuvarlanmisDeger;
	}

	public static int getSistemBaslangicYili() {
		return sistemBaslangicYili;
	}

	public static void setSistemBaslangicYili(int sistemBaslangicYili) {
		PdksUtil.sistemBaslangicYili = sistemBaslangicYili;
	}

	public static int getIzinOffAdet() {
		return izinOffAdet;
	}

	public static void setIzinOffAdet(int izinOffAdet) {
		PdksUtil.izinOffAdet = izinOffAdet;
	}

	public static int getIzinHaftaAdet() {
		return izinHaftaAdet;
	}

	public static void setIzinHaftaAdet(int izinHaftaAdet) {
		PdksUtil.izinHaftaAdet = izinHaftaAdet;
	}

	public static int getPlanOffAdet() {
		return planOffAdet;
	}

	public static void setPlanOffAdet(int planOffAdet) {
		PdksUtil.planOffAdet = planOffAdet;
	}

	/**
	 * @return
	 */
	public static boolean zamanKontrolDurum() {
		boolean zamaniGeldi = Boolean.FALSE;
		File file = new File("/opt/pdks/zamanKontrol.txt");
		zamaniGeldi = !file.exists();
		return zamaniGeldi;
	}

	/**
	 * @param adi
	 * @param str
	 * @param time
	 * @return
	 */
	public static boolean zamanKontrol(String adi, String str, Date time) {
		boolean zamaniGeldi = Boolean.FALSE;
		try {
			str = str != null ? str.trim() : "";
			Calendar cal = Calendar.getInstance(Constants.TR_LOCALE);
			if (time != null)
				cal.setTime(time);
			int simdikiSaat = cal.get(Calendar.HOUR_OF_DAY);
			int simdikiDakika = cal.get(Calendar.MINUTE);
			if (str.length() > 0) {
				String delim = null;
				if (str.indexOf(",") > 0)
					delim = ",";
				else if (str.indexOf(";") > 0)
					delim = ";";
				else if (str.indexOf("|") > 0)
					delim = "|";
				else if (str.indexOf(" ") > 0)
					delim = " ";
				if (delim != null) {
					StringTokenizer st = new StringTokenizer(str, delim);
					while (!zamaniGeldi && st.hasMoreTokens()) {
						String zaman = st.nextToken();
						if (!zamaniGeldi)
							zamaniGeldi = zamaniGeldimi(simdikiSaat, simdikiDakika, zaman);
					}
					st = null;
				} else
					zamaniGeldi = zamaniGeldimi(simdikiSaat, simdikiDakika, str);

			}
			if (zamaniGeldi)
				logger.info(adi + " : " + zamaniGeldi + " " + str + " Zaman - " + simdikiSaat + ":" + simdikiDakika);
			if (time != null)
				time = null;

		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}

		return zamaniGeldi;
	}

	private static boolean zamaniGeldimi(int saat, int dakika, String str) {
		boolean durum = Boolean.FALSE;
		str = str.trim();
		if (str.length() > 0) {
			String delim = null;
			int saatDeger = -1, dakikaDeger = -1;
			if (str.indexOf(":") > 0)
				delim = ":";
			else if (str.indexOf(".") > 0)
				delim = ".";
			else if (str.indexOf("-") > 0)
				delim = "-";
			if (delim != null) {
				StringTokenizer st = new StringTokenizer(str, delim);
				String saatStr = st.nextToken();
				try {
					saatDeger = Integer.parseInt(saatStr);
				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());
					saatDeger = -1;
				}
				String dakikaStr = st.nextToken();
				try {
					dakikaDeger = Integer.parseInt(dakikaStr);
				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());
					dakikaDeger = -1;
				}
				st = null;
			} else {
				try {
					saatDeger = Integer.parseInt(str);
				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());
					saatDeger = -1;
				}
				dakikaDeger = 0;
			}
			durum = saat == saatDeger && dakika == dakikaDeger;

		}
		return durum;
	}

	/**
	 * @param em
	 * @param user
	 * @return
	 */
	public static Session getSessionUser(EntityManager em, User user) {
		Session session1 = null;
		if (user != null) {
			if (em != null)
				session1 = getSession(em, Boolean.FALSE);
			try {
				// Session sessionSQL = user.getSessionSQL();
				// if (sessionSQL != null) {
				// sessionSQL.close();
				// sessionSQL = null;
				// }
			} catch (Exception e) {
				logger.error(e);
			} finally {
				user.setSessionSQL(session1);
			}

		} else
			session1 = getSession(em, Boolean.FALSE);
		return session1;

	}

	public static Session getSession(EntityManager em, Boolean yeni) {
		// Session session1 = (Session) entityManager.getDelegate();
		Session session1 = null;
		Object delegate = null;
		SessionFactory sessionFactory = null;
		try {
			delegate = em.getDelegate();
			if (yeni == null) {
				HibernateSessionProxy hsp = (HibernateSessionProxy) delegate;
				sessionFactory = hsp.getSessionFactory();
				session1 = sessionFactory.getCurrentSession();
			}

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
		}

		try {
			if (session1 == null)
				session1 = (Session) delegate;
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
		}

		if (yeni != null && yeni && session1 != null) {
			if (sessionFactory == null)
				sessionFactory = session1.getSessionFactory();
			session1 = sessionFactory.openSession();
		}

		return session1;
	}

	public static void setBundleName(String bundleName) {
		PdksUtil.bundleName = bundleName;
	}

	public static Dosya getDosya(UploadItem item, Dosya dosya) {
		if (dosya == null)
			dosya = new Dosya();
		dosya.setDosyaIcerik(item.getData());
		String dosyaAdi = item.getFileName();
		String separator = File.separator;
		int index = dosyaAdi.lastIndexOf(separator);
		if (index < 0)
			index = dosyaAdi.lastIndexOf("\\");
		if (index > 0)
			dosyaAdi = dosyaAdi.substring(index + 1);
		dosya.setDosyaAdi(getNotTurkishCharacter(dosyaAdi));
		dosya.setIcerikTipi(item.getContentType());
		dosya.setSize(item.getFileSize());
		dosya.setIslemYapildi(Boolean.TRUE);
		return dosya;
	}

	public static String getNotTurkishCharacter(String kelime) {
		String str = kelime;
		if (str.indexOf("Ü") >= 0)
			str = replaceAll(str, "Ü", "U");
		if (str.indexOf("ü") >= 0)
			str = replaceAll(str, "ü", "u");
		if (str.indexOf("Ö") >= 0)
			str = replaceAll(str, "Ö", "O");
		if (str.indexOf("ö") >= 0)
			str = replaceAll(str, "ö", "o");
		if (str.indexOf("Ğ") >= 0)
			str = replaceAll(str, "Ğ", "G");
		if (str.indexOf("ğ") >= 0)
			str = replaceAll(str, "ğ", "g");
		if (str.indexOf("Ş") >= 0)
			str = replaceAll(str, "Ş", "S");
		if (str.indexOf("ş") >= 0)
			str = replaceAll(str, "ş", "s");
		if (str.indexOf("İ") >= 0)
			str = replaceAll(str, "İ", "I");
		if (str.indexOf("ı") >= 0)
			str = replaceAll(str, "ı", "i");
		return str;
	}

	public static Date setGunSonu(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(getDate(date));
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		Date bitisZamani = cal.getTime();
		return bitisZamani;
	}

	public static String setTurkishStr(String mesaj) {
		String str = new String(mesaj);
		if (mesaj != null) {
			if (str.indexOf("Ş") >= 0)
				str = replaceAll(str, "Ş", "S");
			if (str.indexOf("Ğ") >= 0)
				str = replaceAll(str, "Ğ", "G");
			if (str.indexOf("Ç") >= 0)
				str = replaceAll(str, "Ç", "C");
			if (str.indexOf("İ") >= 0)
				str = replaceAll(str, "İ", "I");
			if (str.indexOf("Ü") >= 0)
				str = replaceAll(str, "Ü", "U");
			if (str.indexOf("Ö") >= 0)
				str = replaceAll(str, "Ö", "O");
			if (str.indexOf("ş") >= 0)
				str = replaceAll(str, "ş", "s");
			if (str.indexOf("ğ") >= 0)
				str = replaceAll(str, "ğ", "g");
			if (str.indexOf("ç") >= 0)
				str = replaceAll(str, "ç", "c");
			if (str.indexOf("ı") >= 0)
				str = replaceAll(str, "ı", "i");
			if (str.indexOf("ü") >= 0)
				str = replaceAll(str, "ü", "u");
			if (str.indexOf("ö") >= 0)
				str = replaceAll(str, "ö", "o");

		}
		return str;
	}

	public static Date getSure(Date girisZaman, Date cikisZaman) {
		Date gecenZaman = null;
		if (girisZaman != null && cikisZaman != null) {
			Calendar cal = Calendar.getInstance();
			long saatFarki = cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET);
			long sureLong = cikisZaman.getTime() - girisZaman.getTime() - saatFarki;
			gecenZaman = new Date(sureLong);
		}
		return gecenZaman;
	}

	public static List<SelectItem> getAyListesi(boolean sayisal) {
		List<SelectItem> list = new ArrayList<SelectItem>();
		Calendar cal = Calendar.getInstance();
		cal.set(cal.get(Calendar.YEAR), Calendar.JANUARY, 1);
		for (int i = 0; i < 12; i++) {
			if (sayisal)
				list.add(new SelectItem(i + 1, convertToDateString(cal.getTime(), "MMMMM")));
			else
				list.add(new SelectItem(String.valueOf(i), convertToDateString(cal.getTime(), "MMMMM")));
			cal.add(Calendar.MONTH, 1);
		}
		return list;
	}

	public static Double stringFormatConvertNumericValue(String submittedValue, Locale locale) throws Exception {
		Double degerDouble = null;
		if (submittedValue != null) {
			try {
				BigDecimal bd = null;
				try {
					if (locale == null)
						locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());
					locale = TR_LOCALE;
				}
				NumberFormat nf = DecimalFormat.getNumberInstance(locale);
				Object value = nf.parseObject(submittedValue);
				Double deger = null;
				if (value instanceof Double)
					deger = (Double) value;
				else if (value instanceof Long)
					deger = new Double(((Long) value).longValue());
				else if (value instanceof Integer)
					deger = new Double(((Integer) value).intValue());
				else if (value instanceof BigDecimal)
					deger = ((BigDecimal) value).doubleValue();
				bd = new BigDecimal(deger);
				bd.setScale(2, BigDecimal.ROUND_HALF_DOWN);
				degerDouble = bd.doubleValue();
				bd = null;
				deger = null;
				value = null;
			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());
				throw new Exception(submittedValue + " verisi sayısal değere dönüştürülemedi!");
			}
		}
		return degerDouble;

	}

	public static String numericValueFormatStr(Object value, Locale locale) throws Exception {
		String str = null;
		if (value != null) {
			Object deger = value;
			if (value instanceof Integer)
				deger = ((Integer) value).longValue();
			else if (value instanceof BigDecimal)
				deger = ((BigDecimal) value).doubleValue();
			if (deger != null) {
				try {
					if (locale == null)
						locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
				} catch (Exception e) {

					locale = TR_LOCALE;
				}
				NumberFormat df = DecimalFormat.getNumberInstance(locale);
				try {
					df.setMaximumFractionDigits(2);
					str = df.format(deger);
				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());
					throw new Exception(value.toString() + " verisi formatlı gösterilemedi!");
				}
			}
		}
		return str;
	}

	public static Date getSession(HttpSession sessionx, Date simdi) {
		Date tarih = null;
		Calendar cal = Calendar.getInstance();

		try {
			long deger = simdi.getTime() - cal.get(Calendar.ZONE_OFFSET);
			deger -= sessionx.getLastAccessedTime();
			tarih = new Date(deger);
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			tarih = null;
		}

		return tarih;
	}

	public static User getLDAPKullanici(String bilgi, String tipi) {
		User kullanici = null;
		try {
			kullanici = LDAPUserManager.getLDAPUserAttributes(bilgi, tipi);
		} catch (Exception e) {
			kullanici = null;
		}
		if (kullanici != null && (kullanici.isDurum() == false || kullanici.getEmail() == null))
			kullanici = null;
		return kullanici;
	}

	public static String getMailAdres(String ePosta) {
		String eMail = null;
		if (ePosta != null) {
			eMail = setTurkishStr(ePosta.toUpperCase(Locale.ENGLISH));
			User kullaniciLDAP = null;
			if (eMail.indexOf("@") > 0) {
				eMail = getInternetAdres(eMail);
				if (hasStringValue(eMail)) {
					kullaniciLDAP = getLDAPKullanici(eMail, LDAPUserManager.USER_ATTRIBUTES_MAIL);
					if (kullaniciLDAP == null)
						kullaniciLDAP = getLDAPKullanici(eMail, LDAPUserManager.USER_ATTRIBUTES_PRINCIPAL_NAME);
				}
			} else
				kullaniciLDAP = getLDAPKullanici(eMail, LDAPUserManager.USER_ATTRIBUTES_SAM_ACCOUNT_NAME);
			if (kullaniciLDAP != null && kullaniciLDAP.getEmail() != null && kullaniciLDAP.getEmail().length() > 0)
				eMail = getInternetAdres(kullaniciLDAP.getEmail());

		}
		return eMail;
	}

	public static Date getGunSonu(Date tarih) {
		Date gunSonu = null;
		if (tarih != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(tarih);
			cal.add(Calendar.DATE, 1);
			cal.setTime(getDate((Date) cal.getTime().clone()));
			cal.set(Calendar.MILLISECOND, -2);
			gunSonu = cal.getTime();
		}
		return gunSonu;
	}

	/**
	 * @param fazlaMesaiOde
	 * @param hesaplananBuAySure
	 * @param gecenAydevredenSure
	 * @return
	 */
	public static PersonelDenklestirme getPdksPersonelDenklestirme(Boolean fazlaMesaiOde, double hesaplananBuAySure, double gecenAydevredenSure) {
		PersonelDenklestirme personelDenklestirme = new PersonelDenklestirme();
		double devredenSure = 0;
		double hesaplananSure = gecenAydevredenSure + hesaplananBuAySure;
		Double odenenSure = 0d;

		if ((fazlaMesaiOde != null && fazlaMesaiOde) || hesaplananSure >= odenenFazlaMesaiSaati) {
			odenenSure = hesaplananSure;
		} else {
			if (hesaplananSure > 0 && gecenAydevredenSure > 0) {
				if (hesaplananBuAySure < 0) {
					odenenSure = hesaplananSure;
				} else {
					odenenSure = gecenAydevredenSure;
					devredenSure = hesaplananBuAySure;
				}

			} else
				devredenSure = hesaplananSure;
		}

		if (odenenSure > 0)
			personelDenklestirme.setOdenenSure(odenenSure);
		else
			devredenSure = hesaplananSure;
		// if (personelDenklestirme.g personelDenklestirme.isOnaylandi())
		personelDenklestirme.setDevredenSure(devredenSure);
		personelDenklestirme.setHesaplananSure(hesaplananSure);
		return personelDenklestirme;
	}

	public static Date getBakiyeYil() {
		Calendar date = Calendar.getInstance();
		date.set(1900, 0, 1);
		Date tarih = getDate(date.getTime());
		return tarih;
	}

	public static String getInternetAdres(String eMail) {
		InternetAddress internetAddress = null;
		try {
			if (eMail != null && eMail.indexOf("@") > 0) {
				eMail = setTurkishStr(eMail.trim().toLowerCase(Locale.ENGLISH));
				internetAddress = new InternetAddress(eMail);
			}
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			internetAddress = null;
		}
		if (internetAddress != null) {
			if (internetAddress.getAddress() != null)
				eMail = internetAddress.getAddress();

		}
		if (internetAddress == null) {
			if (eMail != null && eMail.indexOf("@") > 0)
				logger.debug("Hatalı e-posta : " + eMail);
			eMail = null;
		}
		if (eMail != null)
			eMail = eMail.trim();

		return eMail;
	}

	public static void getXMLFunction(JCO.Client jcoClient, JCO.Function function, boolean execute) throws Exception {
		if (function != null) {
			String name = function.getName();
			Exception exception = null;
			Properties pr = null;
			TreeMap veriMap = new TreeMap();
			String client = "";
			String r3name = "";
			String hata = "";
			try {
				pr = jcoClient.getProperties();
				Attributes at = jcoClient.getAttributes();
				Method[] methods = at.getClass().getMethods();
				Object[] parametre = null;
				for (int i = 0; i < methods.length; i++) {
					Method run = methods[i];
					if (run.getName().indexOf("get") < 0 || run.getName().equals("getClass"))
						continue;
					Object veri = run.invoke(at, parametre);
					if (veri != null && hasStringValue(veri.toString())) {
						String value = veri.toString();
						String tagName = run.getName().substring(3).toUpperCase(Locale.ENGLISH);
						veriMap.put(tagName, value);
					}
				}
				client = pr != null && pr.containsKey("jco.client.client") ? (String) pr.get("jco.client.client") : "";
				String ashost = veriMap != null && veriMap.containsKey("SYSTEMID") ? (String) veriMap.get("SYSTEMID") : "";
				r3name = pr != null && pr.containsKey("jco.client.r3name") ? (String) pr.get("jco.client.r3name") : ashost;

				// logger.info(name + " --> " + r3name + " " + client + " in " + new Date());
				if (execute)
					jcoClient.execute(function);

				// logger.info(name + " --> " + r3name + " " + client + " out " + new Date());
			} catch (Exception e) {
				hata = "ERR_";
				exception = e;
			}

			try {
				String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
				StringBuffer sb = new StringBuffer(xml + "<" + name + ">");
				try {
					if (function.getImportParameterList() != null) {
						JCO.ParameterList imp = function.getImportParameterList();
						try {
							sb.append(imp.toXML());
						} catch (Exception e2) {

						}
					}
					if (exception != null && exception.getMessage() != null)
						sb.append("<EXCEPTION>" + exception.getMessage() + "</EXCEPTION>");
					if (function.getExportParameterList() != null) {
						JCO.ParameterList emp = function.getExportParameterList();
						try {
							sb.append(emp.toXML());
						} catch (Exception e2) {

						}
					}
					if (function.getTableParameterList() != null) {
						JCO.ParameterList tableList = function.getTableParameterList();
						if (tableList != null) {
							try {
								sb.append(tableList.toXML());
							} catch (Exception e2) {

							}
						}
					}

					xml = sb.toString() + "</" + name + ">";
					if (hasStringValue(client))
						client = "_" + client;
					if (hasStringValue(r3name))
						r3name = "_" + r3name;

					fileWrite(xml, hata + name + client + r3name);
					sb = null;
				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());
				}
				veriMap = null;

			} catch (Exception e1) {
				logger.error("PDKS hata in : \n");
				e1.printStackTrace();
				logger.error("PDKS hata out : " + e1.getMessage());
			} finally {
				if (exception != null)
					throw exception;
			}

		}
	}

	public static void getXMLFunction(JCO.Function function) throws Exception {
		if (function != null) {
			String name = function.getName();
			String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
			StringBuffer sb = new StringBuffer(xml + "<" + name + ">");
			if (function.getImportParameterList() != null) {
				JCO.ParameterList imp = function.getImportParameterList();
				try {
					sb.append(imp.toXML());
				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());

				}
			}
			if (function.getExportParameterList() != null) {
				JCO.ParameterList emp = function.getExportParameterList();
				try {
					sb.append(emp.toXML());
				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());

				}
			}
			if (function.getTableParameterList() != null) {
				JCO.ParameterList tableList = function.getTableParameterList();
				if (tableList != null) {
					try {
						sb.append(tableList.toXML());
					} catch (Exception e) {
						logger.error("PDKS hata in : \n");
						e.printStackTrace();
						logger.error("PDKS hata out : " + e.getMessage());

					}
				}
			}
			xml = sb.toString() + "</" + name + ">";
			fileWrite(xml, name);
			sb = null;
		}
	}

	/**
	 * @param jcoClient
	 * @param function
	 * @param tableNames
	 * @throws Exception
	 */
	public static void getXMLFunction3(JCoDestination jcoClient, JCoFunction function, String[] tableNames) throws Exception {

		if (function != null) {
			String name = function.getName();
			Exception exception = null;
			try {
				function.execute(jcoClient);
			} catch (Exception e) {
				exception = e;
			}
			try {
				String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
				String client = "";
				StringBuffer sb = new StringBuffer(xml + "<" + name + ">");
				TreeMap veriMap = new TreeMap();

				try {
					Properties pr = jcoClient.getProperties();
					JCoAttributes at = jcoClient.getAttributes();
					Method[] methods = at.getClass().getMethods();
					Object[] parametre = null;
					for (int i = 0; i < methods.length; i++) {
						Method run = methods[i];
						if (run.getName().indexOf("get") < 0 || run.getName().equals("getClass"))
							continue;
						Object veri = run.invoke(at, parametre);
						if (veri != null && hasStringValue(veri.toString())) {
							String value = veri.toString();
							String tagName = run.getName().substring(3).toUpperCase(Locale.ENGLISH);
							veriMap.put(tagName, value);
						}
					}
					for (Iterator iterator = pr.keySet().iterator(); iterator.hasNext();) {
						String key = (String) iterator.next();
						String value = pr.getProperty(key).trim();
						if (key.indexOf("passwd") >= 0 || hasStringValue(value) == false)
							continue;
						String tagName = key.substring(key.lastIndexOf(".") + 1).toUpperCase(Locale.ENGLISH);
						if (veriMap.containsKey(tagName))
							continue;
						veriMap.put(tagName, value);

					}
					String tag = "<PROPERTIES>";
					for (Iterator iterator = veriMap.keySet().iterator(); iterator.hasNext();) {
						String key = (String) iterator.next();
						String value = ((String) veriMap.get(key)).trim();
						if (!hasStringValue(value))
							continue;
						if (tag != null) {
							sb.append(tag);
							tag = null;
						}
						sb.append("<" + key + ">" + value + "</" + key + ">");
						if (value.equals("CLIENT"))
							client = "_" + value;
					}
					if (veriMap.containsKey("CLIENT"))
						client += "_" + veriMap.get("CLIENT");
					if (veriMap.containsKey("SYSTEMID"))
						client += "_" + veriMap.get("SYSTEMID");

					if (tag == null)
						sb.append("</PROPERTIES>");
				} catch (Exception e) {
					logger.error("PDKS out : \n");
					e.printStackTrace();
					logger.error("PDKS out : " + e.getMessage());

				}
				veriMap = null;
				String hata = "";
				if (function.getImportParameterList() != null) {
					JCoParameterList imp = function.getImportParameterList();
					sb.append(imp.toXML());
				}
				if (exception != null && exception.getMessage() != null) {
					hata = "ERR_";
					sb.append("<EXCEPTION>" + exception.getMessage() + "</EXCEPTION>");
				}
				if (function.getChangingParameterList() != null) {
					JCoParameterList chap = function.getChangingParameterList();
					sb.append(chap.toXML());
				}
				if (function.getExportParameterList() != null) {

					JCoParameterList emp = function.getExportParameterList();
					sb.append(emp.toXML());
				}
				if (function.getTableParameterList() != null) {

					JCoParameterList tableList = function.getTableParameterList();
					if (tableList != null) {
						if (tableNames == null) {
							sb.append(tableList.toXML());
						} else {
							sb.append("<TABLES>");
							for (int i = 0; i < tableNames.length; i++) {
								try {
									JCoTable cikanTable = tableList.getTable(tableNames[i]);
									sb.append(cikanTable.toXML());
								} catch (Exception et) {
								}
							}
							sb.append("</TABLES>");
						}

					}
				}
				xml = sb.toString() + "</" + name + ">";
				fileWrite(xml, hata + name + client + "_3");
				sb = null;
			} catch (Exception e) {
				logger.error("PDKS out : \n");
				e.printStackTrace();
				logger.error("PDKS out : " + e.getMessage());
			} finally {
				if (exception != null)
					throw exception;
			}

		}

	}

	/**
	 * @param jcoClient
	 * @param function
	 * @throws Exception
	 */
	public static void getXMLFunction3(JCoDestination jcoClient, JCoFunction function) throws Exception {
		if (function != null) {
			getXMLFunction3(jcoClient, function, null);
		}

	}

	public static void fileWrite(String content, String fileName) throws Exception {
		fileWrite(content, fileName, Boolean.FALSE);
	}

	public static void fileWriteEkle(String content, String fileName) throws Exception {
		fileWrite(content, fileName, Boolean.TRUE);
	}

	private static void fileWrite(String content, String fileName, Boolean ekle) throws Exception {
		String path = "/tmp/pdks";
		File tmp = new File(path);
		boolean devam = tmp.exists();
		if (!devam) {
			try {
				devam = tmp.mkdirs();
			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());
			}
		}
		if (devam) {
			Writer printWriter = null;
			FileOutputStream fos = null;
			String dosyaAdi = (fileName.indexOf("/") < 0 ? path + "/" : "") + fileName + (fileName.indexOf(".") < 0 ? ".xml" : "");
			File file = new File(dosyaAdi);
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {

				}
			}

			try {
				fos = new FileOutputStream(dosyaAdi, ekle);
				printWriter = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
				printWriter.write(content + "\n");
			} catch (Exception e1) {
				e1.printStackTrace();
			} finally {
				if (printWriter != null) {
					printWriter.flush();
					printWriter.close();
					// fos.flush();
					// fos.close();
				}
			}

		}

	}

	public static Date getDateObjectValue(Object veri) {
		Date zamanValue = null;
		if (veri != null) {
			if (veri instanceof Timestamp)
				zamanValue = new Date(((java.sql.Timestamp) veri).getTime());
			else if (veri instanceof java.sql.Date)
				zamanValue = new Date(((java.sql.Date) veri).getTime());
			else if (veri instanceof Date)
				zamanValue = (Date) veri;
		}
		return zamanValue;
	}

	public static Date getDateFromTimestamp(Timestamp ts) {
		Date date = ts != null ? new Date(ts.getTime()) : null;
		return date;
	}

	public static Long getLongFromBigDecimal(BigDecimal bd) {
		Long long1 = bd != null ? bd.longValue() : null;
		return long1;
	}

	public static Long getLongFromBigInteger(BigInteger bi) {
		Long long1 = bi != null ? bi.longValue() : null;
		return long1;
	}

	public static Locale getLocale() {
		Locale locale = Constants.TR_LOCALE;
		// try {
		// FacesContext fc = FacesContext.getCurrentInstance();
		// locale = fc.getViewRoot().getLocale();
		// } catch (Exception e) {
		// locale = null;
		// }
		// if (locale == null)
		// locale = Constants.TR_LOCALE;
		return locale;
	}

	public static double getOdenenFazlaMesaiSaati() {
		return odenenFazlaMesaiSaati;
	}

	public static void setOdenenFazlaMesaiSaati(double odenenFazlaMesaiSaati) {
		PdksUtil.odenenFazlaMesaiSaati = odenenFazlaMesaiSaati;
	}

	public static String getCanliSunucu() {
		return canliSunucu;
	}

	public static void setCanliSunucu(String canliSunucu) {
		PdksUtil.canliSunucu = canliSunucu;
	}

	public static int getMESAI_YUVARLAMA_KATSAYI() {
		return MESAI_YUVARLAMA_KATSAYI;
	}

	public static void setMESAI_YUVARLAMA_KATSAYI(int value) {
		MESAI_YUVARLAMA_KATSAYI = value;
	}

	public static boolean isSistemDestekVar() {
		return sistemDestekVar;
	}

	public static void setSistemDestekVar(boolean sistemDestekVar) {
		PdksUtil.sistemDestekVar = sistemDestekVar;
	}

	public static String getDateFormat() {
		String str = dateFormat != null ? dateFormat : "dd/MM/yyyy";
		return str;
	}

	public static void setDateFormat(String value) {
		if (value == null || value.trim().length() < 2) {
			value = "dd/MM/yyyy";
		}
		dateFormat = value;
		dateTimeFormat = value + " H:mm";
		dateTimeLongFormat = value + " H:mm:ss";
	}

	public static String getDateTimeFormat() {
		String str = dateTimeFormat != null ? dateTimeFormat : getDateFormat() + " " + saatFormat;
		return str;
	}

	public static String getDateTimeLongFormat() {
		String str = dateTimeLongFormat != null ? dateTimeLongFormat : getDateFormat() + " " + saatFormat + ":ss";
		return str;
	}

	public static String getSaatFormat() {
		return saatFormat;
	}

	public static void setSaatFormat(String saatFormat) {
		PdksUtil.saatFormat = saatFormat;
	}

	public static String getTestSunucu() {
		return testSunucu;
	}

	public static void setTestSunucu(String testSunucu) {
		PdksUtil.testSunucu = testSunucu;
	}

	public static Date getHelpDeskLastDate() {
		return helpDeskLastDate;
	}

	public static void setHelpDeskLastDate(Date helpDeskLastDate) {
		PdksUtil.helpDeskLastDate = helpDeskLastDate;
	}

	public static Integer getYarimYuvarlaLast() {
		return yarimYuvarlaLast;
	}

	public static void setYarimYuvarlaLast(Integer yarimYuvarlaLast) {
		PdksUtil.yarimYuvarlaLast = yarimYuvarlaLast;
	}

	public static boolean isPuantajSorguAltBolumGir() {
		return puantajSorguAltBolumGir;
	}

	public static void setPuantajSorguAltBolumGir(boolean puantajSorguAltBolumGir) {
		PdksUtil.puantajSorguAltBolumGir = puantajSorguAltBolumGir;
	}

}
