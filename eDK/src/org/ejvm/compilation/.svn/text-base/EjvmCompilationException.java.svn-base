package org.ejvm.compilation;

/**
 * <p>
 * Exception class to be thrown when the {@link EjvmCompiler} encounters erroneous code during compilation, such as
 * references to undefined variables.
 * </p>
 * 
 * @author Sebastian Nicolai Kaupe
 */
public class EjvmCompilationException extends RuntimeException {

	private static final long serialVersionUID = 5013251205536637580L;

	private final Reason reason;

	/**
	 * <p>
	 * Creates a new Exception.
	 * </p>
	 * 
	 * @param reason
	 *            The reason this exception was thrown.
	 * @param additionalInfo
	 *            Additional information about the cause of the exception. May be formatted into the message.
	 */
	public EjvmCompilationException(Reason reason, Object... additionalInfo) {
		super(String.format(reason.toString(), additionalInfo));
		this.reason = reason;
	}

	/**
	 * <p>
	 * Creates a new exception.
	 * </p>
	 * 
	 * @param reason
	 *            The reason this exception was thrown.
	 * @param cause
	 *            The {@link Throwable} that caused this exception to be thrown.
	 * @param additionalInfo
	 *            Additional information about the cause of the exception. May be formatted into the message.
	 */
	public EjvmCompilationException(Reason reason, Throwable cause, Object... additionalInfo) {
		super(String.format(reason.toString(), additionalInfo), cause);
		this.reason = reason;
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
	 * This enum contains all reasons why an {@link EjvmCompilationException} may have been thrown.
	 * </p>
	 * 
	 * @author Sebastian Nicolai Kaupe
	 * @since 1.0
	 */
	public static enum Reason {

		/**
		 * <p>
		 * The exception was thrown because the compiler found two constants of the same name. Accepts constant name as
		 * additional information.
		 * </p>
		 * 
		 * @since 1.0
		 */
		DUPLICATE_CONSTANT("Duplicate constant name: %s"),
		/**
		 * <p>
		 * The exception was thrown because the compiler found a reference to a constant not in the constant table.
		 * Accepts constant name and method name as additional information.
		 * </p>
		 * 
		 * @since 1.0
		 */
		NO_SUCH_CONSTANT("Undefined constant %s referenced in method %s"),
		/**
		 * <p>
		 * The exception was thrown because the compiler found two error messages of the same name. Accepts error name
		 * as additional information.
		 * </p>
		 * 
		 * @since 1.0
		 */
		DUPLICATE_ERROR("Duplicate error message name: %s"),
		/**
		 * <p>
		 * The exception was thrown because the compiler found a reference to an error message not in the error message
		 * table. Accepts error name and method name as additional information.
		 * </p>
		 * 
		 * @since 1.0
		 */
		NO_SUCH_ERROR("Undefined error message %s referenced in method %s"),
		/**
		 * <p>
		 * The exception was thrown because the compiler found two methods of the same name. Accepts method name as
		 * additional information.
		 * </p>
		 * 
		 * @since 1.0
		 */
		DUPLICATE_METHOD_NAME("Duplicate method name: %s"),
		/**
		 * <p>
		 * The exception was thrown because the compiler did not find a main method.
		 * </p>
		 * 
		 * @since 1.0
		 */
		NO_MAIN_METHOD("No main method"),
		/**
		 * <p>
		 * The exception was thrown because the compiler did not find a method referenced in an invokation. Accepts the
		 * names of the invoking and the invoked methods as additional information.
		 * </p>
		 * 
		 * @since 1.0
		 */
		NO_SUCH_METHOD("Undefined method invoked in method %s: %s"),
		/**
		 * <p>
		 * The exception was thrown because the compiler encountered a method without code. Accepts method name as
		 * additional information.
		 * </p>
		 * 
		 * @since 1.0
		 */
		EMPTY_METHOD("Method %s contains no code"),
		/**
		 * <p>
		 * The exception was thrown because the compiler found a method longer than the maximum method size (65535
		 * byte). Accepts method name as additional information.
		 * </p>
		 * 
		 * @since 1.0
		 */
		TOO_LONG_METHOD("Method %s is too long (over 65535 byte)"),
		/**
		 * <p>
		 * The exception was thrown because the compiler was unable to find some kind of return statement in a method.
		 * Accepts the method's name as additional argument.
		 * </p>
		 * 
		 * @since 1.0
		 */
		NO_RETURN_STMT("Method %s does not contain a RETURN or IRETURN instruction"),
		/**
		 * <p>
		 * The exception was thrown because the compiler encountered an unknown instruction. Accepts the method's name
		 * and the unknown instruction's name as additional information.
		 * </p>
		 * 
		 * @since 1.0
		 */
		UNKNOWN_INSTRUCTION("Unknown instruction in method %s: %s"),
		/**
		 * <p>
		 * The exception was thrown because the compiler found to many arguments for an instruction. Accepts the
		 * instruction's name and the number of arguments found (so far) as additional information.
		 * </p>
		 * 
		 * @since 1.0
		 */
		TOO_MANY_ARGS("Too many arguments for instruction %s: %d"),
		/**
		 * <p>
		 * The exception was thrown because the compiler found not enough arguments for an instruction. Accepts the
		 * instruction's name and the number of arguments it expects as well as the number of arguments found as
		 * additional information.
		 * </p>
		 * 
		 * @since 1.0
		 */
		NOT_ENOUGH_ARGS("Insufficient arguments: %s takes %d arguments. Found: %d"),
		/**
		 * <p>
		 * The exception was thrown because the compiler found not an argument which's type did not match the type
		 * expected. Accepts the instruction's name, the name of the expected type and the actual value found by the
		 * compiler as additional information.
		 * </p>
		 * 
		 * @since 1.0
		 */
		WRONG_ARG("Wrong argument for instruction %s: Expected %s, found '%s'"),
		/**
		 * <p>
		 * The exception was thrown because the compiler found two label of the same name inside the same method.
		 * Accepts the methods's name and the label as additional information.
		 * </p>
		 * 
		 * @since 1.0
		 */
		DUPLICATE_LABEL("Duplicate label found in method %s: %s"),
		/**
		 * <p>
		 * The exception was thrown because the compiler was unable to find a label referenced in the code. Accepts the
		 * methods's name and the missing label as additional information.
		 * </p>
		 * 
		 * @since 1.0
		 */
		NO_SUCH_LABEL("No such label in method %s: %s"),
		/**
		 * <p>
		 * The exception was thrown because the compiler was unable to find a parameter or local variable referenced in
		 * the code. Accepts the methods's name and the missing variable's name as additional information.
		 * </p>
		 * 
		 * @since 1.0
		 */
		NO_SUCH_VARIABLE("Undefined variable in method %s: %s"),
		/**
		 * <p>
		 * The exception was thrown because the compiler encountered a SETOUT instruction with an output mode not known
		 * to it. Accepts the name of the undefined output format as additional information.
		 * </p>
		 * 
		 * @since 1.0
		 */
		NO_SUCH_OUTPUT_FORMAT("Undefined output format: %s");

		private String msg;

		private Reason(String msg) {
			this.msg = msg;
		}

		@Override
		public String toString() {
			return msg == null ? super.toString() : msg;
		}
	}

}
