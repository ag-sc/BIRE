package Sampling;

import java.util.List;

import Learning.Scorer;
import Variables.State;

public interface Sampler {

	public List<State> getNextStates(State state, Scorer scorer);
}
