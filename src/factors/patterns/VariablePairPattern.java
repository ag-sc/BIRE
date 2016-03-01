package factors.patterns;

import factors.FactorPattern;
import templates.AbstractTemplate;
import variables.AbstractVariable;

public class VariablePairPattern<VariableT extends AbstractVariable> extends FactorPattern {

	private VariableT variable1;
	private VariableT variable2;

	public VariablePairPattern(AbstractTemplate<?, ?, VariablePairPattern<VariableT>> template, VariableT variable1,
			VariableT variable2) {
		super(template);
		this.variable1 = variable1;
		this.variable2 = variable2;
	}

	public VariableT getVariable1() {
		return variable1;
	}

	public VariableT getVariable2() {
		return variable2;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((variable1 == null) ? 0 : variable1.hashCode());
		result = prime * result + ((variable2 == null) ? 0 : variable2.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		VariablePairPattern other = (VariablePairPattern) obj;
		if (variable1 == null) {
			if (other.variable1 != null)
				return false;
		} else if (!variable1.equals(other.variable1))
			return false;
		if (variable2 == null) {
			if (other.variable2 != null)
				return false;
		} else if (!variable2.equals(other.variable2))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "VariablePairPattern [variable1=" + variable1 + ", variable2=" + variable2 + "]";
	}

}
