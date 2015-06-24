package Learning;

import java.util.HashSet;
import java.util.Set;

import Factors.Factor;
import Templates.CheatingTemplate;
import Templates.Template;
import Variables.State;

public class Model {

	Set<Template> templates;

	public Model() {
		// TODO adds a CheatingTemplate for testing purposes
		this.templates = new HashSet<Template>();
		templates.add(new CheatingTemplate());
	}

	public void loadModelfromFile(String file) {
		// TODO load model from file
	}

	public void saveModelToFile(String file) {
		// TODO save model to file
	}

	public Set<Template> getTemplates() {
		return templates;
	}

	public void update(State state, double alpha) {
		for (Factor f : state.getFactors()) {
			f.getTemplate().update(f, alpha);
		}
	}

	@Override
	public String toString() {
		return "Model [templates=" + templates + "]";
	}

}
