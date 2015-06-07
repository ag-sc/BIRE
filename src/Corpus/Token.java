package Corpus;


public class Token {

	long index;
	int start, stop;
	String text;

	public int getBegin() {
		return start;
	}

	public int getEnd() {
		return stop;
	}

	public String getText() {
		return text;
	}

	public long getIndex() {
		return index;
	}
	
	// public EntityAnnotation getAnnotation() {
	// return annotation;
	// }
	//
	// public void setAnnotation(EntityAnnotation annotation) {
	// this.annotation = annotation;
	// }
	//
	// public void removeAnnotation() {
	// this.annotation = null;
	// }
	//
	// public boolean hasAnnotation() {
	// return annotation != null;
	// }

}
