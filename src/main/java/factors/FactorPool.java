package factors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import exceptions.MissingFactorException;

public class FactorPool {

	private static Logger log = LogManager.getFormatterLogger();
	final private Map<FactorScope, Factor<? extends FactorScope>> factorVariables2Factor = new HashMap<>();

	private static FactorPool sharedInstance = null;
	public String name = "";

	private FactorPool() {
//		factorVariables2Factor = new HashMap<>();
//		factorVariables2Factor = new ConcurrentHashMap<>();
	}

	public static FactorPool getInstance() {
		if (sharedInstance == null) {
			sharedInstance = new FactorPool();
		}
		return sharedInstance;
	}

//	public  Set<FactorVariablesT> extractNewFactorScopes(
	public <FactorVariablesT extends FactorScope> void filterNewFactorScopes(
			Set<FactorVariablesT> generatedFactors) {

		Set<FactorScope> variablesFromMap = this.factorVariables2Factor.keySet();
//		Set<FactorVariablesT> newFactors = new HashSet<>(generatedFactors);
		generatedFactors.removeAll(variablesFromMap);

//		return generatedFactors;
	}

	public List<Factor<? extends FactorScope>> getFactors(Collection<? extends FactorScope> factorVariablesList)
			throws MissingFactorException {
		// TODO consider not returning a list with plain factors (with
		// repeating elements) but to return a list of tuples (count, factor).
		// This avoids processing of several identical factors.
		List<Factor<? extends FactorScope>> factors = new ArrayList<>();

		for (FactorScope factorVariables : factorVariablesList) {

			Factor<? extends FactorScope> factor;
			if ((factor = factorVariables2Factor.get(factorVariables)) != null) {
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

	public <FactorVariablesT extends FactorScope> void addFactors(Set<Factor<FactorVariablesT>> newFactors) {
		for (Factor<FactorVariablesT> factor : newFactors) {
			this.factorVariables2Factor.put(factor.getFactorScope(), factor);
		}
	}

	public Collection<Factor<? extends FactorScope>> getAllFactors() {
		return factorVariables2Factor.values();
	}

	public Set<FactorScope> getAllFactorScopes() {
		return factorVariables2Factor.keySet();
	}

	public void clear() {
		factorVariables2Factor.clear();
//		factorVariables2Factor = new ConcurrentHashMap<>();
//		factorVariables2Factor = new HashMap<>();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(
				"FactorPool \"" + name + "\" [#Factors: " + factorVariables2Factor.size() + "]");
		for (Entry<FactorScope, Factor<?>> e : factorVariables2Factor.entrySet()) {
			builder.append(e.getKey() + " -> " + e.getValue().getID());
			builder.append("\n");
		}
		return builder.toString();
	}

}
