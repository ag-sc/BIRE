package Corpus.parser.brat;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import Corpus.AnnotationConfig;
import Corpus.parser.brat.annotations.BratAnnotation;
import Corpus.parser.brat.annotations.BratAttributeAnnotation;
import Corpus.parser.brat.annotations.BratEventAnnotation;
import Corpus.parser.brat.annotations.BratRelationAnnotation;
import Corpus.parser.brat.annotations.BratTextBoundAnnotation;
import Variables.EntityManager;
import Variables.EntityAnnotation;
import Variables.EntityType;

public class Brat2BIREConverter {

	/**
	 * Converts the Brat Annotations, which are stored in the
	 * {@link BratAnnotationManager}, to the BIRE format.
	 * 
	 * @param bratDoc
	 * @param config
	 */
	public EntityManager brat2BireAnnotations(BratAnnotatedDocument bratDoc,
			AnnotationConfig config) {
		EntityManager manager = new EntityManager();
		Map<String, BratAnnotation> annotations = bratDoc.getAllAnnotations();
		for (BratAnnotation ann : annotations.values()) {
			if (ann instanceof BratTextBoundAnnotation) {
				convertTextBoundAnnotation(manager, config,
						(BratTextBoundAnnotation) ann);
			} else if (ann instanceof BratEventAnnotation) {
				convertEventAnnotation(manager, config,
						(BratEventAnnotation) ann);
			} else if (ann instanceof BratRelationAnnotation) {
				convertRelationAnnotation(manager, config,
						(BratRelationAnnotation) ann);
			} else if (ann instanceof BratAttributeAnnotation) {
				convertAttributeAnnotation(manager, config,
						(BratAttributeAnnotation) ann);
			}
		}
		return manager;
	}

	/**
	 * Converts the Brat-TextBasedAnnotation t into an EntityAnnotation and adds
	 * it to a global Map.
	 * 
	 * @param manager
	 * 
	 * @param config
	 * 
	 * @param t
	 */
	private void convertTextBoundAnnotation(EntityManager manager,
			AnnotationConfig config, BratTextBoundAnnotation t) {
		EntityAnnotation entity = getOrCreateEntityByID(t.getID());
		EntityType entityType = config.getEntityType(t.getRole());
		entity.init(entityType, t.getStart(), t.getEnd(), t.getText());
		// entities.put(t.getID(), entity);
	}

	/**
	 * Converts the Brat-EventAnnotation into a several RelationAnnotations. By
	 * combining the trigger of the event with each single argument, the n-ary
	 * event is split into n binary relations. If an argument is an event on its
	 * own, its trigger is used as part of the relation.
	 * 
	 * @param manager
	 * 
	 * @param config
	 * 
	 * @param e
	 */
	private void convertEventAnnotation(EntityManager manager,
			AnnotationConfig config, BratEventAnnotation e) {
		Map<String, EntityAnnotation> arguments = new HashMap<String, EntityAnnotation>();
		for (Entry<String, BratAnnotation> entry : e.getArguments().entrySet()) {
			BratAnnotation ann = entry.getValue();
			EntityAnnotation arg = resolveReference(ann);
			arguments.put(entry.getKey(), arg);
		}
		EntityAnnotation entity = getOrCreateEntityByID(e.getID());
		EntityType entityType = config.getEntityType(e.getRole());
		entity.init(entityType, arguments, e.getTrigger().getStart(), e
				.getTrigger().getEnd(), e.getTrigger().getText());
	}

	private void convertRelationAnnotation(EntityManager manager,
			AnnotationConfig config, BratRelationAnnotation t) {
		Map<String, EntityAnnotation> arguments = new HashMap<String, EntityAnnotation>();
		for (Entry<String, BratAnnotation> entry : t.getArguments().entrySet()) {
			BratAnnotation ann = entry.getValue();
			EntityAnnotation arg = resolveReference(ann);
			arguments.put(entry.getKey(), arg);
		}
		EntityAnnotation entity = getOrCreateEntityByID(t.getID());
		EntityType entityType = config.getEntityType(t.getRole());
		/*
		 * TODO relations are not motivated by tokens in the text and thus have
		 * no start, end and text attribute. Here, these values are filled with
		 * placeholder values
		 */
		entity.init(entityType, -1, -1, "");
	}

	private void convertAttributeAnnotation(EntityManager manager,
			AnnotationConfig config, BratAttributeAnnotation t) {
		// TODO attributes are currently neglected
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
