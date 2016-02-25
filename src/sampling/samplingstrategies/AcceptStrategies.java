package sampling.samplingstrategies;

import variables.AbstractState;

public class AcceptStrategies {
	/**
	 * Returns an accept function that accepts the candidate state if its model
	 * score is GREATER THAN the model score of the current state. In this case
	 * the returned accept function returns true.
	 * 
	 * @return
	 */
	public static <StateT extends AbstractState> AcceptStrategy<StateT> strictModelAccept() {
		return (candidate, current) -> candidate.getModelScore() > current.getModelScore();
	};

	/**
	 * Returns an accept function that accepts the candidate state if its objective
	 * score is GREATER THAN the objective score of the current state. In this case
	 * the returned accept function returns true.
	 * 
	 * @return
	 */
	public static <StateT extends AbstractState> AcceptStrategy<StateT> strictObjectiveAccept() {
		return (candidate, current) -> candidate.getObjectiveScore() > current.getObjectiveScore();
	};

}
