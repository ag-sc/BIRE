package learning;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Vector implements Serializable {

	private static final double DEFAULT_VALUE = 0;
	private HashMap<String, Double> features;

	/**
	 * This class basically wraps a Map of feature names and values.
	 * Additionally, it provides convenience functions for some of the
	 * computations during the learning process.
	 */
	public Vector() {
		features = new HashMap<String, Double>();
	}

	public void set(String feature, Double value) {
		features.put(feature, value);
	}

	public void set(String feature, boolean flag) {
		features.put(feature, flag ? 1.0 : 0.0);
	}

	public void remove(String feature) {
		features.remove(feature);
	}

	public double dotProduct(Vector weights) {
		double result = 0;
		Vector smaller = null;
		Vector bigger = null;
		if (getFeatureNames().size() < weights.getFeatureNames().size()) {
			smaller = this;
			bigger = weights;
		} else {
			smaller = weights;
			bigger = this;
		}

		for (Entry<String, Double> e : smaller.getFeatures().entrySet()) {
			result += e.getValue() * bigger.getValueOfFeature(e.getKey());
		}
		return result;
	}

	/**
	 * Returns the value of this feature. If this vector does not contain this
	 * feature a default value is stored and returned for this feature.
	 * 
	 * @param feature
	 * @return
	 */
	public double getValueOfFeature(String feature) {
		return features.getOrDefault(feature, DEFAULT_VALUE);
	}

	public Map<String, Double> getFeatures() {
		return features;
	}

	public Set<String> getFeatureNames() {
		return features.keySet();
	}

	public void addToValue(String feature, double alpha) {
		double featureValue = getValueOfFeature(feature);
		featureValue += alpha;
		features.put(feature, featureValue);
	}

	public boolean hasValueForFeature(String feature) {
		return features.containsKey(feature);
	}

	public void add(Vector v) {
		for (Entry<String, Double> feature : v.getFeatures().entrySet()) {
			addToValue(feature.getKey(), feature.getValue());
		}
	}

	public void sub(Vector v) {
		for (Entry<String, Double> feature : v.getFeatures().entrySet()) {
			addToValue(feature.getKey(), -feature.getValue());
		}
	}

	@Override
	public String toString() {
		return features.toString();
	}

	public void normalize() {
		double length = 0;
		for (Entry<String, Double> feature : features.entrySet()) {
			length += Math.pow(feature.getValue(), 2);
		}
		length = Math.sqrt(length);
		if (length > 0) {
			for (Entry<String, Double> feature : features.entrySet()) {
				features.put(feature.getKey(), feature.getValue() / length);
			}
		}
	}
}
