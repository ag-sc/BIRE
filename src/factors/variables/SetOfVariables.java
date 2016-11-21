package factors.variables;

import java.util.Set;

import factors.FactorScope;
import templates.AbstractTemplate;

public class SetOfVariables<T> extends FactorScope {
	public Set<T> variables;

	public SetOfVariables(AbstractTemplate<?, ?, SetOfVariables<T>> template,
			Set<T> variables) {
		super(template, variables);
		this.variables = variables;
	}

	@Override
	public String toString() {
		return "SetOfVariables[template=" + template.getClass().getSimpleName() + ", variables=" + variables
				+ "]";
	}

}
