package Factors;

import java.util.UUID;

import Learning.Vector;
import Logging.Log;
import Templates.Template;
import utility.FactorID;

public class Factor {

	private static final String FACTOR_ID_PATTERN = "%s-F%s";

	{
		Log.off();
	}

	// private static long factorIDIndex;
	private Template template;
	private FactorID factorID;
	private Vector features;

	private Factor() {
		this.factorID = generateFactorID();
	}

	public Factor(Template template) {
		this();
		this.template = template;
	}

	private FactorID generateFactorID() {
		// String id = String.valueOf(factorIDIndex++);
		String id = String.valueOf(UUID.randomUUID().toString());
		return new FactorID(id);
	}

	public Vector getFeatureVector() {
		return features;
	}

	public FactorID getID() {
		return factorID;
	}

	public void setFeatures(Vector features) {
		this.features = features;
	}

	@Override
	public String toString() {
		return "Factor [template=" + template + ",\n" + "features=" + features + "]";
	}

}
