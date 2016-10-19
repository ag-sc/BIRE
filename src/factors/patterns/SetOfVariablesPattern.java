package factors.patterns;

import java.util.Set;

import factors.FactorPattern;
import templates.AbstractTemplate;
import variables.AbstractVariable;

public class SetOfVariablesPattern<VariableT extends AbstractVariable> extends FactorPattern {
	public Set<VariableT> variables;

	public SetOfVariablesPattern(AbstractTemplate<?, ?, SetOfVariablesPattern<VariableT>> template,
			Set<VariableT> variables) {
		super(template);
		this.variables = variables;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((variables == null) ? 0 : variables.hashCode());
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
		SetOfVariablesPattern other = (SetOfVariablesPattern) obj;
		if (variables == null) {
			if (other.variables != null)
				return false;
		} else if (!variables.equals(other.variables))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SetOfVariablesPattern [template=" + template.getClass().getSimpleName() + ", variables=" + variables
				+ "]";
	}

}
