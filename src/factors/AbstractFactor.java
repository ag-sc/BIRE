package factors;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import learning.Vector;
import templates.AbstractTemplate;
import utility.FactorID;
import utility.VariableID;
import variables.AbstractState;

public abstract class AbstractFactor implements Serializable {

	protected AbstractTemplate<? extends AbstractState> template;
	protected FactorID factorID;
	protected Vector features;

	public abstract Set<VariableID> getVariableIDs();

	private AbstractFactor() {
		this.factorID = generateFactorID();
	}

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
		String id = String.valueOf(UUID.randomUUID().toString());
		return new FactorID(id);
	}

	public AbstractTemplate<? extends AbstractState> getTemplate() {
		return template;
	}

	@Override
	public String toString() {
		return "Factor [template=" + template + ",\n" + "features=" + features + "]";
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

}
