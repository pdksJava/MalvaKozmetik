package org.pdks.validation;

import java.io.Serializable;
import java.util.Date;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.pdks.session.PdksUtil;

public class DateValidator implements Validator, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4736761892514665879L;

	public void validate(FacesContext facesContext, UIComponent arg1, Object value) throws ValidatorException {

		Date date = (Date) value;
		if (date.compareTo(PdksUtil.buGun()) > 0) {
			FacesMessage facesMessage = new FacesMessage("Tarih bugünden sonra olamaz", "message");
			// FacesContext.getCurrentInstance().addMessage("message", facesMessage);
			throw new ValidatorException(facesMessage);
		}
	}
}