package learning.optimizer;

import learning.Vector;

public class SGD implements Optimizer {

	private float alpha = 0.001F;
	private float momentum = 0.0F;
	private float decay = 0.0F;
	private boolean nesterov = false;

	private Vector m = new Vector();
	private Vector v = new Vector();
	private int t = 0;

	public SGD(double alpha, double momentum, double decay, boolean nesterov) {
		super();
		this.alpha = (float)alpha;
		this.momentum = (float)momentum;
		this.decay = (float)decay;
		this.nesterov = nesterov;
	}

	public SGD() {
	}

	@Override
	public Vector getUpdates(Vector theta, Vector gradient) {
		t++;
		float alpha_t = alpha * (1.0F / (1.0F + decay * t));

		if (momentum == 0 && decay == 0 && !nesterov) {
			// perform sparse updates
			theta.subtractFromValue(gradient.mul(alpha_t));
		} else {
			if (momentum == 0) {
				theta.subtractFromValue(gradient.mul(alpha_t));
			} else {

				v = m.mul(momentum).sub(gradient.mul(alpha_t));

				if (nesterov) {
					theta = theta.add(v.mul(momentum).sub(gradient.mul(alpha_t)));
				} else {
//					theta = 
					theta.addFAST(v);
//					addFAST()
//							p: 0.7283303194695598	r: 0.645539481615431	f1: 0.6844403691002963
//							--------------randomRun-232724325---------------
//							Total training time: 219808 ms.
//							Total test time: 4674 ms.
//							Total time: PT3M44.482S

//					add()
//							p: 0.7283303194695598	r: 0.645539481615431	f1: 0.6844403691002963
// 							--------------randomRun-1012976934---------------
//							Total training time: 221951 ms.
//							Total test time: 4665 ms.
//							Total time: PT3M46.616S
				}
				m = v;
			}
		}
		return theta;
	}

	@Override
	public double getCurrentAlphaValue() {
		return alpha;
	}

	@Override
	public String toString() {
		return "SGD [alpha=" + alpha + ", momentum=" + momentum + ", decay=" + decay + ", nesterov=" + nesterov + ", m="
				+ m + ", v=" + v + ", t=" + t + "]";
	}

	// public static void main(String[] args) {
	// Vector theta = new Vector();
	// theta.set("x1", 1.0);
	// theta.set("x2", 1.0);
	//
	// SGD adam = new SGD(0.001, 0.9, 0, false);
	// for (int t = 0; t < 200; t++) {
	// double x1 = theta.getValueOfFeature("x1");
	// double x2 = theta.getValueOfFeature("x2");
	// double loss = 1.0 / 2.0 * Math.pow(x1 * 2 + x2 * 4, 2);
	//
	// System.out.println();
	// System.out.println("Time: " + t);
	// System.out.println("Theta: " + theta);
	// Vector g = new Vector();
	// g.set("x1", 4 * (x1 + 2 * x2));
	// g.set("x2", 8 * (x1 + 2 * x2));
	// System.out.println("Loss: " + loss);
	// System.out.println("Gradient: " + g);
	// theta = adam.getUpdates(theta, g);
	// System.out.println("Theta: " + theta);
	// }
	// }

}
