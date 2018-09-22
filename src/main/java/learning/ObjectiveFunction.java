package learning;

import variables.AbstractState;

public abstract class ObjectiveFunction<StateT extends AbstractState<?>, ResultT> {

	/**
	 * Computes a real valued score for the given state and stores it in the
	 * state object.
	 * 
	 * @param state
	 * @param goldResult
	 * @return
	 */
	public double score(StateT state, ResultT goldResult) {
		double score = computeScore(state, goldResult);
		state.setObjectiveScore(score);
		return score;
	}

	/**
	 * Computes a real valued score that evaluates the given state. The higher
	 * the score, the more preferred the state is.
	 * 
	 * <br>
	 * <br>
	 * <b>Note to developers:</b> When implementing this method, you could
	 * retrieve a "gold state" or some other kind of reference from the given
	 * state. You could then use this "gold" object as a basis for your
	 * evaluation. However, it is completely up to you, how you want to score
	 * your state. For now, it is intended to compared the state to the
	 * goldResult.
	 * 
	 * @param state
	 * @param goldResult
	 * @return
	 */
	protected abstract double computeScore(StateT state, ResultT goldResult);

	/**
	 * This function return true, if the <b>state1</b> is preferred over
	 * <b>state2</b>, false otherwise.
	 * 
	 * @param state1
	 * @param state2
	 * @return
	 */
	public boolean preference(StateT state1, StateT state2) {
		return state1.getObjectiveScore() > state2.getObjectiveScore();
	}
}
