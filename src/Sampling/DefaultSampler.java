package Sampling;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import Learning.Scorer;
import Variables.Annotation;
import Variables.State;

public class DefaultSampler implements Sampler {

	int numberStates;

	public State getNextState(State state, Scorer scorer) {
		// a set of k next states (List of Annotations)

		Map<State, Double> next_states;

		State next_state;

		next_states = generateNextStates(state, numberStates, scorer);

		next_state = drawRandomlyFrom(next_states);

		if (accept(next_state, state)) {
			next_state.propagateChange();
			return next_state;
		} else {
			return state;
		}

	}

	private boolean accept(State next_state, State state) {
		// TODO Auto-generated method stub
		return false;
	}

	private State drawRandomlyFrom(Map<State, Double> next_states) {
		// TODO Auto-generated method stub
		return null;
	}

	private Map<State, Double> generateNextStates(State state,
			int numberStates, Scorer scorer) {
		Map<State, Double> generatedStates = new HashMap<State, Double>();
		for (int i = 0; i < numberStates; i++) {
			State generatedState = new State(state);
			
			//change relations or entities
				// relation Change
					// sample new relation or modify existing relation
					// if new relation:
						// get existing Entities, sample and create relation
					// if modify relation
						// get existing relations, sample and modify
						// modify type or arguments
				
				// entity change
					// sample entity
					// change boundaries or type
					// if change boundaries
						// expand/contract left/right
					// if change type
						// ...
			double score= scorer.score(generatedState);
			generatedState.setScore(score);
			generatedStates.put(state, score);
		}
		return generatedStates;
	}

}
