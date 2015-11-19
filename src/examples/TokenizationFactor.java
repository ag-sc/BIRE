package examples;

import java.util.Set;

import factors.AbstractFactor;
import utility.VariableID;

public class TokenizationFactor extends AbstractFactor {

	public int from;
	public int to;

	public TokenizationFactor(TokenizationTemplate template, int from, int to) {
		super(template);
		this.from = from;
		this.to = to;
	}

	@Override
	public Set<VariableID> getVariableIDs() {
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + from;
		result = prime * result + to;
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
		if (from != other.from)
			return false;
		if (to != other.to)
			return false;
		return true;
	}

}
