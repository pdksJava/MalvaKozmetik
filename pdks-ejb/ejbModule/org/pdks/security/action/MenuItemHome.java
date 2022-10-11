package org.pdks.security.action;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.MenuItem;
import org.pdks.entity.Tanim;
import org.pdks.security.entity.MenuItemConstant;
import org.pdks.security.entity.User;
import org.pdks.security.entity.UserMenuItemTime;
import org.pdks.session.OrtakIslemler;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;

@Name("menuItemHome")
public class MenuItemHome extends EntityHome<MenuItem> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8362313398220211918L;
	static Logger logger = Logger.getLogger(MenuItemHome.class);

	@RequestParameter
	Long menuItemId;
	@In(create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false)
	User authenticatedUser;
	@In(create = true)
	StartupAction startupAction;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;

	private Session session;

	// SampleDAO sampleDAO=new SampleDAO();

	@Override
	public Object getId() {
		if (menuItemId == null) {
			return super.getId();
		} else {
			return menuItemId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	public String guncelle(MenuItem item) {
		if (item == null)
			item = new MenuItem();
		setInstance(item);
		return "";
	}

	@Transactional
	public String deleteItem() {
		MenuItem item = getInstance();
		session = getSession();

		HashMap fields = new HashMap();
		fields.put("menu.id", item.getId());
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<UserMenuItemTime> list = pdksEntityController.getObjectByInnerObjectList(fields, UserMenuItemTime.class);
		for (UserMenuItemTime userMenuItemTime : list) {
			pdksEntityController.deleteObject(session, entityManager, userMenuItemTime);
		}
		pdksEntityController.deleteObject(session, entityManager, item);

		session.flush();
		session.clear();
		startupAction.fillMenuItemList(session);
		return "";
	}

	@Override
	@Begin(join = true)
	public String update() {
		session = getSession();
		MenuItem item = getInstance();
		Tanim description = item.getDescription();
		if (!description.getTipi().equals(Tanim.TIPI_MENU_BILESENI) || description.getKodu() == null || !description.getKodu().equals(item.getName())) {
			description.setKodu(item.getName());
			description.setTipi(Tanim.TIPI_MENU_BILESENI);
			description.setIslemTarihi(new Date());
			description.setIslemYapan(authenticatedUser);
			session.saveOrUpdate(description);
		}

		session.saveOrUpdate(item);
		session.flush();
		PdksUtil.addMessageInfo("İşlem Başarı ile gerçekleştirildi.");
		startupAction.fillMenuItemList(session);
		return "";

	}

	@Override
	@Begin(join = true)
	public String persist() {
		session = getSession();
		String method = "";
		String adres;
		String cikis = "";
		try {
			method = "get" + instance.getName().substring(0, 1).toUpperCase(Locale.ENGLISH) + instance.getName().substring(1);
			MenuItemConstant menuItemConstant = new MenuItemConstant();
			adres = (String) PdksUtil.getMethodObject(menuItemConstant, method, null);
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			adres = null;
		}

		if (adres == null || !isDefined(instance)) {
			FacesMessage facesMessage = new FacesMessage();
			facesMessage.setSummary("MenuItemConstant.java içerisine tanımlı değil.Lütfen önce tanımlayınız.");
			facesMessage.setDetail("MenuItemConstant.java içerisine tanımlı değil.Lütfen önce tanımlayınız.");
			facesMessage.setSeverity(FacesMessage.SEVERITY_ERROR);
			this.getFacesContext();
			FacesContext.getCurrentInstance().addMessage("", facesMessage);
		} else {
			MenuItem item = getInstance();
			if (item.getTopMenu())
				item.setStatus(Boolean.TRUE);
			else
				item.setStatus(Boolean.FALSE);
			item.getDescription().setDurum(Boolean.TRUE);
			item.getDescription().setTipi(Tanim.TIPI_MENU_BILESENI);
			item.getDescription().setKodu(item.getName());

			session.saveOrUpdate(instance);
			session.flush();
			startupAction.fillMenuItemList(session);
			PdksUtil.addMessageInfo("İşlem Başarı ile gerçekleştirildi.");
			cikis = "ok";
		}

		return cikis;
	}

	private boolean isDefined(MenuItem menuItem) {
		boolean booleanValue = Boolean.FALSE;
		String menuName = "", firstCharMenuName = "";
		menuName = menuItem.getName();
		firstCharMenuName = menuName.substring(0, 1);
		menuName = menuName.substring(1);
		menuName = "get" + firstCharMenuName.toUpperCase() + menuName;
		try {
			@SuppressWarnings("unused")
			String action = (String) PdksUtil.getMethodObject(new MenuItemConstant(), menuName, null);
			booleanValue = Boolean.TRUE;
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			booleanValue = Boolean.FALSE;
		}

		return booleanValue;
	}

	public Session getSession() {
		if (session == null) {
			session = authenticatedUser.getSessionSQL();
			if (session == null)
				session = PdksUtil.getSession(entityManager, false);
		}
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

}
