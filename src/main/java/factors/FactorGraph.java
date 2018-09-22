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
	private Collection<FactorScope> factorScope;

	public FactorGraph() {
		this.factorPool = FactorPool.getInstance();
		this.factorScope = new ConcurrentLinkedQueue<>();
	}


	public void addFactorScopes(List<? extends FactorScope> generatedFactorScopes) {
		this.factorScope.addAll(generatedFactorScopes);
	}

	public void clear() {
		this.factorScope.clear();
		this.factorScope = new ConcurrentLinkedQueue<>();
	}

	public List<Factor<? extends FactorScope>> getFactors() throws MissingFactorException {
		return factorPool.getFactors(factorScope);
	}

	public Collection<FactorScope> getFactorScopes() {
		return factorScope;
	}

	public FactorPool getFactorPool() {
		return factorPool;
	}

}
