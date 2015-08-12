package Learning;

import java.io.Serializable;

public class Score implements Serializable {

	public final double precision;
	public final double recall;
	public final double score;

	public Score() {
		this(0, 0, 0);
	}

	public Score(double precision, double recall, double score) {
		super();
		this.precision = precision;
		this.recall = recall;
		this.score = score;
	}

	@Override
	public String toString() {
		return "[precision=" + precision + ",\trecall=" + recall + ",\tscore=" + score + "]";
	}

}