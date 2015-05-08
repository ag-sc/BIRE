package Learning;

import java.util.List;

import Corpus.Document;
import Sampling.DefaultSampler;
import Variables.Annotation;
import Variables.State;

public class DefaultLearner implements Learner {

	Model model;
	
	int k;
	
	int steps;
	
	Scorer scorer;
	
	double alpha;
	
	ObjectiveFunction objective;
	
	public void train(List<Document> documents, List<State> states) {
		
		scorer = new Scorer();
		
		model = new Model();
		
		scorer.setModel(model);

		Document document;
		State goldState;
		
		DefaultSampler sampler = new DefaultSampler();
		
		for (int i =0; i < documents.size(); i++)
		{
			document = documents.get(i);
			
			goldState = states.get(i);
			
			State state = generateInitialAnnotations(document);
			
			List<Vector> featuresState = null;
			
			for (int j=0; j < steps; j++)
			{	
				State next_state = sampler.getNextState(state, scorer);
				
					if (objective.score(next_state, goldState) > objective.score(state, goldState))
					{
						if (next_state.score() < state.score())
						{
							// model.update(featuresState, alpha);
						}
						
					}
					else
					{
						if (objective.score(next_state, goldState) < objective.score(state, goldState))
						{
							if (next_state.score() > state.score())
							{
								// model.update(featuresState), alpha);
							}
							
						}
					}
			}
			
		}
		
		
		
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public int getK() {
		return k;
	}

	public void setK(int k) {
		this.k = k;
	}

	public Scorer getScorer() {
		return scorer;
	}

	public void setScorer(Scorer scorer) {
		this.scorer = scorer;
	}

	private State generateInitialAnnotations(Document document) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
}
