package Templates;

import Variables.Annotation;
import Changes.StateChange;
import Factors.Factor;
import Learning.Vector;

public interface Template {

	void recompute(Annotation annotation, Vector features);
	
	Vector getWeightVector();

	
	
}
