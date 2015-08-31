package evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import Logging.Log;

public class TaggedTimer {

	private static Map<String, Long> categoryTimings = new ConcurrentHashMap<>();
	private static Map<Long, String> jobCategories = new ConcurrentHashMap<>();
	private static Map<Long, Long> jobStarts = new ConcurrentHashMap<>();

	private static Random random = new Random();
	private static long first = 0;
	private static long last = 0;

	static {
		first = System.currentTimeMillis();
	}

	/**
	 * This will start a new timing job for the given tag that can be stopped by
	 * the returned long-ID
	 * 
	 * @param tag
	 * @return
	 */
	public static long start(String tag) {
		long id = random.nextLong();
		jobCategories.put(id, tag);
		long startTime = System.currentTimeMillis();
		jobStarts.put(id, startTime);
		return id;
	}

	/**
	 * Stops the previously started timing job with the given tag.
	 * 
	 * @param id
	 */
	public static void stop(long id) {
		long stop = System.currentTimeMillis();
		long time = stop - jobStarts.get(id);
		String tag = jobCategories.get(id);
		long oldTime = 0;
		if (categoryTimings.containsKey(tag))
			oldTime = categoryTimings.get(tag);

		categoryTimings.put(tag, oldTime + time);

		jobCategories.remove(id);
		jobStarts.remove(id);
		last = stop;
	}

	public static void printTimings() {
		// long summedTime = 0;
		long totalTime = last - first;
		// for (String tag : categoryTimings.keySet()) {
		// summedTime += categoryTimings.get(tag);
		// }
		Log.d("Total: %s", totalTime);
		for (String tag : categoryTimings.keySet()) {
			long t = categoryTimings.get(tag);
			Log.d("%s:\ttotal: %s,\trel: %s,", tag, t, ((double) t) / ((double) totalTime));
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
