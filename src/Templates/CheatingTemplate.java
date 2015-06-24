package Templates;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import Factors.Factor;
import Learning.ObjectiveFunction;
import Learning.Vector;
import Logging.Log;
import Variables.EntityAnnotation;
import Variables.State;

public class CheatingTemplate implements Template {

	private static final String GOLD = "GOLD";
	private Vector weights;

	private ObjectiveFunction objective = new ObjectiveFunction();

	public CheatingTemplate() {
		weights = new Vector();
		weights.set(GOLD, 1.0);
		// TODO keep weights and actual features consistent
	}

	@Override
	public void update(Factor factor, double alpha) {
		Log.d("Update factor!");
		for (String feature : factor.getFeatureVector().getFeatures()) {
			weights.update(feature, alpha);
		}
	}

	@Override
	public Vector getWeightVector() {
		return weights;
	}

	@Override
	public void applyTo(State state) {
		Set<Factor> factors = computeFactorsForState(state);
		Log.d("Apply %s factors to %s entities in state %s", factors.size(),
				state.getEntities().size(), state.getID());
		for (EntityAnnotation e : state.getEntities()) {
			for (Factor factor : factors) {
				if (appliesTo(factor, e)) {
					e.addFactors(this, Arrays.asList(factor));
				}
			}
		}
	}

	private Set<Factor> computeFactorsForState(State state) {
		Set<Factor> factors = new HashSet<Factor>();
		double score = objective.score(state, state.goldState);
		Log.d("Factor score: %s", score);
		Factor factor = new Factor(this);
		Vector vector = new Vector();
		vector.set(GOLD, score);
		factor.setFeatures(vector);

		factors.add(factor);
		return factors;
	}

	private boolean appliesTo(Factor f, EntityAnnotation e) {
		// TODO Check if factor applies to entity
		return true;
	}

	@Override
	public String toString() {
		return "CheatingTemplate [weights=" + weights + "]";
	}

}
