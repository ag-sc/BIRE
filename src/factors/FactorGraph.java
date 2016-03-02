package factors;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import exceptions.MissingFactorException;

public class FactorGraph implements Serializable {

	/**
	 * The factor pool stores computed factors w.r.t their patterns. This object
	 * is shared across several factor graphs so that individual states do not
	 * need to recompute existing, previously computed factors.
	 */
	private FactorPool factorPool;
	// private Map<FactorPattern, Factor<? extends FactorPattern>>
	// factorPattern2Factor;
	private Set<FactorPattern> factorPatterns;

	public FactorGraph() {
		this.factorPool = new FactorPool();
		// this.factorPattern2Factor = new HashMap<>();
		this.factorPatterns = ConcurrentHashMap.newKeySet();
	}

	public FactorGraph(FactorGraph factorGraph) {
		this.factorPool = factorGraph.factorPool;
		// this.factorPattern2Factor = new
		// HashMap<>(factorGraph.factorPattern2Factor);
		// this.factorPatterns = new HashSet<>(factorGraph.factorPatterns);
		this.factorPatterns = ConcurrentHashMap.newKeySet();
	}

	//
	// /**
	// * Removes obsolete factors from the graph. Obsolete Factors are the ones
	// * that are not among the provided factors.
	// *
	// * @param generatedFactors
	// * @return
	// */
	// public <FactorPatternT extends FactorPattern> void
	// removeObsoleteFactors(Set<FactorPatternT> generatedFactors) {
	// Set<FactorPattern> patternsFromMap = (Set<FactorPattern>)
	// this.factorPattern2Factor.keySet();
	//
	// Set<FactorPattern> obsoleteFactors = new
	// HashSet<>(Sets.difference(patternsFromMap, generatedFactors));
	//
	// /*
	// * Drop all obsolete factors.
	// */
	// for (FactorPattern obsoleteFactorPattern : obsoleteFactors) {
	// this.factorPattern2Factor.remove(obsoleteFactorPattern);
	// }
	// }

	// public void setFactorPatterns(Set<FactorPattern> generatedFactorPatterns)
	// {
	// this.factorPatterns = generatedFactorPatterns;
	// }

	public void addFactorPatterns(Set<? extends FactorPattern> generatedFactorPatterns) {
		this.factorPatterns.addAll(generatedFactorPatterns);
	}

	public void clear() {
		this.factorPatterns.clear();
	}

	// public Set<? extends FactorPattern> getFactorPatterns() {
	// return factorPatterns;
	// }
	//
	// /**
	// * Returns the subset of patterns from the provided set of patterns for
	// * which there is no factor stored in the factor pool of this factor
	// graph.
	// *
	// * @param generatedFactors
	// * @return
	// */
	// public <FactorPatternT extends FactorPattern> Set<FactorPatternT>
	// extractNewFactorPatterns(
	// Set<FactorPatternT> generatedFactors) {
	// return factorPool.extractNewFactorPatterns(generatedFactors);
	// }
	// /**
	// * Removes obsolete factors and returns the novel factor patterns within
	// the
	// * provided set of patterns.
	// *
	// * @param generatedFactors
	// * @return
	// */
	// public <FactorPatternT extends FactorPattern> Set<FactorPatternT>
	// extractNewFactorPatterns(
	// Set<FactorPatternT> generatedFactors) {
	//
	// Set<FactorPattern> patternsFromMap = (Set<FactorPattern>)
	// this.factorPool.keySet();
	//
	// Set<FactorPatternT> newFactors = Sets.difference(generatedFactors,
	// patternsFromMap);
	//
	// return newFactors;
	// }

	// /**
	// * Inserts a compute factor into the graph. The factor is store in the
	// * factor pool object of this graph so that it is shared across states and
	// * can be used by each of them.
	// *
	// * @param newFactors
	// */
	// public <FactorPatternT extends FactorPattern> void
	// addFactors(Set<Factor<FactorPatternT>> newFactors) {
	// factorPool.addFactors(newFactors);
	// // for (Factor<? extends FactorPattern> factor : newFactors) {
	// // this.factorPatterns.add(factor.getFactorPattern());
	// // }
	// }
	// public <FactorPatternT extends FactorPattern> void
	// addFactors(Set<Factor<FactorPatternT>> newFactors) {
	// factorPool.addFactors(newFactors);
	// for (Factor<FactorPatternT> factor : newFactors) {
	// this.factorPattern2Factor.put(factor.getFactorPattern(), factor);
	// }
	// }

	public Set<Factor<? extends FactorPattern>> getFactors() throws MissingFactorException {
		return factorPool.getFactors(factorPatterns);
	}

	public FactorPool getFactorPool() {
		return factorPool;
	}

}
