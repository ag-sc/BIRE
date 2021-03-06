package examples.weather;

import java.util.ArrayList;
import java.util.List;

import sampling.Explorer;

public class PlayOutsideExplorer implements Explorer<PlayOutsideState> {

	@Override
	public List<PlayOutsideState> getNextStates(PlayOutsideState currentState) {
		List<PlayOutsideState> generatedStates = new ArrayList<>();
		PlayOutsideState generatedState = new PlayOutsideState(currentState);
		PlayOutsideVariable variable = generatedState.getPlayOutsideVariable();
		variable.setPlayOutside(!variable.isPlayingOutside());
		generatedStates.add(generatedState);
		return generatedStates;
	}

}
