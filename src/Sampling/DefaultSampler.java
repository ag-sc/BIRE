package Sampling;

import java.util.HashMap;
import java.util.Map;

import Learning.Scorer;
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
		/*-
		 * A(x->x') min(1, )
		 */
		return true;
	}

	private State drawRandomlyFrom(Map<State, Double> next_states) {

		return null;
	}

	private Map<State, Double> generateNextStates(State state,
			int numberStates, Scorer scorer) {
		Map<State, Double> generatedStates = new HashMap<State, Double>();
		for (int i = 0; i < numberStates; i++) {
			State generatedState = new State(state);
			
			 
			// sample token
			// get annotation for token
				// if no annotation
					// add annotation with random type
				// if annotation
					// if delete
						// ...
					// if change type
						// ...
					// if add argument
						// ...
					// if remove argument
						// ...
					// if change boundaries
						// ...

			double score= scorer.score(generatedState);
			generatedState.setScore(score);
			generatedStates.put(state, score);
		}
		return generatedStates;
	}

}
