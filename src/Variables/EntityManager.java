package Variables;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import Corpus.Token;

public class EntityManager {

	/**
	 * Since Entities only hold weak pointer via references to one another,
	 * using a Map is sensible to enable an efficient access to the entities.
	 */
	private Map<String, EntityAnnotation> entities = new HashMap<String, EntityAnnotation>();;
	private Map<Integer, Set<String>> tokenToEntities = new HashMap<Integer, Set<String>>();

	private static final String GENERATED_ID_PREFIX = "G";
	private int idIndex = 0;

	public EntityManager() {
	}

	public EntityManager(EntityManager manager) {
		this();
		this.idIndex = manager.idIndex;
		for (EntityAnnotation entityAnnotation : manager.entities.values()) {
			entities.put(entityAnnotation.id, new EntityAnnotation(this,
					entityAnnotation));
		}
		for (Entry<Integer, Set<String>> e : manager.tokenToEntities.entrySet()) {
			tokenToEntities.put(e.getKey(), new HashSet<String>(e.getValue()));
		}
	}

	public EntityManager(Collection<EntityAnnotation> initialEntities) {
		this();
		for (EntityAnnotation e : initialEntities) {
			addEntityAnnotation(e);
		}
	}

	public void addEntityAnnotation(EntityAnnotation entity) {
		entities.put(entity.id, entity);
		addToTokenToEntityMapping(entity);
	}

	public void removeEntityAnnotation(EntityAnnotation entity) {
		entities.remove(entity.getID());
		removeFromTokenToEntityMapping(entity);
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

	public boolean tokenHasAnnotation(Token token) {
		Set<String> entities = tokenToEntities.get(token.getIndex());
		return entities != null && !entities.isEmpty();
	}

	public Set<String> getAnnotationForToken(Token token) {
		Set<String> entities = tokenToEntities.get(token.getIndex());
		if (entities == null) {
			entities = new HashSet<String>();
			tokenToEntities.put(token.getIndex(), entities);
		}
		return entities;
	}

	public void removeFromTokenToEntityMapping(EntityAnnotation entityAnnotation) {
		for (int i = entityAnnotation.getBeginTokenIndex(); i <= entityAnnotation
				.getEndTokenIndex(); i++) {
			Set<String> entities = tokenToEntities.get(i);
			if (entities == null) {
				entities = new HashSet<String>();
				tokenToEntities.put(i, entities);
			} else {
				entities.remove(entityAnnotation.getID());
			}
		}
	}

	public void addToTokenToEntityMapping(EntityAnnotation entityAnnotation) {
		for (int i = entityAnnotation.getBeginTokenIndex(); i <= entityAnnotation
				.getEndTokenIndex(); i++) {
			Set<String> entities = tokenToEntities.get(i);
			if (entities == null) {
				entities = new HashSet<String>();
				tokenToEntities.put(i, entities);
			}
			entities.add(entityAnnotation.getID());
		}
	}

	public String generateID() {
		String id = GENERATED_ID_PREFIX + idIndex;
		assert !entities.containsKey(id);
		idIndex++;
		return id;
	}

	public Map<Integer, Set<String>> getTokenToEntityMapping() {
		return tokenToEntities;
	}

}
