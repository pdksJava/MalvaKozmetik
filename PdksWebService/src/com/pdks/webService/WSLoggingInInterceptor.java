package com.pdks.webService;

import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.pdks.genel.model.PdksUtil;

public class WSLoggingInInterceptor extends AbstractSoapInterceptor {

	public Logger logger = Logger.getLogger(WSLoggingInInterceptor.class);

	public WSLoggingInInterceptor() {
		super(Phase.RECEIVE);
	}

	@Override
	public void handleMessage(SoapMessage message) throws Fault {
		if (message != null) {
			HttpServletRequest httpRequest = (HttpServletRequest) message.get(AbstractHTTPDestination.HTTP_REQUEST);
			String soapAction = null, transactionId = null, endPoint = null;
			Message inMessage = null;
			Exchange exchange = null;
			Date bugun = new Date();
			try {
				transactionId = "" + bugun.getTime();
			} catch (Exception e) {
				transactionId = UUID.randomUUID().toString();
			}

			try {
				exchange = message.getExchange();
				if (exchange != null) {
					if (httpRequest != null)
						endPoint = "http://" + httpRequest.getServerName() + (httpRequest.getServerPort() != 80 ? ":" + httpRequest.getServerPort() : "") + httpRequest.getRequestURI();
					else {
						endPoint = exchange.getEndpoint().getEndpointInfo().getAddress();
						if (endPoint.indexOf("http") < 0) {
							try {
							} catch (Exception es) {
								logger.error(es);
							}

						}
					}

					inMessage = exchange.getInMessage();
					MessageInfo mi = inMessage != null ? (MessageInfo) inMessage.get(MessageInfo.class) : null;
					if (mi == null) {
						inMessage = message;
						mi = (MessageInfo) inMessage.get(MessageInfo.class);
					}
					if (mi != null) {
						OperationInfo operationInfo = mi.getOperation();
						soapAction = operationInfo.getName().getLocalPart();
					} else {
						Map<String, Object> map = (Map) inMessage.get(Message.PROTOCOL_HEADERS);
						if (map.containsKey("SOAPAction")) {
							List list = (List) map.get("SOAPAction");
							soapAction = (String) list.get(0);
							if (soapAction.indexOf("\"") >= 0)
								soapAction = PdksUtil.replaceAll(soapAction, "\"", "");
						}
					}

				}

			} catch (Exception e) {
				logger.error(e);

			}
			InputStream ins = null;
			String xml = null;
			if (soapAction == null) {
				ins = inMessage.getContent(InputStream.class);
				xml = PdksUtil.StringToByInputStream(ins);
				if (xml != null)
					xml = PdksUtil.getXMLConvert(xml);
				soapAction = getActionName(xml);
				if (soapAction == null) {
					logger.error(xml != null ? xml : "XML okunamadi!");
				}

			}
			if (soapAction == null && xml != null && xml.trim().length() > 0) {
				logger.info(xml);
				soapAction = "noAction";
			}
			try {
				// now get the request xml
				if (soapAction != null && !soapAction.trim().equals("")) {
					int index = soapAction.indexOf(":");
					if (index > 0)
						soapAction = soapAction.substring(index + 1);
					if (ins == null)
						ins = inMessage.getContent(InputStream.class);
					if (xml == null)
						xml = PdksUtil.StringToByInputStream(ins);
					String ekKey = "";
					String orjinalXML = new String(xml);
					message.put("requestOrjinalXML", orjinalXML);
					// if (xml != null)
					// xml = PdksUtil.getXMLConvert(xml);
					if (soapAction.length() > 1)
						soapAction = PdksUtil.setTurkishStr(soapAction.substring(0, 1)).toUpperCase(Locale.ENGLISH) + soapAction.substring(1);

					if (soapAction.equalsIgnoreCase("saveIzinHakedisler") || soapAction.equalsIgnoreCase("getMesaiPDKS") || soapAction.equalsIgnoreCase("SavePersoneller") || soapAction.equalsIgnoreCase("SaveIzinler"))
						ekKey = getParseKey(xml, soapAction);
					String xmlStr = xml;
					// LinkedHashMap<String, String> map = getChangeMap();
					// for (String key : map.keySet()) {
					// String newKey = map.get(key);
					// if (xmlStr.indexOf(key) > 0)
					// xmlStr = PdksUtil.replaceAll(xmlStr, key, newKey);
					// if (xmlStr.indexOf(newKey) > 0)
					// xml = PdksUtil.replaceAll(xmlStr, newKey, key);
					// }
					// if (xmlStr.indexOf("&") > 0)
					// xmlStr = PdksUtil.replaceAll(xmlStr, "&", "&amp;");
					// if (xmlStr.indexOf("&amp;amp;") > 0)
					// xmlStr = PdksUtil.replaceAllManuel(xmlStr, "&amp;amp;", "&amp;");
					InputStream oss = PdksUtil.getInputStreamByString(xmlStr);
					message.setContent(InputStream.class, oss);
					StringBuffer sb = new StringBuffer();
					String header = "<?xml version=\"1.0\" ?>";
					index = xml.indexOf("<S:Envelope");
					if (index >= 0)
						xml = xml.substring(index);

					sb.append(header);
					sb.append("<" + soapAction + ">");
					sb.append("<remoteAddr>" + httpRequest.getRemoteAddr() + "</remoteAddr>");
					if (endPoint != null)
						sb.append("<endPoint>" + endPoint + "</endPoint>");
					sb.append("<data>" + orjinalXML + "</data>");
					sb.append("<return/>");

					sb.append("<startTime>" + PdksUtil.convertToDateString(bugun, "yyyy-MM-dd HH:mm:ss") + "</startTime>");
					sb.append("<stopTime/>");
					sb.append("</" + soapAction + ">");
					xml = sb.toString();

					xml = PdksUtil.formatXML(xml);

					soapAction = "service" + soapAction + "_" + PdksUtil.convertToDateString(bugun, "yyyy-MM-") + ekKey + (transactionId != null ? transactionId : +bugun.getTime());
					message.put("soapAction", soapAction);
					message.put("requestXML", xml);
					PdksUtil.fileWrite(xml, soapAction);
					sb = null;

				}

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * @param xmlString
	 * @return
	 */
	private String getActionName(String xmlString) {
		String soapAction = null;
		Document document = null;
		Element root = null;
		// XMLDecoder xmlDecoder = null;
		try {
			if (xmlString != null && xmlString.trim().length() > 0) {
				if (xmlString.indexOf("&") > 0)
					xmlString = PdksUtil.replaceAll(xmlString, "&", "&amp;");
				document = DocumentHelper.parseText(xmlString);
			}

		} catch (DocumentException e) {

			document = null;
		}
		if (document != null)
			root = document.getRootElement();
		if (root != null && !root.elements().isEmpty()) {
			for (Iterator iterator = root.elementIterator(); iterator.hasNext();) {
				Element element = (Element) iterator.next();
				if (!element.elements().isEmpty()) {
					for (Iterator iterator2 = element.elementIterator(); iterator2.hasNext();) {
						Element methodElement = (Element) iterator2.next();
						soapAction = methodElement.getName();
						break;
					}
				}
			}

		}

		return soapAction;
	}

	/**
	 * @param xmlString
	 * @param soapAction
	 * @return
	 */
	public String getParseKey(String xmlString, String soapAction) {
		String parseKey = "";
		Document document = null;
		Element root = null;
		// XMLDecoder xmlDecoder = null;
		try {
			if (xmlString != null && xmlString.trim().length() > 0) {
				// if (xmlString.indexOf("&") > 0)
				// xmlString = PdksUtil.replaceAll(xmlString, "&", "&amp;");
				document = DocumentHelper.parseText(xmlString);
			}

		} catch (DocumentException e) {
			logger.error(e);
			document = null;
		}
		if (document != null)
			root = document.getRootElement();
		if (root != null && !root.elements().isEmpty()) {
			LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
			for (Iterator iterator = root.elementIterator(); iterator.hasNext();) {
				Element element = (Element) iterator.next();
				if (!element.elements().isEmpty()) {
					for (Iterator iterator2 = element.elementIterator(); iterator2.hasNext();) {
						Element methodElement = (Element) iterator2.next();
						if (!methodElement.getName().equalsIgnoreCase(soapAction))
							break;
						for (Iterator iterator3 = methodElement.elementIterator(); iterator3.hasNext();) {
							Element detay = (Element) iterator3.next();
							if (!detay.elements().isEmpty()) {
								if (methodElement.elements().size() == 1) {
									for (Iterator iterator4 = detay.elementIterator(); iterator4.hasNext();) {
										Element altDetay = (Element) iterator4.next();
										map.put(altDetay.getName(), altDetay.getTextTrim());
									}
								}
								break;
							} else
								map.put(detay.getName(), detay.getTextTrim());
						}

					}
				}

			}
			if (!map.isEmpty()) {
				if (map.size() == 1) {
					for (String key : map.keySet()) {
						parseKey = map.get(key) + "-";
					}
				} else if (soapAction.equalsIgnoreCase("getMesaiPDKS")) {
					if (map.containsKey("sirketKodu"))
						parseKey += map.get("sirketKodu") + "-";
					if (map.containsKey("yil"))
						parseKey += map.get("yil") + "-";
					if (map.containsKey("ay"))
						parseKey += map.get("ay") + "-";
				} else {
					if (map.containsKey("personelNo"))
						parseKey = map.get("personelNo") + "-";
				}
			}
			map = null;
		}
		return parseKey;
	}

}
