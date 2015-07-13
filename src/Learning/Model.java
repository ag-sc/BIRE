package Learning;

import java.util.Arrays;
import java.util.Collection;

import Factors.Factor;
import Logging.Log;
import Templates.Template;
import Variables.State;

public class Model {

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

	public void loadModelfromFile(String file) {
		// TODO load model from file
	}

	public void saveModelToFile(String file) {
		// TODO save model to file
	}

	public Collection<Template> getTemplates() {
		return templates;
	}

	public void update(State state, double alpha) {
		Log.d("Update state: %s", state);
		for (Template template : templates) {
			template.update(state, alpha);
		}
		// for (Factor f : state.getFactors()) {
		// f.getTemplate().update(f, alpha);
		// }
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (Template template : templates) {
			builder.append(template.getClass().getSimpleName());
			builder.append("\n");
			builder.append("\t#Features: ");
			builder.append(template.getWeightVector().getFeatures().size());
			// for (String weight : template.getWeightVector().getFeatures()) {
			// builder.append("\t");
			// builder.append(weight);
			// builder.append(" : ");
			// builder.append(template.getWeightVector().getValueOfFeature(
			// weight));
			// builder.append("\n");
			// }
			builder.append("\n");
		}
		return builder.toString();
	}

	public String toDetailedString() {
		StringBuilder builder = new StringBuilder();
		for (Template template : templates) {
			builder.append(template.getClass().getSimpleName());
			builder.append(" (#Features: ");
			builder.append(template.getWeightVector().getFeatures().size());
			builder.append(")");
			builder.append("\n");
			for (String weight : template.getWeightVector().getFeatures()) {
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
