package Sampling;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Learning.Scorer;
import Variables.Annotation;
import Variables.State;


public class DefaultSampler implements Sampler {

	int numberStates;
	
	public State getNextState(State state, Scorer scorer)
	{
		// a set of k next states (List of Annotations)
				
		HashMap<State, Double> next_states;
						
		State next_state;
		
		Set<State> states = new HashSet<State>();

		next_states = generateNextStates(state, numberStates, scorer);
				
		next_state = drawRandomlyFrom(next_states);
				
		if (accept(next_state,state))
		{
			return next_state;
		}
		else
		{
			return state;
		}

	}

	private boolean accept(State next_state,State state) {
		// TODO Auto-generated method stub
		return false;
	}

	private State drawRandomlyFrom(HashMap<State, Double> next_states) {
		// TODO Auto-generated method stub
		return null;
	}

	private HashMap<State, Double> generateNextStates(State state,
			int numberStates2, Scorer scorer) {
		// TODO Auto-generated method stub
		return null;
	}
		
	
}
