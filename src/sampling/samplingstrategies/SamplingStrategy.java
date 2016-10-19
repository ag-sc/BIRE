package sampling.samplingstrategies;

import java.util.List;

import variables.AbstractState;

public interface SamplingStrategy<StateT extends AbstractState<?>> {
	/**
	 * Interface function for implementing a strategy through which a candidate
	 * state is sampled from the list of generated, possible successor states.
	 * One implementation could e.g. sample the candidate state using the model
	 * scores as a distribution.
	 * 
	 * @param candidates
	 * @return
	 */
	public StateT sampleCandidate(List<StateT> candidates);

	public boolean usesModel();

	public boolean usesObjective();
}