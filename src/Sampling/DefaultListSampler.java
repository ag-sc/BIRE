package Sampling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Changes.StateChange;
import Corpus.Token;
import Learning.Scorer;
import Logging.Log;
import Variables.EntityAnnotation;
import Variables.EntityType;
import Variables.State;

public class DefaultListSampler implements Sampler {

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
	public DefaultListSampler(int numberOfStates) {
		this.numberOfStates = numberOfStates;
	}

	public List<State> getNextStates(State state, Scorer scorer) {
		Set<State> nextStates = generateNextStates(state, numberOfStates,
				scorer);
		List<State> nextStatesSorted = new ArrayList<State>(nextStates);
		Collections.sort(nextStatesSorted,State.modelScoreComparator);
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
			List<Token> tokens = generatedState.getDocument().getTokens();
			// sample token
			Token sampledToken = SamplingHelper.getRandomElement(tokens);
			// get annotation for token
			// if no annotation
			if (!generatedState.tokenHasAnnotation(sampledToken)) {
				// add annotation with random type
				Log.d("%s: add annotation", generatedState.getID());
				SamplingHelper
						.addRandomAnnotation(sampledToken, generatedState);
			} else {
				// Tokens may be referenced/annotated by different entities
				List<String> linkedEntities = new ArrayList<String>(
						generatedState.getAnnotationsForToken(sampledToken));
				// pick one at random
				EntityAnnotation tokenAnnotation = generatedState
						.getEntity(SamplingHelper
								.getRandomElement(linkedEntities));
				// if annotation exists
				// choose a way to alter the state
				StateChange stateChange = SamplingHelper.sampleStateChange();
				switch (stateChange) {
				case DELETE_ANNOTATION:
					Log.d("%s: delete annotation.", generatedState.getID());
					generatedState.removeEntityAnnotation(tokenAnnotation);
					break;
				case CHANGE_TYPE:
					Log.d("%s: change annotation type.", generatedState.getID());
					EntityType sampledType = SamplingHelper
							.sampleEntityType(generatedState);
					tokenAnnotation.setType(sampledType);
					break;
				case ADD_ARGUMENT:
					Log.d("%s: add annotation argument.",
							generatedState.getID());
					SamplingHelper.addRandomArgument(tokenAnnotation,
							generatedState);
					break;
				case REMOVE_ARGUMENT:
					Log.d("%s: remove annotation argument.",
							generatedState.getID());
					SamplingHelper.removeRandomArgument(tokenAnnotation);
					break;
				case CHANGE_BOUNDRARIES:
					Log.d("%s: change annotation boundaries.",
							generatedState.getID());
					SamplingHelper.changeBoundaries(tokenAnnotation,
							generatedState);
					break;
				case CHANGE_ARGUMENT_ROLE:
					Log.d("%s: change argument role", generatedState.getID());
					SamplingHelper.changeRandomArgumentRole(tokenAnnotation,
							generatedState);
					break;
				case CHANGE_ARGUMENT_ENTITY:
					Log.d("%s: change argument entity", generatedState.getID());
					SamplingHelper.changeRandomArgumentEntity(tokenAnnotation,
							generatedState);
					break;
				case DO_NOTHING:
					Log.d("Do not change the state");
					break;
				default:
					break;
				}
			}

			scorer.score(generatedState);
			generatedStates.add(generatedState);
		}
		return generatedStates;
	}

}
