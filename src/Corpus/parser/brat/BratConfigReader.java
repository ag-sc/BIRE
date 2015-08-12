package Corpus.parser.brat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import Corpus.AnnotationConfig;
import Logging.Log;
import Variables.Argument;
import Variables.EntityType;

public class BratConfigReader {

	private static final String COMMENT_INDICATOR = "#";
	private static final String SEPARATION_INDICATOR = "-";
	private static final String ENTITY_SECTION = "[entities]";
	private static final String EVENT_SECTION = "[events]";
	private static final String RELATION_SECTION = "[relations]";
	private static final String ATTRIBUTE_SECTION = "[attributes]";

	private Set<Argument> eventReferencingArguments;
	private Set<EntityType> events;

	/**
	 * Parses the file for the given file path and returns the respective
	 * AnnotationConfig object. Due to the use of internal states, this method
	 * is marked as synchronized and cannot run more than once at a time.
	 * 
	 * @param configFile
	 * @return
	 */
	public synchronized AnnotationConfig readConfig(File configFile) {
		Log.off();
		try {
			eventReferencingArguments = new HashSet<Argument>();
			events = new HashSet<EntityType>();
			BufferedReader reader = new BufferedReader(new FileReader(configFile));
			AnnotationConfig config = new AnnotationConfig();
			ParseState state = null;
			String line;
			int lineNumber = 1;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith(COMMENT_INDICATOR)) {
					Log.i(lineNumber + ": Skip comment " + ": \""
							+ line + "\"");
				} else if (line.startsWith(SEPARATION_INDICATOR)) {
					Log.i(lineNumber + ": Separation line");
				} else if (line.trim().length() == 0) {
					Log.i(lineNumber + ": Empty line ignored");
				} else {
					Log.i(lineNumber + ": parse...");
					state = parseLine(config, line, state);
				}
				lineNumber++;
			}
			reader.close();
			resolveEventReferences();
			return config;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Arguments of events that reference other events are modified in this
	 * method. Since Brat-Events and Brat-Entities are treated as Entities in
	 * BIRE, we need to fix the arguments general reference to events, by adding
	 * all possible events to the allowed type collection of the respective
	 * argument.
	 */
	private void resolveEventReferences() {
		for (Argument argument : eventReferencingArguments) {
			for (EntityType event : events) {
				argument.getTypes().add(event.getName());
			}
		}
	}

	private ParseState parseLine(AnnotationConfig config, String line,
			ParseState state) {

		if (line.equals(ENTITY_SECTION)) {
			Log.i("Begin of entity section");
			state = ParseState.ENTITIES;
		} else if (line.equals(EVENT_SECTION)) {
			Log.i("Begin of event section");
			state = ParseState.EVENTS;
		} else if (line.equals(RELATION_SECTION)) {
			Log.i("Begin of relation section");
			state = ParseState.RELATIONS;
		} else if (line.equals(ATTRIBUTE_SECTION)) {
			Log.i("Begin of attribute section");
			state = ParseState.ATTRIBUTES;
		} else {
			if (state != null) {
				switch (state) {
				case ENTITIES:
					parseEntity(config, line);
					break;
				case EVENTS:
					parseEvent(config, line);
					break;
				case RELATIONS:
					parseRelation(config, line);
					break;
				case ATTRIBUTES:
					parseAttribute(config, line);
					break;
				default:
					break;
				}
			}
		}
		return state;
	}

	private void parseEntity(AnnotationConfig config, String line) {
		StringTokenizer tokenizer = new StringTokenizer(line);
		if (tokenizer.hasMoreTokens()) {
			String entityName = tokenizer.nextToken();
			EntityType type = new EntityType(entityName);
			config.addEntityType(type);
		}
		if (tokenizer.hasMoreTokens()) {
			Log.i("Warning: line contains no supported entity: "
					+ line);
		}
	}

	/*
	 * TODO Modifiers like *, +, ? are currently not supported rigorously.
	 * Specifically, a certain role can only occur once or not at all in the
	 * collection of arguments of an entity.
	 */
	private void parseEvent(AnnotationConfig config, String line) {
		/*-
		 *  Positive_regulation Theme:<EVENT>|Protein, Cause?:<EVENT>|Protein, Site?:Entity, CSite?:Entity
		 */
		String[] typeArgSplit = line.split("\t", 2);
		Log.i("typeArgSplit: " + arrayToString(typeArgSplit));
		String typeName = typeArgSplit[0];
		String args = typeArgSplit[1];
		String argsSplit[] = args.split("\\s");
		Log.i("argsSplit: " + arrayToString(argsSplit));

		Map<String, Argument> coreArguments = new HashMap<String, Argument>();
		Map<String, Argument> optionalArguments = new HashMap<String, Argument>();

		for (int i = 0; i < argsSplit.length; i++) {
			String argString = argsSplit[i].replace(",", "");
			boolean isCore = isCoreArgument(argString);
			Argument arg = extractEventArgument(argString);
			if (isCore)
				coreArguments.put(arg.getRole(), arg);
			else
				optionalArguments.put(arg.getRole(), arg);
		}
		EntityType type = new EntityType(typeName, coreArguments,
				optionalArguments);
		config.addEntityType(type);
		events.add(type);
	}

	private boolean isCoreArgument(String arg) {
		// the ? and * marker do not enforce the occurrence of the given role
		return !arg.contains("?") && !arg.contains("*");
	}

	private Argument extractEventArgument(String arg) {
		String[] roleArgSplit = arg.split("\\:");
		Log.i("roleArgSplit: " + arrayToString(roleArgSplit));
		String role = roleArgSplit[0];
		if (role.endsWith("?") || role.endsWith("*") || role.endsWith("+")) {
			role = role.substring(0, role.length() - 1);
		}
		String[] args = roleArgSplit[1].split("\\|");
		Log.i("args: " + arrayToString(args));
		List<String> types = new ArrayList<String>();
		boolean isEventReferencingPossible = false;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("<EVENT>")) {
				isEventReferencingPossible = true;
			} else {
				types.add(args[i]);
			}
		}
		Argument argument = new Argument(role, types);

		// remember if other events can be used as this arguments
		if (isEventReferencingPossible)
			eventReferencingArguments.add(argument);
		return argument;
	}

	private String arrayToString(String[] a) {
		String aString = "[";
		for (int i = 0; i < a.length; i++) {
			aString += a[i] + " ----- ";
		}
		aString += "]";
		return aString;
	}

	private void parseRelation(AnnotationConfig config, String line) {
		// TODO parse configuration of relations
	}

	private void parseAttribute(AnnotationConfig config, String line) {
		// TODO parse configuration of attributes
	}

	enum ParseState {
		ENTITIES, RELATIONS, EVENTS, ATTRIBUTES;
	}
}
