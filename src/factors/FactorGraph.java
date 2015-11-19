package factors;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import templates.AbstractTemplate;

public class FactorGraph implements Serializable {

	private Multimap<AbstractTemplate<?>, AbstractFactor> factors;

	public FactorGraph() {
		init();
	}

	private void init() {
		factors = HashMultimap.create();
	}

	public FactorGraph(FactorGraph factorGraph) {
		this.factors = HashMultimap.create(factorGraph.factors);
	}

	public void updateFactors(AbstractTemplate<?> template, Collection<AbstractFactor> factors) {
		this.factors.removeAll(template);
		this.factors.putAll(template, factors);
	}

	public Collection<AbstractFactor> getFactors() {
		return new HashSet<>(factors.values());
	}

	public void reset() {
		init();
	}
}
