package org.fstrf.actg.TBOutcomeReport.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

public class LogUtil {

	 static {
	       // Initialize logging.
	       // Based on the JoranConfigurator example in chapter 3 of the logback manual.
	       // It's important that this code is in the static block of the same class that contains the getLogger() method,
	       // since this must be guaranteed to be run before anything calls getLogger().

	       LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
	       JoranConfigurator logConfig = new JoranConfigurator();
	       logConfig.setContext(context);

	       // don't call context.reset() because that'd remove logging from other apps

	       try {
	               logConfig.doConfigure("@log.config.path@/logback.xml");
	       } catch (JoranException e) {
	           throw new RuntimeException(e);
	       }
	   }

    /**
     * Get a logger for the given class.
     *
     * @param klass class
     * @return logger
     * @since 1.0.0
     */

    public static Logger getLogger(Class<?> klass) {
        return LoggerFactory.getLogger(klass);
    }

    public static Logger getLogger(String logger) {
        return LoggerFactory.getLogger(logger);
    }

}