package Learning;

import java.util.Collection;
import java.util.Map;

import Variables.EntityAnnotation;
import Variables.State;

public class ObjectiveFunction {

	public double score(State state, State goldState) {
		Collection<EntityAnnotation> entities = state.getEntities();
		Collection<EntityAnnotation> goldEntities = goldState.getEntities();
		double precision = 0;
		for (EntityAnnotation entity : entities) {
			double max = 0;
			for (EntityAnnotation goldEntity : goldEntities) {
				if (typeMatches(entity, goldEntity)) {
					double overlapScore = overlap(entity, goldEntity);
					if (overlapScore > max) {
						max = overlapScore * argumentScore(entity, goldEntity);
					}
				}
			}
			precision = precision + max;
		}
		return precision / entities.size();
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

	private double overlap(EntityAnnotation entity, EntityAnnotation goldEntity) {
		int overlap = 0;
		int b1 = entity.getBeginTokenIndex();
		int e1 = entity.getEndTokenIndex();
		int b2 = goldEntity.getBeginTokenIndex();
		int e2 = goldEntity.getEndTokenIndex();
		for (int i = b1; i <= e1; i++) {
			if (b2 <= i && i <= e2) {
				overlap++;
			}
		}

		return overlap / (Math.max(e1 - b1, e2 - b2) + 1);
	}

	private boolean typeMatches(EntityAnnotation entity,
			EntityAnnotation goldEntity) {
		return entity.getType().equals(goldEntity.getType());
	}

}
