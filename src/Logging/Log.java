package Logging;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Log {

	private static final String BIRE_LOGGER_NAME = "BIRE";
//	private static Logger log;
	private static Set<String> mutedMethods;
	private static Set<String> mutedClasses;

	static {
		init();
	}

	public static void init() {
//		System.out.println("init logger");
//		log = Logger.getLogger(BIRE_LOGGER_NAME);
//		log.setLevel(Level.ALL);
//		log.setUseParentHandlers(false);
//		ConsoleHandler h = new ConsoleHandler() {
//			{
//				setOutputStream(System.out);
//			}
//		};
//		h.setLevel(Level.ALL);
//		h.setFormatter(new SimpleLogFormatter());
//		log.addHandler(h);
		mutedMethods = new HashSet<String>();
		mutedClasses = new HashSet<String>();
//		Log.status();
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

	public static void status() {
//		System.out
//				.println(String.format("Logger: %s (%s)", log.getName(), log));
//		System.out.println(String.format("Level: %s", log.getLevel()));
//		System.out.println(String.format("Handler: %s",
//				Arrays.toString(log.getHandlers())));
//		System.out.println(String.format("Muted Classes: %s", mutedClasses));
//		System.out.println(String.format("Muted Methods: %s", mutedClasses));
	}

	/**
	 * Mutes the logger for all subsequent log calls from this method.
	 */
	public static void methodOff() {
		StackTraceElement e = getCallingMethod();
		mutedMethods.add(getMethodKey(e));
	}

	public static void methodOn() {
		StackTraceElement e = getCallingMethod();
		mutedMethods.remove(getMethodKey(e));
	}

	public static void off() {
		StackTraceElement e = getCallingMethod();
		mutedClasses.add(getClassKey(e));
	}

	public static void on() {
		StackTraceElement e = getCallingMethod();
		mutedClasses.remove(getClassKey(e));
	}

	private static boolean isMuted(StackTraceElement e) {
		// System.out.println("Muted Classes: " + mutedClasses);
		// System.out.println(e);
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
		logp(level, e.getClassName(), e.getMethodName(), message);
	}

	public static void log(Level level, String message, Object... args) {
		StackTraceElement e = getCallingMethod();
		logp(level, e.getClassName(), e.getMethodName(),
				String.format(message, args));
	}

	public static void d(String message) {
		StackTraceElement e = getCallingMethod();
		if (!isMuted(e))
			logp(DebugLevel.DEBUG, e.getClassName(), e.getMethodName(), message);
	}

	// public static void d(Object message) {
	// StackTraceElement e = getCallingMethod();
	// if (!isMuted(e))
	// log.logp(DebugLevel.DEBUG, e.getClassName(), e.getMethodName(),
	// message == null ? "null" : message.toString());
	// }

	public static void d(String message, Object... args) {
		StackTraceElement e = getCallingMethod();
		if (!isMuted(e))
			logp(DebugLevel.DEBUG, e.getClassName(), e.getMethodName(),
					message, args);
	}

	public static void i(String message) {
		StackTraceElement e = getCallingMethod();
		if (!isMuted(e))
			logp(Level.INFO, e.getClassName(), e.getMethodName(), message);
	}

	public static void i(String message, Object... args) {
		StackTraceElement e = getCallingMethod();
		if (!isMuted(e))
			logp(Level.INFO, e.getClassName(), e.getMethodName(), message, args);
	}

	public static void c(String message) {
		StackTraceElement e = getCallingMethod();
		if (!isMuted(e))
			logp(Level.CONFIG, e.getClassName(), e.getMethodName(), message);
	}

	public static void c(String message, Object... args) {
		StackTraceElement e = getCallingMethod();
		if (!isMuted(e))
			logp(Level.CONFIG, e.getClassName(), e.getMethodName(), message,
					args);
	}

	public static void w(String message) {
		StackTraceElement e = getCallingMethod();
		if (!isMuted(e))
			logp(Level.WARNING, e.getClassName(), e.getMethodName(), message);
	}

	public static void w(String message, Object... args) {
		StackTraceElement e = getCallingMethod();
		if (!isMuted(e))
			logp(Level.WARNING, e.getClassName(), e.getMethodName(), message,
					args);
	}

	public static void s(String message) {
		StackTraceElement e = getCallingMethod();
		if (!isMuted(e))
			logp(Level.SEVERE, e.getClassName(), e.getMethodName(), message);
	}

	public static void s(String message, Object... args) {
		StackTraceElement e = getCallingMethod();
		if (!isMuted(e))
			logp(Level.SEVERE, e.getClassName(), e.getMethodName(), message,
					args);
	}

	private static void logp(Level level, String className, String methodName,
			String message, Object... args) {
//		log.logp(level, className, methodName, String.format(message, args));
		System.out.println(String.format(message, args));
	}

}
