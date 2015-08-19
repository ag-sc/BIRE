package Learning;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import Logging.Log;

public class Vector implements Serializable {

	private static final double DEFAULT_VALUE = 0;
	private HashMap<String, Double> features;

	public Vector() {
		features = new HashMap<String, Double>();
	}

	public void set(String feature, Double value) {
		features.put(feature, value);

	}

	public void remove(String feature) {
		features.remove(feature);
	}

	public double dotProduct(Vector weights) {
		double result = 0;
		for (Entry<String, Double> e : features.entrySet()) {
			result += e.getValue() * weights.getValueOfFeature(e.getKey());
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
		if (features.containsKey(feature)) {
			return features.get(feature);
		} else {
			return DEFAULT_VALUE;
		}

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

	public static Vector substract(Vector v1, Vector v2) {
		Set<String> allFeatures = new HashSet<String>();
		allFeatures.addAll(v1.getFeatureNames());
		allFeatures.addAll(v2.getFeatureNames());
		Vector sub = new Vector();
		for (String f : allFeatures) {
			sub.set(f, v1.getValueOfFeature(f) - v2.getValueOfFeature(f));
		}
		return sub;
	}

	@Override
	public String toString() {
		return features.toString();
	}

}
