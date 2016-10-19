package examples.tokenization;

import variables.AbstractVariable;

public class BoundaryVariable extends AbstractVariable implements Comparable<BoundaryVariable> {

	public final int boundaryPosition;

	public BoundaryVariable(int boundaryPosition) {
		super();
		this.boundaryPosition = boundaryPosition;
	}

	@Override
	public int compareTo(BoundaryVariable o) {
		return o.boundaryPosition;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + boundaryPosition;
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
		BoundaryVariable other = (BoundaryVariable) obj;
		if (boundaryPosition != other.boundaryPosition)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.valueOf(boundaryPosition);
	}

}
