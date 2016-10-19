package learning.optimizer;

import learning.Vector;

public interface Optimizer {

	public Vector getUpdates(Vector theta, Vector gradient);
}
