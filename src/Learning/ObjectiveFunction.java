package Learning;

import java.util.List;
import java.util.Map;

import Variables.EntityAnnotation;
import Variables.State;

public class ObjectiveFunction {

	double score(State state, State goldState) {
		List<EntityAnnotation> entities = state.getEntities();
		List<EntityAnnotation> goldEntities = goldState.getEntities();
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

		Map<String, EntityAnnotation> args1 = entity.getArguments();
		Map<String, EntityAnnotation> args2 = goldEntity.getArguments();
		EntityAnnotation annotation1;
		EntityAnnotation annotation2;

		if (args1.keySet().size() == 0 && args2.keySet().size() == 0)
			return 1.0;

		int matching_roles = 0;

		for (String role : args1.keySet()) {
			annotation1 = args1.get(role);
			if (args2.containsKey(role)) {
				annotation2 = args2.get(role);
				if (overlap(annotation1, annotation2) > 0) {
					matching_roles++;
				}
			}
		}

		return matching_roles / args1.keySet().size();

	}

	private double overlap(EntityAnnotation entity, EntityAnnotation goldEntity) {
		int overlap = 0;
		int b1 = entity.getBegin();
		int e1 = entity.getEnd();
		int b2 = goldEntity.getBegin();
		int e2 = goldEntity.getEnd();
		for (int i = b1; i <= e1; i++) {
			if (b2 <= i && i <= e2) {
				overlap++;
			}
		}

		return overlap / Math.max(e1 - b1, e2 - b2);
	}

	private boolean typeMatches(EntityAnnotation entity,
			EntityAnnotation goldEntity) {
		return entity.getType().equals(goldEntity.getType());
	}

}
