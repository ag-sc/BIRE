package Sampling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.org.apache.bcel.internal.generic.RETURN;

import Changes.BoundaryChange;
import Changes.StateChange;
import Corpus.Token;
import Learning.Scorer;
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
		nextStatesSorted.sort(comparator);
		System.out.println("generated states:");
		for (State s : nextStatesSorted) {
			System.out.println(s);
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
				System.out
						.println(generatedState.getID() + ": add annotation.");
				SamplingHelper
						.addRandomAnnotation(sampledToken, generatedState);
			} else {
				// Tokens may be referenced/annotated by different entities
				List<String> linkedEntities = new ArrayList<String>(
						generatedState.getAnnotationForToken(sampledToken));
				// pick one at random
				EntityAnnotation tokenAnnotation = generatedState
						.getEntity(SamplingHelper
								.getRandomElement(linkedEntities));
				// if annotation exists
				// choose a way to alter the state
				StateChange stateChange = SamplingHelper.sampleStateChange();
				switch (stateChange) {
				case ANNOTATION_DELETED:
					// TODO delete annotation completely or only from this token
					// (this might require to split an annotation in to separate
					// annotations)
					System.out.println(generatedState.getID()
							+ ": delete annotation.");
					generatedState.removeEntityAnnotation(tokenAnnotation);
					break;
				case TYPE_CHANGED:
					System.out.println(generatedState.getID()
							+ ": change annotation type.");
					EntityType sampledType = SamplingHelper
							.sampleEntityType(generatedState);
					tokenAnnotation.setType(sampledType);
					break;
				case ARGUMENT_ADDED:
					System.out.println(generatedState.getID()
							+ ": add annotation argument.");
					SamplingHelper.addRandomArgument(tokenAnnotation,
							generatedState);
					break;
				case ARGUMENT_REMOVED:
					System.out.println(generatedState.getID()
							+ ": remove annotation argument.");
					SamplingHelper.removeRandomArgument(tokenAnnotation);
					break;
				case BOUNDARIES_CHANGED:
					System.out.println(generatedState.getID()
							+ ": change annotation boundaries.");
					SamplingHelper.changeBoundaries(tokenAnnotation,
							generatedState);
					break;
				}
			}

			scorer.score(generatedState);
			generatedStates.add(generatedState);
		}
		return generatedStates;
	}

}
