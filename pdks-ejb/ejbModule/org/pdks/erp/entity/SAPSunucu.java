package org.pdks.erp.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.pdks.entity.BasePDKSObject;

/**
 *  
 * 
 */
@Entity(name = "SAP_SERVER")
public class SAPSunucu  extends BasePDKSObject  implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1006000544009889053L;
	public final static int SUNUCU_TIPI_CANLI = 0;
	public final static int SUNUCU_TIPI_TEST = 1;
	public final static int SUNUCU_TIPI_QAY = 2;
	public final static int SUNUCU_TIPI_BW = 3;
	public final static int SUNUCU_TIPI_ODM = 4;
	public final static int SUNUCU_TIPI_BW_TEST = 5;
	public final static int DURUM_AKTIF = 1;
	public final static int DURUM_PASIF = 0;

	public final static String SAP_MESSAGE_SUCCESS = "S";
	public final static String SAP_MESSAGE_INFO = "I";
	public final static String SAP_MESSAGE_WARNING = "W";
	public final static String SAP_MESSAGE_ABORT = "A";
	public final static String SAP_MESSAGE_ERROR = "R";

	private Integer  sunucuTipi = SUNUCU_TIPI_CANLI, durum = DURUM_AKTIF;
	private String hostName, client, systemNumber, destinationName, dil = "TR", msHostName;
	private Boolean aktif = Boolean.TRUE;

 

	@Column(name = "HOSTNAME")
	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	@Column(name = "CLIENT")
	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	@Column(name = "SYSNUMBER")
	public String getSystemNumber() {
		return systemNumber;
	}

	public void setSystemNumber(String systemNumber) {
		this.systemNumber = systemNumber;
	}

	@Column(name = "DESTINATIONNAME")
	public String getDestinationName() {
		return destinationName;
	}

	public void setDestinationName(String destinationName) {
		this.destinationName = destinationName;
	}

	@Column(name = "SYSTEM_DIL")
	public String getDil() {
		return dil;
	}

	public void setDil(String dil) {
		this.dil = dil;
	}

	@Column(name = "MESSAGESERVER")
	public String getMsHostName() {
		return msHostName;
	}

	public void setMsHostName(String msHostName) {
		this.msHostName = msHostName;
	}

	@Column(name = "SUNUCUTIPI")
	public Integer getSunucuTipi() {
		return sunucuTipi;
	}

	public void setSunucuTipi(Integer sunucuTipi) {
		this.sunucuTipi = sunucuTipi;
	}

	@Column(name = "DURUM", insertable = false, updatable = false)
	public Integer getDurum() {
		return durum;
	}

	public void setDurum(Integer durum) {
		this.durum = durum;
	}

	@Column(name = "DURUM")
	public Boolean getAktif() {
		return aktif;
	}

	public void setAktif(Boolean aktif) {
		this.aktif = aktif;
	}

	@Transient
	public static String getSunucuTipiAciklama(int tip) {
		String aciklama = "";

		switch (tip) {
		case SUNUCU_TIPI_CANLI:
			aciklama = "HR Canlı";
			break;
		case SUNUCU_TIPI_TEST:
			aciklama = "HR Test";
			break;
		case SUNUCU_TIPI_QAY:
			aciklama = "HR QAY";
			break;
		case SUNUCU_TIPI_BW:
			aciklama = "HR BW";
			break;
		case SUNUCU_TIPI_ODM:
			aciklama = "HR ODM";
			break;
		case SUNUCU_TIPI_BW_TEST:
			aciklama = "HR BW Test";
			break;
		default:
			break;
		}
		return aciklama;
	}

	@Transient
	public String getSunucuTipiAciklama() {
		return getSunucuTipiAciklama(sunucuTipi != null ? sunucuTipi : -1);
	}

	public void entityRefresh() {
		// TODO entityRefresh
		
	}
}
