package com.pdks.system.action;

import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.pdks.dao.PdksDAO;
import com.pdks.genel.model.Constants;
import com.pdks.genel.model.PdksUtil;
import com.pdks.webService.PdksVeriOrtakAktar;

/**
 * @author Hasan Sayar
 * 
 */
public class Startup extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2823327185607753266L;
	private Logger logger = Logger.getLogger(Startup.class);

	ServletContext sc = null;

	public void init() throws ServletException {
		logger.info(Constants.UYGULAMA_VERSION + " start in " + new Date());
		ServletConfig servletConfig = getServletConfig();
		ApplicationContext appCtx = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		PdksDAO pdksDAO = (PdksDAO) appCtx.getBean("pdksDAOIntercepted");
		Constants.pdksDAO = pdksDAO;
		DataSource webDS = null;
		sc = getServletContext();
		PdksUtil.setSc(sc);
		PdksUtil.setServletConfig(servletConfig);
		try {
			javax.naming.Context ctx = new javax.naming.InitialContext();
			webDS = (DataSource) ctx.lookup(servletConfig.getInitParameter(Constants.WEB_DATASOURCE));

			sc.setAttribute(Constants.WEB_DATASOURCE, webDS);
			PdksVeriOrtakAktar pdksVeriOrtakAktar = new PdksVeriOrtakAktar();
			pdksVeriOrtakAktar.sistemVerileriniYukle(pdksDAO);
		} catch (Exception e) {

		}
		logger.info(Constants.UYGULAMA_VERSION + " start out " + new Date());

	}

}
