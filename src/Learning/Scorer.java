package Learning;

import Variables.State;

public class Scorer {

	Model model;

	/**
	 * Computes the score of this state according to the trained model. The
	 * computed score is returned but also updated in the state objects
	 * <i>score</i> field.
	 * 
	 * @param state
	 * @return
	 */
	public double score(State state) {
		// TODO Scorer assigns random score to states.
		double score = Math.random();
		state.setScore(score);
		return score;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

}
