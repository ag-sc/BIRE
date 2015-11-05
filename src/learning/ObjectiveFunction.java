package learning;

import variables.AbstractState;

public abstract class ObjectiveFunction<StateT extends AbstractState, ResultT> {

	public double score(StateT state, ResultT goldResult) {
		double score = computeScore(state, goldResult);
		state.setObjectiveScore(score);
		return score;
	}

	protected abstract double computeScore(StateT state, ResultT goldResult);

}
