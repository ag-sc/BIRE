package sampling;

import java.util.ArrayList;
import java.util.List;

import variables.AbstractState;

/**
 * This is an implementation of the Explorer interface that merges the
 * individual results of a given list of explorers into a single list of
 * possible successor states.
 * 
 * @param explorers
 */
public class JoinExplorer<StateT extends AbstractState<?>> implements Explorer<StateT> {

	private List<Explorer<StateT>> explorers;

	/**
	 * This is an implementation of the Explorer interface that merges the
	 * individual results of a given list of explorers into a single list of
	 * possible successor states.
	 * 
	 * @param explorers
	 */
	public JoinExplorer(List<Explorer<StateT>> explorers) {
		this.explorers = explorers;
	}

	@Override
	public List<StateT> getNextStates(StateT currentState) {
		List<StateT> nextStates = new ArrayList<>();
		for (Explorer<StateT> explorer : explorers) {
			nextStates.addAll(explorer.getNextStates(currentState));
		}
		return nextStates;
	}

}
