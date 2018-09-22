package factors;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import learning.Vector;
import templates.AbstractTemplate;
import utility.FactorID;

/**
 * A factor is an object that connects a feature vector to the variables that
 * are involved in computing this feature vector. Since the generation of a
 * factor and the actual computation of its features is separated into to steps,
 * you need to store/reference the variables you need for the computation of the
 * features inside the factorVariables object.
 * 
 * @author sjebbara
 *
 */
public class Factor<FactorScopeT extends FactorScope> implements Serializable {

	private static final AtomicInteger factorIDIndex = new AtomicInteger();
	private final FactorID factorID;
	private final FactorScopeT factorScope;
	private final Vector features = new Vector();

	/**
	 * A factor is an object that connects a feature vector to the variables
	 * that are involved in computing this feature vector. Since the generation
	 * of a factor and the actual computation of its features is separated into
	 * to steps, you need to store/reference the variables you need for the
	 * computation of the features inside the factorVariables object.
	 * 
	 * @author sjebbara
	 *
	 */
	public Factor(FactorScopeT factorScope) {
		this.factorID = generateFactorID();
		this.factorScope = factorScope;
	}

	public FactorID getID() {
		return factorID;
	}

	public Vector getFeatureVector() {
		return features;
	}

	private FactorID generateFactorID() {
		int currentID = factorIDIndex.getAndIncrement();
		String id = "F" + String.valueOf(currentID);
		return new FactorID(id);
	}

	public FactorScopeT getFactorScope() {
		return factorScope;
	}

	public AbstractTemplate<?, ?, ?> getTemplate() {
		return factorScope.getTemplate();
	}

	@Override
	public String toString() {
		return "Factor [factorID=" + factorID + ", factorScope=" + factorScope + ", features=" + features + "]";
	}
}