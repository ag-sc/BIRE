package Logging;

import java.util.logging.Level;

public class DebugLevel extends Level {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6387000821965206318L;
	private static final int DEBUG_LEVEL_VALUE = 600;
	private static final String DEBUG_LEVEL_NAME = "DEBUG";
	public static final DebugLevel DEBUG = new DebugLevel();

	private DebugLevel() {
		this(DEBUG_LEVEL_NAME, DEBUG_LEVEL_VALUE);
	}

	protected DebugLevel(String name, int value, String resourceBundleName) {
		super(name, value, resourceBundleName);
	}

	protected DebugLevel(String name, int value) {
		super(name, value);
	}

}
