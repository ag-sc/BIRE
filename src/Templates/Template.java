package Templates;

import Factors.Factor;
import Learning.Vector;
import Logging.Log;
import Variables.State;

public abstract class Template {
	protected Vector weights;

	public void update(Factor f, double alpha) {
		Log.d("Update factor");
		for (String feature : f.getFeatureVector().getFeatures()) {
			weights.update(feature, alpha);
		}
	}

	public Vector getWeightVector() {
		return weights;
	}

	/**
	 * Iterate over annotations of given state and recompute factors and
	 * features. Update factors in annotations but only the factors associated
	 * with this template.
	 * 
	 * @param state
	 */
	public abstract void applyTo(State state);

}
