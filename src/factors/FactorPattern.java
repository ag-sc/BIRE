package factors;

import templates.AbstractTemplate;

/**
 * Store the variables and their roles for this factor. It is essential that the
 * equals() and hashCode() are implemented correctly.
 * 
 * @author sjebbara
 *
 */
public abstract class FactorPattern {
	protected final AbstractTemplate<?, ?, ?> template;

	public FactorPattern(AbstractTemplate<?, ?, ?> template) {
		this.template = template;
	}

	public AbstractTemplate<?, ?, ?> getTemplate() {
		return template;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((template == null) ? 0 : template.hashCode());
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
		FactorPattern other = (FactorPattern) obj;
		if (template == null) {
			if (other.template != null)
				return false;
		} else if (!template.equals(other.template))
			return false;
		return true;
	}

}
