package Learning;

import java.util.Collection;
import java.util.Map;

import Variables.EntityAnnotation;
import Variables.State;

public class ObjectiveFunction {

	public double score(State state, State goldState) {
		Collection<EntityAnnotation> entities = state.getEntities();
		Collection<EntityAnnotation> goldEntities = goldState.getEntities();
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
		for (EntityAnnotation entity : goldEntities) {
			double max = 0.0;
			for (EntityAnnotation goldEntity : entities) {
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

		double f1 = 2 * (precision * recall) / (precision + recall);
		return f1;
	}

	private double argumentScore(EntityAnnotation entity,
			EntityAnnotation goldEntity) {

		Map<String, String> args1 = entity.getArguments();
		Map<String, String> args2 = goldEntity.getArguments();
		EntityAnnotation annotation1;
		EntityAnnotation annotation2;

		if (args1.keySet().size() == 0 && args2.keySet().size() == 0)
			return 1.0;

		int matchingRoles = 0;

		for (String role : args1.keySet()) {
			annotation1 = entity.getEntity(args1.get(role));
			if (args2.containsKey(role)) {
				annotation2 = goldEntity.getEntity(args2.get(role));
				if (overlap(annotation1, annotation2) > 0) {
					matchingRoles++;
				}
			}
		}

		return matchingRoles / args1.keySet().size();

	}

	private static double overlap(EntityAnnotation entity,
			EntityAnnotation goldEntity) {
		int a = entity.getBeginTokenIndex();
		int b = entity.getEndTokenIndex();
		int x = goldEntity.getBeginTokenIndex();
		int y = goldEntity.getEndTokenIndex();
		int overlap = Math.max(0, Math.min(b, y) - Math.max(a, x) + 1);

		return overlap / (Math.max(b - a, y - x) + 1);
	}

	private boolean typeMatches(EntityAnnotation entity,
			EntityAnnotation goldEntity) {
		return entity.getType().equals(goldEntity.getType());
	}

}
