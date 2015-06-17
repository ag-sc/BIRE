package Templates;

import java.util.Arrays;

import Factors.Factor;
import Learning.ObjectiveFunction;
import Learning.Vector;
import Variables.EntityAnnotation;
import Variables.State;

public class CheatingTemplate implements Template {

	private static final String GOLD = "GOLD";
	Vector weights;
	ObjectiveFunction objective = new ObjectiveFunction();

	public CheatingTemplate() {
		weights = new Vector();
		weights.set(GOLD, 0.0);
	}

	@Override
	public void update(Vector features, double alpha) {
		for (String feature : features.getFeatures()) {
			weights.update(feature,alpha);
		}
	}

//	@Override
//	public void recompute(Annotation annotation, Vector features) {
//		// TODO Auto-generated method stub
//
//	}

	@Override
	public Vector getWeightVector() {
		return weights;
	}

	@Override
	public void applyTo(State state) {
		double score = objective.score(state, state.goldState);
		Factor factor = new Factor();
		factor.setTemplate(this);

		Vector vector = new Vector();
		vector.set(GOLD, score);
		factor.setFeatures(vector);

		for (EntityAnnotation e : state.getEntities()) {
			e.addFactors(this, Arrays.asList(factor));
		}
	}

}
