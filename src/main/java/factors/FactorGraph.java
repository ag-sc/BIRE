package factors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import exceptions.MissingFactorException;

public class FactorGraph implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The factor pool stores computed factors w.r.t their variables. This object is
	 * shared across several factor graphs so that individual states do not need to
	 * recompute existing, previously computed factors.
	 */
	final private static FactorPool factorPool = FactorPool.getInstance();
	private Collection<FactorScope> factorScope = new ArrayList<>();;

	public FactorGraph() {
//		this.factorPool = FactorPool.getInstance();
//		this.factorScope = new ConcurrentLinkedQueue<>();
	}

	public void addFactorScopes(List<? extends FactorScope> generatedFactorScopes) {
		this.factorScope.addAll(generatedFactorScopes);
	}

	public void clear() {
		this.factorScope.clear();
//		this.factorScope = new ConcurrentLinkedQueue<>();
//		this.factorScope = new ArrayList<>();
	}

	private List<Factor<? extends FactorScope>> cache = null;

	public List<Factor<? extends FactorScope>> getFactors() throws MissingFactorException {
//		return factorPool.getFactors(factorScope);
		if (cache == null)
			cache = factorPool.getFactors(factorScope);

		return cache;
	}

	public Collection<FactorScope> getFactorScopes() {
		return factorScope;
	}

//	public FactorPool getFactorPool() {
//		return factorPool;
//	}

}
