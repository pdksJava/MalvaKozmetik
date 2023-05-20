package com.pdks.genel.model;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.Collator;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.InternetAddress;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import com.Ostermiller.util.Base64;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pdks.entity.Tanim;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.QNameMap;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class PdksUtil implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7171523343222649407L;
	static Logger logger = Logger.getLogger(PdksUtil.class);

	public static final Locale RU_LOCALE = new Locale("ru", "RU");
	public static final Locale TR_LOCALE = new Locale("tr", "TR");
	public static final String SEPARATOR_MAIL = ";";
	public static final boolean isUTF8 = Boolean.TRUE;
	private static HttpServletRequest httpServletRequest;

	private static int sistemBaslangicYili, izinOffAdet, izinHaftaAdet, planOffAdet;

	private static String url, bundleName, dateFormat = "dd/MM/yyyy", canliSunucu, testSunucu;

	private static Date sonSistemTarih;

	private static double odenenFazlaMesaiSaati = 30d;

	private static ServletConfig servletConfig;

	private static ServletContext sc;

	private static boolean sistemDestekVar = false;

	/**
	 * @param str
	 * @return
	 */
	public static byte[] getBytesUTF8(String str) {
		byte[] bs = str != null ? str.getBytes(StandardCharsets.UTF_8) : null;
		return bs;
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
	 * @param aciklama
	 * @return
	 */
	public static boolean hasStringValue(String aciklama) {
		boolean strDolu = aciklama != null && aciklama.trim().length() > 0;
		return strDolu;
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
			if (arrayTag != null && arrayTag.trim().length() > 0) {
				if (jsonStr.startsWith("["))
					jsonStr = "{\"" + arrayTag + "\":" + jsonStr + "}";
				else if (jsonStr.startsWith("{"))
					jsonStr = "{\"" + arrayTag + "\":" + jsonStr.substring(1);
			}
			if (jsonStr.indexOf("{") >= 0) {
				JSONObject jsonObject = new JSONObject(jsonStr);
				str = XML.toString(jsonObject);
			}
		} catch (Exception e) {
			str = jsonStr;
		}
		if (str.equals(""))
			str = jsonStr;
		String xml = formatXML("<?xml version=\"1.0\" encoding=\"UTF-8\"?><" + rootName + ">" + str + "</" + rootName + ">");
		return xml;
	}

	/**
	 * @param value
	 * @return
	 */
	public static String getCutFirstSpaces(String value) {
		String str = value;
		if (sistemDestekVar && value != null) {
			while (str.startsWith(" "))
				str = str.substring(1);
			str = str.trim();
		}
		return str;
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
	 * @param xml
	 * @return
	 */
	public static String getXMLConvert(String xml) {
		String data = xml;
		if (data != null) {

			while (data.indexOf(" </") >= 0)
				data = replaceAll(data, " </", "</");
			if (data.indexOf(" xsi:nil=\"true\"") >= 0)
				data = replaceAll(data, " xsi:nil=\"true\"", "");
			if (data.indexOf("\t") >= 0)
				data = replaceAll(data, "\t", "");
			if (data.indexOf("&#26;") >= 0)
				data = replaceAll(data, "&#26;", "");
			if (data.indexOf("&#41;") >= 0)
				data = replaceAll(data, "&#41;", "");
			if (data.indexOf("&#9;") >= 0)
				data = replaceAll(data, "&#9;", "");
			if (data.indexOf("&#304;") >= 0)
				data = replaceAll(data, "&#304;", "İ");
			if (data.indexOf("&#305;") >= 0)
				data = replaceAll(data, "&#305;", "ı");
			if (data.indexOf("&#214;") >= 0)
				data = replaceAll(data, "&#214;", "Ö");
			if (data.indexOf("&#246;") >= 0)
				data = replaceAll(data, "&#246;", "ö");
			if (data.indexOf("&#220;") >= 0)
				data = replaceAll(data, "&#220;", "Ü");
			if (data.indexOf("&#252;") >= 0)
				data = replaceAll(data, "&#252;", "ü");
			if (data.indexOf("&#199;") >= 0)
				data = replaceAll(data, "&#199;", "Ç");
			if (data.indexOf("&#231;") >= 0)
				data = replaceAll(data, "&#231;", "ç");
			if (data.indexOf("&#286;") >= 0)
				data = replaceAll(data, "&#286;", "Ğ");
			if (data.indexOf("&#287;") >= 0)
				data = replaceAll(data, "&#287;", "ğ");
			if (data.indexOf("&#350;") >= 0)
				data = replaceAll(data, "&#350;", "Ş");
			if (data.indexOf("&#351;") >= 0)
				data = replaceAll(data, "&#351;", "ş");
			if (data.indexOf("\u015E") >= 0)
				data = replaceAll(data, "\u015E", "Ş");
			if (data.indexOf("\u015F") >= 0)
				data = replaceAll(data, "\u015F", "ş");
			if (data.indexOf("\u011E") >= 0)
				data = replaceAll(data, "\u011E", "Ğ");
			if (data.indexOf("\u011F") >= 0)
				data = replaceAll(data, "\u011F", "ğ");
			if (data.indexOf("\u00D6") >= 0)
				data = replaceAll(data, "\u00D6", "Ö");
			if (data.indexOf("\u00F6") >= 0)
				data = replaceAll(data, "\u00F6", "ö");
			if (data.indexOf("\u00DC") >= 0)
				data = replaceAll(data, "\u00DC", "Ü");
			if (data.indexOf("\u00FC") >= 0)
				data = replaceAll(data, "\u00FC", "ü");
			if (data.indexOf("\u00C7") >= 0)
				data = replaceAll(data, "\u00C7", "Ç");
			if (data.indexOf("\u00E7") >= 0)
				data = replaceAll(data, "\u00E7", "ç");
			if (data.indexOf("\u0130") >= 0)
				data = replaceAll(data, "\u0130", "İ");
			if (data.indexOf("\u0131") >= 0)
				data = replaceAll(data, "\u0131", "ı");
			if (data.indexOf("&gt;") >= 0)
				data = replaceAll(data, "&gt;", ">");
			if (data.indexOf("&lt;") >= 0)
				data = replaceAll(data, "&lt;", "<");
			if (data.indexOf("&#x11E;") >= 0)
				data = replaceAll(data, "&#x11E;", "Ğ");
			if (data.indexOf("&#x130;") >= 0)
				data = replaceAll(data, "&#x130;", "İ");
			if (data.indexOf("&#x15E;") >= 0)
				data = replaceAll(data, "&#x15E;", "Ş");
			if (data.indexOf("&#x15F;") >= 0)
				data = replaceAll(data, "&#x15F;", "ş");
			if (data.indexOf("&#x131;") >= 0)
				data = replaceAll(data, "&#x131;", "ı");
			if (data.indexOf("&#xDC;") >= 0)
				data = replaceAll(data, "&#xDC;", "Ü");
			if (data.indexOf("&#xFC;") >= 0)
				data = replaceAll(data, "&#xFC;", "ü");
			if (data.indexOf("&#x11F;") >= 0)
				data = replaceAll(data, "&#x11F;", "ğ");
			if (data.indexOf("&#xF6;") >= 0)
				data = replaceAll(data, "&#xF6;", "ö");
			if (data.indexOf("&#xD6;") >= 0)
				data = replaceAll(data, "&#xD6;", "Ö");
			// data = getXmlAciklama(data);

		}

		return data;
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

	/**
	 * @param object
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public static String xstreamToXML(Object object, String name) throws Exception {
		String xml = null;
		if (object != null) {
			try {
				QNameMap qmap = new QNameMap();
				qmap.setDefaultNamespace("");
				qmap.setDefaultPrefix("");
				StaxDriver staxDriver = new StaxDriver(qmap);
				XStream xstream = new XStream(staxDriver);

				Class objectClass = object.getClass();
				if (name == null) {
					name = objectClass.getName();
					if (name.indexOf(".") > 0) {
						name = name.substring(name.lastIndexOf(".") + 1);
						name = String.valueOf(name.charAt(0)).toLowerCase(Locale.ENGLISH) + name.substring(1);
					}
				}
				xstream.alias(name, objectClass);
				xml = xstream.toXML(object);
				if (xml != null) {
					xml = replaceAll(xml, "\n", "");
					xml = replaceAll(xml, ">  <", "><");
					xml = replaceAll(xml, " <", "<");
					xml = formatXML(xml);
				}
				logger.debug(xml);
			} catch (Exception e) {
				xml = null;
			}

		}
		return xml;
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
	 * @param object
	 * @return
	 */
	public static String jaxbObjectToXML(Object object) {
		String xmlContent = null;
		try {
			// Create JAXB Context
			JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());

			// Create Marshaller
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			// Required formatting??
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			// Print XML String to Console
			StringWriter sw = new StringWriter();

			// Write XML to StringWriter
			jaxbMarshaller.marshal(object, sw);

			// Verify XML Content
			xmlContent = sw.toString();

		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return xmlContent;
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

	/**
	 * @param in
	 * @return
	 */
	public static String StringToByInputStream(InputStream in) {
		String str = null;
		try {
			if (in != null) {
				str = IOUtils.toString(in, "utf-8");
				if (str != null && str.trim().equals(""))
					str = null;
			}

		} catch (Exception e) {
			logger.error(e);
		}

		return str;
	}

	/**
	 * @return
	 */
	public static boolean getTestSunucuDurum() {
		String hostName = getHostName();
		String sunucu = testSunucu != null ? testSunucu.toLowerCase(Locale.ENGLISH) : "srvglf";
		boolean test = hostName.toLowerCase(Locale.ENGLISH).startsWith(sunucu);
		return test;
	}

	/**
	 * @return
	 */
	public static boolean getCanliSunucuDurum() {
		String hostName = getHostName();
		String sunucu = canliSunucu != null ? canliSunucu.toLowerCase(Locale.ENGLISH) : "srvglf";
		boolean canli = hostName.toLowerCase(Locale.ENGLISH).startsWith(sunucu);
		return canli;
	}

	/**
	 * @return
	 */
	public static String getHostName() {
		String hostName = null;
		try {
			InetAddress thisIp = InetAddress.getLocalHost();
			hostName = thisIp.getHostName().toLowerCase(Locale.ENGLISH);
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
		String planKey = PdksUtil.getDecodeStringByBase64(key);
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

	/**
	 * @param string
	 * @return
	 * @throws Exception
	 */
	public static InputStream getInputStreamByString(String string) throws Exception {
		InputStream stream = new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
		return stream;
	}

	/**
	 * @param initialStream
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static File getFileByInputStream(InputStream initialStream, String fileName) throws IOException {
		File targetFile = new File(fileName);
		FileUtils.copyInputStreamToFile(initialStream, targetFile);
		return targetFile;
	}

	/**
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static byte[] getFileByteArray(File file) throws FileNotFoundException, IOException {
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

	// String yeniString=new
	// String(gelenString.getBytes("ISO-8859-1"),"ISO-8859-9");

	public static String convertUTF8(String sonuc) {
		if (sonuc != null) {
			if (sonuc.indexOf('İ') >= 0) {// I için 1
				sonuc = replaceAll(sonuc, String.valueOf('İ'), "\u0130");
			}
			if (sonuc.indexOf('ı') >= 0) {// i için 2
				sonuc = replaceAll(sonuc, String.valueOf('ı'), "\u0131");
			}
			if (sonuc.indexOf('Ş') >= 0) {// i için 2
				sonuc = replaceAll(sonuc, String.valueOf('Ş'), "\u015E");
			}
			if (sonuc.indexOf('ş') >= 0) {// i için 2
				sonuc = replaceAll(sonuc, String.valueOf('ş'), "\u015F");
			}
			if (sonuc.indexOf('Ğ') >= 0) {// i için 2
				sonuc = replaceAll(sonuc, String.valueOf('Ğ'), "\u011E");
			}
			if (sonuc.indexOf('ğ') >= 0) {// i için 2
				sonuc = replaceAll(sonuc, String.valueOf('ğ'), "\u011F");
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
			logger.debug("\nÖnceki hali " + sa + "\nSonraki hali: " + sb.toString());
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
				long deger2 = Long.parseLong(PdksUtil.convertToDateString(tarih2, pattern));
				long deger1 = Long.parseLong(PdksUtil.convertToDateString(tarih1, pattern));
				while (deger2 > deger1) {
					cal.setTime(tarih1);
					cal.add(Calendar.DATE, sayi + 1);
					deger1 = Long.parseLong(PdksUtil.convertToDateString(cal.getTime(), pattern));
					if (deger2 >= deger1)
						sayi++;
					// logger.info(deger1 + " " + deger2 + " " + sayi);
				}
				fark = (long) sayi;

				// fark = tarih1 != null && tarih2 != null ? new
				// Long((tarih2.getTime() - tarih1.getTime()) / (1000 * 60 * 60
				// * 24)) : null;
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
		long zoneDSTFark = getTimeZoneDSTFark(tarihBit, tarihBas);
		double fark = setDoubleRounded((double) (tarihBit.getTime() - tarihBas.getTime() + zoneDSTFark) / ((double) 1000 * 60 * 60), 2, BigDecimal.ROUND_HALF_DOWN);
		return fark;
	}

	/**
	 * @param tarihBit
	 * @param tarihBas
	 * @return
	 */
	public static long getTimeZoneDSTFark(Date tarihBit, Date tarihBas) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(tarihBit);
		long zone1 = cal.get(Calendar.ZONE_OFFSET), dts1 = cal.get(Calendar.DST_OFFSET);
		cal.setTime(tarihBas);
		long zone2 = cal.get(Calendar.ZONE_OFFSET), dts2 = cal.get(Calendar.DST_OFFSET);
		long fark = (zone2 - dts2) - (zone1 - dts1);
		return fark;
	}

	/**
	 * @param tarihBit
	 * @param tarihBas
	 * @return
	 */
	public static Double getSaatFarki(Date tarihBit, Date tarihBas) {
		long zoneDSTFark = getTimeZoneDSTFark(tarihBit, tarihBas);
		double fark = getSaatFarkiHesapla(tarihBit, tarihBas);
		if (zoneDSTFark != 0) {
			logger.debug(fark + " " + tarihBit + "  " + tarihBas + " " + zoneDSTFark);
			double fark1 = getSaatFarkiHesapla(tariheAyEkleCikar((Date) tarihBit.clone(), 1), tariheAyEkleCikar((Date) tarihBas.clone(), 1));
			if (fark1 != fark) {
				double fark2 = getSaatFarkiHesapla(tariheAyEkleCikar((Date) tarihBit.clone(), 2), tariheAyEkleCikar((Date) tarihBas.clone(), 2));
				if (fark2 == fark1)
					fark = fark1;
			}

		}

		return fark;

	}

	/**
	 * @param tarihBit
	 * @param tarihBas
	 * @return
	 */
	public static Double getDakikaFarki(Date tarihBit, Date tarihBas) {
		String pattern = "yyyyMMdd HH:mm:ss";
		String str1 = convertToDateString(tarihBit, pattern), str2 = convertToDateString(tarihBas, pattern);
		Date tarih1 = convertToJavaDate(str1, pattern), tarih2 = convertToJavaDate(str2, pattern);
		long zoneDSTFark = getTimeZoneDSTFark(tarihBit, tarihBas);
		double fark = setDoubleRounded((double) (tarih1.getTime() - tarih2.getTime() + zoneDSTFark) / ((double) 1000 * 60), 2, BigDecimal.ROUND_HALF_DOWN);
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

	public static List<String> getListByString(String str, String delim) {
		List<String> mailList = null;
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
				if (delim.equals("|"))
					mailList = getListStringTokenizer(str, delim);
				else
					mailList = Arrays.asList(str.split(delim));
			} else {
				mailList = new ArrayList();
				mailList.add(str);
			}
		}
		return mailList;
	}

	/**
	 * @param str
	 * @param delim
	 * @return
	 */
	public static List<String> getListStringTokenizer(String str, String delim) {
		List<String> mailList = new ArrayList();
		StringTokenizer st = new StringTokenizer(str, delim);
		while (st.hasMoreTokens()) {
			String strSt = st.nextToken();
			mailList.add(strSt);
		}
		return mailList;
	}

	/**
	 * @param str
	 * @param delim
	 * @return
	 */
	public static List<String> getListFromString(String str, String delim) {
		List<String> list = new ArrayList<String>();
		List<String> mailList = null;
		if (str != null) {
			mailList = getListByString(str, delim);

			if (mailList != null && !mailList.isEmpty()) {
				for (Iterator iterator = mailList.iterator(); iterator.hasNext();) {
					String token = (String) iterator.next();
					try {
						String parca = PdksUtil.getInternetAdres(token.trim());
						if (parca != null && !parca.trim().equals(""))
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
		if (request.getParameter("sayfaNo" + sonEk) != null && !request.getParameter("sayfaNo" + sonEk).trim().equals(""))
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

	public static String replaceAll(String str, String pattern, String replace) {
		if (str != null && pattern != null && replace != null)
			str = str.replaceAll(pattern, replace);
		return str;
	}

	public static String replaceAllManuel(String str, String pattern, String replace) {

		if ((str != null) && (pattern != null) && (pattern.length() > 0) && (replace != null)) {
			while (str.indexOf(pattern) >= 0) {
				StringBuffer lSb = new StringBuffer();
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
		Locale locale = Constants.TR_LOCALE;

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
					bundleAdi = "org.jboss.seam.core.SeamResourceBundle";
				message = getMessageBundleMessage(locale, key, bundleAdi);
			} catch (Exception e) {
				try {
					message = getMessageBundleMessage(locale, key, "messages");
				} catch (Exception e2) {
					message = key + " ? (" + locale.getLanguage() + ")";
					logger.error(message + (" " + key + " " + bundleAdi) + " " + e);
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
		String message;
		ResourceBundle bundle = ResourceBundle.getBundle(bundleAdi, locale);
		if (bundle.containsKey(key))
			message = bundle.getString(key);
		else
			message = key + " ?  ";
		return message;
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

	public static double setDoubleRounded(double deger, int digit, int yuvarmaTipi) {
		double yuvarlanmisDeger = deger;
		if (deger != 0)
			try {
				BigDecimal decimal = new BigDecimal(deger).setScale(digit, yuvarmaTipi);
				yuvarlanmisDeger = decimal.doubleValue();
			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());
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

	public static void setBundleName(String bundleName) {
		PdksUtil.bundleName = bundleName;
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

	public static Double stringFormatConvertNumericValue(String submittedValue, Locale locale) throws Exception {
		Double degerDouble = null;
		if (submittedValue != null) {
			try {
				BigDecimal bd = null;

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

	public static Date getGunSonu(Date tarih) {
		Date gunSonu = null;
		if (tarih != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(tarih);
			cal.add(Calendar.DATE, 1);
			cal.setTime(PdksUtil.getDate((Date) cal.getTime().clone()));
			cal.set(Calendar.MILLISECOND, -2);
			gunSonu = cal.getTime();
		}
		return gunSonu;
	}

	public static Date getBakiyeYil() {
		Calendar date = Calendar.getInstance();
		date.set(1900, 0, 1);
		Date tarih = PdksUtil.getDate(date.getTime());
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
			String dosyaAdi = path + "/" + fileName + ".xml";
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

	public static ServletConfig getServletConfig() {
		return servletConfig;
	}

	public static void setServletConfig(ServletConfig servletConfig) {
		PdksUtil.servletConfig = servletConfig;
	}

	public static ServletContext getSc() {
		return sc;
	}

	public static void setSc(ServletContext sc) {
		PdksUtil.sc = sc;
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

	public static void setDateFormat(String dateFormat) {
		PdksUtil.dateFormat = dateFormat;
	}

	public static String getCanliSunucu() {
		return canliSunucu;
	}

	public static void setCanliSunucu(String canliSunucu) {
		PdksUtil.canliSunucu = canliSunucu;
	}

	public static String getTestSunucu() {
		return testSunucu;
	}

	public static void setTestSunucu(String testSunucu) {
		PdksUtil.testSunucu = testSunucu;
	}

}
