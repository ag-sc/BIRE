package Logging;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

class SimpleLogFormatter extends Formatter {
	protected static final String SIMPLE_MESSAGE_FORMAT = "%s\n";
	protected static final String LEVEL_MESSAGE_FORMAT = "%s: %s\n";

	protected static final Set<Level> HIDDEN_LEVELS = new HashSet<Level>();
	static {
		HIDDEN_LEVELS.add(DebugLevel.DEBUG);
	}

	@Override
	public String format(LogRecord record) {

		String message = null;
		if (HIDDEN_LEVELS.contains(record.getLevel())) {
			message = String.format(SIMPLE_MESSAGE_FORMAT, record.getMessage());
		} else {
			message = String.format(LEVEL_MESSAGE_FORMAT, record.getLevel(),
					record.getMessage());
		}
		return message;
	}
}