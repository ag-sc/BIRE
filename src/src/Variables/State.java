package Variables;

import java.util.List;
import java.util.Set;

import Changes.StateChange;
import Factors.Factor;
import Factors.FactorGraph;

public class State {
	
	List<Annotation> annotations;
		
	public State applyChange(int i, StateChange change) throws CloneNotSupportedException
	{
		// overwrite clone
		
		State state = (State) this.clone();
		
		annotations.get(i).applyChange(change);
		
		// do somethings
		return state;
	}

	public List<Annotation> getAnnotations() {
		return annotations;
	}
	
	public double score()
	{
		return 0.0;
	}
	
}
