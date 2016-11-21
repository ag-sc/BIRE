package factors.variables;

import factors.FactorScope;
import templates.AbstractTemplate;

public class SingleVariable<T> extends FactorScope {

	private T variable;

	public SingleVariable(AbstractTemplate<?, ?, SingleVariable<T>> template,
			T variable) {
		super(template, variable);
		this.variable = variable;
	}

	public T getVariable() {
		return variable;
	}


	@Override
	public String toString() {
		return "SingleVariableVariables [template=" + template.getClass().getSimpleName() + ", variable=" + variable
				+ "]";
	}

}
