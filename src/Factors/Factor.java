package Factors;

import java.util.HashSet;
import java.util.Set;

import Learning.Vector;
import Logging.Log;
import Templates.Template;

public class Factor {

	{
		Log.off();
	}
	Template template;

	Vector features;

	public Factor(Template template) {
		this.template = template;
	}

	public double score() {
		Vector weights = template.getWeightVector();
		// Log.d("Score factor:");
		// Set<String> allFeatures = new
		// HashSet<String>(features.getFeatures());
		// allFeatures.addAll(weights.getFeatures());
		// for (String feature : allFeatures) {
		// Log.d("\t%s:\t\t%s * %s", feature,
		// features.getValueOfFeature(feature),
		// weights.getValueOfFeature(feature));
		// }
		double factorScore = Math.exp(features.dotProduct(weights));
		return factorScore;
	}

	public Template getTemplate() {
		return template;
	}

	public Vector getFeatureVector() {
		return features;
	}

	public void setTemplate(Template template) {
		this.template = template;
	}

	public void setFeatures(Vector features) {
		this.features = features;
	}

	@Override
	public String toString() {
		return "Factor [template=" + template + ",\n" + "features=" + features
				+ "]";
	}

}
