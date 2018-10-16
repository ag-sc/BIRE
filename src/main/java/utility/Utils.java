package utility;

import java.util.Collection;
import java.util.stream.Stream;

public class Utils {

	public static <T> Stream<T> getStream(Collection<T> elements, boolean parallel) {
		Stream<T> stream = null;
		if (elements.size() > 10 && parallel) {
			stream = elements.parallelStream();
		} else {
			stream = elements.stream();
		}
		return stream;
	}

}
