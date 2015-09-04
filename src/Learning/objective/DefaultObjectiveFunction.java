package Learning.objective;

import java.util.Collection;
import java.util.Map.Entry;

import com.google.common.collect.Multimap;

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
						// FIXME comparing overlapScore to previous overlapScore
						// x argumentScore ??? Seems wrong!
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
		Multimap<ArgumentRole, EntityID> arguments1 = entity1.getArguments();
		Multimap<ArgumentRole, EntityID> arguments2 = entity2.getArguments();

		if (arguments1.keySet().size() == 0)
			return 1;

		int matchingRoles = 0;

		// count arguments of entity1 that are also in entity2
		for (Entry<ArgumentRole, EntityID> argument1 : arguments1.entries()) {
			ArgumentRole argRole1 = argument1.getKey();
			EntityAnnotation argEntity1 = entity1.getEntity(argument1.getValue());
			/*
			 * Since there are possibly several arguments with the same role,
			 * check if there is at least one that matches (overlaps)
			 * argEntity1.
			 */
			Collection<EntityID> argsForRole2 = arguments2.get(argRole1);
			for (EntityID argForRoleEntityID2 : argsForRole2) {
				EntityAnnotation argEntity2 = entity2.getEntity(argForRoleEntityID2);
				if (overlapScore(argEntity1, argEntity2) > 0) {
					matchingRoles++;
					// only count one match per argument
					break;
				}
			}
		}
		return matchingRoles / arguments1.size();
	}

	public static double overlapScore(EntityAnnotation entity, EntityAnnotation goldEntity) {
		int a = entity.getBeginTokenIndex();
		int b = entity.getEndTokenIndex();
		int x = goldEntity.getBeginTokenIndex();
		int y = goldEntity.getEndTokenIndex();
		int overlap = Math.max(0, Math.min(b, y) - Math.max(a, x) + 1);
		double overlapScore = ((double) overlap) / (b - a + 1);
		return overlapScore;
	}
}
