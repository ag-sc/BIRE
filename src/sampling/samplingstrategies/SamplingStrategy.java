package sampling.samplingstrategies;

import java.util.List;

import variables.AbstractState;

public interface SamplingStrategy<StateT extends AbstractState> {
	public StateT sampleCandidate(List<StateT> candidates);
}