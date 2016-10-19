package examples.tokenization;

import corpus.LabeledInstance;

public class TokenizedSentence extends Sentence implements LabeledInstance<Tokenization> {

	private Tokenization tokenization;

	/**
	 * This is the implementation of the LabeledInstance interface that extends
	 * the Sentence object by a preferred tokenization.
	 * 
	 * @param sentence
	 */
	public TokenizedSentence(String sentence) {
		super(sentence);
	}

	public Tokenization getTokenization() {
		return tokenization;
	}

	public void setTokenization(Tokenization tokenization) {
		this.tokenization = tokenization;
	}

	@Override
	public Tokenization getGoldResult() {
		return tokenization;
	}

	@Override
	public String toString() {
		return "TokenizedSentence [text=" + text + ", tokenization=" + tokenization + "]";
	}

}
