package Corpus.parser.brat.annotations;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class BratAttributeAnnotation extends BratAnnotation {

	public final static Pattern pattern;
	public final static Pattern idPattern;
	static {
		String id = "((A|M)\\d+)";
		String role = "(\\S+)";
		String arg = "((T|E)\\d+)";
		String args = "( " + arg + ")*";
		String all = "^" + id + "\t" + role + args + "$";
		pattern = Pattern.compile(all);
		idPattern = Pattern.compile(id);
	}

	private String role;
	private List<BratAnnotation> arguments = new ArrayList<BratAnnotation>();

	public String getRole() {
		return role;
	}

	public List<BratAnnotation> getArguments() {
		return arguments;
	}

	public BratAttributeAnnotation(String id) {
		super(id);
	}

	public void init(String role, List<BratAnnotation> arguments) {
		this.role = role;
		this.arguments = arguments;
		this.initialized = true;
	}

	@Override
	public String toString() {
		if (initialized) {
			StringBuilder builder = new StringBuilder();
			for (BratAnnotation a : arguments) {
				builder.append(a.getID());
				builder.append(" ");
			}
			return "AttributeAnnotation [id=" + id + ", role=" + role
					+ ", arguments=" + builder.toString() + "]";
		} else {
			return "UNINITIALIZED AttributeAnnotation [id=" + id + "]";
		}
	}

}
