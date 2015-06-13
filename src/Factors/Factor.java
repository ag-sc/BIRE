package Factors;

import Learning.Vector;
import Templates.Template;

public class Factor {

	Template template;
	
	Vector features;
	
	public double score()
	{
		return features.multiply(template.getWeightVector());
	}

//	public void propagateChange(Annotation annotation) {
//		
//		template.recompute(annotation, features);
//		
//	}

	public Template getTemplate() {
		return template;
	}

	public Vector getFeatureVector() {
		return features;
	}

	public void setTemplate(Template template) {
		this.template = template;
	}

	public void setFeatures(Vector features) {
		this.features = features;
	}
	
}
