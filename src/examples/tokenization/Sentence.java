package examples.tokenization;

import corpus.Instance;


public class Sentence implements Instance {

	public String text;

	/**
	 * This is the implementation of the Instance interface for a simple sentence.
	 * @param text
	 */
	public Sentence(String text) {
		super();
		this.text = text;
	}

	@Override
	public String toString() {
		return "Sentence [text=" + text + "]";
	}

}
