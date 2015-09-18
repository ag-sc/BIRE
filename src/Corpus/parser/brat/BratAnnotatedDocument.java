package Corpus.parser.brat;

import java.util.HashMap;
import java.util.Map;

import Corpus.parser.brat.annotations.BratAnnotation;

public class BratAnnotatedDocument {

	private String documentName;
	private Map<String, BratAnnotation> allAnnotations = new HashMap<String, BratAnnotation>();
	private String content;

	public BratAnnotatedDocument(String documentName, String text, Map<String, BratAnnotation> allAnnotations) {
		super();
		this.allAnnotations = allAnnotations;
		this.content = text;
		this.documentName = documentName;
	}

	public String getDocumentName() {
		return documentName;
	}

	public Map<String, BratAnnotation> getAllAnnotations() {
		return allAnnotations;
	}

	public String getText() {
		return content;
	}

	public String annotationsToString() {
		StringBuilder builder = new StringBuilder();
		for (BratAnnotation a : allAnnotations.values()) {
			builder.append(a.toString());
			builder.append("\n");
		}
		return builder.toString();
	}

	@Override
	public String toString() {
		return content + "\n" + annotationsToString();
	}

}
