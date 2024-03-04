package org.pdks.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.log4j.Logger;
import org.pdks.entity.LDAPDomain;
import org.pdks.entity.Personel;
import org.pdks.security.entity.User;
import org.jboss.seam.annotations.Name;

/**
 * @author Hasan Sayar
 * 
 */
@Name("ldapUserManager")
public class LDAPUserManager implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7792161735809981264L;

	public static final String USER_ATTRIBUTES_SN = "sn";// soyad

	public static final String USER_ATTRIBUTES_GIVEN_NAME = "givenName";// ad

	public static final String USER_ATTRIBUTES_SAM_ACCOUNT_NAME = "sAMAccountName";// sicil numarası

	public static final String USER_ATTRIBUTES_PRINCIPAL_NAME = "userPrincipalName";// userName

	public static final String USER_ATTRIBUTES_ACCOUNT_CONTROL = "userAccountControl";// statu

	public static final String USER_ATTRIBUTES_DISPLAY_NAME = "displayName";//

	public static final String USER_ATTRIBUTES_MAIL = "mail";// email

	public static final String USER_ATTRIBUTES_MEMBER = "member";// ad

	public static final String[] LDAP_HATA_KODLARI = new String[] { "525", "52e", "531", "532", "533", "701", "773", "775" };
	public static final String LDAP_HATA_KODU_KEY_ON_EK = "ortak.hata.ldap.";

	private static Logger logger = Logger.getLogger(LDAPUserManager.class);

	private static String ldapHost = "";

	private static String ldapPort = "";

	private static String ldapAdminUsername = "";

	private static String ldapAdminPassword = "";

	private static String ldapDC = "";

	private static String ldapOnEkler = "";

	private static List<LDAPDomain> ldapUserList;

	private LDAPUserManager() {
	}

	/**
	 * @return
	 */
	public static LDAPDomain getDefaultLDAPUser() {
		LDAPDomain ldapUser = null;
		if (ldapHost != null)
			ldapUser = new LDAPDomain(ldapHost, ldapPort, ldapAdminUsername, ldapAdminPassword, ldapDC);
		return ldapUser;
	}

	/**
	 * @param ldapUsername
	 * @param ldapPassword
	 * @return
	 */
	public static boolean authenticate(String ldapUsername, String ldapPassword) {
		boolean authenticated = false;
		List list = new ArrayList(getLdapUserList());
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			LDAPDomain lu = (LDAPDomain) iterator.next();
			User kullanici = null;
			try {
				kullanici = findLDAPUser(ldapUsername, USER_ATTRIBUTES_PRINCIPAL_NAME, list, lu);
			} catch (Exception ee) {
				logger.error(ldapUsername + " " + lu.getLdapHost());
			}
			if (kullanici == null || !kullanici.isDurum())
				continue;

			Hashtable env = new Hashtable();
			String ldapURL = "ldap://" + lu.getLdapHost() + ":" + lu.getLdapPort();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			env.put(Context.SECURITY_PRINCIPAL, ldapUsername);
			env.put(Context.SECURITY_CREDENTIALS, ldapPassword);
			env.put(Context.PROVIDER_URL, ldapURL);
			DirContext ctx = null;
			try {
				ctx = new InitialDirContext(env);
				authenticated = true;
			} catch (Exception ex) {
				if (list.size() == 1)
					logger.error(ex.getMessage());
			} finally {
				try {
					if (ctx != null)
						ctx.close();
				} catch (Exception e) {
					logger.error(e);
					e.printStackTrace();
					System.err.println("Hata esatis : " + e.getMessage());
				}
			}
			if (authenticated) {
				// if (list.size() > 1) logger.info(ldapUsername + " login " + ldapURL);
				break;
			}

		}
		list = null;
		return authenticated;
	}

	/**
	 * @param email
	 * @return
	 */
	public static User getLDAPUserAttributes(String userName) {
		return getLDAPUserAttributes(userName, USER_ATTRIBUTES_PRINCIPAL_NAME);

	}

	/**
	 * LDAP'ta bulunan bir kullanıcının mail adresi ile kullanıcı bilgilerine ulaşır ve ad,soyad,sicil numarası bilgilerini alır
	 * 
	 * @param email
	 * @return
	 */
	public static User getLDAPUserAttributes(String value, String tip) {
		User kullanici = null;
		List<LDAPDomain> list = new ArrayList(getLdapUserList());
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			LDAPDomain lu = (LDAPDomain) iterator.next();
			kullanici = findLDAPUser(value, tip, list, lu);
			if (kullanici != null && kullanici.isDurum())
				break;
		}
		list = null;
		return kullanici;

	}

	/**
	 * @param user
	 * @throws Exception
	 */
	public static void fillAdiSoyadi(User user) throws Exception {
		if (user != null && user.getFirstname() != null && user.getLastname() != null && user.getFullName() != null) {
			String[] adiArray = user.getFirstname().split(" "), soyadiArray = user.getLastname().split(" "), fullNameArray = user.getFullName().split(" ");
			if (fullNameArray.length >= (adiArray.length + soyadiArray.length)) {
				StringBuffer sbAdi = new StringBuffer(), sbSoyadi = new StringBuffer();
				int adiLength = adiArray.length > fullNameArray.length ? fullNameArray.length : adiArray.length;
				for (int i = 0; i < adiLength; i++) {
					if (sbAdi.length() > 0)
						sbAdi.append(" ");
					sbAdi.append(fullNameArray[i].trim());
				}
				for (int i = adiLength; i < fullNameArray.length; i++) {
					if (sbSoyadi.length() > 0)
						sbSoyadi.append(" ");
					sbSoyadi.append(fullNameArray[i].trim());
				}
				if (user.getPdksPersonel() == null || user.getPdksPersonel().getId() == null) {
					Personel personel = user.getPdksPersonel() == null ? new Personel() : user.getPdksPersonel();
					personel.setAd(sbAdi.toString());
					personel.setSoyad(sbSoyadi.toString());
					user.setPdksPersonel(personel);
				} else {
					user.setFirstname(sbAdi.toString());
					user.setLastname(sbSoyadi.toString());
				}

				sbAdi = null;
				sbSoyadi = null;
			}
			adiArray = null;
			soyadiArray = null;
			fullNameArray = null;
		}
	}

	private static User findLDAPUser(String value, String tip, List list, LDAPDomain lu) {
		User user = null;
		Hashtable env = new Hashtable();
		String ldapURL = "ldap://" + lu.getLdapHost() + ":" + lu.getLdapPort();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, lu.getLdapAdminUsername());
		env.put(Context.SECURITY_CREDENTIALS, lu.getLdapAdminPassword());
		env.put(Context.PROVIDER_URL, ldapURL);
		LdapContext ctx = null;
		try {
			// Create the initial directory context
			ctx = new InitialLdapContext(env, null);

			// Create the search controls
			SearchControls searchCtls = new SearchControls();

			// Specify the search scope
			searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			String objectClass = "&(objectClass=user)";
			if (!PdksUtil.hasStringValue(tip)) {
				objectClass = "&";
				tip = USER_ATTRIBUTES_MAIL;
			}
			// specify the LDAP search filter
			String searchFilter = "(" + objectClass;
			if (value != null && tip != null)
				searchFilter += "(" + tip + " = " + value + ")";
			searchFilter += ")";

			// Specify the Base for the search
			String searchBase = lu.getLdapDC();

			// initialize counter to total the group members
			// Specify the attributes to return
			String returnedAtts[] = { USER_ATTRIBUTES_PRINCIPAL_NAME, USER_ATTRIBUTES_GIVEN_NAME, USER_ATTRIBUTES_ACCOUNT_CONTROL, USER_ATTRIBUTES_SAM_ACCOUNT_NAME, USER_ATTRIBUTES_SN, USER_ATTRIBUTES_MAIL, USER_ATTRIBUTES_MEMBER, USER_ATTRIBUTES_DISPLAY_NAME };
			searchCtls.setReturningAttributes(returnedAtts);

			// Search for objects using the filter
			NamingEnumeration answer = ctx.search(searchBase, searchFilter, searchCtls);

			if (answer.hasMoreElements())
				user = new User();

			// LDAP'tan gelen ad,soyad,scili numarası bilgilerini kullanici nesnesine koyuyoruz.
			// Loop through the search results
			while (user != null && answer.hasMoreElements()) {
				SearchResult sr = null;
				Attributes attrs = null;
				try {
					sr = (SearchResult) answer.next();
				} catch (Exception e) {
					logger.error(e);
					e.printStackTrace();
					System.err.println("Hata esatis : " + e.getMessage());
					sr = null;
				}

				// Print out the groups
				if (sr != null)
					attrs = sr.getAttributes();
				if (attrs != null) {
					try {
						user.setUsername("");
						user.setFirstname("");
						user.setLastname("");
						user.setStaffId("");
						user.setEmail("");
						user.setFullName("");
						user.setDurum(Boolean.TRUE);
						ArrayList<String> members = new ArrayList<String>();
						user.setYetkiliPersonelNoList(members);
						for (NamingEnumeration ae = attrs.getAll(); ae.hasMore();) {
							Attribute attr = (Attribute) ae.next();
							// logger.debug("Attribute: " +
							// attr.getID()+" "+(String)attr.get(0));
							String deger = (String) attr.get(0);
							if (attr.getID().equalsIgnoreCase(USER_ATTRIBUTES_PRINCIPAL_NAME)) {
								if (PdksUtil.hasStringValue(deger))
									deger = deger.toLowerCase(Locale.ENGLISH);
								user.setUsername(deger);

							} else if (attr.getID().equalsIgnoreCase(USER_ATTRIBUTES_GIVEN_NAME))
								user.setFirstname(deger);
							else if (attr.getID().equalsIgnoreCase(USER_ATTRIBUTES_DISPLAY_NAME))
								user.setFullName(PdksUtil.hasStringValue(deger) ? deger.toUpperCase(Constants.TR_LOCALE) : deger);
							else if (attr.getID().equalsIgnoreCase(USER_ATTRIBUTES_SN))
								user.setLastname(deger);
							else if (attr.getID().equalsIgnoreCase(USER_ATTRIBUTES_MEMBER)) {
								for (int i = 0; i < attr.size(); i++) {
									String cn = (String) attr.get(i);
									if (cn.startsWith("CN="))
										members.add(cn);
								}

							}

							else if (attr.getID().equalsIgnoreCase(USER_ATTRIBUTES_MAIL)) {
								if (PdksUtil.hasStringValue(deger))
									deger = deger.toLowerCase(Locale.ENGLISH);
								user.setEmail(deger);
							} else if (attr.getID().equalsIgnoreCase(USER_ATTRIBUTES_SAM_ACCOUNT_NAME)) {
								user.setStaffId(deger.trim());
								user.setShortUsername(deger.trim());
							} else if (attr.getID().equalsIgnoreCase(USER_ATTRIBUTES_ACCOUNT_CONTROL)) {
								String accountControl = (String) attr.get(0);
								user.setDurum(accountControl == null || !(accountControl.equals("514") || accountControl.equals("546")));
							}

						}
						fillAdiSoyadi(user);

					} catch (Exception e) {
						logger.error("Problem listing membership: " + e);
					}
				}
			}

		} catch (NamingException e) {
			if (list == null || list.size() == 1)
				logger.error("Problem searching directory: " + e);
		} finally {
			try {
				if (ctx != null)
					ctx.close();
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
				System.err.println("Hata esatis : " + e.getMessage());
			}
		}
		return user;
	}

	public static List<LDAPDomain> getLdapUserList() {
		if (ldapUserList == null)
			ldapUserList = new ArrayList();
		if (ldapUserList.isEmpty()) {
			LDAPDomain domain = getDefaultLDAPUser();
			if (domain != null)
				ldapUserList.add(domain);
		}

		return ldapUserList;
	}

	public static String ldapHatasininDetayAciklamasiniGetir(Exception e) {
		String tempHataKodu = "";
		String tempExceptionStr = "";
		int konum = e.getMessage().indexOf("error,");
		if (konum == -1)
			return "";
		tempExceptionStr = e.getMessage().substring(konum, e.getMessage().length() - 1);
		for (int i = 0; i < LDAP_HATA_KODLARI.length; i++) {
			tempHataKodu = LDAP_HATA_KODLARI[i];
			konum = tempExceptionStr.indexOf(tempHataKodu);
			if (konum == -1)
				continue;
			logger.debug(tempExceptionStr);
			return tempHataKodu;

		}

		return null;
	}

	public static String getLdapAdminPassword() {
		return ldapAdminPassword;
	}

	public static void setLdapAdminPassword(String ldapAdminPassword) {
		LDAPUserManager.ldapAdminPassword = ldapAdminPassword;
	}

	public static String getLdapAdminUsername() {
		return ldapAdminUsername;
	}

	public static void setLdapAdminUsername(String ldapAdminUsername) {
		LDAPUserManager.ldapAdminUsername = ldapAdminUsername;
	}

	public static String getLdapDC() {
		return ldapDC;
	}

	public static void setLdapDC(String ldapDC) {
		LDAPUserManager.ldapDC = ldapDC;
	}

	public static String getLdapHost() {
		return ldapHost;
	}

	public static void setLdapHost(String ldapHost) {
		LDAPUserManager.ldapHost = ldapHost;
	}

	public static String getLdapPort() {
		return ldapPort;
	}

	public static void setLdapPort(String ldapPort) {
		LDAPUserManager.ldapPort = ldapPort;
	}

	public static void setLdapUserList(List<LDAPDomain> ldapUserList) {
		LDAPUserManager.ldapUserList = ldapUserList;
	}

	public static String getLdapOnEkler() {
		return ldapOnEkler;
	}

	public static void setLdapOnEkler(String ldapOnEkler) {
		LDAPUserManager.ldapOnEkler = ldapOnEkler;
	}
}
