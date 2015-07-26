package Factors;

import Learning.Vector;
import Logging.Log;
import Templates.Template;
import Variables.EntityAnnotation;

public class Factor {

	{
		Log.off();
	}
	private Template template;

	private Vector features;
	private EntityAnnotation entity;

	public Factor(Template template) {
		this.template = template;
	}

	// public double score() {
	// Vector weights = template.getWeightVector();
	// // Log.d("Score factor:");
	// // Set<String> allFeatures = new
	// // HashSet<String>(features.getFeatures());
	// // allFeatures.addAll(weights.getFeatures());
	// // for (String feature : allFeatures) {
	// // Log.d("\t%s:\t\t%s * %s", feature,
	// // features.getValueOfFeature(feature),
	// // weights.getValueOfFeature(feature));
	// // }
	// double factorScore = Math.exp(features.dotProduct(weights));
	// return factorScore;
	// }

	// public Template getTemplate() {
	// return template;
	// }

	public Vector getFeatureVector() {
		return features;
	}

	// public void setTemplate(Template template) {
	// this.template = template;
	// }

	public void setFeatures(Vector features) {
		this.features = features;
	}

	public EntityAnnotation getEntity() {
		return entity;
	}

	public void setEntity(EntityAnnotation entity) {
		this.entity = entity;
	}

	@Override
	public String toString() {
		return "Factor [template=" + template + ",\n" + "features=" + features
				+ "]";
	}

}
