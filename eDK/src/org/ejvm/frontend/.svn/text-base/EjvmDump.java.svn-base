package org.ejvm.frontend;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.ejvm.execution.EjvmInstruction;
import org.ejvm.inspection.EjvmProgram;
import org.ejvm.inspection.MethodInformation;
import org.ejvm.util.ArrayUtil;
import org.ejvm.util.EjvmVersion;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * <p>
 * Command line frontend that provides a way to dump information about an eJVM
 * executable file.
 * </p>
 * 
 * @author Sebastian Nicolai Kaupe
 * @since 1.0
 */
public class EjvmDump {

	/**
	 * <p>
	 * Replacement string for unknown information (mostly the method names in
	 * executables compiled without debug information).
	 * </p>
	 */
	private static final String UNKNOWN = "???";

	/**
	 * <p>
	 * If {@code true}, do not add prefixes to instruction arguments in
	 * decompiled code denoting their type (e.g. a 'v' for a local variable or a
	 * 'c' for a constant).
	 * </p>
	 */
	@Option(name = "--no-type-prefixes", usage = "Removes type prefixes from the output.")
	private boolean noTypePrefixes = false;

	/**
	 * <p>
	 * If {@code true}, do not substitute the name of a method (if available)
	 * for its method table index while dumping method code.
	 * </p>
	 */
	@Option(name = "--no-method-name-substitution", usage = "Do not replace method table index of INVOKEVIRTUAL.")
	private boolean noMethodNameSubstitution = false;

	/**
	 * <p>
	 * If {@code true}, do not substitute the target address of a jump for the
	 * offset that is actually provided as argument to the instruction.
	 * </p>
	 */
	@Option(name = "--no-offset-substitution", usage = "Do not replace jump offsets with target addresses.")
	private boolean noOffsetSubstitution = false;

	@Option(name = "-v", aliases = "--version", usage = "Displays the version and exits.", help = true)
	private boolean showVersion = false;

	@Argument(metaVar = "FILE", required = true)
	private String filename;

	/**
	 * <p>
	 * {@link Set} containing all jump instructions working with
	 * addresses/offsets to change the PC instead of an index or no argument at
	 * all.
	 * </p>
	 */
	private final Set<EjvmInstruction> jumpInstructions = EnumSet.of(EjvmInstruction.GOTO, EjvmInstruction.IFEQ,
			EjvmInstruction.IFLT, EjvmInstruction.IF_ICMPEQ);

