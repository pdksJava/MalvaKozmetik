package org.pdks.validation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.apache.log4j.Logger;
import org.pdks.entity.Tatil;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaSablonu;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;
import org.pdks.session.TatilHome;
import org.pdks.session.VardiyaSablonuHome;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;

@Name(value = "pdksValidator")
// @BypassInterceptors
public class PDKSValidator implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8116082380572659430L;

	static Logger logger = Logger.getLogger(PDKSValidator.class);

	@In(required = false)
	FacesMessages facesMessages;
	@In(required = false, create = true)
	PdksEntityController pdksEntityController;

	private ValidatorException setMesajYaz(List<String> mesajList) {

		FacesContext context = FacesContext.getCurrentInstance();
		for (String mesaj : mesajList)
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, mesaj, mesaj));
		return new ValidatorException(new FacesMessage(""));
	}

	private void findComponentValue(UIComponent component, HashMap valueMap, HashMap<String, String> map) {
		if (component != null && !map.isEmpty()) {
			for (UIComponent componentDetay : component.getChildren()) {

				if (map.containsKey(componentDetay.getId())) {
					valueMap.put(componentDetay.getId(), componentDetay);
					map.remove(componentDetay.getId());
				} else
					findComponentValue(componentDetay, valueMap, map);
				if (map.isEmpty())
					break;
			}
		}
	}

	private HashMap<String, String> getListStringValue(List<String> list, String deger) {
		if (list == null && deger != null) {
			list = new ArrayList<String>();
			list.add(deger);
		}
		Map<String, String> requestMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		Map<String, String> valueMap = new HashMap<String, String>();
		for (Iterator<String> iterator = requestMap.keySet().iterator(); iterator.hasNext();) {
			String string = iterator.next();
			StringTokenizer st = new StringTokenizer(string, ":");
			String key = "";
			while (st.hasMoreTokens())
				key = st.nextToken();
			valueMap.put(key, requestMap.get(string));
		}
		HashMap<String, String> map = new HashMap<String, String>();
		for (String key : list)
			if (valueMap.containsKey(key))
				map.put(key, valueMap.get(key));

		return map;
	}

	private HashMap getListValue(List<String> list, String deger, UIComponent component) {
		if (list == null && deger != null) {
			list = new ArrayList<String>();
			list.add(deger);
		}
		HashMap valueMap = new HashMap();
		HashMap<String, String> map = new HashMap<String, String>();
		if (list != null)
			for (String key : list)
				map.put(key, key);
		FacesContext context = FacesContext.getCurrentInstance();
		String formId = (String) component.getAttributes().get("form");
		if (formId != null) {
			UIComponent form = (UIComponent) context.getViewRoot().findComponent(formId);
			findComponentValue(form, valueMap, map);

		}

		return valueMap;
	}

	public Object getFormValue(FacesContext context, UIComponent component, String objectId) throws Exception {
		Object deger = null;

		String clientId = (String) component.getAttributes().get(objectId);
		// Find the actual JSF component for the client ID.
		UIInput objectInput = (UIInput) context.getViewRoot().findComponent(clientId);
		if (objectInput != null)
			deger = objectInput.getValue();
		else {
			UIOutput objectOutput = (UIOutput) context.getViewRoot().findComponent(clientId);
			// Get its value, the entered objectId of the first field.
			if (objectOutput != null)
				deger = objectOutput.getValue();
		}
		if (deger == null)
			throw new Exception(objectId + " = null");
		return deger;
	}

	public void denklestirmeValidate(FacesContext context, UIComponent component, Object value) throws ValidatorException {

	}

	public void izinOnayValidate(FacesContext context, UIComponent component, Object value) throws ValidatorException {

	}

	public void izinTipiValidate(FacesContext context, UIComponent component, Object value) throws ValidatorException {

	}

	public void izinKagidiValidate(FacesContext context, UIComponent component, Object value) throws ValidatorException {

	}

	public void izinSapAktarimValidate(FacesContext context, UIComponent component, Object value) throws ValidatorException {

	}

	public void tatilValidate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		Tatil pdksTatilForm = ((TatilHome) Component.getInstance("tatilHome")).getInstance();
		boolean durum = (Boolean) value;
		boolean periyodik = pdksTatilForm.getId() != null ? pdksTatilForm.isPeriyodik() : false;
		HashMap<String, String> listValue = null;
		if (pdksTatilForm.getId() == null) {
			listValue = getListStringValue(null, "tatilTipiKodu");
			String tatilTipiKodu = listValue.get("tatilTipiKodu");
			periyodik = tatilTipiKodu.equals(Tatil.TATIL_TIPI_PERIYODIK);

		}
		List<String> paramList = new ArrayList<String>();
		if (periyodik) {
			paramList.add("basAy");
			paramList.add("basGun");
			paramList.add("bitisAy");
			paramList.add("bitisGun");
		} else {
			paramList.add("basTarihInputDate");
			paramList.add("bitisTarihInputDate");

		}
		listValue = getListStringValue(paramList, null);
		Tatil pdksTatil = new Tatil();
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		List<String> buffer = new ArrayList<String>();

		if (periyodik) {
			try {
				cal1.set(2999, Integer.parseInt(listValue.get("basAy")), Integer.parseInt(listValue.get("basGun")), 0, 0);
				pdksTatil.setBasTarih(cal1.getTime());
				cal2.set(2999, Integer.parseInt(listValue.get("bitisAy")), Integer.parseInt(listValue.get("bitisGun")), 0, 0);
				pdksTatil.setBitTarih(cal2.getTime());
				pdksTatil.setBasTarih(PdksUtil.setTarih(pdksTatil.getBasTarih(), Calendar.SECOND, 0));

			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());
				buffer.add("Tarihleri seçiniz");
				pdksTatil.setBitTarih(null);
				pdksTatil.setBasTarih(null);
			}
		} else {
			pdksTatil.setBasTarih(PdksUtil.convertToJavaDate(listValue.get("basTarihInputDate"), "dd/MM/yyyy"));
			pdksTatil.setBitTarih(PdksUtil.convertToJavaDate(listValue.get("bitisTarihInputDate"), "dd/MM/yyyy"));
		}

		if (buffer.isEmpty()) {
			if (PdksUtil.tarihKarsilastirNumeric(pdksTatil.getBasTarih(), pdksTatil.getBitTarih()) == 1)
				buffer.add("Başlangıç tarihi bitiş tarihinden büyük olamaz");
			else if (PdksUtil.tarihKarsilastirNumeric(pdksTatil.getBasTarih(), Calendar.getInstance().getTime()) != 1)
				buffer.add("Geçmişe ait tatil giremezsiniz");
			if (durum) {
				HashMap map = new HashMap();
				map.put("basTarih<=", pdksTatil.getBitTarih());
				map.put("bitisTarih>=", pdksTatil.getBasTarih());
				map.put("durum=", Boolean.TRUE);
				if (pdksTatil.getId() != null)
					map.put("id<>", pdksTatil.getId());
				Tatil tatil = (Tatil) pdksEntityController.getObjectByInnerObjectInLogic(map, Tatil.class);
				if (tatil != null)
					buffer.add(tatil.getAd() + " tatili ile çakışmaktadır");

			}
		}
		if (!buffer.isEmpty())
			throw setMesajYaz(buffer);

	}

	public void vardiyaSablonValidate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		VardiyaSablonu pdksVardiyaSablon = ((VardiyaSablonuHome) Component.getInstance("vardiyaSablonuHome")).getInstance();
		pdksVardiyaSablon.vardiyaBul();
		int vardiyaGunAdet = 0, haftaTatilGunAdet = 0;
		for (Iterator<Vardiya> iterator = pdksVardiyaSablon.getVardiyaList().iterator(); iterator.hasNext();) {
			Vardiya pdksVardiya = iterator.next();
			if (pdksVardiya.isCalisma())
				++vardiyaGunAdet;
			else if (pdksVardiya.isHaftaTatil())
				++haftaTatilGunAdet;
		}
		ArrayList<String> buffer = new ArrayList<String>();
		if (vardiyaGunAdet != pdksVardiyaSablon.getCalismaGunSayisi())
			buffer.add("Çalışma gün sayısı " + pdksVardiyaSablon.getCalismaGunSayisi() + " adet seçiniz");

		if (haftaTatilGunAdet != 1)
			buffer.add("Hafta tatili sayısı toplam 1 gün seçiniz");

		if (!buffer.isEmpty())
			throw setMesajYaz(buffer);

	}

	public void departmanValidate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		int cocukYasUstSiniri = 0;
		int yasliYasAltSiniri = (Integer) value;

		HashMap<String, String> listValue = getListStringValue(null, "cocukYasUstSiniri");
		List<String> m = new ArrayList<String>();
		if (!listValue.isEmpty())
			cocukYasUstSiniri = Integer.parseInt(listValue.get("cocukYasUstSiniri"));
		if (cocukYasUstSiniri <= 0)
			m.add("Çoçuk yaş sınırı 0 dan küçük ve negatif olamaz");
		if (cocukYasUstSiniri >= yasliYasAltSiniri)
			m.add("Çoçuk yaş sınırı yaşlı çalışan değerinden büyük eşit olamaz");
		if (!m.isEmpty())
			throw setMesajYaz(m);
	}

	public void normalFazlaMesaiValidate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		double normalFazlaMesaiPara = (Double) value;
		double normalFazlaMesai = 0;
		UIInput input = null;
		List<String> m = new ArrayList<String>();
		HashMap listValue = getListValue(null, "normalFazlaMesai", component);
		if (!listValue.isEmpty())
			input = (UIInput) listValue.get("normalFazlaMesai");

		if (input != null)
			normalFazlaMesai = (Double) input.getValue();
		if (normalFazlaMesaiPara > normalFazlaMesai) {
			m.add("Normal fazla mesai ücret hesaplanan normal fazla mesai değeri " + normalFazlaMesai + " büyük olamaz! ");
		} else if (normalFazlaMesaiPara <= 0)
			m.add("Normal Fazla Mesai ücreti sıfırdan büyük olamadır! ");
		if (!m.isEmpty())
			throw setMesajYaz(m);

	}
}
