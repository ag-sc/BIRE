package Learning;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

import Logging.Log;

public class Vector implements Serializable{

	{
		Log.off();
	}
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
		for (String feature : this.getFeatureNames()) {
			result += this.getValueOfFeature(feature)
					* weights.getValueOfFeature(feature);
		}
		return result;
	}

	// public Vector add(Vector vector) {
	// Vector vec = new Vector();
	//
	// HashMap<String, Double> map = vector.getFeatureMap();
	//
	// for (String feature : features.keySet()) {
	// if (map.containsKey(feature)) {
	// vec.put(feature, features.get(feature) + map.get(feature));
	// } else {
	// vec.put(feature, features.get(feature));
	// }
	// }
	//
	// for (String feature : map.keySet()) {
	// if (!features.containsKey(feature)) {
	// vec.put(feature, map.get(feature));
	// }
	// }
	//
	// return vec;
	//
	// }
	//
	// private void put(String feature, Double d) {
	// features.put(feature, d);
	//
	// }
	//
	// public HashMap<String, Double> getFeatureMap() {
	// return features;
	// }

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
			// TODO default values should not be defined here
			double initialWeight = DEFAULT_VALUE;
//			Log.w("Feature %s does not exist. Insert default value %s",
//					feature, initialWeight);
			features.put(feature, initialWeight);
			return initialWeight;
		}

	}

	public HashMap<String, Double> getFeatures() {
		return features;
	}

	public Set<String> getFeatureNames() {
		return features.keySet();
	}

	public void update(String feature, double alpha) {
		double featureValue = getValueOfFeature(feature);
		featureValue += alpha;
		features.put(feature, featureValue);
	}

	public boolean hasValueForFeature(String feature) {
		return features.containsKey(feature);
	}

	// public double getValue(String feature) {
	// if (features.containsKey(feature)) {
	// return features.get(feature);
	// } else {
	// return -9999999;
	// }
	// }

	@Override
	public String toString() {
		return features.toString();
	}
}
