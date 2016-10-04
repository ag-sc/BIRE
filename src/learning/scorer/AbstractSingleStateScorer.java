package learning.scorer;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import exceptions.MissingFactorException;
import factors.Factor;
import utility.Utils;
import variables.AbstractState;

/**
 * Implements the scorer interface by wrapping some functionality but passing
 * each state individually to a new abstract score method.
 * 
 * @author sjebbara
 *
 */
public abstract class AbstractSingleStateScorer implements Scorer {
	/**
	 * Computes a score for each passed state given the individual factors.
	 * Scoring is done is done in parallel if flag is set and scorer
	 * implementation does not override this method.
	 * 
	 * @param states
	 * @param multiThreaded
	 */
	public void score(List<? extends AbstractState<?>> states, boolean multiThreaded) {
		Stream<? extends AbstractState<?>> stream = Utils.getStream(states, multiThreaded);
		stream.forEach(s -> score(s));
	}

	/**
	 * Computes the score of this state according to the trained model. The
	 * computed score is returned but also updated in the state objects
	 * <i>score</i> field.
	 * 
	 * @param state
	 * @return
	 */

	protected double score(AbstractState<?> state) {
		Collection<Factor<?>> factors = null;
		try {
			factors = state.getFactorGraph().getFactors();
		} catch (MissingFactorException e) {
			e.printStackTrace();
		}
		double score = score(factors);
		state.setModelScore(score);
		return score;
	}

	protected abstract double score(Collection<Factor<?>> factors);

}
