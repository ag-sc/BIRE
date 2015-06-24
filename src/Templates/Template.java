package Templates;

import Factors.Factor;
import Learning.Vector;
import Variables.State;

public interface Template {

	void update(Factor f, double alpha);
//
//	void recompute(Annotation annotation, Vector features);

	Vector getWeightVector();

	/**
	 * Iterate over annotations of given state and recompute factors and
	 * features. Update factors in annotations but only the factors associated
	 * with this template.
	 * 
	 * @param state
	 */
	void applyTo(State state);

}
