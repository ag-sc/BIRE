package Templates;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import Factors.Factor;
import Learning.ObjectiveFunction;
import Learning.Vector;
import Logging.Log;
import Variables.State;

public class CheatingTemplate extends Template implements Serializable {

	{
		Log.off();
	}
	private static final String GOLD = "GOLD";

	private ObjectiveFunction objective = new ObjectiveFunction();

	public CheatingTemplate() {
		// weights.set(GOLD, 1.0);
	}

	@Override
	public List<Factor> generateFactors(State state) {
		List<Factor> factors = new ArrayList<Factor>();
		// TODO one factor for all entites. Is that correct? Is that always
		// possible?
		Factor factor = new Factor(this);
		factors.add(factor);
		Vector featureVector = new Vector();
		factor.setFeatures(featureVector);
		// e.addFactors(this, Arrays.asList(factor));

		double score = objective.score(state, state.goldState).score;
		featureVector.set(GOLD, score);

		// for (EntityAnnotation e : state.getEntities()) {
		// if (e.isChanged()) {
		// Log.d("\tAdd features to entity %s (\"%s\"):", e.getID(),
		// e.getText());
		// Factor factor = new Factor(this);
		// factors.add(factor);
		// Vector featureVector = new Vector();
		// factor.setFeatures(featureVector);
		// // e.addFactors(this, Arrays.asList(factor));
		//
		// double score = objective.score(state, state.goldState);
		// featureVector.set(GOLD, score);
		//
		// Log.d("\tFeatures for entity %s (\"%s\"): %s", e.getID(),
		// e.getText(), featureVector);
		// }
		// }
		return factors;
	}

}
