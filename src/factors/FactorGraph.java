package factors;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import exceptions.MissingFactorException;

public class FactorGraph implements Serializable {

	/**
	 * The factor pool stores computed factors w.r.t their patterns. This object
	 * is shared across several factor graphs so that individual states do not
	 * need to recompute existing, previously computed factors.
	 */
	private FactorPool factorPool;
	private Collection<FactorPattern> factorPatterns;

	public FactorGraph() {
		this.factorPool = new FactorPool();
		// TODO This should be thread safe. Is it in our case?
		this.factorPatterns = new ConcurrentLinkedQueue<>();
	}

	public FactorGraph(FactorGraph factorGraph) {
		this.factorPool = factorGraph.factorPool;
		this.factorPatterns = new ConcurrentLinkedQueue<>();
	}

	public void addFactorPatterns(List<? extends FactorPattern> generatedFactorPatterns) {
		this.factorPatterns.addAll(generatedFactorPatterns);
	}

	public void clear() {
		this.factorPatterns.clear();
		this.factorPatterns = new ConcurrentLinkedQueue<>();
	}

	public List<Factor<? extends FactorPattern>> getFactors() throws MissingFactorException {
		return factorPool.getFactors(factorPatterns);
	}

	public Collection<FactorPattern> getFactorPatterns() {
		return factorPatterns;
	}

	public FactorPool getFactorPool() {
		return factorPool;
	}

}
