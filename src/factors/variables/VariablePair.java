package factors.variables;

import factors.FactorVariables;
import templates.AbstractTemplate;

public class VariablePair<VariableT> extends FactorVariables {

	private VariableT variable1;
	private VariableT variable2;

	public VariablePair(AbstractTemplate<?, ?, VariablePair<VariableT>> template, VariableT variable1,
			VariableT variable2) {
		super(template, variable1, variable2);
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
	public String toString() {
		return "VariablePairPattern [template=" + template.getClass().getSimpleName() + ", variable1=" + variable1
				+ ", variable2=" + variable2 + "]";
	}

}
