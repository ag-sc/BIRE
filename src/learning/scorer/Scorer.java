package learning.scorer;

import java.util.Set;

import exceptions.MissingFactorException;
import factors.Factor;
import variables.AbstractState;

public abstract class Scorer {
	/**
	 * Computes the score of this state according to the trained model. The
	 * computed score is returned but also updated in the state objects
	 * <i>score</i> field.
	 * 
	 * @param state
	 * @return
	 */

	public double score(AbstractState<?> state) {
		Set<Factor<?>> factors = null;
		try {
			factors = state.getFactorGraph().getFactors();
		} catch (MissingFactorException e) {
			e.printStackTrace();
		}
		double score = score(factors);
		state.setModelScore(score);
		return score;
	}

	public abstract double score(Set<Factor<?>> factors);
}
