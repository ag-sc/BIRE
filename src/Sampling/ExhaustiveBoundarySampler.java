package Sampling;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Learning.Scorer;
import Logging.Log;
import Variables.AEntityAnnotation;
import Variables.MutableEntityAnnotation;
import Variables.State;
import utility.EntityID;

public class ExhaustiveBoundarySampler implements Sampler<State> {

	{
		Log.off();
	}

	public List<State> getNextStates(State state, Scorer<State> scorer) {

		Set<State> generatedStates = generateNextStates(state, scorer);
		List<State> nextStatesSorted = new ArrayList<State>(generatedStates);

		// nextStatesSorted.sort(State.comparator);
		// Log.d("generated states:");
		// for (State s : nextStatesSorted) {
		// Log.d("%s", s);
		// }
		return nextStatesSorted;

	}

	private Set<State> generateNextStates(State previousState, Scorer<State> scorer) {
		Set<State> generatedStates = new HashSet<State>();
		Set<EntityID> entities = previousState.getMutableEntityIDs();
		for (EntityID entityID : entities) {
			MutableEntityAnnotation previousStatesEntity = previousState.getMutableEntity(entityID);
			int from = previousStatesEntity.getBeginTokenIndex();
			int to = previousStatesEntity.getEndTokenIndex();
			if (0 < from) {
				// Expand left
				State generatedState = new State(previousState);
				MutableEntityAnnotation entity = generatedState.getMutableEntity(entityID);
				entity.setBeginTokenIndex(from - 1);
				generatedStates.add(generatedState);
			}
			if (to < previousStatesEntity.getState().getDocument().getTokens().size()) {
				// Expand right
				State generatedState = new State(previousState);
				MutableEntityAnnotation entity = generatedState.getMutableEntity(entityID);
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
					MutableEntityAnnotation entity = generatedState.getMutableEntity(entityID);
					entity.setBeginTokenIndex(from + 1);
					generatedStates.add(generatedState);
				}
				{
					// Contract right
					State generatedState = new State(previousState);
					MutableEntityAnnotation entity = generatedState.getMutableEntity(entityID);
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
