package org.pdks.session;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.richfaces.component.html.HtmlTabPanel;
@Name("componentState")
@Scope(ScopeType.EVENT)
public class ComponentState implements Serializable {
 
	/**
	 * 
	 */
	private static final long serialVersionUID = 211101704257647705L;

	// tablarin isimleri

	// secili tabi tutar
	private String seciliTab;

	private HtmlTabPanel someTabPanel = new HtmlTabPanel();

	public HtmlTabPanel getSomeTabPanel() {
		return someTabPanel;
	}

	public void setSomeTabPanel(HtmlTabPanel someTabPanel) {
		this.someTabPanel = someTabPanel;
	}

	// secili tabi getirir
	public String getSeciliTab() {
		if (someTabPanel == null)
			return seciliTab;
		else if (someTabPanel.getSelectedTab() == null)
			return seciliTab;
		else
			return "" + someTabPanel.getSelectedTab();
	}

	// tab panel componentine secili tabin degerini atar.
	public void setSeciliTab(String seciliTab) {
		someTabPanel.setSelectedTab(seciliTab);
	}

}
