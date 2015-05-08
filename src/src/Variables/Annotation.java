package Variables;

import java.util.Set;

import Changes.StateChange;
import Factors.Factor;

public abstract class Annotation {

	Set<Factor> Factors;
	
	Boolean Changed;
	
	int Begin;
	int End;

	String value;
	
	public void setBegin(int begin)
	{
		Begin = begin;
		Changed = true;
		
		for (Factor factor: Factors)
		{
			
		}
		
	}
	
	public void setEnd(int end)
	{
		End = end;
		Changed = true;
	}
	 
	public void applyChange(StateChange change)
	{
		for (Factor factor: Factors)
		{
			factor.propagateChange(this, change);
		}
		
		// apply change
		
	}
	
}
