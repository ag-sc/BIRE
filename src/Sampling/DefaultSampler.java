package Sampling;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Changes.StateChange;
import Corpus.Token;
import Learning.Scorer;
import Logging.Log;
import Variables.EntityAnnotation;
import Variables.EntityType;
import Variables.State;

public class DefaultSampler implements Sampler {

	private DecimalFormat df = new DecimalFormat("0.0000");

	private int numberOfStates;
	/**
	 * This variable is used to determine if a state with a lower score should
	 * be accepted nonetheless. This variable should start with a value < 1 and
	 * decrease during the training. This allows to accept worse states more
	 * often at the beginning of the training than at the end.
	 */
	private double acceptanceFactor = 0.5;

	/**
	 * Creates a new DefaultSampler that samples from <i>numberOfStates</i>
	 * generated States in each sampling step.
	 * 
	 * @param numberOfStates
	 */
	public DefaultSampler(int numberOfStates) {
		this.numberOfStates = numberOfStates;
	}

	@Override
	public List<State> getNextStates(State state, Scorer scorer) {

		Map<State, Double> nextStates = generateNextStates(state,
				numberOfStates, scorer);
		System.out.println("generated states:");
		for (State s : nextStates.keySet()) {
			System.out.println(s);
		}
		State nextState = SamplingHelper.drawRandomlyFrom(nextStates);
		if (SamplingHelper.accept(nextState, state, acceptanceFactor)) {
			// nextState.propagateChange();
			return Arrays.asList(nextState);
		} else {
			return Arrays.asList(state);
		}

	}

	private Map<State, Double> generateNextStates(State previousState,
			int numberOfStates, Scorer scorer) {
		Map<State, Double> generatedStates = new HashMap<State, Double>();
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
				case NOTHING:
					Log.d("Do not change the state");
					break;
				}
			}

			double score = scorer.score(generatedState);
			// generatedState.setScore(score); // this is already done in
			// scorer.score(...)
			generatedStates.put(generatedState, score);
		}
		return generatedStates;
	}

}
