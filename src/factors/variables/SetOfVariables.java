package factors.variables;

import java.util.Set;

import factors.FactorVariables;
import templates.AbstractTemplate;

public class SetOfVariables<VariableT> extends FactorVariables {
	public Set<VariableT> variables;

	public SetOfVariables(AbstractTemplate<?, ?, SetOfVariables<VariableT>> template,
			Set<VariableT> variables) {
		super(template, variables);
		this.variables = variables;
	}

	@Override
	public String toString() {
		return "SetOfVariables[template=" + template.getClass().getSimpleName() + ", variables=" + variables
				+ "]";
	}

}
