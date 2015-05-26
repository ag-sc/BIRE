package Corpus.parser.brat.annotations;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class BratEventAnnotation extends BratAnnotation {
	public final static Pattern pattern;
	public final static Pattern idPattern;
	static {
		String id = "(E\\d+)";
		String role = "(\\S+)";
		String trigger = "(T\\d+)";
		String argRole = "(\\S+)";
		String arg = "((T|E)\\d+)";
		String args = "( " + argRole + ":" + arg + ")*";
		String all = "^" + id + "\t" + role + ":" + trigger + args + "$";
		pattern = Pattern.compile(all);
		idPattern = Pattern.compile(id);
	}

	private String role;
	private BratTextBoundAnnotation trigger;
	private Map<String, BratAnnotation> arguments = new HashMap<String, BratAnnotation>();

	public String getRole() {
		return role;
	}

	public BratTextBoundAnnotation getTrigger() {
		return trigger;
	}

	public Map<String, BratAnnotation> getArguments() {
		return arguments;
	}


	public BratEventAnnotation(String id) {
		super(id);
	}

	public void init(String id, String role, BratTextBoundAnnotation trigger,
			Map<String, BratAnnotation> arguments) {
		this.role = role;
		this.trigger = trigger;
		this.arguments = arguments;
		this.initialized = true;
	}

	@Override
	public String toString() {
		if (initialized) {
			StringBuilder builder = new StringBuilder();
			for (Entry<String, BratAnnotation> e : arguments.entrySet()) {
				builder.append(e.getKey() + ":" + e.getValue().getID());
				builder.append(" ");
			}
			String triggerID = trigger == null ? "UNDEFINED" : trigger.getID();
			return "EventAnnotation [id=" + id + ", role=" + role
					+ ", trigger=" + triggerID + ", arguments="
					+ builder.toString() + "]";
		} else {
			return "UNINITIALIZED EventAnnotation [id=" + id + "]";
		}
	}

}
