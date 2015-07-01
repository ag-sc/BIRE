package Corpus;

import java.io.Serializable;


public class Token implements Serializable{

	int index;
	int from, to;
	String text;

	public Token(int index, int start, int stop, String text) {
		super();
		this.index = index;
		this.from = start;
		this.to = stop;
		this.text = text;
	}

	public int getFrom() {
		return from;
	}

	public int getTo() {
		return to;
	}

	public String getText() {
		return text;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public String toString() {
		return "Token [" + index + ": " + text + " (" + from
				+ "-" + to + ")]";
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
