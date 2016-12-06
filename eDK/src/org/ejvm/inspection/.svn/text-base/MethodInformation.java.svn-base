package org.ejvm.inspection;

import java.nio.charset.Charset;
import java.util.Arrays;

import org.ejvm.util.ByteUtil;

/**
 * Encapsulates all information available about a method of an {@link EjvmProgram}.
 * 
 * @author Sebastian Nicolai Kaupe
 * @since 1.0
 */
public final class MethodInformation {

	private final int index;
	private final int methodAddress;
	private final int parameterCount;
	private final int localVariableCount;
	private final int debugInformationAddress;
	private final int methodNameSize;
	private final int compiledMethodSize;
	private final String methodName;

	/**
	 * <p>
	 * Creates a new {@code MethodInformation} object for the method which's information are passed as parameters.
	 * </p>
	 * 
	 * @param index
	 *            The index of the method in the executable's method table.
	 * @param mtblEntry
	 *            The entry for the method from the executable's method table.
	 * @param dbgInfo
	 *            The debug information for the extracted method. If no debug information were compiled into the
	 *            executable file, {@code null} or an empty array is expected.
	 */
	public MethodInformation(final int index, final byte[] mtblEntry, final byte[] dbgInfo) {
		this.index = index;
		methodAddress = ByteUtil.intFromBytes(mtblEntry, 0);
		parameterCount = mtblEntry[4];
		localVariableCount = mtblEntry[5];
		debugInformationAddress = ByteUtil.intFromBytes(mtblEntry, 6);
		if (dbgInfo != null && dbgInfo.length != 0 && debugInformationAddress != 0) {
			methodNameSize = dbgInfo[0];
			compiledMethodSize = ByteUtil.shortFromBytes(dbgInfo, 1);
			methodName = new String(Arrays.copyOfRange(dbgInfo, 3, dbgInfo.length), Charset.forName("UTF-16BE"));
		} else {
			methodNameSize = -1;
			compiledMethodSize = -1;
			methodName = "";
		}
	}

	/**
	 * <p>
	 * Returns the index of the method in the executable's method table.
	 * </p>
	 * 
	 * @return The index of the method in the method table.
	 */
	public int getMethodIndex() {
		return index;
	}

	/**
	 * <p>
	 * Returns the address of the first instruction of the method.
	 * </p>
	 * 
	 * @return The address of the first instruction of the method.
	 */
	public int getMethodAddress() {
		return methodAddress;
	}

	/**
	 * <p>
	 * Returns the number of parameters this method expects.
	 * </p>
	 * 
	 * @return The number of method parameters.
	 */
	public int getParameterCount() {
		return parameterCount;
	}

	/**
	 * <p>
	 * Returns the number of local variables the method declares.
	 * </p>
	 * 
	 * @return The number of local variables.
	 */
	public int getLocalVariableCount() {
		return localVariableCount;
	}

	/**
	 * <p>
	 * Returns the address of the debug information black for the method. If the executable was compiled without debug
	 * information, this method will return 0.
	 * </p>
	 * 
	 * @return The address of the debug information or 0 if no such exist.
	 */
	public int getDebugInformationAddress() {
		return debugInformationAddress;
	}

	/**
	 * <p>
	 * Returns the size of the method name (in byte).
	 * </p>
	 * <p>
	 * This is an optional information; if the executable was compiled omitting debug information, this method will
	 * return {@code -1}.
	 * </p>
	 * 
	 * @return The size of the name in byte or {@code 0}.
	 */
	public int getMethodNameSize() {
		return methodNameSize;
	}

	/**
	 * Returns the size of the compiled instructions belonging to this method.</p>
	 * <p>
	 * This is an optional information; if the executable was compiled omitting debug information, this method will
	 * return {@code -1}.
	 * 
	 * @return The byte size of the compiled method code.
	 */
	public int getCompiledMethodSize() {
		return compiledMethodSize;
	}

	/**
	 * <p>
	 * Returns the name of the method. A method name may contain up to 128 characters (but only a maximum of 256
	 * UTF-16BE-encoded byte).
	 * </p>
	 * <p>
	 * This is an optional information;if the executable was compiled omitting debug information, this method will
	 * return an empty String.
	 * </p>
	 * 
	 * @return The name of the method.
	 */
	public String getMethodName() {
		return methodName;
	}
}