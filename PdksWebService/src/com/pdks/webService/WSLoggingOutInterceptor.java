package com.pdks.webService;

import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CacheAndWriteOutputStream;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.io.CachedOutputStreamCallback;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.json.XML;

import com.pdks.entity.ServiceData;
import com.pdks.genel.model.Constants;
import com.pdks.genel.model.PdksUtil;

public class WSLoggingOutInterceptor extends AbstractSoapInterceptor {
	public Logger logger = Logger.getLogger(WSLoggingOutInterceptor.class);

	public WSLoggingOutInterceptor() {
		super(Phase.PRE_STREAM);
	}

	@Override
	public void handleMessage(SoapMessage message) throws Fault {
		CacheAndWriteOutputStream cwos = new CacheAndWriteOutputStream(message.getContent(OutputStream.class));
		String soapAction = null, requestXML = "";
		boolean requestXMLDurum = false;
		try {
			Exchange exchange = message.getExchange();

			if (exchange != null) {
				Message inMessage = exchange.getInMessage();
				if (inMessage != null) {
					requestXMLDurum = inMessage.containsKey("requestXML");
					if (requestXMLDurum)
						requestXML = (String) inMessage.get("requestXML");
					if (inMessage.containsKey("soapAction"))
						soapAction = (String) inMessage.get("soapAction");
				}

				if (soapAction == null) {
					Message outMessage = exchange.getOutMessage();
					MessageInfo mo = (MessageInfo) outMessage.get(MessageInfo.class);
					if (mo != null) {
						OperationInfo operationInfo = mo.getOperation();
						if (operationInfo != null)
							soapAction = operationInfo.getName().getLocalPart();
					}

				}

			}

		} catch (Exception e) {
			logger.error(e);
		}
		message.setContent(OutputStream.class, cwos);
		LoggingOutCallBack callback = new LoggingOutCallBack(soapAction, requestXML, requestXMLDurum);
		cwos.registerCallback(callback);
	}

	class LoggingOutCallBack implements CachedOutputStreamCallback {

		private String xml = "", action = null, inputXML;
		private boolean requestXMLDurum = false;

		public LoggingOutCallBack() {
			super();
		}

		public LoggingOutCallBack(String method, String rXML, boolean xRequestXMLDurum) {
			super();
			this.action = method;
			this.inputXML = rXML;
			this.requestXMLDurum = xRequestXMLDurum;
		}

