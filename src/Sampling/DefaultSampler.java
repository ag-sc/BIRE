package Sampling;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import Changes.StateChange;
import Corpus.Token;
import Learning.Scorer;
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

	public State getNextState(State state, Scorer scorer) {

		Map<State, Double> nextStates = generateNextStates(state,
				numberOfStates, scorer);
		System.out.println("generated states:");
		for (State s : nextStates.keySet()) {
			// System.out.println("["
			// + String.valueOf(s.getScore()).substring(0, 5) + "]: " + s);
			System.out.println("[" + df.format(s.getScore()) + "]: " + s);
		}
		State nextState = drawRandomlyFrom(nextStates);

		if (accept(nextState, state)) {
			// nextState.propagateChange();
			return nextState;
		} else {
			return state;
		}

	}

	private boolean accept(State nextState, State state) {
		return true;
	}

	// private boolean accept(State nextState, State state) {
	// if (nextState.getScore() > state.getScore()) {
	// // if new state is better, always accept
	// return true;
	// } else {
	// /*-
	// * if new state is worse, accept with probability p(accept).
	// * p(accept) = exp(-(scoreNew - scoreOld)/acceptanceFactor)
	// */
	// double pNext = Math.exp(-(nextState.getScore() - state.getScore()
	// / acceptanceFactor));
	// if (Math.random() < pNext)
	// return true;
	// else
	// return false;
	// }
	// }

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

	private Map<State, Double> generateNextStates(State previousState,
			int numberOfStates, Scorer scorer) {
		Map<State, Double> generatedStates = new HashMap<State, Double>();
		for (int i = 0; i < numberOfStates; i++) {
			State generatedState = new State(previousState);
			List<Token> tokens = generatedState.getDocument().getTokens();
			// sample token
			Token sampledToken = getRandomElement(tokens);
			// get annotation for token
			// if no annotation
			if (!generatedState.tokenHasAnnotation(sampledToken)) {
				// add annotation with random type
				System.out.println(generatedState.id + ": add annotation.");
				addRandomAnnotation(sampledToken, generatedState);
			} else {
				// Tokens may be referenced/annotated by different entities
				List<String> linkedEntities = new ArrayList<String>(
						generatedState.getAnnotationForToken(sampledToken));
				// pick one at random
				EntityAnnotation tokenAnnotation = generatedState
						.getEntity(getRandomElement(linkedEntities));
				// if annotation exists
				// choose a way to alter the state
				StateChange stateChange = sampleStateChange();
				switch (stateChange) {
				case ANNOTATION_DELETED:
					// TODO delete annotation completely or only from this token
					// (this might require to split an annotation in to separate
					// annotations)
					System.out.println(generatedState.id
							+ ": delete annotation.");
					generatedState.removeEntityAnnotation(tokenAnnotation);
					break;
				case TYPE_CHANGED:
					System.out.println(generatedState.id
							+ ": change annotation type.");
					EntityType sampledType = sampleEntityType(generatedState);
					tokenAnnotation.setType(sampledType);
					break;
				case ARGUMENT_ADDED:
					System.out.println(generatedState.id
							+ ": add annotation argument.");
					addRandomArgument(tokenAnnotation, generatedState);
					break;
				case ARGUMENT_REMOVED:
					System.out.println(generatedState.id
							+ ": remove annotation argument.");
					removeRandomArgument(tokenAnnotation);
					break;
				case BOUNDARIES_CHANGED:
					System.out.println(generatedState.id
							+ ": change annotation boundaries.");
					changeBoundaries(tokenAnnotation, generatedState);
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

	private void addRandomAnnotation(Token sampledToken, State state) {
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
	private void addRandomArgument(EntityAnnotation tokenAnnotation, State state) {
		// get all possible argument (roles)
		List<String> assignedRoles = new ArrayList<String>();
		assignedRoles.addAll(tokenAnnotation.getType().getCoreArguments()
				.keySet());
		assignedRoles.addAll(tokenAnnotation.getType().getOptionalArguments()
				.keySet());
		// remove already assigned roles
		assignedRoles.removeAll(tokenAnnotation.getArguments().keySet());

		if (!assignedRoles.isEmpty()) {
			String sampledRole = getRandomElement(assignedRoles);
			List<EntityAnnotation> entities = new ArrayList<EntityAnnotation>(
					state.getEntities());
			entities.remove(tokenAnnotation);
			if (!entities.isEmpty()) {
				EntityAnnotation sampledEntity = getRandomElement(entities);
				tokenAnnotation.addArgument(sampledRole, sampledEntity.getID());
			}
		}
	}

	/**
	 * Chooses an existing argument (role) randomly and removes it from the Map
	 * of arguments for this entity. Note that other annotation may still
	 * reference this argument.
	 * 
	 * @param tokenAnnotation
	 */
	private void removeRandomArgument(EntityAnnotation tokenAnnotation) {
		if (!tokenAnnotation.getArguments().isEmpty()) {
			Map<String, String> arguments = tokenAnnotation.getArguments();
			List<String> roles = new ArrayList<String>(arguments.keySet());
			if (!roles.isEmpty()) {
				String sampledRole = getRandomElement(roles);
				tokenAnnotation.removeArgument(sampledRole);
			}
		}
	}

	private void changeBoundaries(EntityAnnotation tokenAnnotation, State state) {
		// the boundaries of annotations are on token level!
		int direction = (int) (Math.random() * 4);
		switch (direction) {
		case 0:
			// expand left
			tokenAnnotation.setBeginTokenIndex(tokenAnnotation
					.getBeginTokenIndex() - 1);
			break;
		case 1:
			// contract left
			tokenAnnotation.setBeginTokenIndex(tokenAnnotation
					.getBeginTokenIndex() + 1);
			break;
		case 2:
			// expand right
			tokenAnnotation
					.setEndTokenIndex(tokenAnnotation.getEndTokenIndex() + 1);
			break;
		case 3:
			// contract right
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
		if (list.isEmpty())
			return null;
		return list.get((int) (Math.random() * list.size()));
	}
}
