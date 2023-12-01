package org.pdks.security.action;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class PdksApplicationContext implements ApplicationContextAware {
	
	private static ApplicationContext CONTEXT;
	
	static Logger logger = Logger.getLogger(PdksApplicationContext.class);

	public void setApplicationContext(ApplicationContext context) throws BeansException {
		CONTEXT = context;
	}

	public static Object getBean(String beanName) {
		Object object = null;
		try {
			object = CONTEXT.getBean(beanName);
		} catch (Exception e) {
			logger.debug(beanName + "\n" + e);
		}
		return object;
	}
}
