package learning.regularizer;

import learning.Vector;

public class L2 implements Regularizer {

	private double l2 = 0.0001;

	public L2() {
	}

	public L2(double l2) {
		this.l2 = l2;
	}

	@Override
	public Vector regularize(Vector gradients, Vector weights) {
		Vector regularized = gradients.add(weights.mul(l2));
		return regularized;
	}

}
