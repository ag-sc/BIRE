package examples;

import corpus.Instance;

public class Sentence implements Instance {

	public String text;

	public Sentence(String text) {
		super();
		this.text = text;
	}

	@Override
	public String toString() {
		return "Sentence [text=" + text + "]";
	}

}
