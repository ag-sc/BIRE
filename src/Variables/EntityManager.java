package Variables;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class EntityManager {

	/**
	 * Since Entities only hold weak pointer via references to one another,
	 * using a Map is sensible to enable an efficient access to the entities.
	 */
	private Map<String, EntityAnnotation> entities;

	public EntityManager(EntityManager manager) {
		for (EntityAnnotation entityAnnotation : manager.getAllEntities()) {
			entities.put(entityAnnotation.id, new EntityAnnotation(this,
					entityAnnotation));
		}
	}

	public void addEntityAnnotation(EntityAnnotation entity) {
		entities.put(entity.id, entity);
	}

	public void removeEntityAnnotation(String entityID) {
		entities.remove(entityID);
	}

	public Set<String> getAllEntityIDs() {
		return entities.keySet();
	}

	public Collection<EntityAnnotation> getAllEntities() {
		return entities.values();
	}

	public EntityAnnotation getEntity(String id) {
		return entities.get(id);
	}
}
