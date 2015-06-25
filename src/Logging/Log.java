package Logging;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Log {

	private static final String BIRE_LOGGER_NAME = "BIRE";
	private static final Logger log;
	private static final Set<String> mutedMethods = new HashSet<String>();
	private static final Set<String> mutedClasses = new HashSet<String>();

	static {
		log = Logger.getLogger(BIRE_LOGGER_NAME);
		log.setLevel(Level.ALL);
		log.setUseParentHandlers(false);
		ConsoleHandler h = new ConsoleHandler();
		h.setLevel(Level.ALL);
		h.setFormatter(new SimpleLogFormatter());
		log.addHandler(h);
	}

	private static StackTraceElement getCallingMethod() {
		StackTraceElement[] stackTraceElements = Thread.currentThread()
				.getStackTrace();
		if (stackTraceElements != null && stackTraceElements.length >= 3) {
			return stackTraceElements[3];
		} else {
			return null;
		}
	}

	/**
	 * Mutes the logger for all subsequent log calls from this method.
	 */
	public static void off() {
		StackTraceElement e = getCallingMethod();
		mutedMethods.add(getMethodKey(e));
	}

	public static void on() {
		StackTraceElement e = getCallingMethod();
		mutedMethods.remove(getMethodKey(e));
	}

	public static void classOff() {
		StackTraceElement e = getCallingMethod();
		mutedClasses.add(getClassKey(e));
	}

	public static void classOn() {
		StackTraceElement e = getCallingMethod();
		mutedClasses.remove(getClassKey(e));
	}

	private static boolean isMuted(StackTraceElement e) {
		return mutedClasses.contains(getClassKey(e))
				|| mutedMethods.contains(getMethodKey(e));
	}

	private static String getMethodKey(StackTraceElement e) {
		return e.getClassName() + e.getMethodName();
	}

	private static String getClassKey(StackTraceElement e) {
		return e.getClassName();
	}

	public static void log(Level level, String message) {
		StackTraceElement e = getCallingMethod();
		log.logp(level, e.getClassName(), e.getMethodName(), message);
	}

	public static void log(Level level, String message, Object... args) {
		StackTraceElement e = getCallingMethod();
		log.logp(level, e.getClassName(), e.getMethodName(),
				String.format(message, args));
	}

	public static void d(String message) {
		StackTraceElement e = getCallingMethod();
		if (!isMuted(e))
			log.logp(DebugLevel.DEBUG, e.getClassName(), e.getMethodName(),
					message);
	}

	public static void d(String message, Object... args) {
		StackTraceElement e = getCallingMethod();
		if (!isMuted(e))
			log.logp(DebugLevel.DEBUG, e.getClassName(), e.getMethodName(),
					String.format(message, args));
	}

	public static void i(String message) {
		StackTraceElement e = getCallingMethod();
		if (!isMuted(e))
			log.logp(Level.INFO, e.getClassName(), e.getMethodName(), message);
	}

	public static void i(String message, Object... args) {
		StackTraceElement e = getCallingMethod();
		if (!isMuted(e))
			log.logp(Level.INFO, e.getClassName(), e.getMethodName(),
					String.format(message, args));
	}

	public static void c(String message) {
		StackTraceElement e = getCallingMethod();
		if (!isMuted(e))
			log.logp(Level.CONFIG, e.getClassName(), e.getMethodName(), message);
	}

	public static void c(String message, Object... args) {
		StackTraceElement e = getCallingMethod();
		if (!isMuted(e))
			log.logp(Level.CONFIG, e.getClassName(), e.getMethodName(),
					String.format(message, args));
	}

	public static void w(String message) {
		StackTraceElement e = getCallingMethod();
		if (!isMuted(e))
			log.logp(Level.WARNING, e.getClassName(), e.getMethodName(),
					message);
	}

	public static void w(String message, Object... args) {
		StackTraceElement e = getCallingMethod();
		if (!isMuted(e))
			log.logp(Level.WARNING, e.getClassName(), e.getMethodName(),
					String.format(message, args));
	}

	public static void s(String message) {
		StackTraceElement e = getCallingMethod();
		if (!isMuted(e))
			log.logp(Level.SEVERE, e.getClassName(), e.getMethodName(), message);
	}

	public static void s(String message, Object... args) {
		StackTraceElement e = getCallingMethod();
		if (!isMuted(e))
			log.logp(Level.SEVERE, e.getClassName(), e.getMethodName(),
					String.format(message, args));
	}

}
