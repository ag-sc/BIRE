package exceptions;

public class UnkownTemplateRequestedException extends RuntimeException {
	public UnkownTemplateRequestedException(String message) {
		super(message);
	}
}
