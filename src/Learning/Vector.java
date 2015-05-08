package Learning;

import java.util.HashMap;
import java.util.HashMap;
import java.util.Set;

public class Vector {

	HashMap<String,Double> features;
	
	public Vector()
	{
		features = new HashMap<String,Double>();
	}
	
	public void add(String feature, Double value) {
		features.put(feature, value);
		
	}
	
	public double multiply(Vector vector)
	{
		return 0.0;
	}
	
	public Vector add(Vector vector)
	{
		Vector vec = new Vector();
		
		HashMap<String,Double> map = vector.getFeatureMap();
		
		for (String feature: features.keySet())
		{
			if (map.containsKey(feature))
			{
				vec.put(feature, features.get(feature)+map.get(feature));
			}
			else
			{
				vec.put(feature,features.get(feature));
			}
		}
		
		for (String feature: map.keySet())
		{
			if (!features.containsKey(feature))
			{
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

		if (features.containsKey(feature))
		{
			return features.get(feature);
		}
		else
		{
			return 0.0;
		}

	}
	
	public Set<String> getFeatures()
	{
		return features.keySet();
	}
	

	
	
	public String toString()
	{
		String string ="";
		for (String feature: features.keySet())
		{
			string+= feature +" => " + features.get(feature) +"\t";
		}
		return string;
	}

}
