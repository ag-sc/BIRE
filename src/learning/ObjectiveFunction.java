package learning;

import variables.AbstractState;

public abstract class ObjectiveFunction<StateT extends AbstractState> {

	public double score(StateT state, StateT goldState) {
		double score = computeScore(state, goldState);
		state.setObjectiveScore(score);
		return score;
	}

	protected abstract double computeScore(StateT state, StateT goldState);

}
