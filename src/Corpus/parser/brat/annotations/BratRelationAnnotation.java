package Corpus.parser.brat.annotations;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import Corpus.parser.brat.BratAnnotationManager;
import utility.ID;

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
	private Map<String, ID<? extends BratAnnotation>> arguments = new HashMap<>();

	public String getRole() {
		return role;
	}

	public Map<String, ID<? extends BratAnnotation>> getArguments() {
		return arguments;
	}

	public BratRelationAnnotation(BratAnnotationManager manager,String id, String role, Map<String, ID<? extends BratAnnotation>> arguments) {
		super(manager,id);
		this.role = role;
		this.arguments = arguments;
	}

	@Override
	public String toString() {
		return "RelationAnnotation [id=" + id + ", role=" + role + ", arguments=" + arguments + "]";
	}

}
