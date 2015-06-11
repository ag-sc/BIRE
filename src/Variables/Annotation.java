package Variables;

import java.util.HashSet;
import java.util.Set;

import Changes.StateChange;
import Factors.Factor;

public abstract class Annotation {

	Set<Factor> Factors = new HashSet<Factor>();

	StateChange change;

	public void propagateChange() {
		if (change != null) {
			for (Factor factor : Factors) {
				factor.propagateChange(this);
			}
		}
		change = null;
	}
}
