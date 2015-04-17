package Sampling;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Annotation.Annotation;
import Learning.Scorer;


public class DefaultSampler implements Sampler {

	int numberStates;
	
	public List<Annotation> getNextState(List<Annotation> state, Scorer scorer)
	{
		// a set of k next states (List of Annotations)
				
		HashMap<List<Annotation>,Double> next_states;
						
		List<Annotation> next_state;
		
		Set<List<Annotation>> states = new HashSet<List<Annotation>>();

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

	private boolean accept(List<Annotation> next_state, List<Annotation> state) {
		// TODO Auto-generated method stub
		return false;
	}

	private List<Annotation> drawRandomlyFrom(HashMap<List<Annotation>, Double> next_states) {
		// TODO Auto-generated method stub
		return null;
	}

	private HashMap<List<Annotation>, Double> generateNextStates(List<Annotation> state,
			int numberStates2, Scorer scorer) {
		// TODO Auto-generated method stub
		return null;
	}
		
	
}
