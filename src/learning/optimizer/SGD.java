package learning.optimizer;

import learning.Vector;

public class SGD implements Optimizer {

	private double alpha = 0.001;
	private double momentum = 0.0;
	private double decay = 0.0;
	private boolean nesterov = false;

	private Vector m = new Vector();
	private Vector v = new Vector();
	private int t = 0;

	public SGD(double alpha, double momentum, double decay, boolean nesterov) {
		super();
		this.alpha = alpha;
		this.momentum = momentum;
		this.decay = decay;
		this.nesterov = nesterov;
	}

	public SGD() {
	}

	@Override
	public Vector getUpdates(Vector theta, Vector gradient) {
		t++;
		double alpha_t = alpha * (1.0 / 1.0 + decay * t);
		v = m.mul(momentum).sub(gradient.mul(alpha_t));

		if (nesterov) {
			theta = theta.add(v.mul(momentum).sub(gradient.mul(alpha_t)));
		} else {
			theta = theta.add(v);
		}
		m = v;
		return theta;
	}

	public static void main(String[] args) {
		Vector theta = new Vector();
		theta.set("x1", 1.0);
		theta.set("x2", 1.0);

		SGD adam = new SGD(0.001, 0.9, 0, false);
		for (int t = 0; t < 200; t++) {
			double x1 = theta.getValueOfFeature("x1");
			double x2 = theta.getValueOfFeature("x2");
			double loss = 1.0 / 2.0 * Math.pow(x1 * 2 + x2 * 4, 2);

			System.out.println();
			System.out.println("Time:     " + t);
			System.out.println("Theta:    " + theta);
			Vector g = new Vector();
			g.set("x1", 4 * (x1 + 2 * x2));
			g.set("x2", 8 * (x1 + 2 * x2));
			System.out.println("Loss:     " + loss);
			System.out.println("Gradient: " + g);
			theta = adam.getUpdates(theta, g);
			System.out.println("Theta:    " + theta);
		}
	}

}
