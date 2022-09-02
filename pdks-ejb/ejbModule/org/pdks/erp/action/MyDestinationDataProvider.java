package org.pdks.erp.action;

import java.util.HashMap;
import java.util.Properties;

import com.sap.conn.jco.ext.DataProviderException;
import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;

public class MyDestinationDataProvider implements DestinationDataProvider {

	private DestinationDataEventListener eL;
	private HashMap<String, Properties> secureDBStorage = new HashMap<String, Properties>();

	public Properties getDestinationProperties(String destinationName) {
		try {
			// read the destination from DB
			Properties p = secureDBStorage.get(destinationName);

			if (p != null) {
				// check if all is correct, for example
				if (p.isEmpty())
					throw new DataProviderException(DataProviderException.Reason.INVALID_CONFIGURATION, "destination configuration is incorrect", null);

				return p;
			}

			return null;
		} catch (RuntimeException re) {
			throw new DataProviderException(DataProviderException.Reason.INTERNAL_ERROR, re);
		}
	}

	public void setDestinationDataEventListener(DestinationDataEventListener eventListener) {
		this.eL = eventListener;
	}

	public boolean supportsEvents() {
		return true;
	}

	public DestinationDataEventListener geteL() {
		return eL;
	}

	public void seteL(DestinationDataEventListener eL) {
		this.eL = eL;
	}

	public HashMap<String, Properties> getSecureDBStorage() {
		return secureDBStorage;
	}

	public void setSecureDBStorage(HashMap<String, Properties> secureDBStorage) {
		this.secureDBStorage = secureDBStorage;
	}

	public void changeProperties(String destName, Properties properties) {
		synchronized (secureDBStorage) {
			if (properties == null) {
				if (secureDBStorage.containsKey(destName) && secureDBStorage.remove(destName) != null)
					eL.deleted(destName);
			} else {
				secureDBStorage.put(destName, properties);
				eL.updated(destName); // create or updated
			}
		}
	}

}
