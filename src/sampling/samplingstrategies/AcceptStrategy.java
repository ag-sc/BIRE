package sampling.samplingstrategies;

import variables.AbstractState;

public interface AcceptStrategy<StateT extends AbstractState> {

	/**
	 * Returns true if the candidate state is accepted by this function and
	 * false otherwise. This function can be based e.g. on the model scores of
	 * the given states to determine if the candidate is going to be selected as
	 * the next state in the sampling procedure.
	 * 
	 * @param candidate
	 * @param current
	 * @return
	 */
	public boolean isAccepted(StateT candidate, StateT current);
}
