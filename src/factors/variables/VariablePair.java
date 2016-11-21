package factors.variables;

import factors.FactorScope;
import templates.AbstractTemplate;

public class VariablePair<T> extends FactorScope {

	private T variable1;
	private T variable2;

	public VariablePair(AbstractTemplate<?, ?, VariablePair<T>> template, T variable1,
			T variable2) {
		super(template, variable1, variable2);
		this.variable1 = variable1;
		this.variable2 = variable2;
	}

	public T getVariable1() {
		return variable1;
	}

	public T getVariable2() {
		return variable2;
	}

	@Override
	public String toString() {
		return "VariablePairPattern [template=" + template.getClass().getSimpleName() + ", variable1=" + variable1
				+ ", variable2=" + variable2 + "]";
	}

}
