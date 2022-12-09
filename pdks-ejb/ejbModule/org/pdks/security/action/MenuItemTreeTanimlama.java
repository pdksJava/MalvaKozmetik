package org.pdks.security.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.framework.EntityQuery;
import org.pdks.entity.AccountPermission;
import org.pdks.entity.MenuItem;
import org.pdks.security.entity.User;
import org.pdks.security.entity.UserMenuItemTime;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;
import org.richfaces.component.html.HtmlTree;
import org.richfaces.event.NodeSelectedEvent;
import org.richfaces.model.TreeNode;
import org.richfaces.model.TreeNodeImpl;

/**
 * 
 */
@Name("menuItemTree")
public class MenuItemTreeTanimlama extends EntityQuery<MenuItem> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7487179697404053800L;
	static Logger logger = Logger.getLogger(MenuItemTreeTanimlama.class);

	@In(create = true)
	PdksEntityController pdksEntityController;

	@In(create = true)
	StartupAction startupAction;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false)
	User authenticatedUser;

	private String iconLeaf = "/img/plus.gif";

	private MenuItem currentItem;
	private MenuItem nodeTitle;
	private TreeNode<MenuItem> rootNode;

	private Boolean selectAll = Boolean.FALSE;
	private Map<Long, Boolean> selectedIdsFromTreeMap = new HashMap<Long, Boolean>();
	private Map<Long, Boolean> selectedIdsFromDataTableMap = new HashMap<Long, Boolean>();
	private ArrayList<MenuItem> selectedNodeChildren = new ArrayList<MenuItem>();

	private ArrayList<MenuItem> allTreeMenuItemList;
	private ArrayList<MenuItem> selectedMenuItemFromTreeList;
	private ArrayList<MenuItem> selectedMenuItemFromDataTableList;
	private Session session;

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public String getIconLeaf() {
		return iconLeaf;
	}

	public void setIconLeaf(String iconLeaf) {
		this.iconLeaf = iconLeaf;
	}

	public MenuItem getCurrentItem() {
		if (currentItem == null)
			currentItem = new MenuItem();
		return currentItem;
	}

	public void setCurrentItem(MenuItem currentItem) {
		this.currentItem = currentItem;
	}

	public MenuItem getNodeTitle() {
		return nodeTitle;
	}

	public void setNodeTitle(MenuItem nodeTitle) {
		this.nodeTitle = nodeTitle;
	}

	/**
	 * @return the selectAll
	 */
	public Boolean getSelectAll() {
		return selectAll;
	}

	/**
	 * @param selectAll
	 *            the selectAll to set
	 */
	public void setSelectAll(Boolean selectAll) {
		this.selectAll = selectAll;
	}

	public Map<Long, Boolean> getSelectedIdsFromTree() {
		return selectedIdsFromTreeMap;
	}

	public Map<Long, Boolean> getSelectedIdsFromDataTable() {
		return selectedIdsFromDataTableMap;
	}

	public TreeNode<MenuItem> getTreeNode() {

		loadTree();
		return rootNode;
	}

	/**
	 * data table verilerini(agaca yerlestirilmemis menuitemleri) getirir.
	 */
	@SuppressWarnings("unchecked")
	public List<MenuItem> getAllDataTableMenuItemList() {
		HashMap parametreMap = new HashMap();
		parametreMap.put("status", Boolean.FALSE);
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);

		List<MenuItem> menuItemList = pdksEntityController.getObjectByInnerObjectList(parametreMap, MenuItem.class);
		return menuItemList;
	}

	/**
	 * data table ve tree den secilen menu itemleri listeleri olusuturlur ve bu listelerden hareketle yeni menu item ve account permissionlar ayarlanir.
	 */
	@Transactional
	public String moveMenuItemsFromDataTable2Tree() {
		ArrayList<String> targetListForDataTable = new ArrayList<String>();
		MenuItem selectedMenuItemFromTree = null;
		FacesMessage facesMessage = new FacesMessage();

		// treeden secili olanlari alir.Bu islem sirasinda agactan bir tane menu
		// bileseni secilmeli fakat biz hepsini aliyoruz. 1den fazlaysa uyari
		// veriyoruz.
		getSelectedMenuItemFromTreeList().clear();
		getSelectedMenuItemFromDataTableList().clear();
		for (MenuItem dataItem : allTreeMenuItemList) {
			if (selectedIdsFromTreeMap.containsKey(dataItem.getId()) && selectedIdsFromTreeMap.get(dataItem.getId()).booleanValue()) {
				getSelectedMenuItemFromTreeList().add(dataItem);
			}
		}
		// datatabledan secili olanlar
		if (getSelectAll())
			getSelectedMenuItemFromDataTableList().addAll(getAllDataTableMenuItemList());
		else {
			for (MenuItem menuItem : getAllDataTableMenuItemList()) {
				if (selectedIdsFromDataTableMap.containsKey(menuItem.getId()) && selectedIdsFromDataTableMap.get(menuItem.getId()).booleanValue()) {
					getSelectedMenuItemFromDataTableList().add(menuItem);
				}
			}
		}
		// Kontroller
		if (getSelectedMenuItemFromTreeList().isEmpty()) {

			facesMessage.setSummary("Ağaçtan Menü Bileşeni seçilmediğinden Data tabledan seçilen Menü Bileşeni üst bileşen olrak ayarlandı......");
			facesMessage.setDetail("Ağaçtan Menü Bileşeni seçilmediğinden Data tabledan seçilen Menü Bileşeni üst bileşen olrak ayarlandı...");
			facesMessage.setSeverity(FacesMessage.SEVERITY_INFO);
			FacesContext.getCurrentInstance().addMessage("", facesMessage);
		} else if (getSelectedMenuItemFromTreeList().size() > 1) {
			facesMessage.setSummary("#{messages['pages.menuItemList.mesaj.birdenFazlaSecim']}");
			facesMessage.setDetail("Birden fazla menü seçilemez");
			facesMessage.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage("", facesMessage);
		} else
			selectedMenuItemFromTree = getSelectedMenuItemFromTreeList().get(0);

		if (getSelectedMenuItemFromDataTableList().isEmpty()) {
			facesMessage.setSummary("Data tabledan Menü Bileşeni seçiniz...");
			facesMessage.setDetail("Data tabledan Menü Bileşeni seçiniz...");
			facesMessage.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage("", facesMessage);
		}

		if (FacesContext.getCurrentInstance().getMaximumSeverity() == FacesMessage.SEVERITY_INFO || FacesContext.getCurrentInstance().getMaximumSeverity() == null) {

			// Data tabledan alınan menu Itemlerin ayarlanması
			for (Iterator<MenuItem> iterator4DataTable = this.getSelectedMenuItemFromDataTableList().iterator(); iterator4DataTable.hasNext();) {
				MenuItem tempMenuItemFromDataTable = (MenuItem) iterator4DataTable.next();
				tempMenuItemFromDataTable.setStatus(Boolean.TRUE);
				if (this.getSelectedMenuItemFromTreeList().isEmpty())
					tempMenuItemFromDataTable.setTopMenu(Boolean.TRUE);
				else {// CHILD MENU
					targetListForDataTable.add(tempMenuItemFromDataTable.getName());
					List<MenuItem> tempppp = new ArrayList<MenuItem>(selectedMenuItemFromTree.getChildMenuItemList());
					tempppp.add(tempMenuItemFromDataTable);
					selectedMenuItemFromTree.setChildMenuItemList(tempppp);

				}
				pdksEntityController.saveOrUpdate(session, entityManager, tempMenuItemFromDataTable);
			}
			if (selectedMenuItemFromTree != null)
				pdksEntityController.saveOrUpdate(session, entityManager, selectedMenuItemFromTree);
			session.flush();
			rootNode = null;
			startupAction.fillMenuItemList(session);
			selectedIdsFromTreeMap.clear();
			selectedIdsFromDataTableMap.clear();
		}
		return "ok";
	}

	/**
 	 */
	@Transactional
	public String moveMenuItemsFromTree2DataTable() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		ArrayList<MenuItem> deleteMenuItemList = new ArrayList<MenuItem>();
		ArrayList<String> menuItemNameList = new ArrayList<String>();
		ArrayList<AccountPermission> deleteAccountPermissionList = new ArrayList<AccountPermission>();
		getSelectedMenuItemFromTreeList().clear();
		// treeden secili olanlari alir
		for (MenuItem dataItem : allTreeMenuItemList) {
			try {
				Long key = dataItem.getId();
				if (selectedIdsFromTreeMap.containsKey(key) && selectedIdsFromTreeMap.get(key).booleanValue()) {
					getSelectedMenuItemFromTreeList().add(dataItem);
					selectedIdsFromTreeMap.remove(dataItem.getId()); // Reset.
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e);
			}

		}

		for (MenuItem tempMenuItemFromTree : getSelectedMenuItemFromTreeList()) {
			tempMenuItemFromTree.setStatus(Boolean.FALSE);
			tempMenuItemFromTree.setTopMenu(Boolean.FALSE);
			// Secili menuItemin parenti bulunur. Parentin child listesinden
			// Secili menuItem çıkarılır.
			if (tempMenuItemFromTree.getParentMenuItem() != null) {
				MenuItem dataItem = tempMenuItemFromTree.getParentMenuItem();
				logger.debug(dataItem.getName() + " " + dataItem.getChildMenuItemList().size());
				for (Iterator iterator = dataItem.getChildMenuItemList().iterator(); iterator.hasNext();) {
					MenuItem tempMenuItem = (MenuItem) iterator.next();
					if (tempMenuItem.getId().equals(tempMenuItemFromTree.getId())) {
						// iterator.remove();
						dataItem.getChildMenuItemList().remove(tempMenuItem);
						break;
					}
				}
				pdksEntityController.saveOrUpdate(session, entityManager, dataItem);

			}

			deleteMenuItemList.add(tempMenuItemFromTree);
			menuItemNameList.add(tempMenuItemFromTree.getName());
			altBilesenleriCikart(menuItemNameList, deleteMenuItemList, tempMenuItemFromTree);
			tempMenuItemFromTree.getChildMenuItemList().clear();
		}
		if (!menuItemNameList.isEmpty()) {
			HashMap parametreMap = new HashMap();
			parametreMap.put("target", menuItemNameList);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);

			deleteAccountPermissionList = (ArrayList<AccountPermission>) pdksEntityController.getObjectByInnerObjectList(parametreMap, AccountPermission.class);
			for (Iterator iterator = deleteAccountPermissionList.iterator(); iterator.hasNext();) {
				AccountPermission accountPermission = (AccountPermission) iterator.next();
				accountPermission.setStatus(Boolean.FALSE);
				pdksEntityController.saveOrUpdate(session, entityManager, accountPermission);
			}
			for (MenuItem menuItem : deleteMenuItemList)
				pdksEntityController.saveOrUpdate(session, entityManager, menuItem);

			// pdksEntityController.update(deleteAccountPermissionList);
			// pdksEntityController.update(deleteMenuItemList);
			session.flush();

			rootNode = null;
			startupAction.fillMenuItemList(session);
		}

		return "ok";
	}

	/**
	 * @param deleteMenuItemList
	 * @param tempMenuItemList
	 * @param tempMenuItemFromTree
	 */
	private void altBilesenleriCikart(ArrayList<String> deleteMenuItemNameList, ArrayList<MenuItem> deleteMenuItemList, MenuItem menuItem) {
		if (!menuItem.getChildMenuItemList().isEmpty()) {
			for (Iterator iterator = menuItem.getChildMenuItemListSirali().iterator(); iterator.hasNext();) {
				MenuItem tempMenuItem = (MenuItem) iterator.next();
				tempMenuItem.setStatus(Boolean.FALSE);
				tempMenuItem.setTopMenu(Boolean.FALSE);
				deleteMenuItemList.add(tempMenuItem);
				deleteMenuItemNameList.add(tempMenuItem.getName());

				if (!tempMenuItem.getChildMenuItemList().isEmpty()) {
					altBilesenleriCikart(deleteMenuItemNameList, deleteMenuItemList, tempMenuItem);
					tempMenuItem.getChildMenuItemList().clear();
				}
			}
		}
	}

	public ArrayList<MenuItem> getSelectedMenuItemFromTreeList() {
		if (selectedMenuItemFromTreeList == null)
			selectedMenuItemFromTreeList = new ArrayList<MenuItem>();
		return selectedMenuItemFromTreeList;
	}

	public void setSelectedMenuItemFromTreeList(ArrayList<MenuItem> selectedMenuItemFromTreeList) {
		this.selectedMenuItemFromTreeList = selectedMenuItemFromTreeList;
	}

	/**
	 * @return the selectedMenuItemFromDataTableList
	 */
	public ArrayList<MenuItem> getSelectedMenuItemFromDataTableList() {
		if (selectedMenuItemFromDataTableList == null)
			selectedMenuItemFromDataTableList = new ArrayList<MenuItem>();
		return selectedMenuItemFromDataTableList;
	}

	/**
	 * @param selectedMenuItemFromDataTableList
	 *            the selectedMenuItemFromDataTableList to set
	 */
	public void setSelectedMenuItemFromDataTableList(ArrayList<MenuItem> selectedMenuItemFromDataTableList) {
		this.selectedMenuItemFromDataTableList = selectedMenuItemFromDataTableList;
	}

	public boolean contains(ArrayList<MenuItem> containerList, MenuItem containedMenuItem) {
		boolean booleanValue = Boolean.FALSE;
		for (Iterator<MenuItem> iterator = containerList.iterator(); iterator.hasNext();) {
			MenuItem menuItem = (MenuItem) iterator.next();
			if (menuItem.getId().equals(containedMenuItem.getId())) {
				booleanValue = Boolean.TRUE;
				break;
			}
		}
		return booleanValue;
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public String sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		rootNode = null;
		selectedIdsFromTreeMap.clear();
		selectedIdsFromDataTableMap.clear();
		selectedNodeChildren.clear();

		return "";
	}

	/**
	 * Tree olusturulurken loadTree metodu tarafindan cagirilarak nodlarin eklenmesini saglar.
	 * 
	 * @param menuItemList
	 * @param tempNode
	 * @param node2RootMenuItem
	 */
	private void addChildNodes(MenuItem parentMenuItem, TreeNode<MenuItem> tempNode) {

		if (!parentMenuItem.getChildMenuItemList().isEmpty()) {
			for (Iterator<MenuItem> menuIterator = parentMenuItem.getChildMenuItemListSirali().iterator(); menuIterator.hasNext();) {
				MenuItem tempMenuItem = (MenuItem) menuIterator.next();
				tempMenuItem.setParentMenuItem(parentMenuItem);
				if (tempMenuItem.getStatus()) {
					TreeNodeImpl<MenuItem> nodeImpl = new TreeNodeImpl<MenuItem>();
					tempNode.addChild(tempMenuItem.getId(), nodeImpl);
					nodeImpl.setData(tempMenuItem);
					addChildNodes(tempMenuItem, nodeImpl);
				}
			}
		}
	}

	/**
	 * Treenin oluşturuldugu Metod
	 */
	@SuppressWarnings("unchecked")
	private void loadTree() {
		HashMap parametreMap = new HashMap();
		parametreMap.put("status", Boolean.TRUE);
		parametreMap.put(PdksEntityController.MAP_KEY_ORDER, "orderNo");
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		allTreeMenuItemList = (ArrayList<MenuItem>) pdksEntityController.getObjectByInnerObjectList(parametreMap, MenuItem.class);
		try {
			rootNode = new TreeNodeImpl<MenuItem>();
			for (MenuItem tempMenuItem : allTreeMenuItemList) {
				if (tempMenuItem.getTopMenu()) {
					tempMenuItem.setParentMenuItem(null);
					TreeNodeImpl<MenuItem> nodeImpl = new TreeNodeImpl<MenuItem>();
					rootNode.addChild(tempMenuItem.getId(), nodeImpl);
					nodeImpl.setData(tempMenuItem);
					addChildNodes(tempMenuItem, nodeImpl);
				}
			}
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			throw new FacesException(e.getMessage(), e);
		}
	}

	/**
 	 */
	@SuppressWarnings("unchecked")
	public void processSelection(NodeSelectedEvent event) {
		HtmlTree tree = (HtmlTree) event.getComponent();
		nodeTitle = (MenuItem) tree.getRowData();
		selectedNodeChildren.clear();
		TreeNode<MenuItem> currentNode = tree.getModelTreeNode(tree.getRowKey());
		if (currentNode.isLeaf()) {
			selectedNodeChildren.add((MenuItem) currentNode.getData());
		} else {
			Iterator<Entry<Object, TreeNode<MenuItem>>> it = currentNode.getChildren();
			while (it != null && it.hasNext()) {
				Entry<Object, TreeNode<MenuItem>> entry = it.next();
				selectedNodeChildren.add((MenuItem) entry.getValue().getData());
			}
		}

	}

	/**
	 * @param menuItem
	 * @return
	 */
	public String userList(MenuItem menuItem) {
		if (menuItem != null && menuItem.getMenuItemTimeList() == null) {
			if (menuItem.getId() != null) {
				HashMap parametreMap = new HashMap();
				parametreMap.put("menu.id", menuItem.getId());
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<UserMenuItemTime> list = pdksEntityController.getObjectByInnerObjectList(parametreMap, UserMenuItemTime.class);
				for (Iterator iterator = list.iterator(); iterator.hasNext();) {
					UserMenuItemTime userMenuItemTime = (UserMenuItemTime) iterator.next();
					if (!userMenuItemTime.getUser().isDurum())
						iterator.remove();

				}
				if (!list.isEmpty()) {
					list = PdksUtil.sortListByAlanAdi(list, "lastTime", Boolean.TRUE);
					menuItem.setMenuItemTimeList(list);
				}

			}
		}

		return "";
	}

	/**
 	 */
	public ArrayList<AccountPermission> getPermissionList4MenuItem(ArrayList<MenuItem> tempMenuItemList) {
		ArrayList<AccountPermission> tempPermissionList = new ArrayList<AccountPermission>();
		for (Iterator iterator = tempMenuItemList.iterator(); iterator.hasNext();) {
			MenuItem menuItem = (MenuItem) iterator.next();
			HashMap parametreMap = new HashMap();
			parametreMap.put("target", menuItem.getName());
			parametreMap.put(PdksEntityController.MAP_KEY_ORDER, "orderNo");
			parametreMap.put("status", Boolean.TRUE);
			parametreMap.put("action", AccountPermission.ACTION_VIEW);
			parametreMap.put("discriminator", AccountPermission.DISCRIMINATOR_ROLE);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);

			tempPermissionList = (ArrayList<AccountPermission>) pdksEntityController.getObjectByInnerObjectList(parametreMap, AccountPermission.class);
		}
		return tempPermissionList;
	}

	public MenuItemTreeTanimlama() {
		setEjbql("select menuItem from MenuItem menuItem order orderNo");
	}
}
