package org.pdks.session;

import java.io.Serializable;

import org.pdks.entity.Dosya;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;

@Name("attachmentHome")
public class AttachmentHome extends EntityHome<Dosya> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5492855391230652680L;

	@RequestParameter
	Long attachmentId;

	private Dosya dosya;

	@Override
	public Object getId() {
		if (attachmentId == null) {
			return super.getId();
		} else {
			return attachmentId;
		}
	}

	public void dosyaSet(Dosya dosya) {
		this.dosya = dosya;
	}

	public Dosya getDosya() {
		return dosya;
	}

	public void setDosya(Dosya dosya) {
		this.dosya = dosya;
	}

	@Override
	@Begin
	public void create() {
		super.create();
	}

}