package Templates.variablesets;

import java.util.HashSet;
import java.util.Set;

import Templates.Template;
import utility.EntityID;

public class SingleEntityVariableSet extends VariableSet {

	public EntityID entityID;

	public SingleEntityVariableSet(Template template, EntityID entityID) {
		super(template);
		this.entityID = entityID;
	}

	@Override
	public Set<EntityID> getEntityIDs() {
		Set<EntityID> entities = new HashSet<>();
		entities.add(entityID);
		return entities;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((entityID == null) ? 0 : entityID.hashCode());
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
		SingleEntityVariableSet other = (SingleEntityVariableSet) obj;
		if (entityID == null) {
			if (other.entityID != null)
				return false;
		} else if (!entityID.equals(other.entityID))
			return false;
		return true;
	}

}
