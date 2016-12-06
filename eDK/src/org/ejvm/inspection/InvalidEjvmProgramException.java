package org.ejvm.inspection;

public class InvalidEjvmProgramException extends RuntimeException {

	private static final long serialVersionUID = -7868330374896391384L;

	public InvalidEjvmProgramException(String msg) {
		super(msg);
	}

	public InvalidEjvmProgramException(Throwable cause) {
		super(cause);
	}

	public InvalidEjvmProgramException(String msg, Throwable cause) {
		super(msg, cause);
	}
}