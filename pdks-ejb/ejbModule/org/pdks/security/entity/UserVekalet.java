package org.pdks.security.entity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.pdks.entity.YetkiBaseObject;

@Entity(name = UserVekalet.TABLE_NAME)
public class UserVekalet extends YetkiBaseObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5337345762155953408L;

	public static final String TABLE_NAME = "VELAKET_YONETICI";
	public static final String COLUMN_NAME_VEKALET_VEREN = "VEKALET_VEREN";

	private User vekaletVeren;

	private boolean calendarPopup = Boolean.TRUE;

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_VEKALET_VEREN, nullable = false)
	@Fetch(FetchMode.JOIN)
	public User getVekaletVeren() {
		return vekaletVeren;
	}

	public void setVekaletVeren(User vekaletVeren) {
		this.vekaletVeren = vekaletVeren;
	}

	@Transient
	public boolean isCalendarPopup() {
		return calendarPopup;
	}

	public void setCalendarPopup(boolean calendarPopup) {
		this.calendarPopup = calendarPopup;
	}

	@Transient
	public Long getVekaletVerenId() {
		return vekaletVeren != null && vekaletVeren.getPdksPersonel() != null ? vekaletVeren.getPdksPersonel().getId() : 0;
	}

}
