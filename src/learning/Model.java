package learning;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import logging.Log;
import templates.AbstractTemplate;
import variables.AbstractState;

public class Model<StateT extends AbstractState> implements Serializable {
	private static Logger log = LogManager.getFormatterLogger(Model.class.getName());

	private Collection<AbstractTemplate<StateT>> templates;

	public Model(Collection<AbstractTemplate<StateT>> templates) {
		this.templates = templates;
	}

	/**
	 * Loads a collection of templates (each with its respective weight vector)
	 * from the given file.
	 * 
	 * @param file
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void loadModelfromFile(String file) throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
		templates = (Collection<AbstractTemplate<StateT>>) in.readObject();
		in.close();
	}

	/**
	 * Stores the collection of templates (each with its respective weight
	 * vector) to the specified file.
	 * 
	 * @param file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void saveModelToFile(String file) throws FileNotFoundException, IOException {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
		out.writeObject(templates);
		out.close();
	}

	public Collection<AbstractTemplate<StateT>> getTemplates() {
		return templates;
	}

	public void unroll(StateT state) {
		for (AbstractTemplate<StateT> t : getTemplates()) {
			t.applyTo(state, false);
		}
	}
	// public void update(StateT state, double alpha) {
	// // log.debug("Update state: %s", state);
	// for (Template<StateT> template : templates) {
	// template.update(state, alpha);
	// }
	// }

	// /**
	// * Drops all factors from the memory that are not part of this state (in
	// * order to save memory). This is useful at the end of a sampling step,
	// * where only one state is kept to proceed the training.
	// *
	// * @param state
	// */
	// public void trimToState(StateT state) {
	// for (Template<StateT> template : templates) {
	// template.trimToState(state);
	// }
	// }
	// /**
	// * This function removes all references to previously created states. This
	// * includes that every template clears it's mapping between states and
	// * factors, since they are no more needed after a complete sampling step
	// *
	// * @param unneededStates
	// */
	// public void clean() {
	// for (Template<StateT> template : templates) {
	// template.clean();
	// }
	// }

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (AbstractTemplate<StateT> template : templates) {
			builder.append(template.getClass().getSimpleName());
			builder.append("\n");
			builder.append("\t#Features: ");
			builder.append(template.getWeightVector().getFeatureNames().size());
			builder.append("\n");
		}
		return builder.toString();
	}

	public String toDetailedString() {
		StringBuilder builder = new StringBuilder();
		for (AbstractTemplate<StateT> template : templates) {
			builder.append(template.getClass().getSimpleName());
			builder.append(" (#Features: ");
			builder.append(template.getWeightVector().getFeatureNames().size());
			builder.append(")");
			builder.append("\n");
			for (String weight : template.getWeightVector().getFeatureNames()) {
				builder.append("\t");
				builder.append(weight);
				builder.append(" : ");
				builder.append(template.getWeightVector().getValueOfFeature(weight));
				builder.append("\n");
			}
		}
		return builder.toString();
	}

}
