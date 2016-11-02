package factors.variables;

import factors.FactorVariables;
import templates.AbstractTemplate;

public class SingleVariable<VariableT> extends FactorVariables {

	private VariableT variable;

	public SingleVariable(AbstractTemplate<?, ?, SingleVariable<VariableT>> template,
			VariableT variable) {
		super(template, variable);
		this.variable = variable;
	}

	public VariableT getVariable() {
		return variable;
	}


	@Override
	public String toString() {
		return "SingleVariableVariables [template=" + template.getClass().getSimpleName() + ", variable=" + variable
				+ "]";
	}

}
