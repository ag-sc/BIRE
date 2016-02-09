package examples.weather;

import utility.StateID;
import utility.VariableID;
import variables.AbstractVariable;

public class PlayOutsideVariable extends AbstractVariable<PlayOutsideState> {

	private boolean playOutside;

	public PlayOutsideVariable(PlayOutsideVariable variable) {
		super(variable.id);
		this.playOutside = variable.playOutside;
	}

	public PlayOutsideVariable(VariableID id) {
		super(id);
	}

	public PlayOutsideVariable(VariableID id, boolean playOutside) {
		super(id);
		this.playOutside = playOutside;
	}

	public boolean isPlayingOutside() {
		return playOutside;
	}

	public void setPlayOutside(boolean playOutside) {
		this.playOutside = playOutside;
	}

	@Override
	public String toString() {
		return "[" + playOutside + "]";
	}

}
