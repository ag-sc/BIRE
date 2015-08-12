package evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import Logging.Log;

public class TaggedTimer {

	private static Map<String, Long> categoryTimings = new HashMap<>();
	private static Map<Long, String> jobCategories = new HashMap<>();
	private static Map<Long, Long> jobStarts = new HashMap<>();

	private static Random random = new Random();

	public static long start(String tag) {
		long id = random.nextLong();
		jobCategories.put(id, tag);
		long startTime = System.currentTimeMillis();
		jobStarts.put(id, startTime);
		return id;
	}

	public static void stop(long id) {
		long time = System.currentTimeMillis() - jobStarts.get(id);
		String tag = jobCategories.get(id);
		long oldTime = 0;
		if (categoryTimings.containsKey(tag))
			oldTime = categoryTimings.get(tag);

		categoryTimings.put(tag, oldTime + time);

		jobCategories.remove(id);
		jobStarts.remove(id);
	}

	public static void printTimings() {
		long totalTime = 0;
		for (String tag : categoryTimings.keySet()) {
			totalTime += categoryTimings.get(tag);
		}
		for (String tag : categoryTimings.keySet()) {
			long t = categoryTimings.get(tag);
			Log.d("%s:\ttotal: %s,\trel: %s", tag, t, ((double) t) / ((double) totalTime));
		}
	}

	/*-
	 * GEN: 		total: 1503, 	rel: 0.02041093472031723
	 * SC-SCORE: 	total: 1277, 	rel: 0.017341825441014706
	 * UP-DIFF: 	total: 12875, 	rel: 0.17484416801336286
	 * UP-UPDATE: 	total: 20479, 	rel: 0.2781074731452938
	 * SC-UNROLL: 	total: 37503, 	rel: 0.5092955986800114
	 */
}
