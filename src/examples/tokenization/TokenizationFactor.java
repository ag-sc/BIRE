package examples.tokenization;

import java.util.Set;

import factors.AbstractFactor;
import utility.VariableID;

public class TokenizationFactor extends AbstractFactor {

	public int position;

	/**
	 * This factor applies to the characters in the region specified by
	 * <i>from</i> and <i>to</i>.
	 * 
	 * @param template
	 * @param position
	 */
	public TokenizationFactor(TokenizationTemplate template, int position) {
		super(template);
		this.position = position;
	}

	/**
	 * Since this function is currently not yet needed i the framework we simply
	 * neglect it here.
	 */
	@Override
	public Set<VariableID> getVariableIDs() {
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + position;
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
		TokenizationFactor other = (TokenizationFactor) obj;
		if (position != other.position)
			return false;
		return true;
	}

}
