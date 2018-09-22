package examples.tokenization;

import sampling.Initializer;

public class TokenizationInitializer implements Initializer<String, TokenState> {

	/**
	 * This initializer simply initializes the first state with no token
	 * boundaries at all. It would be possible to initialize it heuristically
	 * with e.g. a token boundary at ever whitespace.
	 */
	public TokenizationInitializer() {
	}

	@Override
	public TokenState getInitialState(String sentence) {
		return new TokenState(sentence);
	}

}
