package com.pdks.quartz.model;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.pdks.dao.PdksDAO;
import com.pdks.dao.impl.BaseDAOHibernate;
import com.pdks.entity.Parameter;
import com.pdks.entity.ServiceData;
import com.pdks.genel.model.Constants;
import com.pdks.genel.model.PdksUtil;

public final class LogRefresh extends QuartzJobBean {

	private Logger logger = Logger.getLogger(LogRefresh.class);

	// ERROR //
	protected void executeInternal(org.quartz.JobExecutionContext ctx) throws org.quartz.JobExecutionException {
		tesisRefresh(logger);
	}

	public void tesisRefresh(Logger logger) {
		PdksDAO pdksDAO = Constants.pdksDAO;
		HashMap fields = new HashMap();
		fields.put("name", "kgsMasterUpdate");
		fields.put("active", Boolean.TRUE);
		Parameter parameter = (Parameter) pdksDAO.getObjectByInnerObject(fields, Parameter.class);
		if (parameter != null && parameter.getValue().equals("2")) {
			fields.clear();
			fields.put(BaseDAOHibernate.MAP_KEY_SELECT, "SP_GET_PDKS_SIRKET_ISLEM");
			try {
				pdksDAO.execSP(fields);
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}

		}
		try {
			fields.clear();
			Date olusturmaTarihi = PdksUtil.tariheAyEkleCikar(new Date(), -2);
			fields.put("olusturmaTarihi<", olusturmaTarihi);
			List dataList = pdksDAO.getObjectByInnerObjectListInLogic(fields, ServiceData.class);
			if (dataList != null && !dataList.isEmpty())
				pdksDAO.deleteObjectList(dataList);
		} catch (Exception e) {

		}

	}

}
