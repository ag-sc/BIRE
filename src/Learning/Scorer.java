package Learning;

import Variables.State;

public class Scorer {

	private Model model;

	public Scorer(Model model) {
		this.model = model;
	}

	/**
	 * Computes the score of this state according to the trained model. The
	 * computed score is returned but also updated in the state objects
	 * <i>score</i> field.
	 * 
	 * @param state
	 * @return
	 */
	public double score(State state) {
		state.unroll(model);
		double score = state.recomputeModelScore();
		return score;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

}
