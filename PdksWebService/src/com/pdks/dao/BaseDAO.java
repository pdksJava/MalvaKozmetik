/*
 * Created on 28.Ara.2006
 *
 * 
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.pdks.dao;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

import org.hibernate.Session;

import com.pdks.genel.model.DAO;

/**
 * @author Hasan Sayar
 * 
 * 
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public interface BaseDAO extends DAO {

	public Session openSession();

	public Session getHibernateCurrentSession();

	/**
	 * obje listesini d�nd�r�r.
	 * 
	 * @param class1
	 * @return
	 */
	public List getObjectList(Class class1);

	/**
	 * id bilgisi verilen objeyi d�nd�r�r.
	 * 
	 * @param id
	 * @param class1
	 * @return
	 */
	public Object getObjectStringId(String id, Class class1);

	/**
	 * Inner Objecti ve id bilgisi verilen obje listesini d�nd�r�r.
	 * 
	 * @param id
	 * @param class1
	 * @return
	 */

	public List getObjectByInnerObjectList(String fieldName, Object fieldValue, Class class1);

	/**
	 * Istenilgi kadar parametre HashMap e eklenerek g�nderilir ve obje listesi d�nd�r�l�r.
	 * 
	 * @param fields
	 * @param class1
	 * @return
	 */

	public List getObjectByInnerObjectList(HashMap fields, Class class1);

	/**
	 * Istenilgi kadar parametre HashMap e eklenerek g�nderilir ve obje d�nd�r�l�r.
	 * 
	 * @param fields
	 * @param class1
	 * @return
	 */
	public Object getObjectByInnerObject(HashMap fields, Class class1);

	/**
	 * Inner Objecti ve id bilgisi verilen objeyi d�nd�r�r.
	 * 
	 * @param id
	 * @param class1
	 * @return
	 */
	public Object getObjectByInnerObject(String fieldName, Object fieldValue, Class class1);

	/**
	 * id bilgisi verilen objeyi d�nd�r�r.
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
	 * Verilen listedeki objelerin listesini d�nd�r�r.
	 * 
	 * @param list
	 *            obje key stringleri listesi
	 * @param class1
	 *            class bilgisi i�in d�n�lecek nesnelerin tipinde olmal�d�r.
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
	 * Objeleri liste olarak kaydetme ve silme i�lemlerini y�r�t�r.
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
	 * Istenilgi kadar parametre HashMap e eklenerek g�nderilir ve obje listesi d�nd�r�l�r.
	 * 
	 * @param fields
	 * @param class1
	 * @return
	 */
	public List getObjectByInnerObjectListInLogic(HashMap fields, Class class1);

	/**
	 * Obje ve obje'ye ba�l� listeyi kaydeder.
	 * 
	 * @param object
	 * @param saveDetayObjectList
	 */
	public void saveObjectAndList(Object object, List saveDetayObjectList);

	/**
	 * Istenilgi kadar parametre HashMap e eklenerek g�nderilir ve obje treemap'de d�nd�r�l�r.
	 * 
	 * @param fields
	 * @param class1
	 * @return
	 */
	public TreeMap getObjectByInnerObjectMapInLogic(HashMap fields, Class class1, boolean uzerineYaz);

	/**
	 * Istenilgi kadar parametre HashMap e eklenerek g�nderilir ve obje treemap'de d�nd�r�l�r.
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
	 * obje kaydeder objeye ba�l� saveList'deki detay objeleri kaydedip ,deleteList'deki objeleri de siler.
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
	public void execSP(HashMap fields);
	
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
