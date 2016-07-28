package learning.regularizer;

import learning.Vector;

public interface Regularizer {

	public Vector regularize(Vector gradients, Vector weights);
}
