package factors.patterns;

import factors.FactorPattern;
import templates.AbstractTemplate;
import variables.AbstractVariable;

public class SingleVariablePattern<VariableT extends AbstractVariable> extends FactorPattern {

	private VariableT variable;

	public SingleVariablePattern(AbstractTemplate<?, ?, SingleVariablePattern<VariableT>> template,
			VariableT variable) {
		super(template);
		this.variable = variable;
	}

	public VariableT getVariable() {
		return variable;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((variable == null) ? 0 : variable.hashCode());
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
		SingleVariablePattern other = (SingleVariablePattern) obj;
		if (variable == null) {
			if (other.variable != null)
				return false;
		} else if (!variable.equals(other.variable))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SingleVariablePattern [template=" + template.getClass().getSimpleName() + ", variable=" + variable
				+ "]";
	}

}
