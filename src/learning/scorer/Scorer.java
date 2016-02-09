package learning.scorer;

import variables.AbstractState;

public interface Scorer<StateT extends AbstractState> {
	/**
	 * Computes the score of this state according to the trained model. The
	 * computed score is returned but also updated in the state objects
	 * <i>score</i> field.
	 * 
	 * @param state
	 * @return
	 */
	public double score(StateT state);
}
