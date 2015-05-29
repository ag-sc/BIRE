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
	private static final String TRIGGER = "Trigger";
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
		/*-
		 * TODO
		 * 
		 * E3 positive_regulation:T3 Theme:T1 Cause:T2
		 * E5 positive_regulation:T5 Theme:T2 Cause:E3 Site:T3
		 * 
		 * What is the Type, what are the arguments of the resulting relation?
		 * What about other events that reference this event. Will they
		 * reference the resulting relation or its trigger entity?
		 * 
		 * The events above are currently converted to:
		 * 
		 * Relation
		 *   Type: positive_regulation
		 *   Arguments:	(Trigger, 	T3)
		 *   			(Theme,		T1)
		 *   			(Cause,		T2)
		 *   
		 * Relation
		 *   Type: positive_regulation
		 *   Arguments:	(Trigger, 	T5)
		 *   			(Theme,		T2)
		 *   			(Cause,		T3)		<--- This is different to the Brat annotation, since the reference to the Event E3 is changed to the trigger of event E3
		 *   			(Site, 		T3)
		 */
		Map<String, EntityAnnotation> arguments = new HashMap<String, EntityAnnotation>();
		// Treat the trigger entity as an argument to the relation.
		arguments.put(TRIGGER, resolveReference(e.getTrigger()));
		for (Entry<String, BratAnnotation> entry : e.getArguments().entrySet()) {
			BratAnnotation ann = entry.getValue();
			EntityAnnotation arg = resolveReference(ann);
			arguments.put(entry.getKey(), arg);
		}
		RelationAnnotation relation = new RelationAnnotation(new RelationType(
				e.getRole()), arguments);
		relations.add(relation);
	}

	private void convertRelationAnnotation(BratRelationAnnotation t) {
		Map<String, EntityAnnotation> arguments = new HashMap<String, EntityAnnotation>();
		for (Entry<String, BratAnnotation> entry : t.getArguments().entrySet()) {
			BratAnnotation ann = entry.getValue();
			EntityAnnotation arg = resolveReference(ann);
			arguments.put(entry.getKey(), arg);
		}
		RelationAnnotation relation = new RelationAnnotation(new RelationType(
				t.getRole()), arguments);
		relations.add(relation);
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
