package Templates.variablesets;

import java.util.HashSet;
import java.util.Set;

import Templates.Template;
import utility.EntityID;

public class EntityAndArgumentVariableSet extends VariableSet {

	public EntityID mainEntityID;
	public EntityID argumentEntityID;

	public EntityAndArgumentVariableSet(Template template, EntityID mainEntityID, EntityID argumentEntityID) {
		super(template);
		this.mainEntityID = mainEntityID;
		this.argumentEntityID = argumentEntityID;
	}

	@Override
	public Set<EntityID> getEntityIDs() {
		Set<EntityID> entities = new HashSet<>();
		entities.add(mainEntityID);
		entities.add(argumentEntityID);
		return entities;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((argumentEntityID == null) ? 0 : argumentEntityID.hashCode());
		result = prime * result + ((mainEntityID == null) ? 0 : mainEntityID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		EntityAndArgumentVariableSet other = (EntityAndArgumentVariableSet) obj;
		if (argumentEntityID == null) {
			if (other.argumentEntityID != null)
				return false;
		} else if (!argumentEntityID.equals(other.argumentEntityID))
			return false;
		if (mainEntityID == null) {
			if (other.mainEntityID != null)
				return false;
		} else if (!mainEntityID.equals(other.mainEntityID))
			return false;
		return true;
	}

}
