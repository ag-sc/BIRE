package examples.weather;

import learning.ObjectiveFunction;

public class PlayOutsideObjectiveFunction extends ObjectiveFunction<PlayOutsideState, Boolean> {

	@Override
	protected double computeScore(PlayOutsideState state, Boolean goldResult) {
		return state.getPlayOutsideVariable().isPlayingOutside() == goldResult ? 1.0 : 0.0;
	}

}
