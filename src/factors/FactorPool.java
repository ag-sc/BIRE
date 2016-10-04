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
	private Map<FactorPattern, Factor<? extends FactorPattern>> factorPattern2Factor;

	public FactorPool() {
		factorPattern2Factor = new ConcurrentHashMap<>();
	}

	public <FactorPatternT extends FactorPattern> Set<FactorPatternT> extractNewFactorPatterns(
			Set<FactorPatternT> generatedFactors) {

		Set<FactorPattern> patternsFromMap = this.factorPattern2Factor.keySet();
		Set<FactorPatternT> newFactors = new HashSet<>(generatedFactors);
		newFactors.removeAll(patternsFromMap);

		return newFactors;
	}

	public List<Factor<? extends FactorPattern>> getFactors(List<? extends FactorPattern> factorPatterns)
			throws MissingFactorException {
		// TODO consider not returning a list with plain factors (with
		// repeating elements) but to return a list of tuples (count, factor).
		// This avoids processing of several identical factors.
		List<Factor<? extends FactorPattern>> factors = new ArrayList<>();

		for (FactorPattern factorPattern : factorPatterns) {
			// synchronized (factorPattern2Factor) {
			if (factorPattern2Factor.containsKey(factorPattern)) {
				Factor<? extends FactorPattern> factor = factorPattern2Factor.get(factorPattern);
				factors.add(factor);
			} else {
				log.error("Could not retrieve factor for requested factor pattern: %s", factorPattern);
				log.error("Make sure \"sequentialScoring\" is set to FALSE in the model when training.");
				throw new MissingFactorException(
						String.format("Could not retrieve factor for requested factor pattern: %s", factorPattern));
			}
			// }
		}

		return factors;
	}

	public <FactorPatternT extends FactorPattern> void addFactors(Set<Factor<FactorPatternT>> newFactors) {
		for (Factor<FactorPatternT> factor : newFactors) {
			this.factorPattern2Factor.put(factor.getFactorPattern(), factor);
		}
	}

	public Collection<Factor<? extends FactorPattern>> getAllFactors() {
		return factorPattern2Factor.values();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("FactorPool [#Factors: " + factorPattern2Factor.size() + "]");
		for (Entry<FactorPattern, Factor<?>> e : factorPattern2Factor.entrySet()) {
			builder.append(e.getKey() + " -> " + e.getValue().getID());
			builder.append("\n");
		}
		return builder.toString();
	}

	public void clear() {
		factorPattern2Factor.clear();
		factorPattern2Factor = new ConcurrentHashMap<>();
	}

}
