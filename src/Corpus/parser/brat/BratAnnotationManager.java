package Corpus.parser.brat;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import Corpus.parser.brat.annotations.BratAnnotation;
import Corpus.parser.brat.annotations.BratAttributeAnnotation;
import Corpus.parser.brat.annotations.BratEventAnnotation;
import Corpus.parser.brat.annotations.BratRelationAnnotation;
import Corpus.parser.brat.annotations.BratTextBoundAnnotation;

public class BratAnnotationManager {
	private Map<String, BratAnnotation> allAnnotations = new HashMap<String, BratAnnotation>();

	private void newAnnotationCreated(BratTextBoundAnnotation annotation) {
		allAnnotations.put(annotation.getID(), annotation);
	}

	public BratTextBoundAnnotation getOrCreateTextBoundByID(String id) {
		BratTextBoundAnnotation annotation = (BratTextBoundAnnotation) allAnnotations
				.get(id);
		if (annotation == null) {
			annotation = new BratTextBoundAnnotation(id);
			newAnnotationCreated(annotation);
		}
		return annotation;
	}

	public BratEventAnnotation getOrCreateEventByID(String id) {
		BratEventAnnotation annotation = (BratEventAnnotation) allAnnotations
				.get(id);
		if (annotation == null) {
			annotation = new BratEventAnnotation(id);
			allAnnotations.put(id, annotation);
		}
		return annotation;
	}

	public BratRelationAnnotation getOrCreateRelationByID(String id) {
		BratRelationAnnotation annotation = (BratRelationAnnotation) allAnnotations
				.get(id);
		if (annotation == null) {
			annotation = new BratRelationAnnotation(id);
			allAnnotations.put(id, annotation);
		}
		return annotation;
	}

	public BratAttributeAnnotation getOrCreateAttributeByID(String id) {
		BratAttributeAnnotation annotation = (BratAttributeAnnotation) allAnnotations
				.get(id);
		if (annotation == null) {
			annotation = new BratAttributeAnnotation(id);
			allAnnotations.put(id, annotation);
		}
		return annotation;
	}

	public BratAnnotation getOrCreateByID(String triggerID) {
		Matcher textMatcher = BratTextBoundAnnotation.idPattern
				.matcher(triggerID);
		if (textMatcher.matches()) {
			return getOrCreateTextBoundByID(triggerID);
		}
		Matcher eventMatcher = BratEventAnnotation.idPattern.matcher(triggerID);
		if (eventMatcher.matches()) {
			return getOrCreateEventByID(triggerID);
		}
		Matcher relationMatcher = BratRelationAnnotation.idPattern
				.matcher(triggerID);
		if (relationMatcher.matches()) {
			return getOrCreateRelationByID(triggerID);
		}
		Matcher attributeMatcher = BratAttributeAnnotation.idPattern
				.matcher(triggerID);
		if (attributeMatcher.matches()) {
			return getOrCreateAttributeByID(triggerID);
		}

		return null;
	}

	public Map<String, BratAnnotation> getAnnotations() {
		return allAnnotations;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (BratAnnotation a : allAnnotations.values()) {
			builder.append(a.toString());
			builder.append("\n");
		}
		return builder.toString();
	}
}
