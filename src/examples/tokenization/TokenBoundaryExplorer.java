package examples.tokenization;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sampling.Explorer;

public class TokenBoundaryExplorer implements Explorer<TokenState> {
	private static Logger log = LogManager.getFormatterLogger();

	/**
	 * This explorer creates possible successor states by inserting a new token
	 * boundary at every possible character position. Additionally, every
	 * existing boundary is removed.
	 */
	public TokenBoundaryExplorer() {
	}

	@Override
	public List<TokenState> getNextStates(TokenState currentState) {
		List<TokenState> nextStates = new ArrayList<>();
		for (int i = 0; i < currentState.sentence.text.length(); i++) {
			if (currentState.tokenization.tokenBoundaries.contains(i)) {
				TokenState generatedState = new TokenState(currentState);
				generatedState.tokenization.tokenBoundaries.remove(i);
				nextStates.add(generatedState);
			} else {
				TokenState generatedState = new TokenState(currentState);
				generatedState.tokenization.tokenBoundaries.add(i);
				nextStates.add(generatedState);
			}
		}
		return nextStates;
	}

}
