package learning;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Vector implements Serializable {

	private static final Map<Integer, String> featureIndexToName = new HashMap<>();
	private static final Map<String, Integer> featureNameToIndex = new HashMap<>();

	private static final Double DEFAULT_VALUE = 0.0;

	private HashMap<Integer, Double> features;

	/**
	 * This class basically wraps a Map of feature names and values. Additionally,
	 * it provides convenience functions for some of the computations during the
	 * learning process.
	 */
	public Vector() {
		features = new HashMap<Integer, Double>();
	}

	private Vector(Vector v) {
		features = new HashMap<Integer, Double>(v.features);
	}

	public void set(String feature, Double value) {

		final Integer index = getIndex(feature);
		if (value != 0.0) {
			features.put(index, value);
		} else {
			remove(index);
		}

	}

	private void set(Integer featureIndex, Double value) {

		if (value != 0.0) {
			features.put(featureIndex, value);
		} else {
			remove(featureIndex);
		}

	}

	public void set(String feature, boolean flag) {
		set(feature, flag ? 1.0 : 0.0);
	}

	public void set(String feature, int value) {
		set(feature, (double) value);
	}

	// public void remove(String feature) {
//		features.remove(getIndex(feature));
//	}

	private void remove(Integer featureIndex) {
		features.remove(featureIndex);
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

	public double getValueOfFeature(Integer featureIndex) {
		return features.getOrDefault(featureIndex, DEFAULT_VALUE);
	}

	public Map<String, Double> getNamedFeatures() {
		return toNamedFeatures(features);
	}

	public Map<Integer, Double> getFeatures() {
		return features;
	}

	public Set<String> getFeatureNames() {
		return getNames(features.keySet());
	}

//	public void addToValue(String feature, double alpha) {
//		double featureValue = getValueOfFeature(feature);
//		featureValue += alpha;
//		set(feature, featureValue);
//	}

	public void addToValue(Integer featureIndex, double alpha) {
		double featureValue = getValueOfFeature(featureIndex);
		featureValue += alpha;
		set(featureIndex, featureValue);
	}

//	public boolean hasValueForFeature(String feature) {
//		return features.containsKey(feature);
//	}

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
		if (getFeatures().size() < weights.getFeatures().size()) {
			smaller = this;
			bigger = weights;
		} else {
			smaller = weights;
			bigger = this;
		}

		for (Entry<Integer, Double> e : smaller.getFeatures().entrySet()) {
			result += e.getValue() * bigger.getValueOfFeature(e.getKey());
		}
		return result;
	}

	public Vector mul(double f) {
		Vector result = new Vector();
		for (Entry<Integer, Double> feature : features.entrySet()) {
			result.set(feature.getKey(), feature.getValue() * f);
		}
		return result;
	}

	public Vector mul_FAST(double f) {
		Vector result = new Vector();
		for (Entry<Integer, Double> feature : features.entrySet()) {
			result.set(feature.getKey(), feature.getValue() * f);
		}
		return result;
	}

	public Vector mul(Vector v) {
		Vector result = new Vector();
		Vector smaller = null;
		Vector bigger = null;
		if (getFeatures().size() < v.getFeatures().size()) {
			smaller = this;
			bigger = v;
		} else {
			smaller = v;
			bigger = this;
		}

		for (Entry<Integer, Double> e : smaller.getFeatures().entrySet()) {
			result.set(e.getKey(), e.getValue() * bigger.getValueOfFeature(e.getKey()));
		}
		return result;
	}

	public Vector div(double f) {
		Vector result = new Vector();
		for (Entry<Integer, Double> feature : features.entrySet()) {
			result.set(feature.getKey(), feature.getValue() / f);
		}
		return result;
	}

	public Vector div(Vector v) {
		Vector result = new Vector();
		for (Entry<Integer, Double> feature : features.entrySet()) {
			result.set(feature.getKey(), feature.getValue() / v.getValueOfFeature(feature.getKey()));
		}
		return result;
	}

	public Vector sqrt() {
		Vector result = new Vector();
		for (Entry<Integer, Double> feature : features.entrySet()) {
			result.set(feature.getKey(), Math.sqrt(feature.getValue()));
		}
		return result;
	}

	public Vector pow(double power) {
		Vector result = new Vector();
		for (Entry<Integer, Double> feature : features.entrySet()) {
			result.set(feature.getKey(), Math.pow(feature.getValue(), power));
		}
		return result;
	}

	public Vector add(Vector v) {
		Vector result = new Vector(this);
		for (Entry<Integer, Double> feature : v.getFeatures().entrySet()) {
			result.addToValue(feature.getKey(), feature.getValue());
		}
		return result;
	}

	public void addFAST(Vector v) {
		for (Entry<Integer, Double> feature : v.getFeatures().entrySet()) {
			addToValue(feature.getKey(), feature.getValue());
		}
	}

	public Vector add(double c) {
		Vector result = new Vector();
		for (Entry<Integer, Double> feature : features.entrySet()) {
			result.set(feature.getKey(), feature.getValue() + c);
		}
		return result;
	}

	public Vector sub(Vector v) {
		Vector result = new Vector(this);
		for (Entry<Integer, Double> feature : v.getFeatures().entrySet()) {
			result.addToValue(feature.getKey(), -feature.getValue());
		}
		return result;
	}

	public void subFAST(Vector v) {
		for (Entry<Integer, Double> feature : v.getFeatures().entrySet()) {
			addToValue(feature.getKey(), -feature.getValue());
		}
	}

	public void addToValue(Vector v) {
		for (Entry<Integer, Double> feature : v.getFeatures().entrySet()) {
			addToValue(feature.getKey(), feature.getValue());
		}
	}

	public void subtractFromValue(Vector v) {
		for (Entry<Integer, Double> feature : v.getFeatures().entrySet()) {
			addToValue(feature.getKey(), -feature.getValue());
		}
	}

	public void normalize() {
		double length = length();
		if (length > 0) {
			for (Entry<Integer, Double> feature : features.entrySet()) {
				set(feature.getKey(), feature.getValue() / length);
			}
		}
	}

	public double length() {
		double length = 0;
		for (Double feature : features.values()) {
			length += Math.pow(feature, 2);
		}
		length = Math.sqrt(length);
		return length;
	}

	@Override
	public String toString() {

		return features.toString();
	}

	/**
	 * Returns a new index if the feature is new or the index to the feature.
	 * 
	 * @param feature
	 * @return
	 */
	private Integer getIndex(String feature) {

		Integer index = featureNameToIndex.get(feature);

		if (index == null) {
			index = featureNameToIndex.size();

			featureNameToIndex.put(feature, index);
			featureIndexToName.put(index, feature);
		}

		return index;
	}

	private Set<String> getNames(Set<Integer> featureIndicies) {

		Set<String> featureNames = new HashSet<>();

		for (Integer index : featureIndicies) {
			featureNames.add(getName(index));
		}

		return featureNames;
	}

	public static String getName(Integer featureIndex) {
		return featureIndexToName.get(featureIndex);
	}

	private static Map<String, Double> toNamedFeatures(HashMap<Integer, Double> features) {
		final Map<String, Double> namedFeatures = new HashMap<>();

		for (Entry<Integer, Double> feature : features.entrySet()) {
			namedFeatures.put(getName(feature.getKey()), feature.getValue());
		}

		return namedFeatures;
	}
}
