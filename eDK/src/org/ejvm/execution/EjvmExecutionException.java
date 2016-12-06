package org.ejvm.execution;

/**
 * <p>
 * Exception thrown when an {@link EjvmProcess} causes an exception during execution. Details can be found in the
 * exception's message. Furthermore, the process may also be attached to the exception and is available via
 * {@link #getProcess()}.
 * </p>
 * 
 * @author Sebastian Nicolai Kaupe
 * @since 1.0
 */
public class EjvmExecutionException extends RuntimeException {

	private static final long serialVersionUID = -2660623957580769713L;

	private final Reason reason;
	private final EjvmProcess process;

	/**
	 * <p>
	 * Creates a new exception.
	 * </p>
	 * 
	 * @param reason
	 *            The reason this exception was thrown.
	 * @param process
	 *            The {@link EjvmProcess} that caused an exceptional situation (optional).
	 */
	public EjvmExecutionException(Reason reason, EjvmProcess process) {
		super(reason.toString());
		this.reason = reason;
		this.process = process;
	}

	/**
	 * <p>
	 * Creates a new exception.
	 * </p>
	 * 
	 * @param reason
	 *            The reason this exception was thrown.
	 * @param process
	 *            The {@link EjvmProcess} that caused an exceptional situation (optional).
	 * @param cause
	 *            The {@link Throwable} that caused this exception to be thrown.
	 */
	public EjvmExecutionException(Reason reason, EjvmProcess process, Throwable cause) {
		super(reason.toString(), cause);
		this.reason = reason;
		this.process = process;
	}

	/**
	 * <p>
	 * Returns the reason for the exception.
	 * </p>
	 * 
	 * @return The {@link Reason} for the exception to be thrown.
	 */
	public Reason getReason() {
		return reason;
	}

	/**
	 * <p>
	 * Returns the process that caused the exceptional situation.
	 * </p>
	 * 
	 * @return The process that caused this exception. May be {@code null}.
	 * 
	 */
	public EjvmProcess getProcess() {
		return this.process;
	}

	/**
	 * <p>
	 * This enum contains all reasons why an {@link EjvmExecutionException} may have been thrown.
	 * </p>
	 * 
	 * @author Sebastian Nicolai Kaupe
	 * @since 1.0
	 */
	public static enum Reason {
		/**
		 * <p>
		 * The exception was thrown because the processor encountered an unknown OpCode.
		 * </p>
		 * 
		 * @since 1.0
		 */
		UNKNOWN_OPCODE,
		/**
		 * <p>
		 * The exception was thrown because the number or types of the arguments of the current instruction did not
		 * match those expected.
		 * </p>
		 * 
		 * @since 1.0
		 */
		WRONG_ARG_TYPE,
		/**
		 * <p>
		 * The exception was thrown because the number of working values on the stack was insufficient for the execution
		 * of the current exception.
		 * </p>
		 * 
		 * @since 1.0
		 */
		NOT_ENOUGH_WORKING_VALUES;
	}
}