package learning.optimizer;

import learning.Vector;

public class Adam implements Optimizer {

	private double alpha = 0.001;
	private double beta1 = 0.9;
	private double beta2 = 0.999;
	private double epsilon = 10e-8;

	private Vector m = new Vector();
	private Vector v = new Vector();
	private int t = 0;

	public Adam() {
	}

	public Adam(double alpha) {
		super();
		this.alpha = alpha;
	}

	public Adam(double alpha, double beta1, double beta2, double epsilon) {
		super();
		this.alpha = alpha;
		this.beta1 = beta1;
		this.beta2 = beta2;
		this.epsilon = epsilon;
	}

	@Override
	public Vector getUpdates(Vector theta, Vector gradient) {
		t++;
		Vector m_t = m.mul(beta1).add(gradient.mul(1 - beta1));
		Vector v_t = v.mul(beta2).add(gradient.pow(2).mul(1 - beta2));

		// Vector m_hat = m.div(1 - Math.pow(beta1, t));
		// Vector v_hat = v.div(1 - Math.pow(beta2, t));

		double alpha_t = alpha * Math.sqrt(1 - Math.pow(beta2, t)) / (1 - Math.pow(beta1, t));
		Vector nom = m_t.mul(alpha_t);
		Vector denom = v_t.sqrt().add(epsilon);
		Vector diff = nom.div(denom);
		theta = theta.sub(diff);
		
//		System.out.println("Grad: " + gradient);
//		System.out.println("m:    " + m);
//		System.out.println("m_t:  " + m_t);
//		System.out.println("v:    " + v);
//		System.out.println("v_t:  " + v_t);
//		System.out.println("alph: " + alpha_t);
//		System.out.println("nom:  " + nom);
//		System.out.println("deno: " + denom);
//		System.out.println("Diff: " + diff);

		m = m_t;
		v = v_t;
		return theta;
	}

	public static void main(String[] args) {
		Vector theta = new Vector();
		theta.set("x1", 1.0);
		theta.set("x2", 1.0);

		Optimizer adam = new Adam();

		for (int t = 0; t < 100; t++) {
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
