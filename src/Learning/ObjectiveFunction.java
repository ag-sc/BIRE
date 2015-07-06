package Learning;

import java.util.Collection;
import java.util.Map;

import Logging.Log;
import Variables.EntityAnnotation;
import Variables.State;

public class ObjectiveFunction {

	public ObjectiveFunction() {
		 Log.off();
	}

	public double score(State state, State goldState) {
		Collection<EntityAnnotation> entities = state.getEntities();
		Collection<EntityAnnotation> goldEntities = goldState.getEntities();
		Log.d("score state:\n\t%s\n\t%s (GOLD)", state, goldState);
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
		if (precision == 0 || recall == 0 || entities.size() == 0
				|| goldEntities.size() == 0) {
			return 0;
		}

		precision /= entities.size();
		recall /= goldEntities.size();
		Log.d("precision: %s", precision);
		Log.d("recall: %s", recall);

		double f1 = 2 * (precision * recall) / (precision + recall);
		return f1;
	}

	private double argumentScore(EntityAnnotation entity1,
			EntityAnnotation entity2) {

		Map<String, String> arguments1 = entity1.getArguments();
		Map<String, String> arguments2 = entity2.getArguments();

		if (arguments1.keySet().size() == 0)
			return 1;

		int matchingRoles = 0;

		for (String role : arguments1.keySet()) {
			// TODO check if entity for id actually exists!
			EntityAnnotation argEntity1 = entity1.getEntity(arguments1
					.get(role));
			if (arguments2.containsKey(role)) {
				EntityAnnotation argEntity2 = entity2.getEntity(arguments2
						.get(role));
				if (overlap(argEntity1, argEntity2) > 0) {
					matchingRoles++;
				}
			}
		}

		return matchingRoles / arguments1.keySet().size();

	}

	private static double overlap(EntityAnnotation entity,
			EntityAnnotation goldEntity) {
		Log.methodOff();
		int a = entity.getBeginTokenIndex();
		int b = entity.getEndTokenIndex();
		int x = goldEntity.getBeginTokenIndex();
		int y = goldEntity.getEndTokenIndex();
		int overlap = Math.max(0, Math.min(b, y) - Math.max(a, x) + 1);
		double overlapScore = ((double) overlap) / (b - a + 1);
		Log.d("Overlap for %s and %s (GOLD): %s (%s)", entity.getID(),
				goldEntity.getID(), overlap, overlapScore);
		return overlapScore;
	}

	private boolean typeMatches(EntityAnnotation entity,
			EntityAnnotation goldEntity) {
		return entity.getType().equals(goldEntity.getType());
	}

}
