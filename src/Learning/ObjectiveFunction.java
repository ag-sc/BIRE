package Learning;

import java.util.Collection;
import java.util.Map;

import Logging.Log;
import Variables.EntityAnnotation;
import Variables.State;

public class ObjectiveFunction {

	{
		Log.off();
	}

	public ObjectiveFunction() {
	}

	public Score score(State state, State goldState) {
		Collection<EntityAnnotation> entities = state.getEntities();
		Collection<EntityAnnotation> goldEntities = goldState.getEntities();
		// Log.d("score state:\n\t%s\n\t%s (GOLD)", state, goldState);
		Score score;
		if (goldEntities.size() == 0 && entities.size() == 0) {
			score = new Score(1, 1, 1);
		} else {
			double precision = 0.0;
			for (EntityAnnotation entity : entities) {
				double max = 0.0;
				for (EntityAnnotation goldEntity : goldEntities) {
					if (typeMatches(entity, goldEntity)) {
						double overlapScore = overlap(entity, goldEntity);
						if (overlapScore > max) {
							max = overlapScore * argumentScore(entity, goldEntity);
						}
					}
				}
				precision += max;
			}

			double recall = 0.0;
			for (EntityAnnotation goldEntity : goldEntities) {
				double max = 0.0;
				for (EntityAnnotation entity : entities) {
					if (typeMatches(goldEntity, entity)) {
						double overlapScore = overlap(goldEntity, entity);
						if (overlapScore > max) {
							max = overlapScore * argumentScore(goldEntity, entity);
						}
					}
				}
				recall += max;
			}
			// Log.d("Precision: %s/%s, Recall: %s/%s", precision,
			// entities.size(),
			// recall, goldEntities.size());
			// TODO score = 0 only because precision/recall = 0
			if ((precision == 0 && recall == 0) || entities.size() == 0 || goldEntities.size() == 0) {
				// Log.d("Score: %s", 0);
				score = new Score();
			} else {
				precision /= entities.size();
				recall /= goldEntities.size();

				double f1 = 2 * (precision * recall) / (precision + recall);
				// Log.d("Score: %s\t\t(Precision: %s, Recall: %s)", f1,
				// precision,
				// recall);
				score = new Score(precision, recall, f1);
			}
		}
		// TODO not the cleanest way to make the score accessible everywhere
		state.setObjectiveFunctionScore(score);
		return score;
	}

	private double argumentScore(EntityAnnotation entity1, EntityAnnotation entity2) {

		Map<String, String> arguments1 = entity1.getArguments();
		Map<String, String> arguments2 = entity2.getArguments();

		if (arguments1.keySet().size() == 0)
			return 1;

		int matchingRoles = 0;

		for (String role : arguments1.keySet()) {
			// TODO check if entity for id actually exists!
			EntityAnnotation argEntity1 = entity1.getEntity(arguments1.get(role));
			if (arguments2.containsKey(role)) {
				EntityAnnotation argEntity2 = entity2.getEntity(arguments2.get(role));
				if (overlap(argEntity1, argEntity2) > 0) {
					matchingRoles++;
				}
			}
		}

		return matchingRoles / arguments1.keySet().size();

	}

	private static double overlap(EntityAnnotation entity, EntityAnnotation goldEntity) {
		Log.methodOff();
		int a = entity.getBeginTokenIndex();
		int b = entity.getEndTokenIndex();
		int x = goldEntity.getBeginTokenIndex();
		int y = goldEntity.getEndTokenIndex();
		int overlap = Math.max(0, Math.min(b, y) - Math.max(a, x) + 1);
		double overlapScore = ((double) overlap) / (b - a + 1);
		// Log.d("Overlap for %s and %s (GOLD): %s (%s)", entity.getID(),
		// goldEntity.getID(), overlap, overlapScore);
		return overlapScore;
	}

	private boolean typeMatches(EntityAnnotation entity, EntityAnnotation goldEntity) {
		return entity.getType().equals(goldEntity.getType());
	}

}
