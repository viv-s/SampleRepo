package org.fstrf.actg.TBOutcomeReport.configuration;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * Class to replace ant spring configuration defined in the web.xml
 * Loaded and instantiated and its onStartup method will be called by the servlet container
 */

public class SpringMVCInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

	@Override
	protected Class<?>[] getRootConfigClasses() {
		return null;
	}

	//maps to servlet config class
	@Override
	protected Class<?>[] getServletConfigClasses() {
		return new Class<?>[]{SpringMVCConfig.class};
	}

	//specifies the servlet mapping
	@Override
	protected String[] getServletMappings() {
		return new String[]{"/"};
	}

}
