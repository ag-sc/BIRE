package Sampling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import Changes.BoundaryChange;
import Changes.StateChange;
import Corpus.Token;
import Logging.Log;
import Variables.EntityAnnotation;
import Variables.EntityType;
import Variables.State;

public class SamplingHelper {
	public static State drawRandomlyFrom(List<State> nextStates) {
		Map<State, Double> stateMap = new HashMap<State, Double>();
		for (State state : nextStates) {
			stateMap.put(state, state.getModelScore());
		}
		return drawRandomlyFrom(stateMap);
	}

	public static State drawRandomlyFrom(Map<State, Double> nextStates) {
		Random rand = new Random();
		List<Entry<State, Double>> listOfStates = new ArrayList<Map.Entry<State, Double>>(
				nextStates.entrySet());

		// compute total sum of scores
		double totalSum = 0;
		for (Entry<State, Double> e : listOfStates) {
			totalSum += e.getValue();
		}

		double index = rand.nextDouble() * totalSum;
		double sum = 0;
		int i = 0;
		while (sum < index) {
			sum += listOfStates.get(i++).getValue();
		}
		return listOfStates.get(Math.max(0, i - 1)).getKey();
	}

	public static boolean accept(State nextState, State state,
			double acceptanceFactor) {
		if (nextState.getModelScore() > state.getModelScore()) {
			// if new state is better, always accept
			return true;
		} else {
			/*-
			 * if new state is worse, accept with probability p(accept).
			 * p(accept) = exp(-(scoreNew - scoreOld)/acceptanceFactor)
			 */
			double pNext = Math.exp(-(nextState.getModelScore() - state.getModelScore()
					/ acceptanceFactor));
			if (Math.random() < pNext)
				return true;
			else
				return false;
		}
	}

	public static void addRandomAnnotation(Token sampledToken, State state) {
		EntityAnnotation tokenAnnotation = state.getNewEntityInstanceForState();

		EntityType sampledType = sampleEntityType(state);
		tokenAnnotation.init(sampledType, sampledToken.getIndex(),
				sampledToken.getIndex());
		state.addEntityAnnotation(tokenAnnotation);
	}

	/**
	 * Chooses a unassigned role for the given EntityType and an existing
	 * annotation from the given state and add the pair as a new argument to the
	 * given entity.
	 * 
	 * @param tokenAnnotation
	 * @param state
	 */
	public static void addRandomArgument(EntityAnnotation tokenAnnotation,
			State state) {
		// get all possible argument (roles)
		List<EntityAnnotation> entities = new ArrayList<EntityAnnotation>(
				state.getEntities());
		entities.remove(tokenAnnotation);
		if (!entities.isEmpty()) {
			List<String> unassignedRoles = new ArrayList<String>();
			unassignedRoles.addAll(tokenAnnotation.getType().getCoreArguments()
					.keySet());
			unassignedRoles.addAll(tokenAnnotation.getType()
					.getOptionalArguments().keySet());
			// remove already assigned roles
			unassignedRoles.removeAll(tokenAnnotation.getArguments().keySet());

			if (!unassignedRoles.isEmpty()) {
				String sampledRole = getRandomElement(unassignedRoles);

				EntityAnnotation sampledEntity = getRandomElement(entities);
				tokenAnnotation.addArgument(sampledRole, sampledEntity.getID());
				System.out.println("\t" + tokenAnnotation.getID() + " + "
						+ sampledRole + ":" + sampledEntity.getID());
			} else {
				System.out.println("\t" + tokenAnnotation.getID() + " ("
						+ tokenAnnotation.getType().getType()
						+ "): No unassigned arguments left");
			}
		} else {
			System.out.println("\t" + tokenAnnotation.getID() + " ("
					+ tokenAnnotation.getType().getType()
					+ "): No entities for argument existing");
		}
	}

