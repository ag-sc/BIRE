package factors;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Sets;

public class FactorPool {

	private static Logger log = LogManager.getFormatterLogger();
	private Map<FactorPattern, Factor<? extends FactorPattern>> factorPattern2Factor;

	public FactorPool() {
		factorPattern2Factor = new HashMap<>();
	}

	public <FactorPatternT extends FactorPattern> Set<FactorPatternT> extractNewFactorPatterns(
			Set<FactorPatternT> generatedFactors) {

		Set<FactorPattern> patternsFromMap = (Set<FactorPattern>) this.factorPattern2Factor.keySet();

		Set<FactorPatternT> newFactors = Sets.difference(generatedFactors, patternsFromMap);

		return newFactors;
	}

	public Set<Factor<? extends FactorPattern>> getFactors(Set<? extends FactorPattern> factorPatterns) {
		Set<Factor<? extends FactorPattern>> factors = new HashSet<>();

		for (FactorPattern factorPattern : factorPatterns) {
			if (factorPattern2Factor.containsKey(factorPattern)) {
				Factor<? extends FactorPattern> factor = factorPattern2Factor.get(factorPattern);
				factors.add(factor);
			} else {
				log.error("Could not retrieve factor for requested factor pattern: %s", factorPattern);
			}
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

}
