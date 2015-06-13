package Learning;

import java.util.HashMap;
import java.util.Set;

import Factors.Factor;
import Templates.Template;
import Variables.State;


public class Model {

	Set<Template> templates;
	HashMap<Integer, Double> model;


	public void loadModelfromFile(String file) {
		// do something
	}

	public void saveModelToFile(String file) {
		// do something
	}

	public Set<Template> getTemplates() {
		return templates;
	}

	public void update(State state, double alpha) {
		for (Factor f : state.getFactors()) {
			f.getTemplate().update(f.getFeatureVector(), alpha);
		}
	}
	
}
