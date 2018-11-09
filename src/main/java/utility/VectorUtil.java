package utility;

import java.util.Collection;
import java.util.Map.Entry;

import exceptions.MissingFactorException;
import factors.Factor;
import learning.Vector;
import templates.AbstractTemplate;
import variables.AbstractState;

public class VectorUtil {

	/**
	 * Computes the differences of all features of both states. For this, the
	 * template uses features from all factors that are not associated to both
	 * states (since these differences would be always 0).
	 * 
	 * @param state1
	 * @param state2
	 * @return
	 */
	public static <StateT extends AbstractState<?>> Vector getFeatureDifferences(
			AbstractTemplate<?, StateT, ?> template, StateT state1, StateT state2) {
		Vector diff = new Vector();
		Collection<Factor<?>> factors1 = null;
		Collection<Factor<?>> factors2 = null;
		try {
			factors1 = state1.getFactorGraph().getFactors();
			factors2 = state2.getFactorGraph().getFactors();
		} catch (MissingFactorException e) {
			e.printStackTrace();
		}
		for (Factor<?> factor : factors1) {
			if (factor.getTemplate() == template) {
				Vector featureVector = factor.getFeatureVector();
				diff.addFAST(featureVector);
			}
		}
		for (Factor<?> factor : factors2) {
			if (factor.getTemplate() == template) {
				Vector featureVector = factor.getFeatureVector();
				diff.subFAST(featureVector);
			}
		}
		return diff;
	}

	public static Vector crossProduct(Vector v1, Vector v2) {
		Vector cross = new Vector();
		for (Entry<String, Double> feature1 : v1.getNamedFeatures().entrySet()) {
			for (Entry<String, Double> feature2 : v2.getNamedFeatures().entrySet()) {
				cross.set(feature1.getKey() + "_AND_" + feature2.getKey(), feature1.getValue() * feature2.getValue());
			}

		}
		return cross;

	}
}
