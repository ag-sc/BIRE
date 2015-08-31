package Learning.objective;

import java.util.Collection;
import java.util.Map;

import Learning.Score;
import Logging.Log;
import Variables.ArgumentRole;
import Variables.EntityAnnotation;
import Variables.State;
import utility.EntityID;

public class DefaultObjectiveFunction extends ObjectiveFunction {

	{
		Log.off();
	}

	public DefaultObjectiveFunction() {
	}

	@Override
	public Score score(State state, State goldState) {
		Collection<EntityAnnotation> entities = state.getEntities();
		Collection<EntityAnnotation> goldEntities = goldState.getEntities();
		Score score;
		if (goldEntities.size() == 0 && entities.size() == 0) {
			score = new Score(1, 1, 1);
		} else {
			double precision = 0.0;
			for (EntityAnnotation entity : entities) {
				double max = 0.0;
				for (EntityAnnotation goldEntity : goldEntities) {
					if (typeMatches(entity, goldEntity)) {
						double overlapScore = overlapScore(entity, goldEntity);
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
						double overlapScore = overlapScore(goldEntity, entity);
						if (overlapScore > max) {
							max = overlapScore * argumentScore(goldEntity, entity);
						}
					}
				}
				recall += max;
			}
			// TODO score = 0 only because precision/recall = 0
			if ((precision == 0 && recall == 0) || entities.size() == 0 || goldEntities.size() == 0) {
				score = new Score();
			} else {
				precision /= entities.size();
				recall /= goldEntities.size();

				double f1 = 2 * (precision * recall) / (precision + recall);
				score = new Score(precision, recall, f1);
			}
		}
		// TODO not the cleanest way to make the score accessible everywhere
		state.setObjectiveScore(score);
		return score;
	}

	private double argumentScore(EntityAnnotation entity1, EntityAnnotation entity2) {
		Map<ArgumentRole, EntityID> arguments1 = entity1.getArguments();
		Map<ArgumentRole, EntityID> arguments2 = entity2.getArguments();

		if (arguments1.keySet().size() == 0)
			return 1;

		int matchingRoles = 0;

		for (ArgumentRole role : arguments1.keySet()) {
			// TODO check if entity for id actually exists!
			EntityAnnotation argEntity1 = entity1.getEntity(arguments1.get(role));
			if (arguments2.containsKey(role)) {
				EntityAnnotation argEntity2 = entity2.getEntity(arguments2.get(role));
				if (overlapScore(argEntity1, argEntity2) > 0) {
					matchingRoles++;
				}
			}
		}

		return matchingRoles / arguments1.keySet().size();
	}

}
