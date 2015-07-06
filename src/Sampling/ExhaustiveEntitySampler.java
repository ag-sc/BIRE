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
		Log.off();
	}
	private int numberOfStates;

	/**
	 * Creates a new DefaultSampler that samples from <i>numberOfStates</i>
	 * generated States in each sampling step.
	 * 
	 * @param numberOfStates
	 */
	public ExhaustiveEntitySampler(int numberOfStates) {
		this.numberOfStates = numberOfStates;
	}

	public ExhaustiveEntitySampler() {
		this.numberOfStates = -1;
	}

	public List<State> getNextStates(State state, Scorer scorer) {

		Set<State> nextStates = generateNextStates(state, scorer);
		List<State> nextStatesSorted = new ArrayList<State>(nextStates);
		nextStatesSorted.sort(State.comparator);
		Log.d("generated states (%s):", nextStatesSorted.size());
		for (State s : nextStatesSorted) {
			Log.d("%s", s);
		}
		return SamplingHelper.getBest(nextStatesSorted, numberOfStates);

	}

	private Set<State> generateNextStates(State previousState, Scorer scorer) {
		Set<State> generatedStates = new HashSet<State>();
		List<Token> tokens = previousState.getDocument().getTokens();
		for (Token token : tokens) {
			if (!previousState.tokenHasAnnotation(token)) {
				// Assign new entity to empty token
				Collection<EntityType> entityTypes = previousState
						.getDocument().getCorpus().getCorpusConfig()
						.getEntitiyTypes();
				for (EntityType entityType : entityTypes) {
					State generatedState = new State(previousState);
					EntityAnnotation tokenAnnotation = generatedState
							.getNewEntityInstanceForState();
					tokenAnnotation.init(entityType, token.getIndex(),
							token.getIndex());
					generatedState.addEntityAnnotation(tokenAnnotation);
					generatedStates.add(generatedState);
				}
			}
		}
		Set<String> entities = previousState.getEntityIDs();
		for (String entityID : entities) {
			Collection<EntityType> entityTypes = previousState.getDocument()
					.getCorpus().getCorpusConfig().getEntitiyTypes();
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
		for (State state : generatedStates) {
			scorer.score(state);
		}
		return generatedStates;
	}
}
