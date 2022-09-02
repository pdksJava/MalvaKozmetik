package org.pdks.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.pdks.entity.Parameter;
import org.pdks.entity.SkinBean;
import org.pdks.entity.Tanim;
import org.pdks.security.action.StartupAction;
import org.pdks.security.entity.MenuItemConstant;
import org.pdks.security.entity.User;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.framework.EntityHome;

import com.pdks.webservice.MailObject;
import com.pdks.webservice.MailStatu;

@Name("parameterHome")
public class ParameterHome extends EntityHome<Parameter> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4296450961710518136L;
	static Logger logger = Logger.getLogger(ParameterHome.class);

	@RequestParameter
	Long parameterId;
	@In(create = true)
	PdksEntityController pdksEntityController;
	@In(required = false)
	User authenticatedUser;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	StartupAction startupAction;
	@In(required = true, create = true)
	Renderer renderer;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;

	private Parameter currentParameter;
	private List<Parameter> parametreList;
	private List<SelectItem> skinList;
	private String skinKodu;
	private Session session;
	private Boolean helpDesk, pasifGoster, admin;

	@Override
	public Object getId() {
		if (parameterId == null) {
			return super.getId();
		} else {
			return parameterId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		admin = authenticatedUser.isAdmin();
		fillParameterList();
	}

	@Transactional
	public String delete() {
		Parameter parameter = getInstance();
		parameter.setChangeDate(new Date());
		parameter.setChangeUser(authenticatedUser);
		session.delete(parameter);
		session.flush();
		session.clear();
		fillParameterList();
		startupAction.fillStartMethod(authenticatedUser, session);
		return "persisted";
	}

	public String guncelle(Parameter parameter) {
		if (parameter == null) {
			parameter = new Parameter();
			parameter.setGuncelle(Boolean.FALSE);
		}

		setInstance(parameter);
		return "";
	}

	@Transactional
	public String save() {
		Parameter parameter = getInstance();
		parameter.setChangeDate(new Date());
		parameter.setChangeUser(authenticatedUser);
		if (parameter.isHelpDeskMi())
			parameter.setGuncelle(Boolean.FALSE);
		session.saveOrUpdate(parameter);
		session.flush();
		session.clear();
		fillParameterList();
		startupAction.fillStartMethod(authenticatedUser, session);// fill all list fillParameter();
		return "persisted";

	}

	public String testMail() {
		try {
			MailObject mailObject = new MailObject();
			mailObject.setSubject("Test Mesajı");
			mailObject.setBody("<p>Test mesajıdır.</p>");
			mailObject.getToList().add(authenticatedUser.getMailPersonel());
			MailStatu mailStatu = null;
			boolean gonderildi = false;
			try {
				mailStatu = ortakIslemler.mailSoapServisGonder(false, mailObject, renderer, "/email/testMail.xhtml", session);
				if (mailStatu != null) {
					gonderildi = mailStatu.isDurum();
					if (!gonderildi)
						PdksUtil.addMessageWarn(mailStatu.getHataMesai());
				}

			} catch (Exception e) {

			}
			if (gonderildi)
				PdksUtil.addMessageInfo("Mesaj Gönderildi.");

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			PdksUtil.addMessageError("Mesaj gönderilmemiştir. " + e.getMessage());

		}
		return "";
	}

	/*
	 * @Override public String remove () {
	 * 
	 * return ""; }
	 */
	public List<Parameter> getParameterList() {

		return parametreList;// this.entityManager.createNamedQuery("select Parameter from Parameter").getResultList();
	}

	public String skinUpdate() {
		boolean guncelle = false;
		for (SelectItem skin : skinList) {
			if (skin.getValue().equals(skinKodu)) {
				SkinBean.setSkinKodu(skinKodu);
				SkinBean.setSkinAdi(skin.getLabel());
				HashMap fields = new HashMap();
				fields.put("name", "skin");
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				Parameter skinParameter = (Parameter) pdksEntityController.getObjectByInnerObject(fields, Parameter.class);
				try {
					if (skinParameter != null) {
						skinParameter.setValue(skin.getLabel());
						skinParameter.setChangeUser(authenticatedUser);
						skinParameter.setChangeDate(new Date());
						session.saveOrUpdate(skinParameter);
						session.flush();
						guncelle = true;
					}
				} catch (Exception e) {
					logger.error(e);
				}

				break;
			}
		}
		if (guncelle)
			fillParameterList();
		return MenuItemConstant.home;
	}

	public String fillParameterList() {

		List<Tanim> skinTanimlar = ortakIslemler.getTanimList(Tanim.TIPI_SKIN, session);
		skinKodu = SkinBean.getSkinKodu();
		if (skinList == null)
			skinList = new ArrayList<SelectItem>();
		else
			skinList.clear();
		for (Tanim skin : skinTanimlar) {
			skinList.add(new SelectItem(skin.getKodu(), skin.getAciklama()));
			if (skinKodu == null && skin.getAciklama().equals(SkinBean.getSkinAdi()))
				skinKodu = skin.getKodu();
		}

		HashMap parametreMap = new HashMap();
		if (!authenticatedUser.isAdmin()) {
			parametreMap.put("guncelle", Boolean.TRUE);
			parametreMap.put("helpDesk", Boolean.FALSE);
		}
		// if (pasifGoster == null || pasifGoster.equals(Boolean.FALSE))
		// parametreMap.put("active", Boolean.TRUE);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);

		helpDesk = false;
		List<Parameter> list = pdksEntityController.getObjectByInnerObjectList(parametreMap, Parameter.class);
		list = PdksUtil.sortListByAlanAdi(list, "id", admin);
		if (authenticatedUser.isAdmin()) {
			List<Parameter> aktifList = new ArrayList<Parameter>(), pasifList = new ArrayList<Parameter>();
			try {
				for (Iterator iterator = list.iterator(); iterator.hasNext();) {
					Parameter parameter = (Parameter) iterator.next();
					boolean helpDeskmi = parameter.isHelpDeskMi();
					if (!helpDesk)
						helpDesk = helpDeskmi;
					if (parameter.getActive().equals(Boolean.FALSE)) {
						pasifList.add(parameter);
						iterator.remove();
					} else if (!helpDeskmi) {
						aktifList.add(parameter);
						iterator.remove();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (!aktifList.isEmpty())
				list.addAll(aktifList);
			if (!pasifList.isEmpty()) {
				if (pasifGoster == null)
					pasifGoster = Boolean.FALSE;
				else if (pasifGoster)
					list.addAll(pasifList);
			}

			aktifList = null;
			pasifList = null;
		}
		setParametreList(list);
		return "";
	}

	public void refreshInstance() {
		if (getInstance().getId() != null)
			session.refresh(getInstance());
	}

	public Parameter getCurrentParameter() {
		if (currentParameter == null)
			currentParameter = new Parameter();
		return currentParameter;
	}

	public void setCurrentParameter(Parameter currentParameter) {
		this.currentParameter = currentParameter;
	}

	public List<Parameter> getParametreList() {
		return parametreList;
	}

	public void setParametreList(List<Parameter> parametreList) {
		this.parametreList = parametreList;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public List<SelectItem> getSkinList() {
		return skinList;
	}

	public void setSkinList(List<SelectItem> skinList) {
		this.skinList = skinList;
	}

	public String getSkinKodu() {
		return skinKodu;
	}

	public void setSkinKodu(String skinKodu) {
		this.skinKodu = skinKodu;
	}

	public Boolean getHelpDesk() {
		return helpDesk;
	}

	public void setHelpDesk(Boolean helpDesk) {
		this.helpDesk = helpDesk;
	}

	public Boolean getPasifGoster() {
		return pasifGoster;
	}

	public void setPasifGoster(Boolean pasifGoster) {
		this.pasifGoster = pasifGoster;
	}

	public Boolean getAdmin() {
		return admin;
	}

	public void setAdmin(Boolean admin) {
		this.admin = admin;
	}

}
