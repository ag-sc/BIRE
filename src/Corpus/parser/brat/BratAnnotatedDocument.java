package Corpus.parser.brat;

import java.util.Map;

import Corpus.parser.brat.annotations.BratAnnotation;
import utility.ID;

public class BratAnnotatedDocument {

	private String documentName;
	private BratAnnotationManager manager;
	private String content;

	public BratAnnotatedDocument(String documentName, String text, BratAnnotationManager manager) {
		super();
		this.manager = manager;
		this.content = text;
		this.documentName = documentName;
	}

	public String getDocumentName() {
		return documentName;
	}

	public Map<ID<? extends BratAnnotation>, BratAnnotation> getAllAnnotations() {
		return manager.getAnnotations();
	}

	public BratAnnotationManager getManager() {
		return manager;
	}

	public String getText() {
		return content;
	}

	public String annotationsToString() {
		StringBuilder builder = new StringBuilder();
		for (BratAnnotation a : manager.getAnnotations().values()) {
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
