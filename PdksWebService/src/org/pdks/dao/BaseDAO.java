/*
 * Created on 28.Ara.2006
 *
 * 
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.pdks.dao;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

import org.hibernate.Session;
import org.pdks.genel.model.DAO;

/**
 * @author Hasan Sayar
 * 
 * 
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public interface BaseDAO extends DAO {

	/**
	 * @return
	 */
	public Session openSession();

	/**
	 * @return
	 */
	public Session getHibernateCurrentSession();
	
	/**
	 * @param list
	 * @param method
	 * @param uzerineYaz
	 * @return
	 */
	public TreeMap getTreeMapByList(List list, String method, boolean uzerineYaz);

	/**
	 * obje listesini döndürür.
	 * 
	 * @param class1
	 * @return
	 */
	public List getObjectList(Class class1);

	/**
	 * id bilgisi verilen objeyi döndürür.
	 * 
	 * @param id
	 * @param class1
	 * @return
	 */
	public Object getObjectStringId(String id, Class class1);

	/**
	 * Inner Objecti ve id bilgisi verilen obje listesini döndürür.
	 * 
	 * @param id
	 * @param class1
	 * @return
	 */

	public List getObjectByInnerObjectList(String fieldName, Object fieldValue, Class class1);

	/**
	 * Istenilgi kadar parametre HashMap e eklenerek gönderilir ve obje listesi döndürür.
	 * 
	 * @param fields
	 * @param class1
	 * @return
	 */

	public List getObjectByInnerObjectList(HashMap fields, Class class1);

	/**
	 * Istenilgi kadar parametre HashMap e eklenerek gönderilir ve obje döndürür.
	 * 
	 * @param fields
	 * @param class1
	 * @return
	 */
	public Object getObjectByInnerObject(HashMap fields, Class class1);

	/**
	 * Inner Objecti ve id bilgisi verilen objeyi döndürür.
	 * 
	 * @param id
	 * @param class1
	 * @return
	 */
	public Object getObjectByInnerObject(String fieldName, Object fieldValue, Class class1);

	/**
	 * id bilgisi verilen objeyi döndürür.
	 * 
	 * @param id
	 * @param class1
	 * @return
	 */
	public Object getObjectNumericId(String id, Class class1);

	/**
	 * objesini kaydeder.
	 * 
	 * @param object
	 */
	public void saveObject(Object object);

	/**
	 * Verilen listedeki objelerin listesini döndürür.
	 * 
	 * @param list
	 *            obje key stringleri listesi
	 * @param class1
	 *            class bilgisi için dönülecek nesnelerin tipinde olmalıdır.
	 * @return
	 */
	public List getObjectList(List list, Class class1);

	/**
	 * HIS objesini siler.
	 * 
	 * @param object
	 */
	public void deleteObject(Object object);

	/**
	 * HIS objelerini siler.
	 * 
	 * @param object
	 */
	public void deleteObjectList(List list);

	/**
	 * Objeleri liste olarak kaydetme ve silme işlemlerini yürütür.
	 * 
	 * @param saveList
	 * @param deleteList
	 */
	public void saveAndDeleteObjectList(List saveList, List deleteList);

	/**
	 * @param objectList
	 */
	public void saveObjectParcaliList(Collection objectList);

	/**
	 * obje listesini kaydeder
	 * 
	 * @param objectList
	 */
	public void saveObjectList(List objectList);

	/**
	 * Istenilgi kadar parametre HashMap e eklenerek gönderilir ve obje listesi döndürür.
	 * 
	 * @param fields
	 * @param class1
	 * @return
	 */
	public List getObjectByInnerObjectListInLogic(HashMap fields, Class class1);

	/**
	 * Obje ve obje'ye bağlı listeyi kaydeder.
	 * 
	 * @param object
	 * @param saveDetayObjectList
	 */
	public void saveObjectAndList(Object object, List saveDetayObjectList);

	/**
	 * Istenilgi kadar parametre HashMap e eklenerek gönderilir ve obje treemap'de döndürür.
	 * 
	 * @param fields
	 * @param class1
	 * @return
	 */
	public TreeMap getObjectByInnerObjectMapInLogic(HashMap fields, Class class1, boolean uzerineYaz);

	/**
	 * Istenilgi kadar parametre HashMap e eklenerek gönderilir ve obje treemap'de döndürür.
	 * 
	 * @param fields
	 * @param class1
	 * @return
	 */
	public TreeMap getObjectByInnerObjectMap(HashMap fields, Class class1, boolean uzerineYaz);

	/**
	 * @param method
	 * @param class1
	 * @param uzerineYaz
	 * @return
	 */
	public TreeMap getObjectMap(String method, Class class1, boolean uzerineYaz);

	/**
	 * @param method
	 * @param fieldName
	 * @param fieldValue
	 * @param class1
	 * @param uzerineYaz
	 * @return
	 */
	public TreeMap getObjectByInnerObjectMap(String method, String fieldName, Object fieldValue, Class class1, boolean uzerineYaz);

	/**
	 * obje kaydeder objeye bağlı saveList'deki detay objeleri kaydedip ,deleteList'deki objeleri de siler.
	 * 
	 * @param object
	 * @param saveList
	 * @param deleteList
	 */
	public void saveObjectAndList(Object object, List saveList, List deleteList);

	/**
	 * @param saveObjectArray
	 * @param deleteObjectArray
	 */
	public void deleteAndSaveObject(Object[] saveObjectArray, Object[] deleteObjectArray);

	/**
	 * @param fields
	 */
	public void execSP(LinkedHashMap<String, Object> fields);

	/**
	 * @param veriMap
	 * @param class1
	 * @return
	 * @throws Exception
	 */
	public List execSPList(LinkedHashMap<String, Object> veriMap, Class class1) throws Exception;

	/**
	 * @param fields
	 * @return
	 */
	public List getSPList(HashMap fields);

	/**
	 * @param fields
	 * @param sb
	 * @param class1
	 * @return
	 */
	public List getNativeSQLList(HashMap fields, StringBuffer sb, Class class1);
}
