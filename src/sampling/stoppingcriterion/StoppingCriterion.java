package sampling.stoppingcriterion;

import java.util.List;

import variables.AbstractState;

/**
 * This interface provides a way to define, when the DefaultSampler should stop
 * sampling, e.g. after reaching a fixed amount of sampling steps, or after the
 * model score converges.
 * 
 * @author sjebbara
 *
 * @param <StateT>
 */
public interface StoppingCriterion<StateT extends AbstractState> {
	/**
	 * If true, the sampler will stop sampling and return the sampling chain
	 * until that point. If false, the sampling proceeds.
	 * 
	 * @param chain
	 * @param step
	 * @return
	 */
	public boolean checkCondition(List<StateT> chain, int step);
}