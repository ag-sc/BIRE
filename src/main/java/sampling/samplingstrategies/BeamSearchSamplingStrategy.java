package sampling.samplingstrategies;

import java.util.List;

import variables.AbstractState;

public interface BeamSearchSamplingStrategy<StateT extends AbstractState<?>> {
	public static class StatePair<StateT> {
		private StateT parentState;
		private StateT candidateState;

		public StatePair(StateT parentState, StateT candidateState) {
			super();
			this.parentState = parentState;
			this.candidateState = candidateState;
		}

		public StateT getParentState() {
			return parentState;
		}

		public StateT getCandidateState() {
			return candidateState;
		}
	}

	/**
	 * Interface function for implementing a strategy through which a candidate
	 * state is sampled from the list of generated, possible successor states.
	 * One implementation could e.g. sample the candidate state using the model
	 * scores as a distribution.
	 * 
	 * @param candidates
	 * @return
	 */
	public List<StatePair<StateT>> sampleCandidate(List<StatePair<StateT>> candidates);

	public boolean usesModel();

	public boolean usesObjective();
}