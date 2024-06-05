package org.pdks.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.mail.internet.InternetAddress;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.pdks.entity.DepartmanMailGrubu;
import org.pdks.entity.Tanim;
import org.pdks.security.entity.User;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;

@Name("departmanMailHome")
public class DepartmanMailHome extends EntityHome<DepartmanMailGrubu> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8254204570949087604L;
	/**
	 * 
	 */
	static Logger logger = Logger.getLogger(DepartmanMailHome.class);
	@RequestParameter
	Long pdksDepartmanId;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	User authenticatedUser;
	@In(required = true, create = true)
	OrtakIslemler ortakIslemler;
	@In(required = false)
	FacesMessages facesMessages;

	DepartmanMailGrubu departmanMail;
	private String mailAdresleri, departmanAciklama;
	private String mailString;
	private List<Tanim> departmanTanimList = new ArrayList<Tanim>();
	private List mailList = new ArrayList();
	private Tanim departmanTanim;
	boolean yeni;
	private Session session;

	@Override
	public Object getId() {
		if (pdksDepartmanId == null) {
			return super.getId();
		} else {
			return pdksDepartmanId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	@Transactional
	public String save() {
		mailDuzenle();
		if (yeni) {

			departmanMail.setOlusturmaTarihi(new Date());
			departmanMail.setOlusturanUser(authenticatedUser);
			entityManager.persist(departmanMail);
		} else {

			departmanMail.setGuncellemeTarihi(new Date());
			departmanMail.setGuncelleyenUser(authenticatedUser);
			entityManager.merge(departmanMail);
		}
		sayfaGirisAction();
		return "";

	}

	private void mailDuzenle() {
		StringBuffer sb = new StringBuffer();

		if (mailList.size() > 0) {
			sb.append(mailList.get(0));
			for (int i = 1; i < mailList.size(); i++) {
				sb.append(";");
				sb.append(mailList.get(i));

			}
			mailAdresleri = sb.toString();
		} else {
			mailAdresleri = "";
		}
		departmanMail.setMailAdress(mailAdresleri);
	}

	public String add() {
		boolean ekle = Boolean.TRUE;

		String eMail = PdksUtil.setTurkishStr(mailString.trim()).toLowerCase(Locale.ENGLISH);
		mailString = "";
		try {
			if (eMail.indexOf("@") < 1)
				throw new Exception(eMail);
			InternetAddress internetAddress = new InternetAddress(eMail);
			eMail = internetAddress.getAddress();
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			facesMessages.add(eMail + " yanlış bir email adresi girdiniz. Lütfen geçerli bir email adresi giriniz!!", "");
			return "";
		}

		for (Iterator iterator = mailList.iterator(); iterator.hasNext();) {
			String type = (String) iterator.next();
			if (type.equals(eMail))
				ekle = Boolean.FALSE;
		}
		if (ekle) {
			mailList.add(eMail);
			facesMessages.add(eMail + " adresi başarılı bir şekilde eklendi!!", "");
		} else {
			facesMessages.add(eMail + " adresi listede mevcut Tekrar kontrol ediniz!!", "");
		}
		return "";
	}

	public String delete(String value) {

		for (Iterator iterator = mailList.iterator(); iterator.hasNext();) {
			String type = (String) iterator.next();
			if (type.equals(value))
				iterator.remove();
		}

		return "";

	}

	public String fillMail() {
		yeni = Boolean.FALSE;

		if (mailList.size() > 0)
			mailList.clear();
		departmanMail = null;
		HashMap parametreMap = new HashMap();
		parametreMap.put("departman.id", departmanTanim.getId());
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		departmanMail = (DepartmanMailGrubu) pdksEntityController.getObjectByInnerObject(parametreMap, DepartmanMailGrubu.class);
		if (departmanMail == null) {
			departmanMail = new DepartmanMailGrubu();
			departmanMail.setDepartman(departmanTanim);
			yeni = Boolean.TRUE;
		} else {
			String mail = departmanMail.getMailAdress();
			mailList = new ArrayList(Arrays.asList(mail.split(";")));
		}
		return "";
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public String sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		departmanAciklama = "Departman";
		HashMap parametreMap = new HashMap();
		parametreMap.put("parentTanim.tipi", Tanim.TIPI_PERSONEL_EK_SAHA);
		parametreMap.put("parentTanim.kodu", "ekSaha1");
		parametreMap.put("durum", Boolean.TRUE);

		parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		departmanTanimList = pdksEntityController.getObjectByInnerObjectList(parametreMap, Tanim.class);
		if (!departmanTanimList.isEmpty()) {
			Tanim tanim = departmanTanimList.get(0).getParentTanim();
			departmanAciklama = tanim.getAciklama();
		}

		return "";
	}

	public Tanim getDepartmanTanim() {
		return departmanTanim;
	}

	public void setDepartmanTanim(Tanim departmanTanim) {
		this.departmanTanim = departmanTanim;
	}

	public DepartmanMailGrubu getDepartmanMail() {
		return departmanMail;
	}

	public void setDepartmanMail(DepartmanMailGrubu departmanMail) {
		this.departmanMail = departmanMail;
	}

	public List<String> getMailList() {
		return mailList;
	}

	public String getMailString() {
		return mailString;
	}

	public void setMailString(String mailString) {
		this.mailString = mailString;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public String getDepartmanAciklama() {
		return departmanAciklama;
	}

	public void setDepartmanAciklama(String departmanAciklama) {
		this.departmanAciklama = departmanAciklama;
	}

	public void setMailList(List mailList) {
		this.mailList = mailList;
	}

	public String getMailAdresleri() {
		return mailAdresleri;
	}

	public void setMailAdresleri(String mailAdresleri) {
		this.mailAdresleri = mailAdresleri;
	}

	public List<Tanim> getDepartmanTanimList() {
		return departmanTanimList;
	}

	public void setDepartmanTanimList(List<Tanim> departmanTanimList) {
		this.departmanTanimList = departmanTanimList;
	}

}
