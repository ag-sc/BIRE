package sampling;

import java.util.List;

import learning.Learner;
import variables.AbstractState;

public interface Sampler<StateT extends AbstractState<?>, ResultT> {

	/**
	 * Generates a chain of states starting at the initial state. Trains the model
	 * with the generated states using the learner.
	 * 
	 * @param initialState
	 * @param goldResult
	 * @param learner
	 * @return
	 */
	public List<StateT> generateChain(StateT initialState, ResultT goldResult, Learner<StateT> learner);

	/**
	 * Generates a chain of states starting at the initial state.
	 * 
	 * @param initialState
	 * @return
	 */
	public List<StateT> generateChain(StateT initialState);

	public List<StateT> collectBestNStates(StateT initialState, int n);

}
