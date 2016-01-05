package sampling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import variables.AbstractState;

public class SamplingUtils {

	/**
	 * Selects a state from the given list according to the probability
	 * distribution defined by the states' (model/objective) scores. Each score
	 * is divided by the total sum of all scores, in order to create a
	 * probability distribution across states. If "softmax" is true, the
	 * probability distribution is computed using the softmax formula.
	 * 
	 * @param nextStates
	 * @param model
	 * @param softmax
	 * @return
	 */
	public static <StateT extends AbstractState> StateT drawFromDistribution(List<StateT> nextStates, boolean model,
			boolean softmax) {
		// compute total sum of scores
		Function<StateT, Double> toScore = null;
		if (model) {
			toScore = s -> s.getModelScore();
		} else {
			toScore = d -> d.getObjectiveScore();
		}
		Function<Double, Double> toProbability = null;
		if (softmax) {
			toProbability = d -> Math.exp(d);
		} else {
			toProbability = d -> d;
		}
		double totalSum = 0;
		for (StateT s : nextStates) {
			if (model) {
				totalSum += toProbability.apply(toScore.apply(s));
			} else {
				totalSum += toProbability.apply(toScore.apply(s));
			}
		}

		double index = Math.random() * totalSum;
		double sum = 0;
		int i = 0;
		while (sum < index) {
			if (model) {
				sum += toProbability.apply(toScore.apply(nextStates.get(i++)));
			} else {
				sum += toProbability.apply(toScore.apply(nextStates.get(i++)));
			}
		}
		return nextStates.get(Math.max(0, i - 1));
	}

	// TODO implement a "temperature" approach (simulated annealing)
	/**
	 * This function decides if the currentState should be replaced with the
	 * selectedNextState. The decision is based on the scores (model or
	 * objective) of both states. Currently, it implements the accept function
	 * from the Metropolis Hastings algorithm.
	 * 
	 * @param candidateState
	 * @param currentState
	 * @param useModelDistribution
	 * @return
	 */
	public static <StateT extends AbstractState> boolean accept(StateT candidateState, StateT currentState,
			boolean useModelDistribution) {
		double pCurrent = 0;
		double pCandidate = 0;
		if (useModelDistribution) {
			pCurrent = currentState.getModelScore();
			pCandidate = candidateState.getModelScore();
		} else {
			pCurrent = currentState.getObjectiveScore();
			pCandidate = candidateState.getObjectiveScore();
		}
		// double k = 1;
		// double T = 1;
		// // Simulated Annealing
		// double p = Math.exp(-(En - Ec) / (k * T));
		// // always accept when p>0 otherwise accept with probability p
		// return Math.random() < p;

		double ratio = pCandidate / pCurrent;
		if (ratio >= 1) {
			return true;
		} else {
			return Math.random() < ratio;
		}
		// return En >= Ec;
		// return true;
	}

	public static <T> T drawRandomElement(List<T> allNextStates) {
		int randomIndex = (int) (allNextStates.size() * Math.random());
		return allNextStates.get(randomIndex);
	}

	public static <T> List<T> nRandomElements(List<T> allNextStates, int n) {
		List<T> copy = new ArrayList<>(allNextStates);
		Collections.shuffle(copy);
		return copy.subList(0, Math.min(n, copy.size()));

	}
}
