package factors;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import exceptions.MissingFactorException;

public class FactorGraph implements Serializable {

	/**
	 * The factor pool stores computed factors w.r.t their variables. This object
	 * is shared across several factor graphs so that individual states do not
	 * need to recompute existing, previously computed factors.
	 */
	private FactorPool factorPool;
	private Collection<FactorVariables> factorVariables;

	public FactorGraph() {
		this.factorPool = FactorPool.getInstance();
		this.factorVariables = new ConcurrentLinkedQueue<>();
	}

//	public FactorGraph(FactorGraph factorGraph) {
//		// TODO using the same factor pool instead of using the getInstance()
//		// method might lead to unexpected and confusing behavior.
//		this.factorPool = FactorPool.getInstance();
//		this.factorPatterns = new ConcurrentLinkedQueue<>();
//	}

	public void addFactorVariables(List<? extends FactorVariables> generatedFactorVariables) {
		this.factorVariables.addAll(generatedFactorVariables);
	}

	public void clear() {
		this.factorVariables.clear();
		this.factorVariables = new ConcurrentLinkedQueue<>();
	}

	public List<Factor<? extends FactorVariables>> getFactors() throws MissingFactorException {
		return factorPool.getFactors(factorVariables);
	}

	public Collection<FactorVariables> getFactorVariables() {
		return factorVariables;
	}

	public FactorPool getFactorPool() {
		return factorPool;
	}
	//
	// public void setFactorPool(FactorPool factorPool) {
	// this.factorPool = factorPool;
	// }

}
