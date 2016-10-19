package examples.weather;

import utility.StateID;
import utility.VariableID;
import variables.AbstractVariable;

public class PlayOutsideVariable extends AbstractVariable {

	private boolean playOutside;

	public PlayOutsideVariable(PlayOutsideVariable variable) {
		this.playOutside = variable.playOutside;
	}

	public PlayOutsideVariable(VariableID id, boolean playOutside) {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (playOutside ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PlayOutsideVariable other = (PlayOutsideVariable) obj;
		if (playOutside != other.playOutside)
			return false;
		return true;
	}

}
