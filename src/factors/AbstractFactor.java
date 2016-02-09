package factors;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import learning.Vector;
import templates.AbstractTemplate;
import utility.FactorID;
import utility.VariableID;
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
public abstract class AbstractFactor implements Serializable {

	private static final AtomicInteger factorIDIndex = new AtomicInteger();
	protected AbstractTemplate<? extends AbstractState> template;
	protected FactorID factorID;
	protected Vector features;

	/**
	 * When implementing this method, you should return the IDs of all variables
	 * that are necessary to compute the features of this factor. The returned
	 * set of variable IDs is later used, to associate changed variables with
	 * factors. This allows the framework to cache most factors and only
	 * recompute those whose variables have changed.
	 * 
	 * @return
	 */
	public abstract Set<VariableID> getVariableIDs();

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
	private AbstractFactor() {
		this.factorID = generateFactorID();
	}

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
	public AbstractFactor(AbstractTemplate<? extends AbstractState> template) {
		this();
		this.template = template;
	}

	public FactorID getID() {
		return factorID;
	}

	public Vector getFeatureVector() {
		return features;
	}

	public void setFeatures(Vector features) {
		this.features = features;
	}

	private FactorID generateFactorID() {
		int currentID = factorIDIndex.getAndIncrement();
		String id = "F" + String.valueOf(currentID);
		return new FactorID(id);
	}

	public AbstractTemplate<? extends AbstractState> getTemplate() {
		return template;
	}

	// TODO Check if hashCode and equals work this way
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((template == null) ? 0 : template.hashCode());
		result = prime * result + ((getVariableIDs() == null) ? 0 : getVariableIDs().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractFactor other = (AbstractFactor) obj;
		if (template == null) {
			if (other.template != null)
				return false;
		} else if (!template.equals(other.template))
			return false;
		if (!Objects.equals(this.getVariableIDs(), other.getVariableIDs())) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Factor [template=" + template + ",\n" + "features=" + features + "]";
	}

}
