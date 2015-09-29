package Logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

/**
 * This class acts a simple wrapper for the actual logger, that allows to access
 * the logger from everywhere in the code. Since this is a rather hack-ish
 * implementation, switching to an external logging library might happen in the
 * future.
 * 
 * @author sjebbara
 *
 */
public class Log {

	private static Logger logger = LogManager.getFormatterLogger();

	static {
		init();
	}

	public static void init() {
		System.out.println("init logger");
		Configurator.initialize("BIRE-LOGGING", "res/log4j2.xml");
	}

	public static void methodOff() {
	}

	public static void methodOn() {
	}

	public static void off() {
	}

	public static void on() {
	}

	public static void d(String message) {
		logger.debug(message);
	}

	public static void d(String message, Object... args) {
		logger.debug(message, args);
	}

	public static void i(String message) {
		logger.info(message);
	}

	public static void i(String message, Object... args) {
		logger.info(message, args);
	}

	public static void w(String message) {
		logger.warn(message);
	}

	public static void w(String message, Object... args) {
		logger.warn(message, args);
	}

	public static void e(String message) {
		logger.error(message);
	}

	public static void e(String message, Object... args) {
		logger.error(message, args);
	}
}
