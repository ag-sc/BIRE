package Corpus.parser.brat.annotations;

import java.util.regex.Pattern;

public class BratTextBoundAnnotation extends BratAnnotation {

	public final static Pattern pattern;
	public final static Pattern idPattern;
	static {
		String id = "(T\\d+)";
		String role = "(\\S+)";
		String start = "(\\d+)";
		String stop = "(\\d+)";
		String text = "(.+)";
		String all = "^" + id + "\t+" + role + " " + start + " " + stop + "\t"
				+ text + "$";

		pattern = Pattern.compile(all);
		idPattern = Pattern.compile(id);
	}

	private String role;
	private int start;
	private int end;
	private String text;

	public BratTextBoundAnnotation(String id) {
		super(id);
	}

	public void init(String role, int start, int end, String text) {
		this.role = role;
		this.start = start;
		this.end = end;
		this.text = text;
		this.initialized = true;
	}

	public String getRole() {
		return role;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public String getText() {
		return text;
	}

	@Override
	public String toString() {
		if (initialized) {
			return "TextBoundAnnotation [id=" + id + ", role=" + role
					+ ", start=" + start + ", end=" + end + ", text=" + text
					+ "]";
		} else {
			return "UNINITIALIZED TextBoundAnnotation [id=" + id + "]";
		}
	}

}