		@Override
		public void onClose(CachedOutputStream cos) {
			try {
				if (cos != null) {
					xml = PdksUtil.StringToByInputStream(cos.getInputStream());
					if (action == null) {
						String str = "<soap:Body>";
						int index = xml.indexOf(str);
						if (index > 0) {
							str = xml.substring(index + str.length());
							index = str.indexOf("Response");
							if (index > 0) {
								str = str.substring(0, index);
								index = str.indexOf(":");
								if (index > 0)
									action = str.substring(index + 1);
							}

						}
					}
					if (action != null) {
						logger.debug(action + " --> " + xml);
						if (inputXML != null) {
							if (inputXML.indexOf("<return/>") > 0)
								xml = PdksUtil.replaceAllManuel(inputXML, "<return/>", "<return>" + xml + "</return>");
							if (inputXML.indexOf("<stopTime/>") > 0)
								xml = PdksUtil.replaceAllManuel(xml, "<stopTime/>", "<stopTime>" + PdksUtil.convertToDateString(new Date(), "yyyy-MM-dd HH:mm:ss") + "</stopTime>");
						}
						if (!action.startsWith("service")) {
							if (action.length() > 1)
								action = PdksUtil.setTurkishStr(action.substring(0, 1)).toUpperCase(Locale.ENGLISH) + action.substring(1);
							Date bugun = new Date();
							action = "service" + action + "_" + PdksUtil.convertToDateString(bugun, "yyyy-MM-") + bugun.getTime();

						}
						try {
							xml = PdksUtil.formatXML(xml);
							// xml = xml.indexOf("&amp;") > 0 ? PdksUtil.replaceAllManuel(xml, "&amp;", "&") : xml;
							PdksUtil.fileWrite(xml, action);
						} catch (Exception eg) {
							logger.error(eg);
							eg.printStackTrace();
						}

						if (requestXMLDurum)
							try {
								String[] dizi = new String[] { "ns2", "web" };
								String jsonStr = xml;
								for (int i = 0; i < dizi.length; i++) {
									String str = dizi[i];
									String schema = "xmlns:" + str + "=\"http://webService.pdks.com/\"";
									if (jsonStr.indexOf(schema) > 0) {
										jsonStr = PdksUtil.replaceAllManuel(jsonStr, schema, "");
										jsonStr = PdksUtil.replaceAllManuel(jsonStr, str + ":", "");
									}
								}
								dizi = null;
								JSONObject json = null;
								try {
									json = XML.toJSONObject(jsonStr);
								} catch (Exception ej) {
									logger.debug(ej);
								}
								HashMap<String, String> sonucMap = new HashMap<String, String>();
								String fonksiyonAdi = null;
								if (json != null) {
									Iterator<String> keys = json.keys();
									while (keys.hasNext()) {
										String key1 = keys.next();
										fonksiyonAdi = key1;
										JSONObject jsonDatalar = (JSONObject) json.get(key1);
										Iterator<String> keysData = jsonDatalar.keys();
										while (keysData.hasNext()) {
											String key = keysData.next();
											if (key.equals("data") || key.equals("return")) {
												JSONObject service = (JSONObject) jsonDatalar.get(key);
												Iterator<String> keysVeriler = service.keys();
												while (keysVeriler.hasNext()) {
													String keyVeri = keysVeriler.next();
													JSONObject jsonServiceObject = (JSONObject) service.get(keyVeri);
													Iterator<String> keysService = jsonServiceObject.keys();
													while (keysService.hasNext()) {
														String keyService = keysService.next();
														if (keyService.indexOf("Body") >= 0) {
															try {
																JSONObject jsonObject = (JSONObject) jsonServiceObject.get(keyService);
																String jsonString = jsonObject.toString(4);
																jsonString = PdksUtil.replaceAllManuel(PdksUtil.replaceAllManuel(jsonString, "\n", ""), "  ", " ");
																sonucMap.put(key, jsonString);
															} catch (Exception ex) {
																logger.error(ex);
															}

														}
													}

												}

											}
										}

									}
								}
								if (!sonucMap.isEmpty()) {
									ServiceData serviceData = new ServiceData(fonksiyonAdi);
									LinkedHashMap<String, String> map = WSLoggingOutInterceptor.getChangeMap();
									serviceData.setInputData(getString(map, sonucMap.get("data")));
									serviceData.setOutputData(getString(map, sonucMap.get("return")));
									try {
										Constants.pdksDAO.saveObject(serviceData);
									} catch (Exception e1) {
										logger.error(e1);
									}
									map = null;

								}
								sonucMap = null;

							} catch (Exception e) {
								logger.error(e);
								e.printStackTrace();
							}
					}

				}
			} catch (Exception e) {
				logger.error("Error trying to log request", e);
			}
		}

		/**
		 * @param map
		 * @param str
		 * @return
		 */
		private String getString(LinkedHashMap<String, String> map, String str) {
			if (map != null && str != null) {
				for (String pattern : map.keySet()) {
					String replace = map.get(pattern);
					if (str.indexOf(pattern) >= 0)
						str = PdksUtil.replaceAllManuel(str, pattern, replace);
				}
			}
			return str;
		}

		public String getXML() {
			return xml;
		}

		@Override
		public void onFlush(CachedOutputStream arg0) {
		}
	}

	/**
	 * @return
	 */
	public static LinkedHashMap<String, String> getChangeMap() {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put("&amp;", "&");
		map.put("&lt;", "<");
		map.put("&gt;", ">");
		return map;
	}
}
