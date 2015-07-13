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

	{
		// Log.off();
	}
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

		Set<State> generatedStates = generateNextStates(state, numberOfStates,
				scorer);
		List<State> nextStatesSorted = new ArrayList<State>(generatedStates);
		
//		nextStatesSorted.sort(State.comparator);
//		Log.d("generated states:");
//		for (State s : nextStatesSorted) {
//			Log.d("%s", s);
//		}
		return nextStatesSorted;

	}

	private Set<State> generateNextStates(State previousState,
			int numberOfStates, Scorer scorer) {
		Log.methodOff();
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
				// TODO consider merging entities
				StateChange stateChange = SamplingHelper.sampleStateChange(
						StateChange.CHANGE_BOUNDRARIES, StateChange.DO_NOTHING);
				switch (stateChange) {
				case CHANGE_BOUNDRARIES:
					Log.d("%s: change annotation boundaries.",
							generatedState.getID());
					SamplingHelper.changeBoundaries(sampledEntity,
							generatedState);
					break;
				case DO_NOTHING:
					Log.d("Do not change the state");
					break;
				default:
					Log.d("%s: unsupported state change",
							generatedState.getID());
					break;
				}
			}

			generatedStates.add(generatedState);
		}
		return generatedStates;
	}

}
