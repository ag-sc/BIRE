package sampling;

import java.util.List;

import learning.Learner;
import variables.AbstractState;

public interface AbstractSampler<StateT extends AbstractState, ResultT> {

	public List<StateT> generateChain(StateT initialState, ResultT goldResult, Learner<StateT> learner);

	public List<StateT> generateChain(StateT initialState);

}
