package Factors;

import Changes.StateChange;
import Learning.Vector;
import Templates.Template;
import Variables.Annotation;

public class Factor {

	Template template;
	
	Vector features;
	
	public double score()
	{
		return features.multiply(template.getWeightVector());
	}

	public void propagateChange(Annotation annotation) {
		
		template.recompute(annotation, features);
		
	}
	
}
