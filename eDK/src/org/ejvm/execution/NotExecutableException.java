package org.ejvm.execution;

import org.ejvm.inspection.EjvmProgram;

/**
 * <p>
 * Exception thrown when an {@link EjvmProgram} cannot be executed by the {@link EjvmMachine}. Details can be found in
 * the exception's message. The nonexecutable program may also be attached to the exception and is available via
 * {@link #getProgram()}.
 * </p>
 * 
 * @author Sebastian Nicolai Kaupe
 * @since 1.0
 */
public class NotExecutableException extends RuntimeException {

	private static final long serialVersionUID = -4803389672362762514L;

	private final EjvmProgram program;

	/**
	 * <p>
	 * Creates a new exception.
	 * </p>
	 * 
	 * @param msg
	 *            The descriptive message. It should contain a human-readable, preferably easy to understand description
	 *            of the event that caused this exception to be thrown.
	 * @param program
	 *            The {@link EjvmProgram} that cannot be executed (optional).
	 */
	public NotExecutableException(String msg, EjvmProgram program) {
		super(msg);
		this.program = program;
	}

	/**
	 * <p>
	 * Creates a new exception.
	 * </p>
	 * 
	 * @param msg
	 *            The descriptive message. It should contain a human-readable, preferably easy to understand description
	 *            of the event that caused this exception to be thrown.
	 * @param program
	 *            The {@link EjvmProgram} that cannot be executed (optional).
	 * @param cause
	 *            The {@link Throwable} that caused this exception to be thrown.
	 */
	public NotExecutableException(String msg, EjvmProgram program, Throwable cause) {
		super(msg, cause);
		this.program = program;
	}

	/**
	 * <p>
	 * Returns the nonexecutable program.
	 * </p>
	 * 
	 * @return The program that caused this exception. May be {@code null}.
	 * 
	 */
	public EjvmProgram getProgram() {
		return program;
	}
}