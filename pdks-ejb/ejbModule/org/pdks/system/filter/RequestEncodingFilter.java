package org.pdks.system.filter;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.pdks.session.PdksUtil;

import org.pdks.erp.action.ConnectionMan;
import org.pdks.erp.action.MyDestinationDataProvider;
import org.pdks.erp.action.SapRfcManager;
import com.sap.conn.jco.ext.Environment;

public class RequestEncodingFilter implements Filter, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7249210497478717720L;
	static Logger logger = Logger.getLogger(RequestEncodingFilter.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
		SapRfcManager.removePoolManager();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		try {
			request.setCharacterEncoding("UTF-8");
			response.setCharacterEncoding("UTF-8");
			browserIE9Control((HttpServletRequest) request, (HttpServletResponse) response);

		} catch (UnsupportedEncodingException e) {

		}
		chain.doFilter(request, response);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig arg0) throws ServletException {
		MyDestinationDataProvider myProvider = null;
		try {
			myProvider = new MyDestinationDataProvider();
			Environment.registerDestinationDataProvider(myProvider);
			logger.info("SAP Pool olustu. " + PdksUtil.getCurrentTimeStampStr());
		} catch (Exception e) {
			logger.error(e);
			myProvider = null;

		}
		ConnectionMan.setMyProvider(myProvider);

	}

	/**
	 * @param req
	 * @param resp
	 */
	private void browserIE9Control(HttpServletRequest req, HttpServletResponse resp) {
		try {
			boolean ie9 = Boolean.FALSE;
			if (req != null)
				ie9 = PdksUtil.isInternetExplorer(req);

			if (resp != null && ie9)
				resp.setHeader("X-UA-Compatible", "IE=EmulateIE8");
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			logger.error("Filter hata : " + e.getMessage());
		}
	}

}
