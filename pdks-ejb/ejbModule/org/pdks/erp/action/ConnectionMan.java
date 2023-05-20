package org.pdks.erp.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.pdks.session.PdksUtil;

import org.pdks.sap.entity.SAPSunucu;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.ext.DestinationDataProvider;
public class ConnectionMan implements Serializable {


	static Logger logger = Logger.getLogger(ConnectionMan.class);
	private static final long serialVersionUID = 6284445448701690000L;

	private String sapPoolKey;

	private int sunucuTipi;

	private JCoFunction function;

	private HashMap<String, String> parameterMap;

	private static MyDestinationDataProvider myProvider;

	public ConnectionMan() {
		super();
		this.parameterMap = SapRfcManager.getParameterMap();
		this.sunucuTipi = Integer.parseInt(parameterMap.get("sapSunucuTipi"));

	}

	public ConnectionMan(int sunucuTipi) {
		super();
		this.sunucuTipi = sunucuTipi;
		this.parameterMap = SapRfcManager.getParameterMap();
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public JCoDestination getJCoDestination() throws Exception {
		JCoDestination client = null;
		try {
			String userId = parameterMap.get("sapUserId");
			String password = parameterMap.get("sapPassword");
			client = getJCoDestination(sunucuTipi, userId, password);
		} catch (Exception e) {
			throw new Exception(PdksUtil.setTurkishStr(e.getMessage()));
		}

		return client;
	}

	/**
	 * @param sunucuTipi
	 * @param userId
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public JCoDestination getJCoDestination(int sunucuTipi, String userId, String password) throws Exception {
		JCoDestination destination = null;
		boolean sapAcikmi = (parameterMap.get("sapAcik")).equals("1");
		String hataMesaji = null;
		if (sapAcikmi) {
			List<SAPSunucu> sapSunucuList = new ArrayList<SAPSunucu>(), sapAppSunucuList = new ArrayList<SAPSunucu>();
			for (Iterator iterator = SapRfcManager.getSapSunucular().iterator(); iterator.hasNext();) {
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
			sapPoolKey = null;
			int maxConnections = Integer.parseInt(SapRfcManager.getParameterMap().get("sapPoolMaxConnection"));
			for (Iterator iterator = sapSunucuList.iterator(); iterator.hasNext();) {
				SAPSunucu sapSunucu = (SAPSunucu) iterator.next();

				boolean appServer = sapSunucu.getSystemNumber() != null && sapSunucu.getSystemNumber().trim().length() > 0;
				String key = "R3" + sapSunucu.getHostName() + "_" + userId + (sapSunucu.getDil() != null ? "_" + sapSunucu.getDil() : "");
				try {
					sapPoolKey = PdksUtil.encodePassword(key);
					destination = JCoDestinationManager.getDestination(sapPoolKey);
					if (destination != null)
						destination.ping();
				} catch (Exception e) {
					if (getMyProvider() != null)
						myProvider.changeProperties(sapPoolKey, null);
				}
				if (destination == null) {
					Properties connectProperties = new Properties();
					if (!appServer) {
						connectProperties.setProperty(DestinationDataProvider.JCO_MSHOST, sapSunucu.getHostName());
						if (sapSunucu.getDestinationName() != null)
							connectProperties.setProperty(DestinationDataProvider.JCO_R3NAME, sapSunucu.getDestinationName());
						if (sapSunucu.getMsHostName() != null)
							connectProperties.setProperty(DestinationDataProvider.JCO_GROUP, sapSunucu.getMsHostName());

					} else {
						connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, sapSunucu.getHostName());
						if (sapSunucu.getSystemNumber() != null)
							connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR, sapSunucu.getSystemNumber().toString());
					}
					connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, sapSunucu.getClient());
					connectProperties.setProperty(DestinationDataProvider.JCO_USER, userId);
					connectProperties.setProperty(DestinationDataProvider.JCO_PASSWD, password);
					connectProperties.setProperty(DestinationDataProvider.JCO_LANG, sapSunucu.getDil());
					connectProperties.setProperty(DestinationDataProvider.JCO_POOL_CAPACITY, String.valueOf(maxConnections));
					try {

						if (myProvider != null) {
							myProvider.changeProperties(sapPoolKey, connectProperties);
							destination = JCoDestinationManager.getDestination(sapPoolKey);
							destination.ping();
						}

					} catch (Exception e) {
						destination = null;
						// e.printStackTrace();
						if (myProvider != null)
							myProvider.changeProperties(sapPoolKey, null);
						hataMesaji = sapSunucu.getHostName() + " --> " + e;

					}
				}

				if (destination == null && hataMesaji != null) {

					throw new Exception(hataMesaji);
				}
			}
		}
		return destination;
	}

	public String getSapPoolKey() {
		return sapPoolKey;
	}

	public void setSapPoolKey(String sapPoolKey) {
		this.sapPoolKey = sapPoolKey;
	}

	public JCoFunction getFunction() {
		return function;
	}

	public void setFunction(JCoFunction function) {
		this.function = function;
	}

	public static MyDestinationDataProvider getMyProvider() {
		return myProvider;
	}

	public static void setMyProvider(MyDestinationDataProvider myProvider) {
		ConnectionMan.myProvider = myProvider;
	}

	public int getSunucuTipi() {
		return sunucuTipi;
	}

	public void setSunucuTipi(int sunucuTipi) {
		this.sunucuTipi = sunucuTipi;
	}

	public HashMap<String, String> getParameterMap() {
		return parameterMap;
	}

	public void setParameterMap(HashMap<String, String> parameterMap) {
		this.parameterMap = parameterMap;
	}


}
