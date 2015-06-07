package Sampling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import Changes.StateChange;
import Corpus.Token;
import Learning.Scorer;
import Variables.EntityAnnotation;
import Variables.EntityType;
import Variables.State;

public class DefaultSampler implements Sampler {

	int numberStates;
	/**
	 * This variable is used to determine if a state with a lower score should
	 * be accepted nonetheless. This value should start with a value < 1 and
	 * decrease during the training. This allows to accept worse states more
	 * often at the beginning of the training than at the end.
	 */
	double acceptanceFactor = 1;

	public State getNextState(State state, Scorer scorer) {
		// a set of k next states (List of Annotations)

		Map<State, Double> next_states;

		State next_state;

		next_states = generateNextStates(state, numberStates, scorer);

		next_state = drawRandomlyFrom(next_states);

		if (accept(next_state, state)) {
			next_state.propagateChange();
			return next_state;
		} else {
			return state;
		}

	}

	private boolean accept(State nextState, State state) {
		if (nextState.getScore() > state.getScore()) {
			// if new state is better, always accept
			return true;
		} else {
			/*-
			 * if new state is worse, accept with probability p(accept).
			 * p(accept) = exp(-(scoreNew - scoreOld)/acceptanceFactor)
			 */
			double pNext = Math.exp(-(nextState.getScore() - state.getScore()
					/ acceptanceFactor));
			if (Math.random() < pNext)
				return true;
			else
				return false;
		}
	}

	private State drawRandomlyFrom(Map<State, Double> nextStates) {
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

	private Map<State, Double> generateNextStates(State state,
			int numberStates, Scorer scorer) {
		Map<State, Double> generatedStates = new HashMap<State, Double>();
		for (int i = 0; i < numberStates; i++) {
			State generatedState = new State(state);
			List<Token> tokens = generatedState.getDocument().getTokens();
			// sample token
			Token sampledToken = getRandomElement(tokens);
			// get annotation for token
			// if no annotation
			if (!state.tokenHasAnnotation(sampledToken)) {
				// add annotation with random type
				addRandomAnnotation(sampledToken, generatedState);
			} else {
				// Tokens may be referenced/annotated by different entities
				List<String> linkedEntities = new ArrayList<String>(
						state.getAnnotationForToken(sampledToken));
				// pick one at random
				EntityAnnotation tokenAnnotation = state
						.getEntity(getRandomElement(linkedEntities));
				// if annotation exists
				// choose a way to alter the state
				StateChange stateChange = sampleStateChange();
				switch (stateChange) {
				case ANNOTATION_DELETED:
					// TODO state as handler of Token/entity mapping for
					// consistency
					generatedState.removeEntityAnnotation(tokenAnnotation);
					break;
				case TYPE_CHANGED:
					EntityType sampledType = sampleEntityType(generatedState);
					tokenAnnotation.setType(sampledType);
					break;
				case ARGUMENT_ADDED:
					addRandomArgument(tokenAnnotation, generatedState);
					break;
				case ARGUMENT_REMOVED:
					removeRandomArgument(tokenAnnotation);
					break;
				case BOUNDARIES_CHANGED:
					changeBoundaries(tokenAnnotation, generatedState);
					break;
				}
			}

			double score = scorer.score(generatedState);
			generatedState.setScore(score);
			generatedStates.put(state, score);
		}
		return generatedStates;
	}

	private void addRandomAnnotation(Token sampledToken, State state) {
		EntityAnnotation tokenAnnotation = state.getNewEntityInstance();

		EntityType sampledType = sampleEntityType(state);

		tokenAnnotation.setBeginTokenIndex(sampledToken.getIndex());
		tokenAnnotation.setEndTokenIndex(sampledToken.getIndex());
		tokenAnnotation.setType(sampledType);
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
	private void addRandomArgument(EntityAnnotation tokenAnnotation, State state) {
		// get all possible argument (roles)
		List<String> assignedRoles = new ArrayList<String>();
		assignedRoles.addAll(tokenAnnotation.getType().getCoreArguments()
				.keySet());
		assignedRoles.addAll(tokenAnnotation.getType().getOptionalArguments()
				.keySet());
		// remove already assigned roles
		assignedRoles.removeAll(tokenAnnotation.getArguments().keySet());

		String sampledRole = getRandomElement(assignedRoles);

		List<EntityAnnotation> entities = new ArrayList<EntityAnnotation>(
				state.getEntities());
		EntityAnnotation sampledEntity = getRandomElement(entities);
		tokenAnnotation.addArgument(sampledRole, sampledEntity.getID());
	}

	/**
	 * Chooses an existing argument (role) randomly and removes it from the Map
	 * of arguments for this entity. Note that other annotation may still
	 * reference this argument.
	 * 
	 * @param tokenAnnotation
	 */
	private void removeRandomArgument(EntityAnnotation tokenAnnotation) {
		Map<String, String> arguments = tokenAnnotation.getArguments();
		List<String> roles = new ArrayList<String>(arguments.keySet());
		String sampledRole = getRandomElement(roles);
		tokenAnnotation.removeArgument(sampledRole);
	}

	private void changeBoundaries(EntityAnnotation tokenAnnotation, State state) {
		// the boundaries of Annotation are on token level!
		int direction = (int) (Math.random() * 4);
		switch (direction) {
		case 0:
			// expand left
			tokenAnnotation.setBeginTokenIndex(tokenAnnotation.getBeginTokenIndex() - 1);
			break;
		case 1:
			// contract left
			tokenAnnotation.setBeginTokenIndex(tokenAnnotation.getBeginTokenIndex() + 1);
			break;
		case 2:
			// expand right
			tokenAnnotation.setEndTokenIndex(tokenAnnotation.getEndTokenIndex() + 1);
			break;
		case 3:
			// contract right
			tokenAnnotation.setEndTokenIndex(tokenAnnotation.getEndTokenIndex() - 1);
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
	private EntityType sampleEntityType(State state) {
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
	private StateChange sampleStateChange() {
		int randomIndex = (int) (Math.random() * StateChange.values().length);
		return StateChange.values()[randomIndex];
	}

	/**
	 * Returns an element off the specified list that is selected according to a
	 * uniform distribution.
	 * 
	 * @param list
	 * @return
	 */
	private <T> T getRandomElement(List<T> list) {
		return list.get((int) (Math.random() * list.size()));
	}
}
