package Corpus.parser.brat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import Corpus.parser.brat.annotations.BratAnnotation;
import Corpus.parser.brat.annotations.BratAttributeAnnotation;
import Corpus.parser.brat.annotations.BratEventAnnotation;
import Corpus.parser.brat.annotations.BratRelationAnnotation;
import Corpus.parser.brat.annotations.BratTextBoundAnnotation;
import Variables.EntityAnnotation;
import Variables.EntityType;
import Variables.RelationAnnotation;
import Variables.RelationType;

public class Brat2BIREConverter {
	/**
	 * Collects all existing EntityAnnotations by there ID. This Map is needed
	 * to handle references to Entities that are not yet parsed.
	 */
	private Map<String, EntityAnnotation> entities = new HashMap<String, EntityAnnotation>();
	private Set<RelationAnnotation> relations = new HashSet<RelationAnnotation>();

	public Map<String, EntityAnnotation> getEntities() {
		return entities;
	}

	public Set<RelationAnnotation> getRelations() {
		return relations;
	}

	/**
	 * Converts the Brat Annotations, which are stored in the
	 * {@link BratAnnotationManager}, to the BIRE format.
	 * 
	 * @param manager
	 */
	public void brat2BireAnnotations(BratAnnotationManager manager) {
		Map<String, BratAnnotation> annotations = manager.getAnnotations();
		for (BratAnnotation ann : annotations.values()) {
			if (ann instanceof BratTextBoundAnnotation) {
				convertTextBoundAnnotation((BratTextBoundAnnotation) ann);
			} else if (ann instanceof BratEventAnnotation) {
				convertEventAnnotation((BratEventAnnotation) ann);
			} else if (ann instanceof BratRelationAnnotation) {
				convertRelationAnnotation((BratRelationAnnotation) ann);
			} else if (ann instanceof BratAttributeAnnotation) {
				convertAttributeAnnotation((BratAttributeAnnotation) ann);
			}
		}
	}

	/**
	 * Converts the Brat-TextBasedAnnotation t into an EntityAnnotation and adds
	 * it to a global Map.
	 * 
	 * @param t
	 */
	private void convertTextBoundAnnotation(BratTextBoundAnnotation t) {
		EntityAnnotation entity = getOrCreateEntityByID(t.getID());
		entity.init(new EntityType(t.getRole()), t.getStart(), t.getEnd(),
				t.getText());
		entities.put(entity.getID(), entity);
	}

	/**
	 * Converts the Brat-EventAnnotation into a several RelationAnnotations. By
	 * combining the trigger of the event with each single argument, the n-ary
	 * event is split into n binary relations. If an argument is an event on its
	 * own, its trigger is used as part of the relation.
	 * 
	 * @param e
	 */
	private void convertEventAnnotation(BratEventAnnotation e) {
		EntityAnnotation arg1 = getOrCreateEntityByID(e.getTrigger().getID());
		for (Entry<String, BratAnnotation> entry : e.getArguments().entrySet()) {
			String type = entry.getKey();
			BratAnnotation ann = entry.getValue();
			EntityAnnotation arg2 = resolveReference(ann);
			RelationAnnotation relation = new RelationAnnotation(
					new RelationType(type), arg1, arg2);
			relations.add(relation);
		}
	}

	private void convertRelationAnnotation(BratRelationAnnotation t) {
		if (t.getArguments().size() == 2) {
			String type = t.getRole();
			BratAnnotation ann1 = t.getArguments().get("Arg1");
			BratAnnotation ann2 = t.getArguments().get("Arg2");

			EntityAnnotation arg1 = resolveReference(ann1);
			EntityAnnotation arg2 = resolveReference(ann2);
			RelationAnnotation relation = new RelationAnnotation(
					new RelationType(type), arg1, arg2);
			relations.add(relation);

		} else {
			// TODO how to encode Brat-Relations with multiple arguments in
			// BIRE-Relations which are binary relations?
		}
	}

	private void convertAttributeAnnotation(BratAttributeAnnotation t) {
		// TODO
	}

	/**
	 * Retrieves an EntityAnnotation Object (or creates it if necessary) using
	 * the given BratAnnotation. If the BratAnnotation is a
	 * BratTextBoundAnnotation, the created EntityAnnotation resembles a direct
	 * conversion. If it is an BratEventAnnotation, the created Entity refers to
	 * the trigger of the event (which is in turn a BratTextBoundAnnotation).
	 * 
	 * @param ann
	 * @return
	 */
	private EntityAnnotation resolveReference(BratAnnotation ann) {
		EntityAnnotation entity = null;
		if (ann instanceof BratTextBoundAnnotation) {
			/*
			 * Create an entity reference to a BratTextBasedAnnotation.
			 */
			entity = getOrCreateEntityByID(ann.getID());
		} else if (ann instanceof BratEventAnnotation) {
			/*
			 * Create an entity reference to a the Trigger of an
			 * BratEventAnnotation.
			 */
			BratEventAnnotation eventArg = (BratEventAnnotation) ann;
			entity = getOrCreateEntityByID(eventArg.getTrigger().getID());
		}
		return entity;
	}

	/**
	 * This methods is used to handle/resolve references to EntityAnnotations.
	 * If no Entity with the given ID is processed yet, a corresponding
	 * EntityAnnotations is created and returned. As soon as it is processed,
	 * this Entity should be filled with data via the init(...) method
	 * 
	 * @param id
	 * @return
	 */
	private EntityAnnotation getOrCreateEntityByID(String id) {
		EntityAnnotation e = entities.get(id);
		if (e == null) {
			e = new EntityAnnotation(id);
			entities.put(id, e);
		}
		return e;
	}
}
