package org.pdks.security.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.faces.FacesException;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.framework.EntityQuery;
import org.pdks.entity.AccountPermission;
import org.pdks.entity.MenuItem;
import org.pdks.security.entity.MenuItemConstant;
import org.pdks.security.entity.Role;
import org.pdks.security.entity.User;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;
import org.richfaces.component.html.HtmlTree;
import org.richfaces.event.NodeSelectedEvent;
import org.richfaces.model.TreeNode;
import org.richfaces.model.TreeNodeImpl;

/**
 * 
 */
@Name("menuItemPermissionTanimlama")
public class MenuItemPermissionTanimlama extends EntityQuery<MenuItem> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4766060916725451626L;
	static Logger logger = Logger.getLogger(MenuItemPermissionTanimlama.class);

	@In(create = true)
	PdksEntityController pdksEntityController;

	@In(create = true)
	StartupAction startupAction;

	@In(required = false)
	User authenticatedUser;

	@In(required = false, create = true)
	EntityManager entityManager;

	private boolean selectedFromTree;

	private String iconLeaf = "/img/plus.gif";

	private MenuItem nodeTitle;
	private MenuItem selectedMenuItem;

	private AccountPermission selectedAccPermissionFromModalPanel;

	private TreeNode<MenuItem> rootNodeForAllMenuItem;
	private TreeNode<MenuItem> rootNodeForRole = new TreeNodeImpl<MenuItem>();

	private TreeMap<Long, Boolean> selectedIdsFromTreeMap = new TreeMap<Long, Boolean>();
	private TreeMap<Long, Boolean> selectedIdsFromDataTableMap = new TreeMap<Long, Boolean>();

	private ArrayList<MenuItem> allTreeMenuItemList;
	private ArrayList<MenuItem> selectedMenuItemFromTreeList;
	private ArrayList<Role> selectedRolFromDataTableList = new ArrayList<Role>();
	private ArrayList<MenuItem> selectedNodeChildren = new ArrayList<MenuItem>();
	private Session session;

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	@Out(scope = ScopeType.SESSION)
	private ArrayList<AccountPermission> permissionList4ModelPanel = new ArrayList<AccountPermission>();

	public boolean isSelectedFromTree() {
		return selectedFromTree;
	}

	public void setSelectedFromTree(boolean selectedFromTree) {
		this.selectedFromTree = selectedFromTree;
	}

	public String getIconLeaf() {
		return iconLeaf;
	}

	public void setIconLeaf(String iconLeaf) {
		this.iconLeaf = iconLeaf;
	}

	public MenuItem getSelectedMenuItem() {
		if (selectedMenuItem == null)
			selectedMenuItem = new MenuItem();
		return selectedMenuItem;
	}

	public void setSelectedMenuItem(MenuItem selectedMenuItem) {
		this.selectedMenuItem = selectedMenuItem;
	}

	public AccountPermission getSelectedAccPermissionFromModalPanel() {
		return selectedAccPermissionFromModalPanel;
	}

	public void setSelectedAccPermissionFromModalPanel(AccountPermission selectedAccPermissionFromModalPanel) {
		this.selectedAccPermissionFromModalPanel = selectedAccPermissionFromModalPanel;
	}

	public MenuItem getNodeTitle() {
		return nodeTitle;
	}

	public void setNodeTitle(MenuItem nodeTitle) {
		this.nodeTitle = nodeTitle;
	}

	public void setMenuItemTree4Rol(Role rol) {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		HashMap parametreMap = new HashMap();
		parametreMap.put("status", Boolean.TRUE);
		parametreMap.put("topMenu", Boolean.TRUE);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<MenuItem> menuItemList = (ArrayList<MenuItem>) pdksEntityController.getObjectByInnerObjectList(parametreMap, MenuItem.class);

		parametreMap.clear();
		parametreMap.put("status", Boolean.TRUE);
		parametreMap.put("action", AccountPermission.ACTION_VIEW);
		parametreMap.put("discriminator", AccountPermission.DISCRIMINATOR_ROLE);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<AccountPermission> permissionList = pdksEntityController.getObjectByInnerObjectList(parametreMap, AccountPermission.class);
		String key = "";

		parametreMap.clear();
		for (AccountPermission accountPermission : permissionList) {
			key = accountPermission.getTarget() + "-" + accountPermission.getRecipient();
			parametreMap.put(key, accountPermission);
		}

		/*
		 * for (MenuItem tempMenuItem : menuItemList) { key = tempMenuItem.getName() + "-" + rol.getRolename(); if (parametreMap.containsKey(key)) { treeDataList.add(tempMenuItem); removeChilds(tempMenuItem,parametreMap,rol,key); } }
		 */
		try {
			for (MenuItem tempMenuItem : menuItemList) {
				key = tempMenuItem.getName() + "-" + rol.getRolename();
				if (parametreMap.containsKey(key)) {
					TreeNodeImpl<MenuItem> nodeImpl = new TreeNodeImpl<MenuItem>();// yeni bir top menu node tanimlanir
					rootNodeForRole.addChild(tempMenuItem.getId(), nodeImpl);// yeni node root a eklenir
					nodeImpl.setData(tempMenuItem);// yeni node a menu bileseni eklenir.
					addChildNodesRolMenuItemTree(nodeImpl, tempMenuItem, parametreMap, rol, key);
				}
			}
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			throw new FacesException(e.getMessage(), e);
		}

	}

	private void addChildNodesRolMenuItemTree(TreeNodeImpl<MenuItem> tempNode, MenuItem menuItem, HashMap parametreMap, Role rol, String key) {
		if (!menuItem.getChildMenuItemList().isEmpty()) {
			for (MenuItem tempMenuItem : menuItem.getChildMenuItemListSirali()) {
				key = tempMenuItem.getName() + "-" + rol.getRolename();
				if (parametreMap.containsKey(key)) {
					TreeNodeImpl<MenuItem> nodeImpl = new TreeNodeImpl<MenuItem>();// yeni bir node tanimlanir
					tempNode.addChild(tempMenuItem.getId(), nodeImpl);// yeni node ust node a eklenir
					nodeImpl.setData(tempMenuItem);// yeni node a menu bileseni eklenir.
					addChildNodesRolMenuItemTree(nodeImpl, tempMenuItem, parametreMap, rol, key);
				}
			}
		}
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		selectedIdsFromTreeMap.clear();
		// fillMenuItemTree();
	}

	public void fillMenuItemTree() {
		HashMap parametreMap = new HashMap();
		parametreMap.put("status", Boolean.TRUE);
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);

		allTreeMenuItemList = (ArrayList<MenuItem>) pdksEntityController.getObjectByInnerObjectList(parametreMap, MenuItem.class);
		rootNodeForAllMenuItem = new TreeNodeImpl<MenuItem>();
		for (Iterator iterator = allTreeMenuItemList.iterator(); iterator.hasNext();) {
			MenuItem menuItem = (MenuItem) iterator.next();
			if (menuItem.getName().equals(MenuItemConstant.menuIslemleri))
				iterator.remove();
		}

		loadTree(rootNodeForAllMenuItem, allTreeMenuItemList);

	}

	public TreeNode<MenuItem> getRootNodeForAllMenuItem() {
		if (rootNodeForAllMenuItem == null)
			fillMenuItemTree();
		return rootNodeForAllMenuItem;
	}

	public void setRootNodeForAllMenuItem(TreeNode<MenuItem> rootNodeForAllMenuItem) {
		this.rootNodeForAllMenuItem = rootNodeForAllMenuItem;
	}

	public TreeNode<MenuItem> getRootNodeForRole() {
		return rootNodeForRole;
	}

	public void setRootNodeForRole(TreeNode<MenuItem> rootNodeForRole) {
		this.rootNodeForRole = rootNodeForRole;
	}

	public Map<Long, Boolean> getSelectedIdsFromTree() {
		return selectedIdsFromTreeMap;
	}

	public Map<Long, Boolean> getSelectedIdsFromDataTable() {
		return selectedIdsFromDataTableMap;
	}

	public String menuSec() {
		MenuItem menuItem = selectedMenuItem;
		if (menuItem != null)
			selectedIdsFromDataTableMap.put(menuItem.getId(), menuItem.getCheck());

		return "";
	}

	/**
	 * veritabanindan agaca yerlestirilmemis menu Itemlarini scrollable data tableda gosterilmek uzere ceker
	 */
	@SuppressWarnings("unchecked")
	public List<Role> getAllRoleList() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		HashMap parametreMap = new HashMap();
		parametreMap.put("status=", Boolean.TRUE);
		parametreMap.put("rolename <> ", Role.TIPI_ADMIN);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);

		List<Role> menuItemList = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, Role.class);
		return menuItemList;
	}

	/**
	 * Scrollable data tabledan seÃ§ilen menu Item larÄ± alÄ±r ve selectedMenuItemFromDataTableList listesine ekler.
	 */

	public String addSelectionsFromDataTable2Tree() {
		// treeden secili olanlari alir.Bu islem sirasinda agactan bir tane menu bileseni secilmeli fakat biz hepsini aliyoruz. 1den fazlaysa uyari veriyoruz.
		if (selectedRolFromDataTableList != null)
			selectedRolFromDataTableList.clear();
		else
			selectedRolFromDataTableList = new ArrayList<Role>();
		if (selectedMenuItemFromTreeList != null)
			selectedMenuItemFromTreeList.clear();
		else
			selectedMenuItemFromTreeList = new ArrayList<MenuItem>();
		if (allTreeMenuItemList != null) {
			for (MenuItem dataItem : allTreeMenuItemList) {
				if (selectedIdsFromTreeMap.containsKey(dataItem.getId()) && selectedIdsFromTreeMap.get(dataItem.getId())) {
					selectedMenuItemFromTreeList.add(dataItem);
				}
			}
		}
		// datatabledan secili olanlar
		if (!selectedMenuItemFromTreeList.isEmpty()) {
			List<Role> roller = getAllRoleList();
			for (Role rol : roller) {
				if (selectedIdsFromDataTableMap.containsKey(rol.getId()) && selectedIdsFromDataTableMap.get(rol.getId()).booleanValue()) {
					selectedRolFromDataTableList.add(rol);
				}
			}
			if (!selectedRolFromDataTableList.isEmpty()) {
				if (saveRolsAndMenuItemsAsAccountPermission() != null)
					startupAction.fillStartMethod(authenticatedUser, session);
			} else
				PdksUtil.addMessageWarn("Yetkilendirilecek rol seçiniz!");
		} else
			PdksUtil.addMessageWarn("Yetkilendirilecek menüyü ağaçtan seçiniz!");

		return "";
	}

	/**
	 * Scrollable data tabledan seÃ§ilen menu Item larÄ± alÄ±r ve selectedMenuItemFromDataTableList listesine siler.
	 */

	public String removeSelectionsFromTree2DataTable() {

		// treeden secili olanlari alir.Bu islem sirasinda agactan bir tane menu bileseni secilmeli fakat biz hepsini aliyoruz. 1den fazlaysa uyari veriyoruz.
		if (selectedRolFromDataTableList != null)
			selectedRolFromDataTableList.clear();
		else
			selectedRolFromDataTableList = new ArrayList<Role>();
		if (selectedMenuItemFromTreeList != null)
			selectedMenuItemFromTreeList.clear();
		else
			selectedMenuItemFromTreeList = new ArrayList<MenuItem>();
		if (allTreeMenuItemList != null) {
			for (MenuItem dataItem : allTreeMenuItemList) {
				if (selectedIdsFromTreeMap.containsKey(dataItem.getId()) && selectedIdsFromTreeMap.get(dataItem.getId())) {
					selectedMenuItemFromTreeList.add(dataItem);
				}
			}
		}
		// datatabledan secili olanlar
		if (!selectedMenuItemFromTreeList.isEmpty()) {
			List<Role> roller = getAllRoleList();
			for (Role rol : roller) {
				if (selectedIdsFromDataTableMap.containsKey(rol.getId()) && selectedIdsFromDataTableMap.get(rol.getId()).booleanValue()) {
					selectedRolFromDataTableList.add(rol);
				}
			}
			if (!selectedRolFromDataTableList.isEmpty()) {
				if (deleteMenuItemsAndRolesFromAccountPermission() != null)
					startupAction.fillStartMethod(authenticatedUser, session);
			} else
				PdksUtil.addMessageWarn("Yetkilendirilecek rol seçiniz!");
		} else
			PdksUtil.addMessageWarn("Yetkilendirilecek menüyü ağaçtan seçiniz!");

		return "ok";

	}

	/**
	 * takeSelectionFromDataTable tarafÄ±ndan selectedRolFromDataTableList listesine eklenmis rolleri ve secilen agac node, menuItemlari(sessiondan inject edilmiÅŸ selectedMenuItemFromTreeList()teki menuItemlari) alir ve accountpermissiona kaydeder.
	 */
	@Transactional
	public String saveRolsAndMenuItemsAsAccountPermission() {
		String donus = null;
		HashMap parametreMap = new HashMap();
		parametreMap.put("status", Boolean.TRUE);
		parametreMap.put("action", AccountPermission.ACTION_VIEW);
		parametreMap.put("discriminator", AccountPermission.DISCRIMINATOR_ROLE);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);

		List<AccountPermission> permissionList = pdksEntityController.getObjectByInnerObjectList(parametreMap, AccountPermission.class);
		String key = "";

		parametreMap.clear();
		for (AccountPermission accountPermission : permissionList) {
			key = accountPermission.getTarget() + "-" + accountPermission.getRecipient();
			parametreMap.put(key, accountPermission);
		}
		ArrayList<AccountPermission> saveAccountPermissionList = new ArrayList<AccountPermission>();
		ArrayList<AccountPermission> updateAccountPermissionList = new ArrayList<AccountPermission>();
		for (Iterator<Role> iterator4DataTable = this.getSelectedRolFromDataTableList().iterator(); iterator4DataTable.hasNext();) {
			Role tempRoleFromDataTable = (Role) iterator4DataTable.next();
			for (Iterator<MenuItem> iterator4Tree = this.getSelectedMenuItemFromTreeList().iterator(); iterator4Tree.hasNext();) {
				MenuItem tempMenuItemFromTree = (MenuItem) iterator4Tree.next();
				key = tempMenuItemFromTree.getName() + "-" + tempRoleFromDataTable.getRolename();
				if (parametreMap.containsKey(key)) {
					AccountPermission tempAccountPermission = (AccountPermission) parametreMap.get(key);
					if (!tempAccountPermission.getStatus()) {
						tempAccountPermission.setStatus(Boolean.TRUE);
						updateAccountPermissionList.add(tempAccountPermission);
					}
				} else {
					AccountPermission tempAccountPermission = new AccountPermission();
					tempAccountPermission.setAction(AccountPermission.ACTION_VIEW);
					tempAccountPermission.setDiscriminator(AccountPermission.DISCRIMINATOR_ROLE);
					tempAccountPermission.setRecipient(tempRoleFromDataTable.getRolename());
					tempAccountPermission.setTarget(tempMenuItemFromTree.getName());
					tempAccountPermission.setStatus(Boolean.TRUE);
					saveAccountPermissionList.add(tempAccountPermission);
					parametreMap.put(key, tempAccountPermission);
				}
				// Parent menu itemi varsa bulunarak child icin verilen permission eklenir.
				ustMenuBileseninePermissionEkle(parametreMap, updateAccountPermissionList, saveAccountPermissionList, tempRoleFromDataTable, tempMenuItemFromTree);
			}
		}
		if (!updateAccountPermissionList.isEmpty() || !saveAccountPermissionList.isEmpty()) {
			for (Iterator iterator = updateAccountPermissionList.iterator(); iterator.hasNext();) {
				AccountPermission accountPermission = (AccountPermission) iterator.next();
				pdksEntityController.saveOrUpdate(session, entityManager, accountPermission);
			}
			for (Iterator iterator = saveAccountPermissionList.iterator(); iterator.hasNext();) {
				AccountPermission accountPermission = (AccountPermission) iterator.next();
				pdksEntityController.saveOrUpdate(session, entityManager, accountPermission);
			}
			session.flush();
			donus = "";
		}
		saveAccountPermissionList = null;
		updateAccountPermissionList = null;
		getSelectedMenuItemFromTreeList().clear();

		return donus;
	}

	/**
	 * @param saveAccountPermissionList
	 * @param tempRoleFromDataTable
	 * @param tempMenuItemFromTree
	 */
	private void ustMenuBileseninePermissionEkle(HashMap parametreMap, ArrayList<AccountPermission> updateAccountPermissionList, ArrayList<AccountPermission> saveAccountPermissionList, Role tempRoleFromDataTable, MenuItem tempMenuItemFromTree) {
		String key = "";

		for (MenuItem dataItem : allTreeMenuItemList) {
			boolean tempController = Boolean.FALSE;
			for (MenuItem tempMenuItem : dataItem.getChildMenuItemListSirali()) {
				if (tempMenuItem.getId() == tempMenuItemFromTree.getId()) {// dataitem in childlarinda tempMenuItemFromTree varsa dataitem tempMenuItemFromTree'Ä±n parentÄ±dÄ±r.
					key = dataItem.getName() + "-" + tempRoleFromDataTable.getRolename();
					if (parametreMap.containsKey(key)) {// kayitli bir permission varsa
						AccountPermission tempAccountPermission = (AccountPermission) parametreMap.get(key);
						if (!tempAccountPermission.getStatus()) {
							tempAccountPermission.setStatus(Boolean.TRUE);
							updateAccountPermissionList.add(tempAccountPermission);
						}
					} else {// yoksa
						AccountPermission tempAccountPermissionForParent = new AccountPermission();
						tempAccountPermissionForParent.setAction(AccountPermission.ACTION_VIEW);
						tempAccountPermissionForParent.setDiscriminator(AccountPermission.DISCRIMINATOR_ROLE);
						tempAccountPermissionForParent.setRecipient(tempRoleFromDataTable.getRolename());
						tempAccountPermissionForParent.setTarget(dataItem.getName());
						tempAccountPermissionForParent.setStatus(Boolean.TRUE);
						saveAccountPermissionList.add(tempAccountPermissionForParent);
						parametreMap.put(key, tempAccountPermissionForParent);
					}
					// Parent menu itemi varsa bulunarak child icin verilen permission eklenir.
					ustMenuBileseninePermissionEkle(parametreMap, updateAccountPermissionList, saveAccountPermissionList, tempRoleFromDataTable, dataItem);
					tempController = Boolean.TRUE;
					break;
				}
			}
			if (tempController)
				break;
		}
	}

	@Transactional
	public String deleteMenuItemsAndRolesFromAccountPermission() {
		String donus = null;
		ArrayList<AccountPermission> deleteAccountPermissionList = new ArrayList<AccountPermission>();

		HashMap parametreMap = new HashMap();
		parametreMap.put("status", Boolean.TRUE);
		parametreMap.put("action", AccountPermission.ACTION_VIEW);
		parametreMap.put("discriminator", AccountPermission.DISCRIMINATOR_ROLE);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);

		List<AccountPermission> permissionList = pdksEntityController.getObjectByInnerObjectList(parametreMap, AccountPermission.class);
		String key = "";

		parametreMap.clear();
		for (AccountPermission accountPermission : permissionList) {
			key = accountPermission.getTarget() + "-" + accountPermission.getRecipient();
			parametreMap.put(key, accountPermission);
		}
		for (Role tempRoleFromDataTable : this.getSelectedRolFromDataTableList()) {
			// secilen rolun view yetkisine sahip oldugu menu itemlerin permissionlarÄ± Ã§ekilir.
			// Tum permissionlarÄ±n cekilmesi veya rolve menu item ikilisi icin permission cekilmesi yerine role ait tum permissionlarÄ±n cekilmesi tercih edilmistir.
			for (MenuItem tempMenuItemFromTree : this.getSelectedMenuItemFromTreeList()) {
				key = tempMenuItemFromTree.getName() + "-" + tempRoleFromDataTable.getRolename();
				if (parametreMap.containsKey(key)) {
					AccountPermission accountPermission = (AccountPermission) parametreMap.get(key);
					deleteAccountPermissionList.add(accountPermission);
					altMenuBilesenlerindenYetkileriCikar(deleteAccountPermissionList, parametreMap, tempRoleFromDataTable, tempMenuItemFromTree);
				}
			}
		}
		if (!deleteAccountPermissionList.isEmpty()) {
			for (AccountPermission accountPermission : deleteAccountPermissionList) {
				accountPermission.setStatus(Boolean.FALSE);
				pdksEntityController.saveOrUpdate(session, entityManager, accountPermission);
			}
			session.flush();
			donus = "";
		}
		deleteAccountPermissionList = null;

		return donus;
	}

	/**
	 * @param deleteAccountPermissionList
	 * @param parametreMap
	 * @param tempRoleFromDataTable
	 * @param tempMenuItemFromTree
	 */
	private void altMenuBilesenlerindenYetkileriCikar(ArrayList<AccountPermission> deleteAccountPermissionList, HashMap parametreMap, Role tempRoleFromDataTable, MenuItem tempMenuItemFromTree) {
		String key;
		for (MenuItem childOfTempMenuItemFromTree : tempMenuItemFromTree.getChildMenuItemListSirali()) {
			try {
				if (childOfTempMenuItemFromTree != null && tempRoleFromDataTable != null) {
					key = childOfTempMenuItemFromTree.getName() + "-" + tempRoleFromDataTable.getRolename();
					if (parametreMap.containsKey(key)) {
						AccountPermission tempAccountPermission = (AccountPermission) parametreMap.get(key);
						deleteAccountPermissionList.add(tempAccountPermission);
						altMenuBilesenlerindenYetkileriCikar(deleteAccountPermissionList, parametreMap, tempRoleFromDataTable, childOfTempMenuItemFromTree);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
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
	public ArrayList<Role> getSelectedRolFromDataTableList() {

		return selectedRolFromDataTableList;
	}

	/**
	 * @param selectedMenuItemFromDataTableList
	 *            the selectedMenuItemFromDataTableList to set
	 */
	public void selectedRolFromDataTableList(ArrayList<Role> selectedRolFromDataTableList) {
		this.selectedRolFromDataTableList = selectedRolFromDataTableList;
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

	/**
	 * Tree olusturulurken loadTree metodu tarafindan cagirilarak nodlarin eklenmesini saglar.
	 * 
	 * @param menuItemList
	 * @param tempNode
	 * @param node2RootMenuItem
	 */
	private void addChildNodes(MenuItem menuItem, TreeNode<MenuItem> tempNode) {
		menuItem.setCheck(Boolean.FALSE);
		selectedIdsFromTreeMap.put(menuItem.getId(), Boolean.FALSE);
		if (!menuItem.getChildMenuItemList().isEmpty()) {
			for (Iterator<MenuItem> menuIterator = menuItem.getChildMenuItemListSirali().iterator(); menuIterator.hasNext();) {
				MenuItem tempMenuItem = (MenuItem) menuIterator.next();
				if (!tempMenuItem.getStatus())
					continue;
				selectedIdsFromTreeMap.put(tempMenuItem.getId(), Boolean.FALSE);
				tempMenuItem.setCheck(Boolean.FALSE);
				TreeNodeImpl<MenuItem> nodeImpl = new TreeNodeImpl<MenuItem>();// yeni bir node tanimlanir
				tempNode.addChild(tempMenuItem.getId(), nodeImpl);// yeni node ust node a eklenir
				nodeImpl.setData(tempMenuItem);// yeni node a menu bileseni eklenir.
				addChildNodes(tempMenuItem, nodeImpl);
			}
		}
	}

	/**
	 * Treenin oluÅŸturuldugu Metod
	 */
	@SuppressWarnings("unchecked")
	private void loadTree(TreeNode<MenuItem> rootNode, List<MenuItem> treeDataList) {

		try {
			for (MenuItem tempMenuItem : treeDataList) {
				tempMenuItem.setCheck(Boolean.FALSE);
				selectedIdsFromTreeMap.put(tempMenuItem.getId(), Boolean.FALSE);
				if (tempMenuItem.getTopMenu()) {
					TreeNodeImpl<MenuItem> nodeImpl = new TreeNodeImpl<MenuItem>();// yeni bir top menu node tanimlanir
					rootNode.addChild(tempMenuItem.getId(), nodeImpl);// yeni node root a eklenir
					nodeImpl.setData(tempMenuItem);// yeni node a menu bileseni eklenir.
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

	public ArrayList<MenuItem> getAllTreeMenuItemList() {
		return allTreeMenuItemList;
	}

	public void setAllTreeMenuItemList(ArrayList<MenuItem> allTreeMenuItemList) {
		this.allTreeMenuItemList = allTreeMenuItemList;
	}

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

	public ArrayList<MenuItem> getMenuItemList() {
		HashMap parametreMap = new HashMap();
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);

		ArrayList<MenuItem> tempMenuItemList = (ArrayList<MenuItem>) pdksEntityController.getObjectByInnerObjectList(parametreMap, MenuItem.class);
		return tempMenuItemList;
	}

	public void fillPermissionList() {
		HashMap parametreMap = new HashMap();

		parametreMap.put("target", selectedMenuItem.getName());
		parametreMap.put("status", Boolean.TRUE);
		parametreMap.put("action", AccountPermission.ACTION_VIEW);
		parametreMap.put("discriminator", AccountPermission.DISCRIMINATOR_ROLE);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);

		ArrayList<AccountPermission> tempPermissionList = (ArrayList<AccountPermission>) pdksEntityController.getObjectByInnerObjectList(parametreMap, AccountPermission.class);
		setPermissionList4ModelPanel(tempPermissionList);
	}

	/**
	 * Menu agacÄ±nda R resmine tiklandiginda outputpaneldeki table da gosterilen permissionlari gosterir.
	 */
	public ArrayList<AccountPermission> getPermissionList4ModelPanel() {
		return permissionList4ModelPanel;
	}

	public void setPermissionList4ModelPanel(ArrayList<AccountPermission> permissionList4ModelPanel) {
		this.permissionList4ModelPanel = permissionList4ModelPanel;
	}

	/**
	 * 
	 */
	@Transactional
	public String deleteAccountPermissionFromModelPanel(AccountPermission selectedAccPermission) {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		ArrayList<AccountPermission> deleteAccountPermissionList = new ArrayList<AccountPermission>();
		// permissionun ait oldugu menuitem bulunur.
		HashMap parametreMap = new HashMap();

		parametreMap.put("name", selectedAccPermission.getTarget());
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		MenuItem menuItem = (MenuItem) pdksEntityController.getObjectByInnerObject(parametreMap, MenuItem.class, "");
		// permissionun ait oldugu rol bulunur.
		parametreMap.clear();

		parametreMap.put("rolename", selectedAccPermission.getRecipient());
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		Role rol = (Role) pdksEntityController.getObjectByInnerObject(parametreMap, Role.class, "");

		// permissionlar cekilir ve mape konur.
		parametreMap.clear();
		parametreMap.put("status", Boolean.TRUE);
		parametreMap.put("action", AccountPermission.ACTION_VIEW);
		parametreMap.put("discriminator", AccountPermission.DISCRIMINATOR_ROLE);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<AccountPermission> permissionList = pdksEntityController.getObjectByInnerObjectList(parametreMap, AccountPermission.class);
		String key = "";
		parametreMap.clear();
		for (AccountPermission accountPermission : permissionList) {
			key = accountPermission.getTarget() + "-" + accountPermission.getRecipient();
			parametreMap.put(key, accountPermission);
		}
		// secilen pemission silinir
		deleteAccountPermissionList.add(selectedAccPermission);
		// menuitemin childlarindan bu role ait permission silinir.
		altMenuBilesenlerindenYetkileriCikar(deleteAccountPermissionList, parametreMap, rol, menuItem);

		if (!deleteAccountPermissionList.isEmpty()) {
			for (Iterator iterator = deleteAccountPermissionList.iterator(); iterator.hasNext();) {
				AccountPermission accountPermission = (AccountPermission) iterator.next();
				accountPermission.setStatus(Boolean.FALSE);
				pdksEntityController.saveOrUpdate(session, entityManager, accountPermission);
			}
			session.flush();
			startupAction.fillStartMethod(authenticatedUser, session);
		}

		getSelectedMenuItemFromTreeList().clear();
		return "k";
	}

	public MenuItemPermissionTanimlama() {
		setEjbql("select menuItem from MenuItem menuItem");
	}

	public Map<Long, Boolean> getSelectedIdsFromTreeMap() {
		return selectedIdsFromTreeMap;
	}

	public void setSelectedIdsFromTreeMap(TreeMap<Long, Boolean> selectedIdsFromTreeMap) {
		this.selectedIdsFromTreeMap = selectedIdsFromTreeMap;
	}

	public Map<Long, Boolean> getSelectedIdsFromDataTableMap() {
		return selectedIdsFromDataTableMap;
	}

	public void setSelectedIdsFromDataTableMap(TreeMap<Long, Boolean> selectedIdsFromDataTableMap) {
		this.selectedIdsFromDataTableMap = selectedIdsFromDataTableMap;
	}

	public ArrayList<MenuItem> getSelectedNodeChildren() {
		return selectedNodeChildren;
	}

	public void setSelectedNodeChildren(ArrayList<MenuItem> selectedNodeChildren) {
		this.selectedNodeChildren = selectedNodeChildren;
	}

	public void setSelectedRolFromDataTableList(ArrayList<Role> selectedRolFromDataTableList) {
		this.selectedRolFromDataTableList = selectedRolFromDataTableList;
	}

}
