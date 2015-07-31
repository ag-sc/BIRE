package Sampling;

import java.util.ArrayList;
import java.util.Collection;
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

public class ExhaustiveEntitySampler implements Sampler {

	{
		// Log.off();
	}
	private int numberOfStates;

	// /**
	// * Creates a new DefaultSampler that samples from <i>numberOfStates</i>
	// * generated States in each sampling step.
	// *
	// * @param numberOfStates
	// */
	// public ExhaustiveEntitySampler(int numberOfStates) {
	// this.numberOfStates = numberOfStates;
	// }

	public ExhaustiveEntitySampler() {
		this.numberOfStates = 0;
	}

	public List<State> getNextStates(State state, Scorer scorer) {

		Set<State> nextStates = generateNextStates(state, scorer);
		List<State> nextStatesSorted = new ArrayList<State>(nextStates);

		// nextStatesSorted.sort(State.comparator);
		// Log.d("generated states (%s):", nextStatesSorted.size());
		// for (State s : nextStatesSorted) {
		// Log.d("%s", s);
		// }

		if (numberOfStates > 0)
			return SamplingHelper.getBest(nextStatesSorted, numberOfStates);
		else
			return nextStatesSorted;

	}

	private Set<State> generateNextStates(State previousState, Scorer scorer) {
		Set<State> generatedStates = new HashSet<State>();
		List<Token> tokens = previousState.getDocument().getTokens();
		// Add new entities to empty tokens
		for (Token token : tokens) {

			if (!previousState.tokenHasAnnotation(token)) {
				// Assign new entity to empty token
				Collection<EntityType> entityTypes = previousState
						.getDocument().getCorpus().getCorpusConfig()
						.getEntityTypes();
				for (EntityType entityType : entityTypes) {
					State generatedState = new State(previousState);
					EntityAnnotation tokenAnnotation = new EntityAnnotation(
							generatedState);
					tokenAnnotation.init(entityType, token.getIndex(),
							token.getIndex());
					generatedState.addEntityAnnotation(tokenAnnotation);
					generatedStates.add(generatedState);
				}
			}

		}
		// Modify existing entities
		Set<String> previousStatesEntityIDs = previousState.getEntityIDs();
		for (String entityID : previousStatesEntityIDs) {
			EntityAnnotation previousStatesEntity = previousState
					.getEntity(entityID);
			Collection<EntityType> entityTypes = previousState.getDocument()
					.getCorpus().getCorpusConfig().getEntityTypes();
			// remove the type that this entity already has assigned
			entityTypes.remove(previousStatesEntity.getType());
			// change Type of every entity to every possible type
			for (EntityType entityType : entityTypes) {
				State generatedState = new State(previousState);
				EntityAnnotation entity = generatedState.getEntity(entityID);
				entity.setType(entityType);
				generatedStates.add(generatedState);
			}
			// Create on state with that particular entity removed
			State generatedState = new State(previousState);
			EntityAnnotation entity = generatedState.getEntity(entityID);
			generatedState.removeEntityAnnotation(entity);
			generatedStates.add(generatedState);
		}
		// // add an unchanged state
		State generatedState = new State(previousState);
		generatedStates.add(generatedState);
		return generatedStates;
	}
}
