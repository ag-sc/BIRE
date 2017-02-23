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

	public Vector(Vector v) {
		features = new HashMap<String, Double>(v.features);
	}

	public void set(String feature, Double value) {
		if (value != 0) {
			features.put(feature, value);
		} else {
			features.remove(feature);
		}
	}

	public void set(String feature, boolean flag) {
		set(feature, flag ? 1.0 : 0.0);
	}

	public void set(String feature, int value) {
		set(feature, (double) value);
	}

	public void remove(String feature) {
		features.remove(feature);
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
		set(feature, featureValue);
	}

	public boolean hasValueForFeature(String feature) {
		return features.containsKey(feature);
	}

	/*
	 * *********************
	 * 
	 * VECTOR ARITHMETICS
	 * 
	 * *********************
	 */
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

	public Vector mul(double f) {
		Vector result = new Vector();
		for (Entry<String, Double> feature : features.entrySet()) {
			result.set(feature.getKey(), feature.getValue() * f);
		}
		return result;
	}

	public Vector mul(Vector v) {
		Vector result = new Vector();
		Vector smaller = null;
		Vector bigger = null;
		if (getFeatureNames().size() < v.getFeatureNames().size()) {
			smaller = this;
			bigger = v;
		} else {
			smaller = v;
			bigger = this;
		}

		for (Entry<String, Double> e : smaller.getFeatures().entrySet()) {
			result.set(e.getKey(), e.getValue() * bigger.getValueOfFeature(e.getKey()));
		}
		return result;
	}

	public Vector div(double f) {
		Vector result = new Vector();
		for (Entry<String, Double> feature : features.entrySet()) {
			result.set(feature.getKey(), feature.getValue() / f);
		}
		return result;
	}

	public Vector div(Vector v) {
		Vector result = new Vector();
		for (Entry<String, Double> feature : features.entrySet()) {
			result.set(feature.getKey(), feature.getValue() / v.getValueOfFeature(feature.getKey()));
		}
		return result;
	}

	public Vector sqrt() {
		Vector result = new Vector();
		for (Entry<String, Double> feature : features.entrySet()) {
			result.set(feature.getKey(), Math.sqrt(feature.getValue()));
		}
		return result;
	}

	public Vector pow(double power) {
		Vector result = new Vector();
		for (Entry<String, Double> feature : features.entrySet()) {
			result.set(feature.getKey(), Math.pow(feature.getValue(), power));
		}
		return result;
	}

	public Vector add(Vector v) {
		Vector result = new Vector(this);
		for (Entry<String, Double> feature : v.getFeatures().entrySet()) {
			result.addToValue(feature.getKey(), feature.getValue());
		}
		return result;
	}

	public Vector add(double c) {
		Vector result = new Vector();
		for (Entry<String, Double> feature : features.entrySet()) {
			result.set(feature.getKey(), feature.getValue() + c);
		}
		return result;
	}

	public Vector sub(Vector v) {
		Vector result = new Vector(this);
		for (Entry<String, Double> feature : v.getFeatures().entrySet()) {
			result.addToValue(feature.getKey(), -feature.getValue());
		}
		return result;
	}

	public void addToValue(Vector v) {
		for (Entry<String, Double> feature : v.getFeatures().entrySet()) {
			addToValue(feature.getKey(), feature.getValue());
		}
	}

	public void subtractFromValue(Vector v) {
		for (Entry<String, Double> feature : v.getFeatures().entrySet()) {
			addToValue(feature.getKey(), -feature.getValue());
		}
	}

	public void normalize() {
		double length = length();
		if (length > 0) {
			for (Entry<String, Double> feature : features.entrySet()) {
				set(feature.getKey(), feature.getValue() / length);
			}
		}
	}

	public double length() {
		double length = 0;
		for (Entry<String, Double> feature : features.entrySet()) {
			length += Math.pow(feature.getValue(), 2);
		}
		length = Math.sqrt(length);
		return length;
	}

	@Override
	public String toString() {
		return features.toString();
	}
}
