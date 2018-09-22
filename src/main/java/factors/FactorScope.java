package factors;

import java.util.Arrays;

import templates.AbstractTemplate;

/**
 * Store the variables and their roles for this factor. It is essential that the
 * equals() and hashCode() are implemented correctly.
 * 
 * @author sjebbara
 *
 */
public abstract class FactorScope {
	protected final AbstractTemplate<?, ?, ?> template;
	protected final Object[] variables;

	public FactorScope(AbstractTemplate<?, ?, ?> template, Object... variables) {
		this.template = template;
		this.variables = variables;
	}

	public AbstractTemplate<?, ?, ?> getTemplate() {
		return template;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((template == null) ? 0 : template.hashCode());
		result = prime * result + Arrays.hashCode(variables);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FactorScope other = (FactorScope) obj;
		if (template == null) {
			if (other.template != null)
				return false;
		} else if (!template.equals(other.template))
			return false;
		if (!Arrays.equals(variables, other.variables))
			return false;
		return true;
	}

}
