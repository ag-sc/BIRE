package Sampling;

import java.util.List;

import Learning.Scorer;
import Variables.IState;

public interface Sampler<StateT extends IState> {

	public List<StateT> getNextStates(StateT state, Scorer<StateT> scorer);
}
