package Sampling;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Changes.StateChange;
import Learning.Scorer;
import Logging.Log;
import Variables.EntityAnnotation;
import Variables.State;

public class BoundarySampler implements Sampler {

	private int numberOfStates;

	/**
	 * Creates a new DefaultSampler that samples from <i>numberOfStates</i>
	 * generated States in each sampling step.
	 * 
	 * @param numberOfStates
	 */
	public BoundarySampler(int numberOfStates) {
		this.numberOfStates = numberOfStates;
	}

	public List<State> getNextStates(State state, Scorer scorer) {

		Set<State> nextStates = generateNextStates(state, numberOfStates,
				scorer);
		List<State> nextStatesSorted = new ArrayList<State>(nextStates);
		nextStatesSorted.sort(State.comparator);
		Log.d("generated states:");
		for (State s : nextStatesSorted) {
			Log.d("%s", s);
		}
		return nextStatesSorted;

	}

	private Set<State> generateNextStates(State previousState,
			int numberOfStates, Scorer scorer) {
		Set<State> generatedStates = new HashSet<State>();
		for (int i = 0; i < numberOfStates; i++) {
			State generatedState = new State(previousState);
			// pick one entity at random
			EntityAnnotation sampledEntity = SamplingHelper
					.getRandomElement(new ArrayList<EntityAnnotation>(
							generatedState.getEntities()));
			// if annotation exists
			if (sampledEntity != null) {
				// choose a way to alter the state
				StateChange stateChange = SamplingHelper
						.sampleStateChange(StateChange.BOUNDARIES_CHANGED, StateChange.NOTHING);
				switch (stateChange) {
				case BOUNDARIES_CHANGED:
					System.out.println(generatedState.getID()
							+ ": change annotation boundaries.");
					SamplingHelper.changeBoundaries(sampledEntity,
							generatedState);
					break;
				case NOTHING:
					Log.d("Do not change the state");
					break;
				default:
					System.out.println(generatedState.getID()
							+ ": unsupported state change");
					break;
				}
			}

			scorer.score(generatedState);
			generatedStates.add(generatedState);
		}
		return generatedStates;
	}

}
