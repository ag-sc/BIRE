package Corpus.parser.brat.annotations;

import java.util.regex.Pattern;

import Corpus.parser.brat.BratAnnotationManager;

public class BratTextBoundAnnotation extends BratAnnotation {

	public final static Pattern pattern;
	public final static Pattern idPattern;

	static {
		String id = "(T\\d+)";
		String role = "(\\S+)";
		String start = "(\\d+)";
		String stop = "(\\d+)";
		String text = "(.+)";
		String all = "^" + id + "\t+" + role + " " + start + " " + stop + "\t" + text + "$";

		pattern = Pattern.compile(all);
		idPattern = Pattern.compile(id);
	}

	private String role;
	private int start;
	private int end;
	private String text;

	public BratTextBoundAnnotation(BratAnnotationManager manager, String id, String role, int start, int end,
			String text) {
		super(manager, id);
		this.role = role;
		this.start = start;
		this.end = end;
		this.text = text;
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
		return "TextBoundAnnotation [id=" + id + ", role=" + role + ", start=" + start + ", end=" + end + ", text="
				+ text + "]";
	}

}