	public static void main(String[] args) {
		EjvmDump dumper = new EjvmDump();
		CmdLineParser parser = new CmdLineParser(dumper);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("eJVMdump executable [options...]");
			parser.printUsage(System.err);
			return;
		}
		dumper.dump();
	}

	private void dump() {
		if (showVersion) {
			System.out.println("eJVM version " + EjvmVersion.currentVersion().toString());
			return;
		}
		Path executable = Paths.get(filename);
		EjvmProgram program = EjvmProgram.fromFile(executable);
		System.out.printf("Program Name: %s (%d bytes at address 0x%08X)%n", program.getProgramName(),
				program.getProgramNameLength(), program.getProgramNameOffset());
		System.out.println("Required eJVM Version: " + EjvmVersion.asString(program.getRequiredEjvmVersion()));
		printMethodTable(program);
		printConstantTable(program);
		printErrorMessageTable(program);
		System.out.println("Method dumps:");
		dumpMethods(program);
	}

	private void printMethodTable(final EjvmProgram program) {
		int longestMethodNameSize = 0;
		List<MethodInformation> methodInformations = program.getMethodInformation();
		for (MethodInformation mi: methodInformations) {
			String name = mi.getMethodNameSize() != -1 ? mi.getMethodName() : UNKNOWN;
			if (name.length() > longestMethodNameSize)
				longestMethodNameSize = name.length();
		}
		System.out.println("Method table:");
		for (MethodInformation mi: methodInformations) {
			String name = mi.getMethodNameSize() != -1 ? mi.getMethodName() : UNKNOWN;
			System.out.printf(" %3d: %" + longestMethodNameSize + "s@0x%08X  %2d parameters  %2d local variables",
					mi.getMethodIndex(), name, mi.getMethodAddress(), mi.getParameterCount(),
					mi.getLocalVariableCount());
			if (mi.getCompiledMethodSize() != -1)
				System.out.printf("  %5d bytes length%n", mi.getCompiledMethodSize());
			else
				System.out.println();
		}
	}

	private void printConstantTable(final EjvmProgram program) {
		int numberOfConstants = program.getNumberOfConstants();
		if (numberOfConstants > 0) {
			System.out.println("Constant table:");
			for (int i = 0; i < numberOfConstants; i++) {
				System.out.printf(" %3d: %5d (0x%04X)%n", i, program.getConstant(i), program.getConstant(i));
			}
		}
	}

	private void printErrorMessageTable(final EjvmProgram program) {
		int numberOfErrorMessages = program.getNumberOfErrorMessages();
		if (numberOfErrorMessages > 0) {
			System.out.printf("Error Message Table (Start at 0x%08X):%n", program.getErrorMessageTableOffset());
			for (int i = 0; i < numberOfErrorMessages; i++) {
				System.out.printf(" %3d: %s%n", i, program.getErrorMessage(i));
			}
		}
	}

	private final void dumpMethods(final EjvmProgram program) {
		List<MethodInformation> methods = program.getMethodInformation();
		ByteBuffer programBytes = program.getByteView();
		/*
		 * We cannot be sure that debug information have been compiled into the
		 * executable, so we cannot rely on the methodLength field of the
		 * MethodInformation object. Instead, we'll put all starting addresses
		 * into an array and later make sure that we only iterate over
		 * instructions until we hit the starting address of an other method. We
		 * add the address of the first debug information block or, if no debug
		 * information are available, of the program name to the end of the
		 * array to make sure we know when to stop reading the instructions for
		 * the last method in the method table.
		 */
		int lastAddress = program.getMethodInformation(0).getDebugInformationAddress();
		if (lastAddress == 0) {
			lastAddress = program.getProgramNameOffset();
		}
		Integer[] methodAddresses = new Integer[methods.size() + 1];
		for (int i = 0; i < methodAddresses.length - 1; i++) {
			methodAddresses[i] = methods.get(i).getMethodAddress();
		}
		methodAddresses[methodAddresses.length - 1] = lastAddress;
		for (MethodInformation mi: methods) {
			int index = 0;
			int skippedBytes = 0;
			System.out.print(" " + (mi.getMethodName().isEmpty() ? UNKNOWN : mi.getMethodName()) + "(");
			for (int i = 0; i < mi.getParameterCount(); i++) {
				if (i != 0)
					System.out.print(", "); // Only print comma if it is not the
											// first parameter.
				System.out.print("p_" + i);
			}
			System.out.println("):");
			programBytes.position(mi.getMethodAddress());
			do {
				int oldSkippedBytes = skippedBytes;
				// Contains string representation of the bytes for the
				// instruction and its arguments.
				StringBuilder byteStringBuilder = new StringBuilder();
				EjvmInstruction instruction = EjvmInstruction.forOpcode(programBytes.get());
				byteStringBuilder.append(String.format("0x%2X", instruction.getOpCode()));
				EjvmInstruction.ArgumentType[] argTypes = instruction.getArgumentTypes();
				Number[] arguments = new Number[argTypes.length];
				for (int i = 0; i < argTypes.length; i++) {
					byte[] bytes = new byte[argTypes[i].getArgumentSize()];
					programBytes.get(bytes);
					arguments[i] = argTypes[i].fromBytes(bytes);
					skippedBytes += argTypes[i].getArgumentSize();
					for (byte b: bytes) {
						byteStringBuilder.append(String.format(" 0x%02X", b));
					}
				}
				System.out.printf("  %4d (0x%08X):   %-19s   %s", index, mi.getMethodAddress() + index
						+ oldSkippedBytes, byteStringBuilder.toString(), instruction.name());
				for (int i = 0; i < arguments.length; i++) {
					Number n = arguments[i];
					/*
					 * The type prefix denotes the type of an index argument and
					 * is used to make it more easy to distinguish local
					 * variables and method parameters by adding a 'p' or 'v' as
					 * prefix to their printed-out value. Additionally, global
					 * variable indexes are prefixed with a 'c'.
					 */
					String typePrefix = "";
					if (!noTypePrefixes) {
						switch (instruction) {
						case LDC:
							typePrefix = "c_";
							break;
						case ISTORE: // Fall through.
						case ILOAD:
							typePrefix = isParameterIndex(n.shortValue(), mi) ? "p_" : "v_";
							break;
						case IINC:
							// Only prefix the first argument, not the second.
							if (i == 0)
								typePrefix = isParameterIndex(n.shortValue(), mi) ? "p_" : "v_";
							break;
						case ERR:
							typePrefix = "e_";
						default:
							break; // Empty block to avoid warning - we do not
									// need a complete switch here.
						}
					}
					if (jumpInstructions.contains(instruction) && !noOffsetSubstitution) {
						// Substitute target address for the offset provided as
						// argument.
						System.out.printf(" " + typePrefix + "0x%08X", mi.getMethodAddress() + index + skippedBytes + 1
								+ n.shortValue());
					} else if (instruction == EjvmInstruction.INVOKEVIRTUAL && !noMethodNameSubstitution) {
						// Try to substitute method table index with method
						// name.
						MethodInformation invokedMethod = program.getMethodInformation(n.intValue());
						if (!invokedMethod.getMethodName().isEmpty()) {
							System.out.printf(" " + invokedMethod.getMethodName());
						} else {
							// No method name included, just print the method
							// table index.
							System.out.printf(" " + typePrefix + n.shortValue());
						}
					} else {
						System.out.printf(" " + typePrefix + n.shortValue());
						if (instruction == EjvmInstruction.BIPUSH) {
							// Add char representation of argument as we can
							// also push chars on the stack.
							char charRepresentation = (char) n.shortValue();
							if (Character.isISOControl(charRepresentation)) {
								String escapeSequence = "�";
								switch (charRepresentation) {
								case '\0':
									escapeSequence = "\\0";
									break;
								case '\b':
									escapeSequence = "\\b";
									break;
								case '\f':
									escapeSequence = "\\f";
									break;
								case '\n':
									escapeSequence = "\\n";
									break;
								case '\r':
									escapeSequence = "\\r";
									break;
								case '\t':
									escapeSequence = "\\t";
									break;
								}
								System.out.printf(" (\'%s\')", escapeSequence);
							} else {
								System.out.printf(" (\'%c\')", charRepresentation);
							}
						}
					}
				}
				System.out.println();
				index++;
			} while (ArrayUtil.indexOf(methodAddresses, (mi.getMethodAddress() + index + skippedBytes)) == -1);
		}
	}

	/**
	 * Decides if the variable behind {@code index} is a method parameter or
	 * local variable.
	 * 
	 * @param index
	 *            The variable index in the stack frame.
	 * @param method
	 *            The method.
	 * @return {@code true} if the variable at {@code index} in the stack frame
	 *         is a parameter of {@code method}, otherwise {@code false}.
	 */
	private static boolean isParameterIndex(final int index, final MethodInformation method) {
		// Works because this implementation stores parameters always at the
		// lowest indexes in the stack frame.
		return index < method.getParameterCount();
	}
}