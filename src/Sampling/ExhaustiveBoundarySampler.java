package Sampling;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Learning.Scorer;
import Logging.Log;
import Variables.EntityAnnotation;
import Variables.State;
import utility.EntityID;

public class ExhaustiveBoundarySampler implements Sampler {

	{
		Log.off();
	}

	public List<State> getNextStates(State state, Scorer scorer) {

		Set<State> generatedStates = generateNextStates(state, scorer);
		List<State> nextStatesSorted = new ArrayList<State>(generatedStates);

		// nextStatesSorted.sort(State.comparator);
		// Log.d("generated states:");
		// for (State s : nextStatesSorted) {
		// Log.d("%s", s);
		// }
		return nextStatesSorted;

	}

	private Set<State> generateNextStates(State previousState, Scorer scorer) {
		Set<State> generatedStates = new HashSet<State>();
		Set<EntityID> entities = previousState.getEntityIDs();
		for (EntityID entityID : entities) {
			EntityAnnotation previousStatesEntity = previousState.getEntity(entityID);
			int from = previousStatesEntity.getBeginTokenIndex();
			int to = previousStatesEntity.getEndTokenIndex();
			if (from > 0) {
				// Expand left
				State generatedState = new State(previousState);
				EntityAnnotation entity = generatedState.getEntity(entityID);
				entity.setBeginTokenIndex(from - 1);
				generatedStates.add(generatedState);
			}
			if (to < previousStatesEntity.getState().getDocument().getTokens().size()) {
				// Expand right
				State generatedState = new State(previousState);
				EntityAnnotation entity = generatedState.getEntity(entityID);
				entity.setEndTokenIndex(to + 1);
				generatedStates.add(generatedState);
			}
			if (to - from > 1) {
				/**
				 * Here we just assume that to >= from. That is why we do not
				 * check if to > 0 or from < "max"-1. Given a consistent state,
				 * these conditions are implied in this if-block
				 */
				{
					// Contract left
					State generatedState = new State(previousState);
					EntityAnnotation entity = generatedState.getEntity(entityID);
					entity.setBeginTokenIndex(from + 1);
					generatedStates.add(generatedState);
				}
				{
					// Contract right
					State generatedState = new State(previousState);
					EntityAnnotation entity = generatedState.getEntity(entityID);
					entity.setEndTokenIndex(to - 1);
					generatedStates.add(generatedState);
				}
			}
		}
		// add an unchanged state
		State generatedState = new State(previousState);
		generatedStates.add(generatedState);

		return generatedStates;
	}
}
