package learning.regularizer;

import learning.Vector;

public interface Regularizer {
	public double penalize(Vector weights);

	public Vector regularize(Vector gradients, Vector weights);
}
