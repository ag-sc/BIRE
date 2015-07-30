package Learning;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import Factors.Factor;
import Logging.Log;
import Templates.Template;
import Variables.State;

public class Model implements Serializable {

	{
		Log.off();
	}
	private Collection<Template> templates;

	public Model(Collection<Template> templates) {
		this.templates = templates;
	}

	public Model(Template... templates) {
		this.templates = Arrays.asList(templates);
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
	public void loadModelfromFile(String file) throws FileNotFoundException,
			IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
		templates = (Collection<Template>) in.readObject();
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
	public void saveModelToFile(String file) throws FileNotFoundException,
			IOException {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
				file));
		out.writeObject(templates);
		out.close();
	}

	public Collection<Template> getTemplates() {
		return templates;
	}

	public void update(State state, double alpha) {
		// Log.d("Update state: %s", state);
		for (Template template : templates) {
			template.update(state, alpha);
		}
	}

	/**
	 * This function removes all references to previously created states. This
	 * includes that every template clears it's mapping between states and
	 * factors, since they are no more needed after a complete sampling step
	 * 
	 * @param unneededStates
	 */
	public void clean() {
		for (Template template : templates) {
			template.clean();
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (Template template : templates) {
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
		for (Template template : templates) {
			builder.append(template.getClass().getSimpleName());
			builder.append(" (#Features: ");
			builder.append(template.getWeightVector().getFeatureNames().size());
			builder.append(")");
			builder.append("\n");
			for (String weight : template.getWeightVector().getFeatureNames()) {
				builder.append("\t");
				builder.append(weight);
				builder.append(" : ");
				builder.append(template.getWeightVector().getValueOfFeature(
						weight));
				builder.append("\n");
			}
		}
		return builder.toString();
	}
}
