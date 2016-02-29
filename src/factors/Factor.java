package factors;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import learning.Vector;
import templates.AbstractTemplate;
import utility.FactorID;
import variables.AbstractState;

/**
 * A factor is an object that connects a feature vector to the variables that
 * were involved computing this feature vector. Since the current system does
 * NOT use any caching of factors and features across states, it is not yet
 * necessary to implement the getVariableIDs() methods correctly. However, this
 * will change in the future. </br>
 * Since the generation of a factor and the actual computation of its features
 * is separated into to steps, you need to store/reference the variables you
 * need for the computation of the features inside this factor object (for
 * example as a global variable in this object).
 * 
 * @author sjebbara
 *
 */
public class Factor<FactorPatternT extends FactorPattern> implements Serializable {

	private static final AtomicInteger factorIDIndex = new AtomicInteger();
	private final FactorID factorID;
	private final FactorPatternT factorPattern;
	private final Vector features = new Vector();

	/**
	 * A factor is an object that connects a feature vector to the variables
	 * that were involved computing this feature vector. Since the current
	 * system does NOT use any caching of factors and features across states, it
	 * is not yet necessary to implement the getVariableIDs() methods correctly.
	 * However, this will change in the future. </br>
	 * Since the generation of a factor and the actual computation of its
	 * features is separated into to steps, you need to store/reference the
	 * variables you need for the computation of the features inside this factor
	 * object (for example as a global variable in this object).
	 * 
	 * @author sjebbara
	 *
	 */
	public Factor(FactorPatternT factorPattern) {
		this.factorID = generateFactorID();
		this.factorPattern = factorPattern;
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

	public FactorPatternT getFactorPattern() {
		return factorPattern;
	}

	public AbstractTemplate<?, ?, ?> getTemplate() {
		return factorPattern.getTemplate();
	}
}
