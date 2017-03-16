package sampling;

import java.util.List;

import learning.Learner;
import variables.AbstractState;

public interface IBeamSearchSampler<StateT extends AbstractState<?>, ResultT> {

	/**
	 * Generates a chain of states starting at the initial state. Trains the
	 * model with the generated states using the learner.
	 * 
	 * @param initialState
	 * @param goldResult
	 * @param learner
	 * @return
	 */
	public List<List<StateT>> generateChain(List<StateT> initialStates, ResultT goldResult, Learner<StateT> learner);

	/**
	 * Generates a chain of states starting at the initial state.
	 * 
	 * @param initialState
	 * @return
	 */
	public List<List<StateT>> generateChain(List<StateT> initialStates);

}
