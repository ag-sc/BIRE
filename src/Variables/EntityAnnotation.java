package Variables;

import Changes.StateChange;
import Factors.Factor;

public class EntityAnnotation extends Annotation {
	int Begin;
	int End;

	String text;

	EntityType type;

	public void setBegin(int begin) {
		Begin = begin;
		change = StateChange.BOUNDARIES_CHANGED;
	}

	public void setEnd(int end) {
		End = end;
		change = StateChange.BOUNDARIES_CHANGED;
	}

	
}
