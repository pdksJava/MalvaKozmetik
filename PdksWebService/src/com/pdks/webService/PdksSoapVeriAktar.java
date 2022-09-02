package com.pdks.webService;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import org.apache.cxf.annotations.WSDLDocumentation;
import org.apache.cxf.interceptor.InInterceptors;
import org.apache.log4j.Logger;

import com.pdks.genel.model.PdksUtil;
import com.pdks.mail.model.MailObject;
import com.pdks.mail.model.MailStatu;

@WebService(targetNamespace = "http://webService.pdks.com/", portName = "PdksSoapVeriAktarPort", serviceName = "PdksSoapVeriAktarService")
@InInterceptors(interceptors = { "com.pdks.webService.WSLoggingInInterceptor" })
public class PdksSoapVeriAktar implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4921115687991106177L;
	public Logger logger = Logger.getLogger(PdksSoapVeriAktar.class);

	@WebResult(name = "mailStatu", targetNamespace = "")
	@WebMethod(operationName = "sendMail", action = "urn:SendMail")
	@RequestWrapper(className = "com.pdks.webService.jaxws.SendMail", localName = "sendMail", targetNamespace = "http://webService.pdks.com/")
	@ResponseWrapper(className = "com.pdks.webService.jaxws.SendMailResponse", localName = "sendMailResponse", targetNamespace = "http://webService.pdks.com/")
	public MailStatu sendMail(@WebParam(name = "mail") MailObject mail) throws Exception {
		MailStatu mailStatu = null;
		if (mail != null) {
			PdksVeriOrtakAktar ortakAktar = new PdksVeriOrtakAktar();
			try {
				mailStatu = ortakAktar.sendMail(mail);
			} catch (Exception e) {
				mailStatu = new MailStatu();
				mailStatu.setHataMesai(e.getMessage());
			}
		}
		if (mailStatu == null)
			mailStatu = new MailStatu();

		if (mailStatu.getHataMesai() == null)
			mailStatu.setHataMesai("Hata oluştu!");
		else {
			String mesaj = mailStatu.getHataMesai();
			if (mesaj.indexOf("\n") >= 0)
				mesaj = PdksUtil.replaceAllManuel(mesaj, "\n", " ");
			mailStatu.setHataMesai(mesaj);
		}

		return mailStatu;
	}

	/**
	 * @param izinList
	 * @return
	 * @throws Exception
	 */

	@WebResult(name = "return", targetNamespace = "")
	@RequestWrapper(className = "com.pdks.webService.jaxws.SendERPIzinler", localName = "sendERPIzinler", targetNamespace = "http://webService.pdks.com/")
	@ResponseWrapper(className = "com.pdks.webService.jaxws.SendERPIzinlerResponse", localName = "sendERPIzinlerResponse", targetNamespace = "http://webService.pdks.com/")
	@WebMethod(operationName = "sendERPIzinler", action = "urn:SendERPIzinler")
	@WSDLDocumentation("PDKS uygulaması kullanımı içindir")
	public List<IzinERP> sendERPIzinler(@WebParam(name = "izin") List<IzinERP> izinList) throws Exception {
		List<IzinERP> list = null;
		PdksVeriOrtakAktar ortakAktar = new PdksVeriOrtakAktar();
		try {
			list = ortakAktar.sendERPIzinler(izinList);
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * @param mesaiIdList
	 * @return
	 * @throws Exception
	 */
	@WebResult(name = "return", targetNamespace = "")
	@RequestWrapper(className = "com.pdks.webService.jaxws.SendFazlaMesaiList", localName = "sendFazlaMesaiList", targetNamespace = "http://webService.pdks.com/")
	@ResponseWrapper(className = "com.pdks.webService.jaxws.SendFazlaMesaiListResponse", localName = "sendFazlaMesaiListResponse", targetNamespace = "http://webService.pdks.com/")
	@WebMethod(operationName = "sendFazlaMesaiList", action = "urn:SendFazlaMesaiList")
	@WSDLDocumentation("PDKS uygulaması kullanımı içindir")
	public List<MesaiPDKS> sendFazlaMesaiList(@WebParam(name = "mesaiId") List<Long> mesaiIdList) throws Exception {
		List<MesaiPDKS> list = null;
		PdksVeriOrtakAktar ortakAktar = new PdksVeriOrtakAktar();
		try {
			list = ortakAktar.sendFazlaMesaiList(mesaiIdList);
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * @param mesaiIdList
	 * @return
	 * @throws Exception
	 */
	@WebResult(name = "return", targetNamespace = "")
	@RequestWrapper(className = "com.pdks.webService.jaxws.GetFazlaMesaiList", localName = "getFazlaMesaiList", targetNamespace = "http://webService.pdks.com/")
	@ResponseWrapper(className = "com.pdks.webService.jaxws.GetFazlaMesaiListResponse", localName = "getFazlaMesaiListResponse", targetNamespace = "http://webService.pdks.com/")
	@WebMethod(operationName = "getFazlaMesaiList", action = "urn:GetFazlaMesaiList")
	@WSDLDocumentation("PDKS uygulaması kullanımı içindir")
	public HashMap<String, Object> getFazlaMesaiList(@WebParam(name = "mesaiId") List<Long> mesaiIdList) throws Exception {
		HashMap<String, Object> map = null;
		PdksVeriOrtakAktar ortakAktar = new PdksVeriOrtakAktar();
		try {
			map = ortakAktar.getFazlaMesaiList(mesaiIdList);
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		return map;
	}

	/**
	 * @param sirketKodu
	 * @param personelNo
	 * @param basTarih
	 * @param bitTarih
	 * @return
	 * @throws Exception
	 */
	@WebResult(name = "return", targetNamespace = "")
	@RequestWrapper(className = "com.pdks.webService.jaxws.GetERPPersonelList", localName = "getERPPersonelList", targetNamespace = "http://webService.pdks.com/")
	@ResponseWrapper(className = "com.pdks.webService.jaxws.GetERPPersonelListResponse", localName = "getERPPersonelListResponse", targetNamespace = "http://webService.pdks.com/")
	@WebMethod(operationName = "getERPPersonelList", action = "urn:GetERPPersonelList")
	@WSDLDocumentation("PDKS uygulaması kullanımı içindir")
	public List<PersonelERP> getERPPersonelList(@WebParam(name = "sirketKodu") String sirketKodu, @WebParam(name = "personelNo") String personelNo, @WebParam(name = "basTarih") String basTarih, @WebParam(name = "bitTarih") String bitTarih) throws Exception {
		List<PersonelERP> list = null;
		PdksVeriOrtakAktar ortakAktar = new PdksVeriOrtakAktar();
		try {
			list = ortakAktar.getERPPersonelList(sirketKodu, personelNo, basTarih, bitTarih);
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * @param sirketKodu
	 * @param yil
	 * @param ay
	 * @param donemKapat
	 * @return
	 * @throws Exception
	 */
	@WebResult(name = "return", targetNamespace = "")
	@RequestWrapper(className = "com.pdks.webService.jaxws.GetMesaiPDKS", localName = "getMesaiPDKS", targetNamespace = "http://webService.pdks.com/")
	@ResponseWrapper(className = "com.pdks.webService.jaxws.GetMesaiPDKSResponse", localName = "getMesaiPDKSResponse", targetNamespace = "http://webService.pdks.com/")
	@WebMethod(operationName = "getMesaiPDKS", action = "getMesaiPDKS")
	@WSDLDocumentation("Bordro uygulaması PDKS uygulamasındaki fazla mesai bilgileri çeker  ")
	public List<MesaiPDKS> getMesaiPDKS(@WebParam(name = "sirketKodu") String sirketKodu, @WebParam(name = "yil") Integer yil, @WebParam(name = "ay") Integer ay, @WebParam(name = "donemKapat") Boolean donemKapat) throws Exception {
		List<MesaiPDKS> list = null;
		if (yil != null && ay != null) {
			PdksVeriOrtakAktar ortakAktar = new PdksVeriOrtakAktar();
			try {
				if (yil >= 2020 && ay > 0 && ay < 13)
					list = ortakAktar.getMesaiPDKS(sirketKodu, yil, ay, donemKapat);
			} catch (Exception e) {

				logger.error(e);
				e.printStackTrace();
			}

		}
		return list;
	}

	/**
	 * @param izinHakedisList
	 * @return
	 * @throws Exception
	 */
	@WebResult(name = "return", targetNamespace = "")
	@WebMethod(operationName = "saveIzinHakedisler", action = "urn:SaveIzinHakedisler")
	@RequestWrapper(className = "com.pdks.webService.jaxws.SaveIzinHakedisler", localName = "saveIzinHakedisler", targetNamespace = "http://webService.pdks.com/")
	@ResponseWrapper(className = "com.pdks.webService.jaxws.SaveIzinHakedislerResponse", localName = "saveIzinHakedislerResponse", targetNamespace = "http://webService.pdks.com/")
	@WSDLDocumentation("Bordro uygulamasından PDKS uygulamasına senelik izin hakediş aktarımında kullanır. (Sure Birimi : Gun='1'/Saat='2', Zaman format : yyyy-MM-dd HH:mm) ")
	public List<IzinHakedis> saveIzinHakedisler(@WebParam(name = "izinHakedis") List<IzinHakedis> izinHakedisList) throws Exception {
		if (izinHakedisList != null && !izinHakedisList.isEmpty()) {
			PdksVeriOrtakAktar ortakAktar = new PdksVeriOrtakAktar();
			try {
				ortakAktar.saveHakedisIzinler(izinHakedisList);
			} catch (Exception e) {

				logger.error(e);
				e.printStackTrace();
			}

		}
		return izinHakedisList;
	}

	@WebMethod(operationName = "getIzinHakedisler", action = "urn:GetIzinHakedisler")
	@RequestWrapper(className = "com.pdks.webService.jaxws.GetIzinHakedisler", localName = "getIzinHakedisler", targetNamespace = "http://webService.pdks.com/")
	@ResponseWrapper(className = "com.pdks.webService.jaxws.GetIzinHakedislerResponse", localName = "getIzinHakedislerResponse", targetNamespace = "http://webService.pdks.com/")
	@WSDLDocumentation("PDKS uygulamasında Bordro uygulamasından okunan senelik izin hakedişler için kullanır. (Sure Birimi : Gun='1'/Saat='2', Zaman format : yyyy-MM-dd HH:mm) ")
	public List<IzinHakedis> getIzinHakedisler(@WebParam(name = "personeller") List<String> personeller) throws Exception {
		List<IzinHakedis> izinHakedisList = null;
		if (personeller != null && !personeller.isEmpty()) {
			PdksVeriOrtakAktar ortakAktar = new PdksVeriOrtakAktar();
			try {
				izinHakedisList = ortakAktar.getIzinHakedisler(personeller);
			} catch (Exception e) {

				logger.error(e);
				e.printStackTrace();
			}

		}
		return izinHakedisList;
	}

	/**
	 * @param izinList
	 * @return
	 * @throws Exception
	 */
	@WebResult(name = "return", targetNamespace = "")
	@RequestWrapper(className = "com.pdks.webService.jaxws.SaveIzinler", localName = "saveIzinler", targetNamespace = "http://webService.pdks.com/")
	@ResponseWrapper(className = "com.pdks.webService.jaxws.SaveIzinlerResponse", localName = "saveIzinlerResponse", targetNamespace = "http://webService.pdks.com/")
	@WebMethod(operationName = "saveIzinler", action = "urn:SaveIzinler")
	@WSDLDocumentation("Bordro uygulamasından PDKS uygulamasına izinleri aktarımında kullanır. (Sure Birimi : Gun='1'/Saat='2', Zaman format : yyyy-MM-dd HH:mm) ")
	public List<IzinERP> saveIzinler(@WebParam(name = "izin") List<IzinERP> izinList) throws Exception {
		if (izinList != null && !izinList.isEmpty()) {
			PdksVeriOrtakAktar ortakAktar = new PdksVeriOrtakAktar();
			try {
				ortakAktar.saveIzinler(izinList);
			} catch (Exception e) {

				logger.error(e);
				e.printStackTrace();
			}

		}
		return izinList;
	}

	/**
	 * @param personelList
	 * @return
	 * @throws Exception
	 */
	@WebResult(name = "return", targetNamespace = "")
	@RequestWrapper(className = "com.pdks.webService.jaxws.SavePersoneller", localName = "savePersoneller", targetNamespace = "http://webService.pdks.com/")
	@ResponseWrapper(className = "com.pdks.webService.jaxws.SavePersonellerResponse", localName = "savePersonellerResponse", targetNamespace = "http://webService.pdks.com/")
	@WebMethod(operationName = "savePersoneller", action = "urn:SavePersoneller")
	@WSDLDocumentation("Bordro uygulamasından PDKS uygulamasına personel verisini aktarımında kullanır (Tarih format : yyyy-MM-dd)")
	public List<PersonelERP> savePersoneller(@WebParam(name = "personel") List<PersonelERP> personelList) throws Exception {
		if (personelList != null && !personelList.isEmpty()) {
			PdksVeriOrtakAktar ortakAktar = new PdksVeriOrtakAktar();
			try {
				ortakAktar.savePersoneller(personelList);
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}

		}
		return personelList;
	}
}
