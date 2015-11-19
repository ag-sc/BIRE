package examples;

import corpus.LabeledInstance;

public class TokenizedSentence extends Sentence implements LabeledInstance<TokenState> {

	private TokenState tokenization;

	public TokenizedSentence(String sentence) {
		super(sentence);
	}

	public TokenState getTokenization() {
		return tokenization;
	}

	public void setTokenization(TokenState tokenization) {
		this.tokenization = tokenization;
	}

	@Override
	public TokenState getGoldResult() {
		return tokenization;
	}

	@Override
	public String toString() {
		return "TokenizedSentence [tokenization=" + tokenization + "]";
	}

}
