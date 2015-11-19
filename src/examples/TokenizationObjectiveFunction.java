package examples;

import java.util.Set;

import com.google.common.collect.Sets;

import learning.ObjectiveFunction;

public class TokenizationObjectiveFunction extends ObjectiveFunction<TokenState, TokenState> {

	/**
	 * This function simply evaluates the state's token boundaries w.r.t. the
	 * provided goldResult using precision and recall.
	 */
	public TokenizationObjectiveFunction() {
	}

	@Override
	protected double computeScore(TokenState state, TokenState goldResult) {
		if (goldResult.tokenBoundaries.size() == 0 && state.tokenBoundaries.size() == 0) {
			return 1;
		} else if (goldResult.tokenBoundaries.size() == 0 && state.tokenBoundaries.size() != 0) {
			return 0;
		} else if (goldResult.tokenBoundaries.size() != 0 && state.tokenBoundaries.size() == 0) {
			return 0;
		}
		Set<Integer> correctBoundaries = Sets.intersection(state.tokenBoundaries, goldResult.tokenBoundaries);
		double precision = correctBoundaries.size();
		precision /= (state.tokenBoundaries.size());
		double recall = correctBoundaries.size();
		recall /= (goldResult.tokenBoundaries.size());

		double g1 = Math.sqrt(precision * recall);
		return g1;
	}

}
