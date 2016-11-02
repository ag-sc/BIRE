package factors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import exceptions.MissingFactorException;

public class FactorPool {

	private static Logger log = LogManager.getFormatterLogger();
	private Map<FactorVariables, Factor<? extends FactorVariables>> factorVariables2Factor;

	private static boolean shareFactors = true;
	private static FactorPool sharedInstance = null;
	public String name = "";

	private FactorPool() {
		factorVariables2Factor = new ConcurrentHashMap<>();
	}
	//
	// public FactorPool(String name) {
	// this();
	// this.name = name;
	// }

	// public static void setShareFactors(boolean b) {
	// shareFactors = b;
	// }

	public static FactorPool getInstance() {
		if (sharedInstance == null) {
			sharedInstance = new FactorPool();
		}
		return sharedInstance;
		// if (shareFactors) {
		// if (sharedInstance == null) {
		// sharedInstance = new FactorPool();
		// }
		// return sharedInstance;
		// } else {
		// return new FactorPool();
		// }
	}

	public <FactorVariablesT extends FactorVariables> Set<FactorVariablesT> extractNewFactorVariables(
			Set<FactorVariablesT> generatedFactors) {

		Set<FactorVariables> variablesFromMap = this.factorVariables2Factor.keySet();
		Set<FactorVariablesT> newFactors = new HashSet<>(generatedFactors);
		newFactors.removeAll(variablesFromMap);

		return newFactors;
	}

	public List<Factor<? extends FactorVariables>> getFactors(Collection<? extends FactorVariables> factorVariablesList)
			throws MissingFactorException {
		// TODO consider not returning a list with plain factors (with
		// repeating elements) but to return a list of tuples (count, factor).
		// This avoids processing of several identical factors.
		List<Factor<? extends FactorVariables>> factors = new ArrayList<>();
		for (FactorVariables factorVariables : factorVariablesList) {
			if (factorVariables2Factor.containsKey(factorVariables)) {
				Factor<? extends FactorVariables> factor = factorVariables2Factor.get(factorVariables);
				factors.add(factor);
			} else {
				log.error("Could not retrieve factor for requested factor variables: %s", factorVariables);
				log.error("Make sure \"sequentialScoring\" is set to FALSE in the model when training.");
				throw new MissingFactorException(
						String.format("Could not retrieve factor for requested factor variables: %s", factorVariables));
			}
		}
		return factors;
	}

	public <FactorVariablesT extends FactorVariables> void addFactors(Set<Factor<FactorVariablesT>> newFactors) {
		for (Factor<FactorVariablesT> factor : newFactors) {
			this.factorVariables2Factor.put(factor.getFactorVariables(), factor);
		}
	}

	public Collection<Factor<? extends FactorVariables>> getAllFactors() {
		return factorVariables2Factor.values();
	}

	public void clear() {
		factorVariables2Factor.clear();
		factorVariables2Factor = new ConcurrentHashMap<>();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(
				"FactorPool \"" + name + "\" [#Factors: " + factorVariables2Factor.size() + "]");
		for (Entry<FactorVariables, Factor<?>> e : factorVariables2Factor.entrySet()) {
			builder.append(e.getKey() + " -> " + e.getValue().getID());
			builder.append("\n");
		}
		return builder.toString();
	}

}
