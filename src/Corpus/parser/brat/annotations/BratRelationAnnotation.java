package Corpus.parser.brat.annotations;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class BratRelationAnnotation extends BratAnnotation {
	public final static Pattern pattern;
	public final static Pattern idPattern;
	static {
		String id = "(R\\d+)";
		String role = "(\\S+)";
		String argRole = "(\\S+)";
		String arg = "((T|E)\\d+)";
		String args = "( " + argRole + ":" + arg + ")*";
		String all = "^" + id + "\t" + role + args + "$";
		pattern = Pattern.compile(all);
		idPattern = Pattern.compile(id);
	}

	private String role;
	private Map<String, BratAnnotation> arguments = new HashMap<String, BratAnnotation>();

	public BratRelationAnnotation(String id) {
		super(id);
	}

	public String getRole() {
		return role;
	}

	public Map<String, BratAnnotation> getArguments() {
		return arguments;
	}

	public void init(String role, Map<String, BratAnnotation> arguments) {
		this.role = role;
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
			return "RelationAnnotation [id=" + id + ", role=" + role
					+ ", arguments=" + builder.toString() + "]";
		} else {
			return "UNINITIALIZED RelationAnnotation [id=" + id + "]";
		}
	}

}
