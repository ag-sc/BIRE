package Learning;

import java.util.List;


public class Scorer {

	Model model;
	
	public double score(List<FeatureVector> features)
	{
		return 0.0;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}
	
}
