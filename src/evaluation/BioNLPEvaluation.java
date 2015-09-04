package evaluation;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.google.common.collect.Multimap;

import Learning.Score;
import Logging.Log;
import Variables.ArgumentRole;
import Variables.EntityAnnotation;
import Variables.State;
import utility.EntityID;

public class BioNLPEvaluation {

	{
		Log.off();
	}

	public BioNLPEvaluation() {
	}

	public static int strictEquality(State state, State goldState) {
		Collection<EntityAnnotation> entities = state.getEntities();
		Collection<EntityAnnotation> goldEntities = goldState.getEntities();
		int matches = 0;
		for (EntityAnnotation entity : entities) {
			for (EntityAnnotation goldEntity : goldEntities) {
				boolean match = matchEntities(entity, goldEntity);
				if (match) {
					matches++;
				}
			}
		}

		return matches;
	}

	/**
	 * True, if these two entities match, false otherwise, given the strict
	 * equality defined at:
	 * http://www.nactem.ac.uk/tsujii/GENIA/SharedTask/evaluation.shtml
	 * 
	 * 
	 * @param e1
	 * @param e2
	 * @return
	 */
	private static boolean matchEntities(EntityAnnotation e1, EntityAnnotation e2) {
		if (!e1.getType().getName().equals(e2.getType().getName()))
			return false;
		if (e1.getBeginTokenIndex() != e2.getBeginTokenIndex() || e1.getEndTokenIndex() != e2.getEndTokenIndex())
			return false;
		if (!matchArguments(e1, e2))
			return false;

		return true;

	}

	/**
	 * True, if the arguments of both given entities match each other, false
	 * otherwise. More detail at:
	 * http://www.nactem.ac.uk/tsujii/GENIA/SharedTask/evaluation.shtml
	 * 
	 * @param e1
	 * @param e2
	 * @return
	 */
	private static boolean matchArguments(EntityAnnotation e1, EntityAnnotation e2) {
		Multimap<ArgumentRole, EntityID> arguments1 = e1.getArguments();
		Multimap<ArgumentRole, EntityID> arguments2 = e2.getArguments();
		// this is a fast-reject test
		if (arguments1.size() != arguments2.size())
			return false;
		for (Entry<ArgumentRole, EntityID> argument1 : arguments1.entries()) {
			if (!containsArgument(e2, arguments2, e1, argument1))
				return false;
		}
		for (Entry<ArgumentRole, EntityID> argument2 : arguments2.entries()) {
			if (!containsArgument(e1, arguments1, e2, argument2))
				return false;
		}
		return true;
	}

	/**
	 * True, if the given Map of arguments <i>arguments1</i> of entity <i>e1</i>
	 * contains an argument that matches the argument <i>argument2</i> of entity
	 * <i>e2</i>, false otherwise. More detail at:
	 * http://www.nactem.ac.uk/tsujii/GENIA/SharedTask/evaluation.shtml
	 * 
	 * @param e1
	 * @param arguments1
	 * @param e2
	 * @param argument2
	 * @return
	 */
	private static boolean containsArgument(EntityAnnotation e1, Multimap<ArgumentRole, EntityID> arguments1,
			EntityAnnotation e2, Entry<ArgumentRole, EntityID> argument2) {
		Collection<EntityID> possibleMatches = arguments1.get(argument2.getKey());
		for (EntityID entityID : possibleMatches) {
			if (matchEntities(e1.getEntity(entityID), e2.getEntity(argument2.getValue())))
				return true;
		}
		return false;
	}
}
