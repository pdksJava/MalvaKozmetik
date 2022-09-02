package org.pdks.security.action;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.el.MethodExpression;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;

import org.pdks.entity.AccountPermission;
import org.pdks.entity.MenuItem;
import org.pdks.security.entity.MenuItemConstant;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.StatusMessages;
import org.jboss.seam.security.Identity;
import org.richfaces.component.html.HtmlDropDownMenu;
import org.richfaces.component.html.HtmlMenuGroup;
import org.richfaces.component.html.HtmlMenuItem;

@Name("menuLoaderAction")
public class MenuLoaderActionBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3857102837520938592L;
	@In(create = true)
	PdksEntityController pdksEntityController;
	@In(create = true)
	Map<String, MenuItem> topActiveMenuItemMap;

	@In(required = false, create = true)
	StatusMessages statusMessages;
	@In(create = true)
	MenuItemConstant menuItemConstant;
	@In(required = false)
	Identity identity;
	@In(required = false, create = true)
	UserHome userHome;
	@In(required = false)
	List<MenuItem> menuItemList;
	private HtmlDropDownMenu adminMenu;
	private HtmlDropDownMenu guestMenu;
	private HtmlDropDownMenu kullaniciIslemleri;
	private HtmlDropDownMenu menuIslemleri;
	private HtmlDropDownMenu izinIslemleri;
	private HtmlDropDownMenu puantajIslemleri;
	private HtmlDropDownMenu raporIslemleri;

	public HtmlDropDownMenu getMenuIslemleri() {
		menuIslemleri = createMenu(MenuItemConstant.menuIslemleri);
		return menuIslemleri;
	}

	public void setMenuIslemleri(HtmlDropDownMenu menuIslemleri) {
		this.menuIslemleri = menuIslemleri;
	}

	public HtmlDropDownMenu getKullaniciIslemleri() {
		kullaniciIslemleri = createMenu(MenuItemConstant.kullaniciIslemleri);
		return kullaniciIslemleri;
	}

	public void setKullaniciIslemleri(HtmlDropDownMenu kullaniciIslemleri) {
		this.kullaniciIslemleri = kullaniciIslemleri;
	}

	public HtmlDropDownMenu getIzinIslemleri() {
		izinIslemleri = createMenu(MenuItemConstant.izinIslemleri);
		return izinIslemleri;
	}

	public void setIzinIslemleri(HtmlDropDownMenu izinIslemleri) {
		this.izinIslemleri = izinIslemleri;
	}

	public HtmlDropDownMenu getPuantajIslemleri() {
		puantajIslemleri = createMenu(MenuItemConstant.puantajIslemleri);
		return puantajIslemleri;
	}

	public HtmlDropDownMenu getRaporIslemleri() {
		raporIslemleri = createMenu(MenuItemConstant.raporIslemleri);
		return raporIslemleri;
	}

	public void setPuantajIslemleri(HtmlDropDownMenu puantajIslemleri) {
		this.puantajIslemleri = puantajIslemleri;
	}

	public void setRaporIslemleri(HtmlDropDownMenu raporIslemleri) {
		this.raporIslemleri = raporIslemleri;
	}

	public HtmlDropDownMenu getAdminMenu() {
		adminMenu = createMenu(MenuItemConstant.admin);
		return adminMenu;
	}

	public void setAdminMenu(HtmlDropDownMenu adminMenu) {
		this.adminMenu = adminMenu;
	}

	public HtmlDropDownMenu getGuestMenu() {
		guestMenu = createMenu(MenuItemConstant.guest);
		return guestMenu;
	}

	public void setGuestMenu(HtmlDropDownMenu guestMenu) {
		this.guestMenu = guestMenu;
	}

	public HtmlDropDownMenu createMenu(String menuItemName) {
		MenuItem topMenu = menuItemName != null ? topActiveMenuItemMap.get(menuItemName) : null;
		HtmlDropDownMenu menu = null;
		if (topMenu != null) {
			Application app = FacesContext.getCurrentInstance().getApplication();

			menu = (HtmlDropDownMenu) app.createComponent(HtmlDropDownMenu.COMPONENT_TYPE);
			menu.setRendered(Boolean.TRUE);
			menu.setValue(topMenu.getDescription().getAciklama());
			menu.getChildren().add(menu);
			HtmlMenuItem hmiHomePage;
			HtmlMenuGroup htmlMenuGroup = new HtmlMenuGroup();
			for (MenuItem subMenu : topMenu.getChildMenuItemListSirali()) {
				try {
					if (subMenu.getChildMenuItemList().isEmpty()) {

						if (subMenu.getStatus() && userHome.hasPermission(subMenu.getName(), AccountPermission.ACTION_VIEW)) {// gormeye yetkisi yoksa menüyü yaratmasın
							hmiHomePage = new HtmlMenuItem();
							hmiHomePage.setValue(subMenu.getDescription().getAciklama());

							hmiHomePage.setActionExpression(this.startAction(subMenu));
							// hmiHomePage.setRendered(userHome.hasPermission(subMenu.getName(), AccountPermission.ACTION_VIEW));
							if (hmiHomePage.getActionExpression() != null)
								menu.getChildren().add(hmiHomePage);
						}

						// demekki sub menuleri yok

					} else {// sub menuleri de var
						if (userHome.hasPermission(subMenu.getName(), AccountPermission.ACTION_VIEW)) {// gormeye yetkisi yoksa menüyü yaratmasın
							htmlMenuGroup = new HtmlMenuGroup();
							htmlMenuGroup.setValue(subMenu.getDescription().getAciklama());
							// htmlMenuGroup.setRendered(userHome.hasPermission(subMenu.getName(), AccountPermission.ACTION_VIEW));
							menu.getChildren().add(htmlMenuGroup);
							addChildNodes(subMenu, htmlMenuGroup);// sub menüleri doldur
						}
					}
				} catch (Exception e) {
				}
			}
		}
		return menu;

	}

	/**
	 * Tree olusturulurken loadTree metodu tarafindan cagirilarak nodlarin eklenmesini saglar.
	 * 
	 * @param menuItemList
	 * @param tempNode
	 * @param node2RootMenuItem
	 */
	private void addChildNodes(MenuItem menuItem, HtmlMenuGroup menuGroup) {
		HtmlMenuItem hmiHomePage;
		HtmlMenuGroup htmlMenuGroup = new HtmlMenuGroup();
		for (MenuItem subMenu : menuItem.getChildMenuItemListSirali()) {
			if (subMenu.getChildMenuItemList().isEmpty()) {// demekki sub menuleri yok
				if (subMenu.getStatus() && userHome.hasPermission(subMenu.getName(), AccountPermission.ACTION_VIEW)) {// gormeye yetkisi yoksa menüyü yaratmasın
					hmiHomePage = new HtmlMenuItem();
					hmiHomePage.setValue(subMenu.getDescription().getAciklama());
					hmiHomePage.setActionExpression(startAction(subMenu));
					// hmiHomePage.setRendered(userHome.hasPermission(subMenu.getName(), AccountPermission.ACTION_VIEW));
					if (hmiHomePage.getActionExpression() != null)
						menuGroup.getChildren().add(hmiHomePage);
				}
			} else {// sub menuleri de var
				if (userHome.hasPermission(subMenu.getName(), AccountPermission.ACTION_VIEW)) {// gormeye yetkisi yoksa menüyü yaratmasın
					htmlMenuGroup = new HtmlMenuGroup();
					htmlMenuGroup.setValue(subMenu.getDescription().getAciklama());
					// htmlMenuGroup.setRendered(userHome.hasPermission(subMenu.getName(), AccountPermission.ACTION_VIEW));
					menuGroup.getChildren().add(htmlMenuGroup);
					addChildNodes(subMenu, htmlMenuGroup);// sub menüleri doldur
				}
			}
		}
	}

	private MethodExpression startAction(MenuItem menuItem) {
		String menuName = "", firstCharMenuName = "", action = "";
		menuName = menuItem.getName().trim();
		firstCharMenuName = menuName.substring(0, 1);
		menuName = menuName.substring(1);
		menuName = "get" + firstCharMenuName.toUpperCase() + menuName;
		try {
			action = (String) PdksUtil.getMethodObject(new MenuItemConstant(), menuName, null);
		} catch (Exception e) {
			action = "/home.seam";
		}
		if (action == null)
			return null;
		FacesContext ctx = FacesContext.getCurrentInstance();
		Application app = ctx.getApplication();
		Class[] params = {};
		MethodExpression actionExpression = app.getExpressionFactory().createMethodExpression(ctx.getELContext(), action, String.class, params);
		return actionExpression;
	}

}
