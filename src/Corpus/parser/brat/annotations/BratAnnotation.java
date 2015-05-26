package Corpus.parser.brat.annotations;

public abstract class BratAnnotation {

	protected String id;
	protected boolean initialized = false;

	public BratAnnotation(String id) {
		super();
		this.id = id;
	}

	public String getID() {
		return id;
	}

	public boolean isInitialized() {
		return initialized;
	}

}
