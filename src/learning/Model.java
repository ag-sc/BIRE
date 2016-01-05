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

import templates.AbstractTemplate;
import variables.AbstractState;

public class Model<StateT extends AbstractState> implements Serializable {
	private static Logger log = LogManager.getFormatterLogger(Model.class.getName());

	private Collection<AbstractTemplate<StateT>> templates;

	/**
	 * The model contains a collection of templates that can generate factors
	 * (and features) for a given state. The model's weights are stored in the
	 * templates and shared across all their respective factors.
	 * 
	 * @param templates
	 */
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
