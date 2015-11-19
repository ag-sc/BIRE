package examples;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sampling.Explorer;

public class TokenBoundaryExplorer implements Explorer<TokenState> {
	private static Logger log = LogManager.getFormatterLogger();

	/**
	 * This explorer creates possible successor states, inserting a new token
	 * boundary at every possible character position. Additionally, every
	 * existing boundary is removed.
	 */
	public TokenBoundaryExplorer() {
	}

	@Override
	public List<TokenState> getNextStates(TokenState currentState) {
		List<TokenState> nextStates = new ArrayList<>();
		for (int i = 0; i < currentState.sentence.text.length(); i++) {
			if (currentState.tokenBoundaries.contains(i)) {
				{
					TokenState generatedState = new TokenState(currentState);
					generatedState.tokenBoundaries.remove(i);
					nextStates.add(generatedState);
				}
				if (i > 0 && !currentState.tokenBoundaries.contains(i - 1)) {
					TokenState generatedState = new TokenState(currentState);
					generatedState.tokenBoundaries.remove(i);
					generatedState.tokenBoundaries.add(i - 1);
					nextStates.add(generatedState);
				}
				if (i < currentState.sentence.text.length() - 1 && !currentState.tokenBoundaries.contains(i + 1)) {
					TokenState generatedState = new TokenState(currentState);
					generatedState.tokenBoundaries.remove(i);
					generatedState.tokenBoundaries.add(i + 1);
					nextStates.add(generatedState);
				}
			} else {
				TokenState generatedState = new TokenState(currentState);
				generatedState.tokenBoundaries.add(i);
				nextStates.add(generatedState);
			}
		}
		TokenState generatedState = new TokenState(currentState);
		nextStates.add(generatedState);
		return nextStates;
	}

}
