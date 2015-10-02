package Sampling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Corpus.Token;
import Learning.Scorer;
import Variables.EntityType;
import Variables.MutableEntityAnnotation;
import Variables.State;
import utility.EntityID;

public class ExhaustiveEntitySampler implements Sampler<State> {

	{
		// Log.off();
	}

	public ExhaustiveEntitySampler() {
	}

	public List<State> getNextStates(State state, Scorer<State> scorer) {
		Set<State> nextStates = generateNextStates(state, scorer);
		List<State> nextStatesSorted = new ArrayList<State>(nextStates);

		return nextStatesSorted;
	}

	private Set<State> generateNextStates(State previousState, Scorer<State> scorer) {
		Set<State> generatedStates = new HashSet<State>();
		List<Token> tokens = previousState.getDocument().getTokens();
		// Add new entities to empty tokens
		for (Token token : tokens) {

			if (!previousState.tokenHasAnnotation(token)) {
				// Assign new entity to empty token
				Collection<EntityType> entityTypes = previousState.getDocument().getCorpus().getCorpusConfig()
						.getEntityTypes();
				for (EntityType entityType : entityTypes) {
					State generatedState = new State(previousState);
					MutableEntityAnnotation tokenAnnotation = new MutableEntityAnnotation(generatedState, entityType,
							token.getIndex(), token.getIndex() + 1);
					generatedState.addMutableEntity(tokenAnnotation);
					generatedStates.add(generatedState);
				}
			}

		}
		// Modify existing entities
		Set<EntityID> previousStatesEntityIDs = previousState.getMutableEntityIDs();
		for (EntityID entityID : previousStatesEntityIDs) {
			MutableEntityAnnotation previousStatesEntity = previousState.getMutableEntity(entityID);
			Collection<EntityType> entityTypes = previousState.getDocument().getCorpus().getCorpusConfig()
					.getEntityTypes();
			// remove the type that this entity already has assigned
			entityTypes.remove(previousStatesEntity.getType());
			// change Type of every entity to every possible type
			for (EntityType entityType : entityTypes) {
				State generatedState = new State(previousState);
				MutableEntityAnnotation entity = generatedState.getMutableEntity(entityID);
				entity.setType(entityType);
				generatedStates.add(generatedState);
			}
			// Create on state with that particular entity removed
			State generatedState = new State(previousState);
			MutableEntityAnnotation entity = generatedState.getMutableEntity(entityID);
			generatedState.removeMutableEntity(entity);
			generatedStates.add(generatedState);
		}
		// // add an unchanged state
		State generatedState = new State(previousState);
		generatedStates.add(generatedState);
		return generatedStates;
	}
}
