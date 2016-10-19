package learning.scorer;

import java.util.List;

import variables.AbstractState;

public interface Scorer {
	/**
	 * Computes a score for each passed state given the individual factors.
	 * Scoring is done is done in parallel if flag is set and scorer
	 * implementation implements this behavior.
	 * 
	 * @param states
	 * @param multiThreaded
	 */
	public void score(List<? extends AbstractState<?>> states, boolean multiThreaded);

}
