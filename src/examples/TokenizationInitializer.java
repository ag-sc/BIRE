package examples;

import sampling.Initializer;

public class TokenizationInitializer<SentenceT extends Sentence> implements Initializer<SentenceT, TokenState> {

	@Override
	public TokenState getInitialState(SentenceT sentence) {
		return new TokenState(sentence);
	}

}
