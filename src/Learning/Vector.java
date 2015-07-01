package Learning;

import java.util.HashMap;
import java.util.Set;

import Logging.Log;

public class Vector {

	private HashMap<String, Double> features;

	public Vector() {
		Log.off();
		features = new HashMap<String, Double>();
	}

	public void set(String feature, Double value) {
		features.put(feature, value);

	}

	public double multiply(Vector weights) {
		Log.d("Features: %s", features);
		Log.d("Weights: %s", weights);
		double product = 1;
		for (String feature : features.keySet()) {
			product *= features.get(feature)
					* weights.getValueOfFeature(feature);
		}
		return product;
	}

	public Vector add(Vector vector) {
		Vector vec = new Vector();

		HashMap<String, Double> map = vector.getFeatureMap();

		for (String feature : features.keySet()) {
			if (map.containsKey(feature)) {
				vec.put(feature, features.get(feature) + map.get(feature));
			} else {
				vec.put(feature, features.get(feature));
			}
		}

		for (String feature : map.keySet()) {
			if (!features.containsKey(feature)) {
				vec.put(feature, map.get(feature));
			}
		}

		return vec;

	}

	private void put(String feature, Double d) {
		features.put(feature, d);

	}

	public HashMap<String, Double> getFeatureMap() {
		return features;
	}

	public double getValueOfFeature(String feature) {
		if (features.containsKey(feature)) {
			return features.get(feature);
		} else {
			return 0.0;
		}

	}

	public Set<String> getFeatures() {
		return features.keySet();
	}

	@Override
	public String toString() {
		return features.toString();
	}

	public void update(String feature, double alpha) {
		double featureValue = features.get(feature);
		featureValue += alpha;
		features.put(feature, featureValue);
	}

}
