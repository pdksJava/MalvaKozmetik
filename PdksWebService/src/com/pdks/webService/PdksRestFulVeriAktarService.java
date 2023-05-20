package com.pdks.webService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.pdks.genel.model.PdksUtil;
import com.pdks.mail.model.MailObject;
import com.pdks.mail.model.MailStatu;

@Service
@Path("/services")
public class PdksRestFulVeriAktarService implements Serializable {

	/**
	 * 
	 */

	public static final long serialVersionUID = -2420146759483423027L;

	public Logger logger = Logger.getLogger(PdksRestFulVeriAktarService.class);

	@Context
	HttpServletRequest request;
	@Context
	HttpServletResponse response;

	private Gson gson = new Gson();

	/**
	 * @return
	 * @throws Exception
	 */
	@POST
	@Path("/sendMail")
	@Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response sendMail() throws Exception {
		MailObject mail = null;
		String sonuc = "";
		String data = getBodyString();
		MailStatu mailStatu = null;
		if (data != null) {
			mail = gson.fromJson(data, MailObject.class);
			PdksVeriOrtakAktar ortakAktar = new PdksVeriOrtakAktar();
			try {
				mailStatu = ortakAktar.sendMail(mail);
			} catch (Exception e) {
				mailStatu = new MailStatu();
				if (e.getMessage() != null) {
					String mesaj = e.getMessage();
					if (mesaj.indexOf("{") >= 0)
						PdksUtil.replaceAll(mesaj, "{", "[");
					if (mesaj.indexOf("}") >= 0)
						PdksUtil.replaceAll(mesaj, "}", "]");
					mailStatu.setHataMesai(mesaj);
				}

				else
					mailStatu.setHataMesai("Hata oluştu!");

			}
			sonuc = gson.toJson(mailStatu);
		}
		Response response = null;
		try {
			response = Response.ok(sonuc, MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
		}
		return response;
	}
	
	/**
	 * @return
	 * @throws Exception
	 */
	@POST
	@Path("/helpDeskDate")
	@Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response helpDeskDate() throws Exception {
		PdksVeriOrtakAktar ortakAktar = new PdksVeriOrtakAktar();
		String status = ortakAktar.helpDeskDate();
		HashMap<String, Object> durumMap = new HashMap<String, Object>();
		durumMap.put("dt", status);
		Response response = null;
		try {
			String sonuc = gson.toJson(durumMap);
			response = Response.ok(sonuc, MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
		}
		return response;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	@POST
	@Path("/helpDeskStatus")
	@Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response helpDeskStatus() throws Exception {
		PdksVeriOrtakAktar ortakAktar = new PdksVeriOrtakAktar();
		Boolean status = ortakAktar.helpDeskStatus();
		HashMap<String, Object> durumMap = new HashMap<String, Object>();
		durumMap.put("status", status);
		Response response = null;
		try {
			String sonuc = gson.toJson(durumMap);
			response = Response.ok(sonuc, MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
		}
		return response;
	}

	/**
	 * @param izinList
	 * @return
	 * @throws Exception
	 */
	@POST
	@Path("/saveIzinler")
	@Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response saveIzinler() throws Exception {
		List<IzinERP> izinList = null;
		String sonuc = "";
		String data = getBodyString();
		if (data != null) {
			List<LinkedTreeMap> jsonList = gson.fromJson(data, List.class);
			izinList = new ArrayList<IzinERP>();
			for (LinkedTreeMap map : jsonList) {
				String json = gson.toJson(map);
				IzinERP izinERP = gson.fromJson(json, IzinERP.class);
				izinList.add(izinERP);
			}
			jsonList = null;
			if (!izinList.isEmpty()) {
				PdksVeriOrtakAktar ortakAktar = new PdksVeriOrtakAktar();
				ortakAktar.saveIzinler(izinList);
				sonuc = gson.toJson(izinList);
			}
		}
		Response response = null;
		try {
			response = Response.ok(sonuc, MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
		}
		return response;
	}

	/**
	 * @param personelList
	 * @throws Exception
	 */
	@POST
	@Path("/savePersoneller")
	@Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response savePersoneller() throws Exception {
		List<PersonelERP> personelList = null;
		String data = getBodyString();
		if (data != null) {
			List<LinkedTreeMap> list3 = gson.fromJson(data, List.class);
			personelList = new ArrayList<PersonelERP>();
			TreeMap<String, PersonelERP> perMap = new TreeMap<String, PersonelERP>();
			TreeMap<String, TreeMap> dataMap = new TreeMap<String, TreeMap>();
			dataMap.put("personelERPMap", perMap);
			for (LinkedTreeMap map : list3) {
				String json = gson.toJson(map);
				try {
					PersonelERP personelERP = gson.fromJson(json, PersonelERP.class);
					personelERP.setYazildi(Boolean.FALSE);
					personelList.add(personelERP);

					perMap.put(personelERP.getPersonelNo(), personelERP);
				} catch (Exception e) {

				}
			}
			if (!personelList.isEmpty()) {
				PdksVeriOrtakAktar ortakAktar = new PdksVeriOrtakAktar();
				ortakAktar.savePersoneller(personelList);

			}

		} else
			personelList = new ArrayList<PersonelERP>();
		data = gson.toJson(personelList);
		Response response = null;
		try {
			response = Response.ok(data, MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
		}
		return response;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	private String getBodyString() throws Exception {
		String data = PdksUtil.StringToByInputStream(request.getInputStream());
		return data;
	}
}
