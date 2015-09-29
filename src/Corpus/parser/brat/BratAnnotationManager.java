package Corpus.parser.brat;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import Corpus.parser.brat.annotations.BratAnnotation;
import utility.ID;

public class BratAnnotationManager {
	private Map<ID<? extends BratAnnotation>, BratAnnotation> annotations = new HashMap<ID<? extends BratAnnotation>, BratAnnotation>();
	private Multimap<String, BratAnnotation> annotationsByFilename = HashMultimap.create();

	public Map<ID<? extends BratAnnotation>, BratAnnotation> getAnnotations() {
		return annotations;
	}

	public BratAnnotation getAnnotation(ID<? extends BratAnnotation> id) {
		return annotations.get(id);
	}

	public void addAnnotation(String annFilename, BratAnnotation annotation) {
		annotations.put(annotation.getID(), annotation);
		annotationsByFilename.put(annFilename, annotation);
	}

	public Collection<BratAnnotation> getAnnotations(String annFilename) {
		return annotationsByFilename.get(annFilename);
	}

	public Multimap<String, BratAnnotation> getAnnotationsByFilename() {
		return annotationsByFilename;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (BratAnnotation a : annotations.values()) {
			builder.append(a.toString());
			builder.append("\n");
		}
		return builder.toString();
	}
}
