package com.pdks.webservice;

import java.util.HashMap;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java element interface generated in the com.pdks.webservice package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the Java representation for XML content. The Java representation of XML content can consist of schema derived interfaces and classes representing the binding of schema type definitions, element declarations and model
 * groups. Factory methods for each of these are provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

	private final static QName _SavePersoneller_QNAME = new QName("http://webService.pdks.com/", "savePersoneller");
	private final static QName _GetERPPersonelList_QNAME = new QName("http://webService.pdks.com/", "getERPPersonelList");
	private final static QName _GetIzinHakedisler_QNAME = new QName("http://webService.pdks.com/", "getIzinHakedisler");
	private final static QName _GetERPPersonelListResponse_QNAME = new QName("http://webService.pdks.com/", "getERPPersonelListResponse");
	private final static QName _SaveIzinler_QNAME = new QName("http://webService.pdks.com/", "saveIzinler");
	private final static QName _SendERPIzinlerResponse_QNAME = new QName("http://webService.pdks.com/", "sendERPIzinlerResponse");
	private final static QName _SaveIzinHakedisler_QNAME = new QName("http://webService.pdks.com/", "saveIzinHakedisler");
	private final static QName _SendFazlaMesaiListResponse_QNAME = new QName("http://webService.pdks.com/", "sendFazlaMesaiListResponse");
	private final static QName _SavePersonellerResponse_QNAME = new QName("http://webService.pdks.com/", "savePersonellerResponse");
	private final static QName _SendERPIzinler_QNAME = new QName("http://webService.pdks.com/", "sendERPIzinler");
	private final static QName _SendMail_QNAME = new QName("http://webService.pdks.com/", "sendMail");
	private final static QName _SaveIzinHakedislerResponse_QNAME = new QName("http://webService.pdks.com/", "saveIzinHakedislerResponse");
	private final static QName _GetFazlaMesaiList_QNAME = new QName("http://webService.pdks.com/", "getFazlaMesaiList");
	private final static QName _Exception_QNAME = new QName("http://webService.pdks.com/", "Exception");
	private final static QName _SendFazlaMesaiList_QNAME = new QName("http://webService.pdks.com/", "sendFazlaMesaiList");
	private final static QName _SaveIzinlerResponse_QNAME = new QName("http://webService.pdks.com/", "saveIzinlerResponse");
	private final static QName _GetIzinHakedislerResponse_QNAME = new QName("http://webService.pdks.com/", "getIzinHakedislerResponse");
	private final static QName _GetMesaiPDKSResponse_QNAME = new QName("http://webService.pdks.com/", "getMesaiPDKSResponse");
	private final static QName _GetMesaiPDKS_QNAME = new QName("http://webService.pdks.com/", "getMesaiPDKS");
	private final static QName _GetFazlaMesaiListResponse_QNAME = new QName("http://webService.pdks.com/", "getFazlaMesaiListResponse");
	private final static QName _SendMailResponse_QNAME = new QName("http://webService.pdks.com/", "sendMailResponse");
	private final static QName _PersonelERPYonetici2PerNo_QNAME = new QName("", "yonetici2PerNo");
	private final static QName _PersonelERPGrubaGirisTarihi_QNAME = new QName("", "grubaGirisTarihi");

	/**
	 * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.pdks.webservice
	 * 
	 */
	public ObjectFactory() {
	}

	/**
	 * Create an instance of {@link GetFazlaMesaiListResponse }
	 * 
	 */
	public GetFazlaMesaiListResponse createGetFazlaMesaiListResponse() {
		return new GetFazlaMesaiListResponse();
	}

	/**
	 * Create an instance of {@link GetFazlaMesaiListResponse.Return }
	 * 
	 */
	public GetFazlaMesaiListResponse.Return createGetFazlaMesaiListResponseReturn() {
		return new GetFazlaMesaiListResponse.Return();
	}

	/**
	 * Create an instance of {@link GetIzinHakedislerResponse }
	 * 
	 */
	public GetIzinHakedislerResponse createGetIzinHakedislerResponse() {
		return new GetIzinHakedislerResponse();
	}

	/**
	 * Create an instance of {@link GetMesaiPDKSResponse }
	 * 
	 */
	public GetMesaiPDKSResponse createGetMesaiPDKSResponse() {
		return new GetMesaiPDKSResponse();
	}

	/**
	 * Create an instance of {@link Exception }
	 * 
	 */
	public Exception createException() {
		return new Exception();
	}

	/**
	 * Create an instance of {@link SendFazlaMesaiList }
	 * 
	 */
	public SendFazlaMesaiList createSendFazlaMesaiList() {
		return new SendFazlaMesaiList();
	}

	/**
	 * Create an instance of {@link SaveIzinlerResponse }
	 * 
	 */
	public SaveIzinlerResponse createSaveIzinlerResponse() {
		return new SaveIzinlerResponse();
	}

	/**
	 * Create an instance of {@link SendMailResponse }
	 * 
	 */
	public SendMailResponse createSendMailResponse() {
		return new SendMailResponse();
	}

	/**
	 * Create an instance of {@link GetMesaiPDKS }
	 * 
	 */
	public GetMesaiPDKS createGetMesaiPDKS() {
		return new GetMesaiPDKS();
	}

	/**
	 * Create an instance of {@link SavePersonellerResponse }
	 * 
	 */
	public SavePersonellerResponse createSavePersonellerResponse() {
		return new SavePersonellerResponse();
	}

	/**
	 * Create an instance of {@link SendFazlaMesaiListResponse }
	 * 
	 */
	public SendFazlaMesaiListResponse createSendFazlaMesaiListResponse() {
		return new SendFazlaMesaiListResponse();
	}

	/**
	 * Create an instance of {@link GetFazlaMesaiList }
	 * 
	 */
	public GetFazlaMesaiList createGetFazlaMesaiList() {
		return new GetFazlaMesaiList();
	}

	/**
	 * Create an instance of {@link SaveIzinHakedislerResponse }
	 * 
	 */
	public SaveIzinHakedislerResponse createSaveIzinHakedislerResponse() {
		return new SaveIzinHakedislerResponse();
	}

	/**
	 * Create an instance of {@link SendMail }
	 * 
	 */
	public SendMail createSendMail() {
		return new SendMail();
	}

	/**
	 * Create an instance of {@link SendERPIzinler }
	 * 
	 */
	public SendERPIzinler createSendERPIzinler() {
		return new SendERPIzinler();
	}

	/**
	 * Create an instance of {@link SaveIzinler }
	 * 
	 */
	public SaveIzinler createSaveIzinler() {
		return new SaveIzinler();
	}

	/**
	 * Create an instance of {@link GetERPPersonelListResponse }
	 * 
	 */
	public GetERPPersonelListResponse createGetERPPersonelListResponse() {
		return new GetERPPersonelListResponse();
	}

	/**
	 * Create an instance of {@link GetIzinHakedisler }
	 * 
	 */
	public GetIzinHakedisler createGetIzinHakedisler() {
		return new GetIzinHakedisler();
	}

	/**
	 * Create an instance of {@link SaveIzinHakedisler }
	 * 
	 */
	public SaveIzinHakedisler createSaveIzinHakedisler() {
		return new SaveIzinHakedisler();
	}

	/**
	 * Create an instance of {@link SendERPIzinlerResponse }
	 * 
	 */
	public SendERPIzinlerResponse createSendERPIzinlerResponse() {
		return new SendERPIzinlerResponse();
	}

	/**
	 * Create an instance of {@link GetERPPersonelList }
	 * 
	 */
	public GetERPPersonelList createGetERPPersonelList() {
		return new GetERPPersonelList();
	}

	/**
	 * Create an instance of {@link SavePersoneller }
	 * 
	 */
	public SavePersoneller createSavePersoneller() {
		return new SavePersoneller();
	}

	/**
	 * Create an instance of {@link HashMap }
	 * 
	 */
	public HashMap createHashMap() {
		return new HashMap();
	}

	/**
	 * Create an instance of {@link MailStatu }
	 * 
	 */
	public MailStatu createMailStatu() {
		return new MailStatu();
	}

	/**
	 * Create an instance of {@link IzinHakedis }
	 * 
	 */
	public IzinHakedis createIzinHakedis() {
		return new IzinHakedis();
	}

	/**
	 * Create an instance of {@link MesaiPDKS }
	 * 
	 */
	public MesaiPDKS createMesaiPDKS() {
		return new MesaiPDKS();
	}

	/**
	 * Create an instance of {@link MailPersonel }
	 * 
	 */
	public MailPersonel createMailPersonel() {
		return new MailPersonel();
	}

	/**
	 * Create an instance of {@link MailObject }
	 * 
	 */
	public MailObject createMailObject() {
		return new MailObject();
	}

	/**
	 * Create an instance of {@link MailFile }
	 * 
	 */
	public MailFile createMailFile() {
		return new MailFile();
	}

	/**
	 * Create an instance of {@link IzinERP }
	 * 
	 */
	public IzinERP createIzinERP() {
		return new IzinERP();
	}

	/**
	 * Create an instance of {@link IzinHakedisDetay }
	 * 
	 */
	public IzinHakedisDetay createIzinHakedisDetay() {
		return new IzinHakedisDetay();
	}

	/**
	 * Create an instance of {@link PersonelERP }
	 * 
	 */
	public PersonelERP createPersonelERP() {
		return new PersonelERP();
	}

	/**
	 * Create an instance of {@link GetFazlaMesaiListResponse.Return.Entry }
	 * 
	 */
	public GetFazlaMesaiListResponse.Return.Entry createGetFazlaMesaiListResponseReturnEntry() {
		return new GetFazlaMesaiListResponse.Return.Entry();
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link SavePersoneller }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://webService.pdks.com/", name = "savePersoneller")
	public JAXBElement<SavePersoneller> createSavePersoneller(SavePersoneller value) {
		return new JAXBElement<SavePersoneller>(_SavePersoneller_QNAME, SavePersoneller.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link GetERPPersonelList }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://webService.pdks.com/", name = "getERPPersonelList")
	public JAXBElement<GetERPPersonelList> createGetERPPersonelList(GetERPPersonelList value) {
		return new JAXBElement<GetERPPersonelList>(_GetERPPersonelList_QNAME, GetERPPersonelList.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link GetIzinHakedisler }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://webService.pdks.com/", name = "getIzinHakedisler")
	public JAXBElement<GetIzinHakedisler> createGetIzinHakedisler(GetIzinHakedisler value) {
		return new JAXBElement<GetIzinHakedisler>(_GetIzinHakedisler_QNAME, GetIzinHakedisler.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link GetERPPersonelListResponse }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://webService.pdks.com/", name = "getERPPersonelListResponse")
	public JAXBElement<GetERPPersonelListResponse> createGetERPPersonelListResponse(GetERPPersonelListResponse value) {
		return new JAXBElement<GetERPPersonelListResponse>(_GetERPPersonelListResponse_QNAME, GetERPPersonelListResponse.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link SaveIzinler }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://webService.pdks.com/", name = "saveIzinler")
	public JAXBElement<SaveIzinler> createSaveIzinler(SaveIzinler value) {
		return new JAXBElement<SaveIzinler>(_SaveIzinler_QNAME, SaveIzinler.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link SendERPIzinlerResponse }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://webService.pdks.com/", name = "sendERPIzinlerResponse")
	public JAXBElement<SendERPIzinlerResponse> createSendERPIzinlerResponse(SendERPIzinlerResponse value) {
		return new JAXBElement<SendERPIzinlerResponse>(_SendERPIzinlerResponse_QNAME, SendERPIzinlerResponse.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link SaveIzinHakedisler }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://webService.pdks.com/", name = "saveIzinHakedisler")
	public JAXBElement<SaveIzinHakedisler> createSaveIzinHakedisler(SaveIzinHakedisler value) {
		return new JAXBElement<SaveIzinHakedisler>(_SaveIzinHakedisler_QNAME, SaveIzinHakedisler.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link SendFazlaMesaiListResponse }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://webService.pdks.com/", name = "sendFazlaMesaiListResponse")
	public JAXBElement<SendFazlaMesaiListResponse> createSendFazlaMesaiListResponse(SendFazlaMesaiListResponse value) {
		return new JAXBElement<SendFazlaMesaiListResponse>(_SendFazlaMesaiListResponse_QNAME, SendFazlaMesaiListResponse.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link SavePersonellerResponse }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://webService.pdks.com/", name = "savePersonellerResponse")
	public JAXBElement<SavePersonellerResponse> createSavePersonellerResponse(SavePersonellerResponse value) {
		return new JAXBElement<SavePersonellerResponse>(_SavePersonellerResponse_QNAME, SavePersonellerResponse.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link SendERPIzinler }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://webService.pdks.com/", name = "sendERPIzinler")
	public JAXBElement<SendERPIzinler> createSendERPIzinler(SendERPIzinler value) {
		return new JAXBElement<SendERPIzinler>(_SendERPIzinler_QNAME, SendERPIzinler.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link SendMail }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://webService.pdks.com/", name = "sendMail")
	public JAXBElement<SendMail> createSendMail(SendMail value) {
		return new JAXBElement<SendMail>(_SendMail_QNAME, SendMail.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link SaveIzinHakedislerResponse }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://webService.pdks.com/", name = "saveIzinHakedislerResponse")
	public JAXBElement<SaveIzinHakedislerResponse> createSaveIzinHakedislerResponse(SaveIzinHakedislerResponse value) {
		return new JAXBElement<SaveIzinHakedislerResponse>(_SaveIzinHakedislerResponse_QNAME, SaveIzinHakedislerResponse.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link GetFazlaMesaiList }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://webService.pdks.com/", name = "getFazlaMesaiList")
	public JAXBElement<GetFazlaMesaiList> createGetFazlaMesaiList(GetFazlaMesaiList value) {
		return new JAXBElement<GetFazlaMesaiList>(_GetFazlaMesaiList_QNAME, GetFazlaMesaiList.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link Exception }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://webService.pdks.com/", name = "Exception")
	public JAXBElement<Exception> createException(Exception value) {
		return new JAXBElement<Exception>(_Exception_QNAME, Exception.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link SendFazlaMesaiList }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://webService.pdks.com/", name = "sendFazlaMesaiList")
	public JAXBElement<SendFazlaMesaiList> createSendFazlaMesaiList(SendFazlaMesaiList value) {
		return new JAXBElement<SendFazlaMesaiList>(_SendFazlaMesaiList_QNAME, SendFazlaMesaiList.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link SaveIzinlerResponse }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://webService.pdks.com/", name = "saveIzinlerResponse")
	public JAXBElement<SaveIzinlerResponse> createSaveIzinlerResponse(SaveIzinlerResponse value) {
		return new JAXBElement<SaveIzinlerResponse>(_SaveIzinlerResponse_QNAME, SaveIzinlerResponse.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link GetIzinHakedislerResponse }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://webService.pdks.com/", name = "getIzinHakedislerResponse")
	public JAXBElement<GetIzinHakedislerResponse> createGetIzinHakedislerResponse(GetIzinHakedislerResponse value) {
		return new JAXBElement<GetIzinHakedislerResponse>(_GetIzinHakedislerResponse_QNAME, GetIzinHakedislerResponse.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link GetMesaiPDKSResponse }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://webService.pdks.com/", name = "getMesaiPDKSResponse")
	public JAXBElement<GetMesaiPDKSResponse> createGetMesaiPDKSResponse(GetMesaiPDKSResponse value) {
		return new JAXBElement<GetMesaiPDKSResponse>(_GetMesaiPDKSResponse_QNAME, GetMesaiPDKSResponse.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link GetMesaiPDKS }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://webService.pdks.com/", name = "getMesaiPDKS")
	public JAXBElement<GetMesaiPDKS> createGetMesaiPDKS(GetMesaiPDKS value) {
		return new JAXBElement<GetMesaiPDKS>(_GetMesaiPDKS_QNAME, GetMesaiPDKS.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link GetFazlaMesaiListResponse }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://webService.pdks.com/", name = "getFazlaMesaiListResponse")
	public JAXBElement<GetFazlaMesaiListResponse> createGetFazlaMesaiListResponse(GetFazlaMesaiListResponse value) {
		return new JAXBElement<GetFazlaMesaiListResponse>(_GetFazlaMesaiListResponse_QNAME, GetFazlaMesaiListResponse.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link SendMailResponse }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://webService.pdks.com/", name = "sendMailResponse")
	public JAXBElement<SendMailResponse> createSendMailResponse(SendMailResponse value) {
		return new JAXBElement<SendMailResponse>(_SendMailResponse_QNAME, SendMailResponse.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "", name = "yonetici2PerNo", scope = PersonelERP.class)
	public JAXBElement<String> createPersonelERPYonetici2PerNo(String value) {
		return new JAXBElement<String>(_PersonelERPYonetici2PerNo_QNAME, String.class, PersonelERP.class, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "", name = "grubaGirisTarihi", scope = PersonelERP.class)
	public JAXBElement<String> createPersonelERPGrubaGirisTarihi(String value) {
		return new JAXBElement<String>(_PersonelERPGrubaGirisTarihi_QNAME, String.class, PersonelERP.class, value);
	}

}
