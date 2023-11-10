package com.pdks.restService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.jboss.seam.annotations.Name;
import org.pdks.entity.Personel;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaGun;
import org.pdks.session.OrtakIslemler;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

@Service
@Path(ServiceRestful.PATH)
@Name("serviceRestful")
public class ServiceRestful implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -576918797796931536L;

	public static final String PATH = "/srv";

	static Logger logger = Logger.getLogger(ServiceRestful.class);

	public static final String OPERATION_SUCCESS = "SUCCESS";
	public static final String OPERATION_ERROR = "ERROR";

	private static EntityManager entityManager;

	Session sessionSQL;

	Gson gson = new Gson();

	public ServiceRestful() {
		super();

	}

	/**
	 * @return
	 */
	private Session getSession() {
		Session session = null;
		try {
			if (entityManager == null)
				entityManager = Persistence.createEntityManagerFactory("pdks").createEntityManager();
			if (entityManager != null)
				session = PdksUtil.getSession(entityManager, Boolean.FALSE);
			if (session != null)
				session.clear();
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}

		return session;
	}

	@POST
	@Path("personelCalismaPlaniGetir")
	@Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public String personelCalismaPlaniGetir(@QueryParam("perKodu") String pdksSicilNo, @QueryParam("basTarih") String basTarih, @QueryParam("bitTarih") String bitTarih) throws Exception {
		if (sessionSQL == null)
			sessionSQL = getSession();
		BaseReturn<LinkedHashMap<String, Object>> resp = new BaseReturn<LinkedHashMap<String, Object>>();
		LinkedHashMap<String, Object> map = null;
		String message = null;
		if (sessionSQL != null) {
			PdksEntityController controller = new PdksEntityController();
			HashMap fields = new HashMap();
			fields.put("pdksSicilNo", pdksSicilNo);
			fields.put(PdksEntityController.MAP_KEY_SESSION, sessionSQL);
			Personel personel = (Personel) controller.getObjectByInnerObject(fields, Personel.class);
			if (personel != null) {
				OrtakIslemler ortakIslemler = new OrtakIslemler();
				List<Personel> personeller = new ArrayList<Personel>();
				personeller.add(personel);
				Date baslamaTarih = PdksUtil.getDateFromString(basTarih);
				Date bitisTarih = PdksUtil.getDateFromString(bitTarih);
				if (baslamaTarih != null && bitisTarih != null) {
					TreeMap<String, VardiyaGun> vgunMap = ortakIslemler.getVardiyalar(personeller, baslamaTarih, bitisTarih, null, false, sessionSQL, false);
					if (vgunMap != null && !vgunMap.isEmpty())
						map = new LinkedHashMap<String, Object>();
					for (String key : vgunMap.keySet()) {
						VardiyaGun vardiyaGun = vgunMap.get(key);
						if (vardiyaGun != null && vardiyaGun.getVardiyaDate() != null) {
							String tarih = PdksUtil.convertToDateString(vardiyaGun.getVardiyaDate(), "yyyyMMdd");
							LinkedHashMap<String, Object> value = new LinkedHashMap<String, Object>();
							Vardiya vardiya = vardiyaGun.getVardiya();
							value.put("kodu", vardiya.getKisaAdi());
							value.put("calisma", vardiya.isCalisma());
							if (vardiyaGun.getTatil() != null)
								value.put("arife", vardiyaGun.getTatil().getYarimGun());
							map.put(tarih, value);

						}
					}
				} else
					message = " Tarih bilgisi eksik!";
			} else
				message = pdksSicilNo + " kodlu personel bulunamadı!";
		} else
			message = " Bağlantı kurulumadı!";
		if (message == null && map == null)
			message = " Hata oluştu!";
		resp.setMessage(message);
		resp.setCode(map != null ? OPERATION_SUCCESS : OPERATION_ERROR);
		resp.setData(map);
		String strGson = gson.toJson(resp);
		return strGson;
	}
}
