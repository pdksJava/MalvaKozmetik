package org.pdks.erp.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import org.pdks.sap.entity.SAPSunucu;
import com.sap.mw.jco.JCO;

/**
 *  
 * 
 */
@Name("sapRfcManager")
public class SapRfcManager implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2976669510938109990L;
	static Logger logger = Logger.getLogger(SapRfcManager.class);

	private static HashMap<String, String> parameterMap;

	private static Log log;

	private String sapPoolKey;

	private static List<SAPSunucu> sapSunucular;

	public JCO.Client getJCOClient() throws Exception {
		JCO.Client client = null;
		try {
			String userId = parameterMap.get("sapUserId");
			String password = parameterMap.get("sapPassword");
			int sunucuTipi = Integer.parseInt(parameterMap.get("sapSunucuTipi"));
			client = getJCOClient(sunucuTipi, userId, password);
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			throw new Exception(e.getMessage());
		}

		return client;
	}

	public JCO.Client getJCOClient(int xSunucuTipi) throws Exception {
		JCO.Client client = null;
		try {
			String userId = parameterMap.get("sapUserId");
			String password = parameterMap.get("sapPassword");
			client = getJCOClient(xSunucuTipi, userId, password);
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			throw new Exception(e.getMessage());
		}

		return client;
	}

	public JCO.Client getJCOClient(int sunucuTipi, String userId, String password) throws Exception {
		boolean sapAcikmi = (parameterMap.get("sapAcik")).equals("1");
		JCO.Client jcoClient = null;
		if (sapAcikmi) {
			List<SAPSunucu> sapSunucuList = new ArrayList<SAPSunucu>(), sapAppSunucuList = new ArrayList<SAPSunucu>();
			for (Iterator iterator = sapSunucular.iterator(); iterator.hasNext();) {
				SAPSunucu sapSunucu = (SAPSunucu) iterator.next();
				if (sapSunucu.getSunucuTipi() == sunucuTipi) {
					if (sapSunucu.getSystemNumber() == null)
						sapSunucuList.add(sapSunucu);
					else
						sapAppSunucuList.add(sapSunucu);
				}
			}
			if (!sapAppSunucuList.isEmpty()) {
				if (sapAppSunucuList.size() > 1)
					Collections.shuffle(sapAppSunucuList);
				sapSunucuList.addAll(sapAppSunucuList);
			}
			sapAppSunucuList = null;
			JCO.Pool pool = null;
			sapPoolKey = null;
			int maxConnections = Integer.parseInt(parameterMap.get("sapPoolMaxConnection"));
			for (Iterator iterator = sapSunucuList.iterator(); iterator.hasNext();) {
				SAPSunucu sapSunucu = (SAPSunucu) iterator.next();
				sapPoolKey = "R3" + sapSunucu.getHostName() + "_" + userId + (sapSunucu.getDil() != null ? "_" + sapSunucu.getDil() : "");
				boolean appServer = sapSunucu.getSystemNumber() != null && sapSunucu.getSystemNumber().trim().length() > 0;
				try {
					if (appServer)
						sapPoolKey += "_" + sapSunucu.getSystemNumber().trim();
					pool = JCO.getClientPoolManager().getPool(sapPoolKey);
				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());
					pool = null;
				}
				if (pool == null) {
					try {
						if (appServer) {
							JCO.addClientPool(sapPoolKey, maxConnections, sapSunucu.getClient(), userId, password, sapSunucu.getDil(), sapSunucu.getHostName(), sapSunucu.getSystemNumber());
						} else {
							JCO.addClientPool(sapPoolKey, maxConnections, sapSunucu.getClient(), userId, password, sapSunucu.getDil(), sapSunucu.getMsHostName(), sapSunucu.getDestinationName(), sapSunucu.getHostName());
						}
					} catch (Exception e) {
						logger.error("PDKS hata in : \n");
						e.printStackTrace();
						logger.error("PDKS hata out : " + e.getMessage());
						removePool(sapPoolKey);
						sapPoolKey = null;
						jcoClient = null;
						if (!iterator.hasNext())
							throw new Exception(e.getMessage());
					}
				}
				try {
					if (sapPoolKey != null)
						jcoClient = JCO.getClient(sapPoolKey);
				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());
					jcoClient = null;
					removePool(sapPoolKey);
					sapSunucuList = null;
					if (!iterator.hasNext())
						throw new Exception(e.getMessage());
				}
				if (jcoClient != null)
					break;
			}
			sapSunucuList = null;
			if (jcoClient == null)
				throw new Exception("SAP JCO Client Alınamadı");
		}
		return jcoClient;
	}

	private static void removePool(String key) throws Exception {
		if (key != null && JCO.getClientPoolManager().getPool(key) != null)
			JCO.getClientPoolManager().removePool(key);

	}

	public static void removePoolManager() {
		String[] sapPoolKeys = null;
		try {
			sapPoolKeys = JCO.getClientPoolManager().getPoolNames();
		} catch (Exception e) {
		}
		if (sapPoolKeys != null && sapPoolKeys.length > 0) {
			for (int i = 0; i < sapPoolKeys.length; i++) {
				String sapPoolKey = sapPoolKeys[i];
				try {
					removePool(sapPoolKey);
					System.out.println(sapPoolKey + " PoolName Remove Pool.");
				} catch (Exception e) {
					System.err.println(sapPoolKey + " " + e);
				}

			}
		}
	}

	/**
	 * @return
	 */
	public String getSapPoolKey() {
		return sapPoolKey;
	}

	/**
	 * @param string
	 */
	public void setSapPoolKey(String string) {
		sapPoolKey = string;
	}

	public static HashMap<String, String> getParameterMap() {
		return parameterMap;
	}

	public static void setParameterMap(HashMap<String, String> parameterMap) {
		SapRfcManager.parameterMap = parameterMap;
	}

	public static Log getLog() {
		return log;
	}

	public static void setLog(Log log) {
		SapRfcManager.log = log;
	}

	public static List<SAPSunucu> getSapSunucular() {
		return sapSunucular;
	}

	public static void setSapSunucular(List<SAPSunucu> sapSunucular) {
		SapRfcManager.sapSunucular = sapSunucular;
	}

}
