package Templates.variablesets;

import java.io.Serializable;
import java.util.Set;

import Templates.Template;
import utility.EntityID;

public abstract class VariableSet implements Serializable{
	public final Template template;

	public VariableSet(Template template) {
		this.template = template;
	}

	public abstract Set<EntityID> getEntityIDs();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((template == null) ? 0 : template.hashCode());
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
		VariableSet other = (VariableSet) obj;
		if (template == null) {
			if (other.template != null)
				return false;
		} else if (!template.equals(other.template))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[" + template.getClass().getSimpleName() + ": " + getEntityIDs() + "]";
	}

}
