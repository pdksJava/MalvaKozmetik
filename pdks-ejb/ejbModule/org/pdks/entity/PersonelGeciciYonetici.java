package org.pdks.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.pdks.security.entity.User;

@Entity(name = "PERSONEL_GECICI_YONETICI")
public class PersonelGeciciYonetici extends YetkiBaseObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6118941829992674855L;

	private Personel personelGecici;

	private User bagliYonetici;

	private boolean calendarPopup = Boolean.TRUE;

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "VEKALET_VEREN", nullable = false)
	@Fetch(FetchMode.JOIN)
	public User getBagliYonetici() {
		return bagliYonetici;
	}

	public void setBagliYonetici(User bagliYonetici) {
		this.bagliYonetici = bagliYonetici;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "PERSONEL_GECICI", nullable = false)
	@Fetch(FetchMode.JOIN)
	public Personel getPersonelGecici() {
		return personelGecici;
	}

	public void setPersonelGecici(Personel personelGecici) {
		this.personelGecici = personelGecici;
	}

	@Column(name = "CALENDARPOPUP")
	public boolean isCalendarPopup() {
		return calendarPopup;
	}

	public void setCalendarPopup(boolean calendarPopup) {
		this.calendarPopup = calendarPopup;
	}

	@Transient
	public Long getPersonelGeciciId() {
		return personelGecici != null ? personelGecici.getId() : 0;
	}

}
