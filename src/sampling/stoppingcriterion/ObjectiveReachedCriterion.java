package sampling.stoppingcriterion;

import java.util.List;

import variables.AbstractState;

public class ObjectiveReachedCriterion<StateT extends AbstractState<?>> implements StoppingCriterion<StateT> {

	private int maxSteps;

	/**
	 * This criterion stops the sampling chain, if the last produced state
	 * already achieved an objective score of 1 or if a maximum number of steps
	 * is reached. Remember that this criterion should only be applied during
	 * the training phase, since the testing phase should be unaware of the
	 * feedback of the objective function.
	 * 
	 * @param maxSteps
	 */
	public ObjectiveReachedCriterion(int maxSteps) {
		this.maxSteps = maxSteps;
	}

	@Override
	public boolean checkCondition(List<StateT> chain, int step) {
		return chain.get(chain.size() - 1).getObjectiveScore() == 1 || maxSteps <= step;
	}

}
