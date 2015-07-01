package Corpus;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import Variables.EntityType;

public class AnnotationConfig implements Serializable {

	private Map<String, EntityType> entityTypes = new HashMap<String, EntityType>();

	public Collection<EntityType> getEntitiyTypes() {
		return entityTypes.values();
	}

	public void addEntityType(EntityType entityType) {
		entityTypes.put(entityType.getType(), entityType);
	}

	public EntityType getEntityType(String type) {
		return entityTypes.get(type);
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for (EntityType entityType : entityTypes.values()) {
			b.append(entityType);
			b.append("\n");
		}
		return "AnnotationConfig:\n" + b;
	}

}
