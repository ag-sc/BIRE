package Templates.variablesets;

import java.util.HashSet;
import java.util.Set;

import Templates.Template;
import utility.EntityID;

public class UnorderedEntititesVariableSet extends VariableSet {
	public Set<EntityID> entities;

	public UnorderedEntititesVariableSet(Template template, Set<EntityID> entities) {
		super(template);
		this.entities = entities;
	}

	@Override
	public Set<EntityID> getEntityIDs() {
		Set<EntityID> entities = new HashSet<>(this.entities);
		return entities;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((entities == null) ? 0 : entities.hashCode());
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
		UnorderedEntititesVariableSet other = (UnorderedEntititesVariableSet) obj;
		if (entities == null) {
			if (other.entities != null)
				return false;
		} else if (!entities.equals(other.entities))
			return false;
		return true;
	}
}
