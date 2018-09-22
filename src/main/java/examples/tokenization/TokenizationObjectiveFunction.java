package examples.tokenization;

import java.util.Set;

import com.google.common.collect.Sets;

import learning.ObjectiveFunction;

public class TokenizationObjectiveFunction extends ObjectiveFunction<TokenState, Tokenization> {

	/**
	 * This function simply evaluates the state's token boundaries w.r.t. the
	 * provided goldResult using precision and recall. The more token boundaries
	 * are found, the higher the score.
	 */
	public TokenizationObjectiveFunction() {
	}

	@Override
	protected double computeScore(TokenState state, Tokenization goldResult) {
		if (goldResult.tokenBoundaries.size() == 0 && state.tokenization.tokenBoundaries.size() == 0) {
			return 1;
		} else if (goldResult.tokenBoundaries.size() == 0 && state.tokenization.tokenBoundaries.size() != 0) {
			return 0;
		} else if (goldResult.tokenBoundaries.size() != 0 && state.tokenization.tokenBoundaries.size() == 0) {
			return 0;
		}
		Set<Integer> correctBoundaries = Sets.intersection(state.tokenization.tokenBoundaries,
				goldResult.tokenBoundaries);
		double precision = correctBoundaries.size();
		precision /= (state.tokenization.tokenBoundaries.size());
		double recall = correctBoundaries.size();
		recall /= (goldResult.tokenBoundaries.size());

		double g1 = Math.sqrt(precision * recall);
		return g1;
	}

}
