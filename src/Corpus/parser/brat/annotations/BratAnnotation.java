package Corpus.parser.brat.annotations;

import Corpus.parser.brat.BratAnnotationManager;
import utility.ID;

public abstract class BratAnnotation {

	private BratAnnotationManager manager;
	protected ID<? extends BratAnnotation> id;

	public BratAnnotation(BratAnnotationManager manager, String id) {
		this.manager = manager;
		this.id = new ID<>(id);
	}

	public ID<? extends BratAnnotation> getID() {
		return id;
	}

	public BratAnnotation getAnnotationByID(ID<? extends BratAnnotation> id) {
		return manager.getAnnotation(id);
	}

}
