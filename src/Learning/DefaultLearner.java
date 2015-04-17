package Learning;

import java.util.List;
import java.util.Set;

import Sampling.DefaultSampler;
import Sampling.Sampler;
import Annotation.Annotation;
import Corpus.Document;

public class DefaultLearner implements Learner {

	Model model;
	
	int k;
	
	int steps;
	
	Scorer scorer;
	
	double alpha;
	
	ObjectiveFunction objective;
	
	public void train(List<Document> documents,
			List<List<Annotation>> annotations) {
		
		scorer = new Scorer();
		
		model = new Model();
		
		scorer.setModel(model);
		
		
		Document document;
		List<Annotation> goldAnnotations;
		
		DefaultSampler sampler = new DefaultSampler();
		
		for (int i =0; i < documents.size(); i++)
		{
			document = documents.get(i);
			goldAnnotations = annotations.get(i);
			
			List<Annotation> state = generateInitialAnnotations(document);
			
			List<FeatureVector> featuresState = null;
			
			for (int j=0; j < steps; j++)
			{	
				List<Annotation> next_state = sampler.getNextState(state, scorer);
			
					List<FeatureVector> featuresNextState = null; // features for state
				
					if (objective.score(next_state, goldAnnotations) > objective.score(state, goldAnnotations))
					{
						if (scorer.score(featuresNextState) < scorer.score(featuresState))
						{
							model.update(featuresState, alpha);
						}
						
					}
					else
					{
						if (objective.score(next_state, goldAnnotations) < objective.score(state, goldAnnotations))
						{
							if (scorer.score(featuresNextState) > scorer.score(featuresState))
							{
								model.update(featuresState), alpha);
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

	private List<Annotation> generateInitialAnnotations(Document document) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
}