	/**
	 * Chooses an existing argument (role) randomly and removes it from the Map
	 * of arguments for this entity. Note that other annotation may still
	 * reference this argument.
	 * 
	 * @param tokenAnnotation
	 */
	public static void removeRandomArgument(EntityAnnotation tokenAnnotation) {
		if (!tokenAnnotation.getArguments().isEmpty()) {
			Map<String, String> arguments = tokenAnnotation.getArguments();
			List<String> roles = new ArrayList<String>(arguments.keySet());
			if (!roles.isEmpty()) {
				String sampledRole = getRandomElement(roles);
				tokenAnnotation.removeArgument(sampledRole);
				System.out
						.println("\t"
								+ tokenAnnotation.getID()
								+ " ("
								+ tokenAnnotation.getType().getType()
								+ "): This type is not supposed to have arguments (yet, it seems to have some. If this message shows, something is not working consistently)");
				assert (false);
			}
		} else {
			System.out.println("\t" + tokenAnnotation.getID() + " ("
					+ tokenAnnotation.getType().getType()
					+ "): No arguments assigned, yet");
		}
	}

	public static void changeBoundaries(EntityAnnotation tokenAnnotation,
			State state) {
		// the boundaries of annotations are on token level!
		switch (sampleBoundaryChange(tokenAnnotation)) {
		case EXPAND_LEFT:
			System.out.println("\texpand left");
			tokenAnnotation.setBeginTokenIndex(tokenAnnotation
					.getBeginTokenIndex() - 1);
			break;
		case CONTRACT_LEFT:
			System.out.println("\tcontract left");
			tokenAnnotation.setBeginTokenIndex(tokenAnnotation
					.getBeginTokenIndex() + 1);
			break;
		case EXPAND_RIGHT:
			System.out.println("\texpand right");
			tokenAnnotation
					.setEndTokenIndex(tokenAnnotation.getEndTokenIndex() + 1);
			break;
		case CONTRACT_RIGHT:
			System.out.println("\tcontract right");
			tokenAnnotation
					.setEndTokenIndex(tokenAnnotation.getEndTokenIndex() - 1);
			break;
		default:
			break;
		}
	}

	/**
	 * Samples an EntityType from the list of all possible Entity Types that is
	 * given via the AnnotationConfig. This object is accessible through the
	 * Document/Corpus in which this State is situated.
	 * 
	 * @param state
	 * @return
	 */
	public static EntityType sampleEntityType(State state) {
		List<EntityType> possibleEntityTypes = new ArrayList<EntityType>(state
				.getDocument().getCorpus().getCorpusConfig().getEntitiyTypes());

		EntityType randomType = possibleEntityTypes
				.get((int) (Math.random() * possibleEntityTypes.size()));
		return randomType;
	}

	/**
	 * Chooses one of the state changes as defined in {@link StateChange}
	 * according to a uniform distribution.
	 * 
	 * @return
	 */
	public static StateChange sampleStateChange(StateChange... stateChanges) {
		if (stateChanges == null || stateChanges.length == 0) {
			stateChanges = StateChange.values();
		}
		int randomIndex = (int) (Math.random() * stateChanges.length);
		return stateChanges[randomIndex];
	}

	public static BoundaryChange sampleBoundaryChange(EntityAnnotation entity) {
		List<BoundaryChange> possibleBoundaryChanges = new ArrayList<BoundaryChange>();
		possibleBoundaryChanges.add(BoundaryChange.EXPAND_LEFT);
		possibleBoundaryChanges.add(BoundaryChange.EXPAND_RIGHT);
		if (entity.getEndTokenIndex() - entity.getBeginTokenIndex() > 0) {
			possibleBoundaryChanges.add(BoundaryChange.CONTRACT_LEFT);
			possibleBoundaryChanges.add(BoundaryChange.CONTRACT_RIGHT);
		}
		return getRandomElement(possibleBoundaryChanges);
	}

	/**
	 * Returns an element off the specified list that is selected according to a
	 * uniform distribution.
	 * 
	 * @param list
	 * @return
	 */
	public static <T> T getRandomElement(List<T> list) {
		if (list.isEmpty())
			return null;
		return list.get((int) (Math.random() * list.size()));
	}

}
