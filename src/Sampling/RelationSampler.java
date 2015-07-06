package Sampling;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Changes.StateChange;
import Learning.Scorer;
import Logging.Log;
import Variables.EntityAnnotation;
import Variables.State;

public class RelationSampler implements Sampler {

	{
		Log.off();
	}
	Comparator<State> comparator = new Comparator<State>() {

		@Override
		public int compare(State s1, State s2) {
			// inverse sign for descending order
			return (int) -Math.signum(s1.getModelScore() - s2.getModelScore());
		}
	};
	private int numberOfStates;

	/**
	 * Creates a new DefaultSampler that samples from <i>numberOfStates</i>
	 * generated States in each sampling step.
	 * 
	 * @param numberOfStates
	 */
	public RelationSampler(int numberOfStates) {
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
				StateChange stateChange = SamplingHelper.sampleStateChange(
						StateChange.ADD_ARGUMENT, StateChange.REMOVE_ARGUMENT,
						StateChange.CHANGE_ARGUMENT_ROLE,
						StateChange.CHANGE_ARGUMENT_ENTITY,
						StateChange.DO_NOTHING);
				switch (stateChange) {
				case ADD_ARGUMENT:
					System.out.println(generatedState.getID()
							+ ": add annotation argument.");
					SamplingHelper.addRandomArgument(sampledEntity,
							generatedState);
					break;
				case REMOVE_ARGUMENT:
					System.out.println(generatedState.getID()
							+ ": remove annotation argument.");
					SamplingHelper.removeRandomArgument(sampledEntity);
					break;
				case CHANGE_ARGUMENT_ROLE:
					Log.d("%s: change argument role", generatedState.getID());
					SamplingHelper.changeRandomArgumentRole(sampledEntity,
							generatedState);
					break;
				case CHANGE_ARGUMENT_ENTITY:
					Log.d("%s: change argument entity", generatedState.getID());
					SamplingHelper.changeRandomArgumentEntity(sampledEntity,
							generatedState);
					break;
				case DO_NOTHING:
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
