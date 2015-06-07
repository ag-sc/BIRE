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

	private void convertTextBoundAnnotation(EntityManager manager,
			AnnotationConfig config, BratTextBoundAnnotation t) {
		EntityAnnotation entity = new EntityAnnotation(manager, t.getID());
		EntityType entityType = config.getEntityType(t.getRole());
		entity.init(entityType, t.getStart(), t.getEnd(), t.getText());
		// entities.put(t.getID(), entity);
		manager.addEntityAnnotation(entity);
	}

	private void convertEventAnnotation(EntityManager manager,
			AnnotationConfig config, BratEventAnnotation e) {
		Map<String, String> arguments = new HashMap<String, String>();
		for (Entry<String, BratAnnotation> entry : e.getArguments().entrySet()) {
			BratAnnotation ann = entry.getValue();
			// Entities in the BIRE annotation implementation only keep weak
			// references (IDs) to other entities.
			arguments.put(entry.getKey(), ann.getID());
		}
		EntityAnnotation entity = new EntityAnnotation(manager, e.getID());
		EntityType entityType = config.getEntityType(e.getRole());
		entity.init(entityType, arguments, e.getTrigger().getStart(), e
				.getTrigger().getEnd(), e.getTrigger().getText());
		manager.addEntityAnnotation(entity);
	}

	private void convertRelationAnnotation(EntityManager manager,
			AnnotationConfig config, BratRelationAnnotation t) {
		Map<String, String> arguments = new HashMap<String, String>();
		for (Entry<String, BratAnnotation> entry : t.getArguments().entrySet()) {
			BratAnnotation ann = entry.getValue();
			arguments.put(entry.getKey(), ann.getID());
		}
		EntityAnnotation entity = new EntityAnnotation(manager, t.getID());
		EntityType entityType = config.getEntityType(t.getRole());
		/*
		 * TODO relations are not motivated by tokens in the text and thus have
		 * no start, end and text attribute. Here, these values are filled with
		 * placeholder values
		 */
		entity.init(entityType, arguments, -1, -1, "");
		manager.addEntityAnnotation(entity);
	}

	private void convertAttributeAnnotation(EntityManager manager,
			AnnotationConfig config, BratAttributeAnnotation t) {
		// TODO attributes are currently neglected
	}
}
