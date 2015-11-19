package sampling.stoppingcriterion;

import java.util.List;

import variables.AbstractState;

public class StepLimitCriterion<StateT extends AbstractState> implements StoppingCriterion<StateT> {

	private int maxSteps;

	public StepLimitCriterion(int maxSteps) {
		this.maxSteps = maxSteps;
	}

	@Override
	public boolean checkCondition(List<StateT> chain, int step) {
		return maxSteps < step;
	}

}
