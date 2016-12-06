package org.ejvm.compilation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.ejvm.antlr.eJVMBaseListener;
import org.ejvm.antlr.eJVMLexer;
import org.ejvm.antlr.eJVMParser;
import org.ejvm.antlr.eJVMParser.ArgumentContext;
import org.ejvm.antlr.eJVMParser.CodeLineContext;
import org.ejvm.antlr.eJVMParser.ConstantDefinitionContext;
import org.ejvm.antlr.eJVMParser.ErrorMessageDefinitionContext;
import org.ejvm.antlr.eJVMParser.LocalVariablesSectionContext;
import org.ejvm.antlr.eJVMParser.MethodCodeContext;
import org.ejvm.antlr.eJVMParser.MethodContext;
import org.ejvm.antlr.eJVMParser.ParameterListContext;
import org.ejvm.antlr.eJVMParser.ProgramContext;
import org.ejvm.antlr.eJVMParser.ProgramNameContext;
import org.ejvm.compilation.EjvmCompilationException.Reason;
import org.ejvm.execution.EjvmInstruction;
import org.ejvm.execution.EjvmInstruction.ArgumentType;
import org.ejvm.execution.EjvmProcess;
import org.ejvm.inspection.EjvmProgram;
import org.ejvm.util.ByteUtil;
import org.ejvm.util.EjvmVersion;

/**
 * <p>
 * The {@code EjvmCompiler} provides methods to compile eJVM source code into an eJVM Executable File.
 * </p>
 * 
 * @author Sebastian Nicolai Kaupe
 * @since 1.0
 * @see #compile(InputStream, OutputStream)
 */
public class EjvmCompiler {

	private static final byte[] OFFSET_DUMMY = { 0x00, 0x00 };

	private boolean omitDebugInformation = false;

	private PrintStream messageOut = System.out;

	/**
	 * <p>
	 * Creates a new {@code EjvmCompiler}. Debug information will <em>not</em> be omitted. Compiler messages will be
	 * sent to {@link System#out}.
	 * </p>
	 */
	public EjvmCompiler() {
		this(false);
	}

	/**
	 * <p>
	 * Creates a new {@code EjvmCompiler}. Compiler messages will be sent to {@link System#out}.
	 * </p>
	 * 
	 * @param omitDebugInformation
	 *            The compiler will not include debug information in the executable if this is {@code true}.
	 */
	public EjvmCompiler(boolean omitDebugInformation) {
		this.omitDebugInformation = omitDebugInformation;
	}

	/**
	 * <p>
	 * Sets if the compiler will omit debug information from the executable compiled.
	 * </p>
	 * 
	 * @param omit
	 *            {@code true} if the compiler shall not include debug information.
	 */
	public void setOmitDebugInformation(boolean omit) {
		this.omitDebugInformation = omit;
	}

	/**
	 * <p>
	 * Returns {@code true} if the compiler will omit debug information from the executable compiled.
	 * </p>
	 * 
	 * @return {@code true} if the compiler will not compile debug information; otherwise {@code false}.
	 */
	public boolean omitsDebugInformation() {
		return this.omitDebugInformation;
	}

	/**
	 * <p>
	 * Replaces the {@link PrintStream} used by the compiler to write compiler messages to (e.g. warnings) with
	 * {@code msgOut}. By default, the compiler writes its messages to {@link System#out}.
	 * </p>
	 * 
	 * @param msgOut
	 *            The new output channel for compiler messages.
	 */
	public void setMessageOut(PrintStream msgOut) {
		this.messageOut = msgOut;
	}

	/**
	 * <p>
	 * Invoking this method will try to parse and compile the eJVM program available from {@code src}, with the compiled
	 * executable written to {@code out}.
	 * </p>
	 * <p>
	 * Compiler messages, such as warnings, will be written to the message output of this compiler (default:
	 * {@link System#out}). Use {@link #setMessageOut(PrintStream)} to replace this with another output channel.
	 * </p>
	 * <p>
	 * Invoking this method several times is possible without having to create a new {@link EjvmCompiler} instance.
	 * </p>
	 * 
	 * @param src
	 *            The {@link InputStream} to read the eJVM program from.
	 * @param out
	 *            The {@link OutputStream} into which the compiled executable will be written.
	 * @throws IOException
	 *             In case of any I/O-related error.
	 * @throws EjvmCompilationException
	 *             Thrown when the compiler encounters erroneous code.
	 */
	public void compile(InputStream src, OutputStream out) throws IOException, EjvmCompilationException {
		ANTLRInputStream in = new ANTLRInputStream(src);
		eJVMLexer lexer = new eJVMLexer(in);
		eJVMParser parser = new eJVMParser(new CommonTokenStream(lexer));
		ParseTree tree = parser.program();
		ParseTreeWalker walker = new ParseTreeWalker();
		try {
			walker.walk(new CompileListener(out, omitDebugInformation, messageOut), tree);
		} catch (RuntimeException e) {
			// Is it an IOException from CompileListener#exitProgram(ProgramContext)?
			if (e.getCause() != null && e.getCause() instanceof IOException) {
				// Re-throw the IOException - this way, the handler won't need to handle two different exceptions for
				// I/O-related errors.
				throw (IOException) e.getCause();
			} else {
				throw e;
			}
		}
	}

	/**
	 * <p>
	 * This private class realizes the actual compiler by acting as a listener for the ANTLR parser.
	 * </p>
	 * 
	 * @author Sebastian Nicolai Kaupe
	 * @since 1.0
	 */
	private static final class CompileListener extends eJVMBaseListener {

		// Buffer for the UTF-16BE-encoded bytes of the program's name.
		private byte[] nameBytes;
		// Field for the length of the name.
		// Deque for Method objects. The main method is stored at the first position (once found).
		private Deque<Method> methods;
		// The constant table. We use a LinkedHashMap to preserve the ordering of the constants.
		private List<Constant> constantTable;
		// Used to store error messages defined in the program.
		private List<ErrorMessage> errorMessageTable;

		// The method the compiler currently works on.
		private Method currentMethod;

		// The OutputStream to write the executable to.
		private OutputStream out;

		// Contains all eJVM instructions that fulfill the role of return statements.
		private final Set<EjvmInstruction> RETURN_INSTRUCTIONS = EnumSet.of(EjvmInstruction.RETURN,
				EjvmInstruction.IRETURN);

		// Decides whether or not to include debug information in the executable.
		private boolean omitDebugInformation;

		// Used to write compiler messages to, such as warnings.
		private PrintStream msgOut;

		public CompileListener(OutputStream out, boolean omitDebugInformation, PrintStream msgOut) {
			this.out = out;
			this.omitDebugInformation = omitDebugInformation;
			this.msgOut = msgOut;
		}

		/**
		 * <p>
		 * Prepares execution of the compiler by initializing all fields that need initialization.
		 * </p>
		 * 
		 * @param ctx
		 */
		@Override
		public void enterProgram(ProgramContext ctx) {
			// Initialize all data structured needed for compilation.
			// Do not initialize nameBytes - we do it once needed in #enterProgramName().
			methods = new LinkedList<>();
			constantTable = new ArrayList<>();
			errorMessageTable = new ArrayList<>();
		}

		/**
		 * <p>
		 * Retrieves the program name from the parser and truncates it to the maximal length allowed, if needed.
		 * </p>
		 * 
		 * @param ctx
		 */
		@Override
		public void enterProgramName(ProgramNameContext ctx) {
			byte[] name = ctx.getText().getBytes(Charset.forName("UTF-16BE"));
			if (name.length <= 255) {
				this.nameBytes = name;
			} else {
				this.nameBytes = Arrays.copyOf(name, 255);
				msgOut.println("Warning: Too long program name. Abbreviated to "
						+ new String(nameBytes, Charset.forName("UTF-16BE")));
			}
		}

		/**
		 * <p>
		 * Adds another constant to the constant table for this eJVM program.
		 * </p>
		 * 
		 * @param ctx
		 */
		@Override
		public void enterConstantDefinition(ConstantDefinitionContext ctx) {
			String name = ctx.identifier().getText();
			Short value;
			if (ctx.numberLiteral().hexLiteral() != null) {
				value = Short.parseShort(ctx.numberLiteral().hexLiteral().getText().replace("0x", ""), 16);
			} else if (ctx.numberLiteral().decimalLiteral() != null) {
				value = Short.parseShort(ctx.numberLiteral().decimalLiteral().getText(), 10);
			} else {
				// We should never be able to reach this if the parser does its job right.
				throw new AssertionError();
			}
			Constant c = new Constant(name, value.shortValue());
			if (constantTable.contains(c)) {
				throw new EjvmCompilationException(Reason.DUPLICATE_CONSTANT, name);
			}
			constantTable.add(c);
		}

		/**
		 * <p>
		 * Adds another error message to the error message table for this eJVM program.
		 * </p>
		 * 
		 * @param ctx
		 */
		@Override
		public void enterErrorMessageDefinition(ErrorMessageDefinitionContext ctx) {
			String name = ctx.identifier().getText();
			byte[] msg = ctx.string().getText().replace("\"", "").getBytes(Charset.forName("UTF-16BE"));
			if (msg.length > 255) {
				msg = Arrays.copyOf(msg, 255);
				// TODO Find a better way to produce output.
				msgOut.printf("Warning: Too long error message %s. Abbreviated to %s", name, new String(nameBytes,
						Charset.forName("UTF-16BE")));
			}
			ErrorMessage e = new ErrorMessage(name, msg);
			if (errorMessageTable.contains(e)) {
				throw new EjvmCompilationException(Reason.DUPLICATE_ERROR, name);
			}
			errorMessageTable.add(e);
		}

		/**
		 * <p>
		 * Prepares a method for compilation by retrieving information about its name, parameters and local variables.
		 * </p>
		 * 
		 * @param ctx
		 * @throws EjvmCompilationException
		 *             If there is already a method of the same name.
		 */
		@Override
		public void enterMethod(MethodContext ctx) throws RuntimeException {
			this.currentMethod = new Method();
			currentMethod.name = ctx.identifier().getText();
			// Is there already another method of the same name?
			if (methods.contains(currentMethod)) {
				throw new EjvmCompilationException(Reason.DUPLICATE_METHOD_NAME, currentMethod.name);
			}
			// Check for and process parameters and local variables.
			if (ctx.parameterList() != null) {
				// We have a list of method parameters, count them and put their names into the localValues list.
				ParameterListContext list = ctx.parameterList();
				int paramCnt = 0;
				while (list.identifier(paramCnt) != null) {
					currentMethod.localValues.add(list.identifier(paramCnt).getText());
					paramCnt++;
				}
				currentMethod.parameterCount = paramCnt;
			}
			if (ctx.localVariablesSection() != null) {
				// We have local variables. Count and store them, too.
				LocalVariablesSectionContext list = ctx.localVariablesSection();
				int varCnt = 0;
				while (list.localVariableDefinition(varCnt) != null) {
					currentMethod.localValues.add(list.localVariableDefinition(varCnt).identifier().getText());
					varCnt++;
				}
				currentMethod.localVariableCount = varCnt;
			}
			// Compiling is done in #enterMethodCode (except for resolving jumps, which is done in #exitMethod).
		}

		/**
		 * <p>
		 * Compiles the code of the current method by looping through all code lines and compiling them into their byte
		 * representation. Jumps and invokations are <em>not</em> resolved by this method. Resolving jumps inside the
		 * method is done in {@link #exitMethod(MethodContext)}, while invocations of other methods are resolved in
		 * {@link #exitProgram(ProgramContext)}.
		 * </p>
		 * 
		 * @param code
		 * @throws RuntimeException
		 *             If an I/O error occurs when the executable is written to the output stream.
		 * @throws EjvmCompilationException
		 *             If the compiler encounters erroneous code, such as unkown instructions, wrong parameters or
		 *             duplicate labels.
		 */
		@Override
		public void enterMethodCode(MethodCodeContext code) {
			// ByteArrayOutputStream to store the compiled code. We convert into byte[] before resolving addresses.
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			// bytesWritten counts the number of bytes written so far. This is important when we resolve jumps and
			// method invocations later on.
			int bytesWritten = 0;
			// codeLine is the index of the code line currently being compiled.
			int codeLine = 0;
			// foundReturnStatement will be set to true once a RETURN or IRETURN instruction is found, thus enabling us
			// to check if every method contains such a statement once all code lines are compiled.
			boolean foundReturnStatement = false;
			while (code.codeLine(codeLine) != null) {
				CodeLineContext line = code.codeLine(codeLine);
				if (line.label() != null) {
					// Add the label to the map of labels/offsets (if not already there).
					if (currentMethod.labelsToOffset.containsKey(line.label().identifier().getText())) {
						throw new EjvmCompilationException(Reason.DUPLICATE_LABEL, currentMethod.name, line.label()
								.getText());
					}
					currentMethod.labelsToOffset.put(line.label().identifier().getText(), bytesWritten);
				}
				// Retrieve instruction from the code line.
				EjvmInstruction instruction;
				try {
					instruction = EjvmInstruction.valueOf(line.instruction().identifier().getText().toUpperCase());
				} catch (IllegalArgumentException e) {
					throw new EjvmCompilationException(Reason.UNKNOWN_INSTRUCTION, currentMethod.name, line
							.instruction().getText());
				}
				// Check if the instruction is some kind of RETURN statement.
				if (RETURN_INSTRUCTIONS.contains(instruction)) {
					foundReturnStatement = true;
				}
				bytes.write(instruction.getOpCode());
				bytesWritten++;
				// Retrieve the arguments from the code line.
				int argumentCnt = 0;
				while (line.argument(argumentCnt) != null) {
					ArgumentType[] argTypes = instruction.getArgumentTypes();
					if (argumentCnt >= argTypes.length) {
						throw new EjvmCompilationException(Reason.TOO_MANY_ARGS, instruction.name(), argumentCnt + 1);
					}
					ArgumentContext arg = line.argument(argumentCnt);
					if (argTypes[argumentCnt] == ArgumentType.NUMBER_LITERAL) {
						// We have a numerical argument, simply convert it and write it to the output.
						short value;
						if (arg.numberLiteral() == null) {
							throw new EjvmCompilationException(Reason.WRONG_ARG, instruction.name(),
									argTypes[argumentCnt].name(), arg.getText());
						} else if (arg.numberLiteral().hexLiteral() != null) {
							String str = arg.numberLiteral().hexLiteral().getText();
							value = Short.parseShort(str.replace("0x", ""), 16);
						} else if (arg.numberLiteral().decimalLiteral() != null) {
							value = Short.parseShort(arg.numberLiteral().decimalLiteral().getText(), 10);
						} else {
							// We should never be able to reach this if the parser does its job right.
							throw new AssertionError();
						}
						bytes.write(ByteUtil.shortToBytes(value), 0, 2);
						bytesWritten += 2;
					} else if (argTypes[argumentCnt] == ArgumentType.OFFSET) {
						/*
						 * This argument is an offset - add it to the list of offsets that need resolving (after
						 * checking that it is indeed an offset) and write two zero-bytes as a dummy into the output.
						 * Those will be replaced with the actual jump offset during he resolving phase of this method's
						 * compilation.
						 */
						if (arg.identifier() == null) {
							// Our target label's name is not a name.
							throw new EjvmCompilationException(Reason.WRONG_ARG, instruction.name(),
									argTypes[argumentCnt].name(), arg.getText());
						}
						String target = arg.identifier().getText();
						currentMethod.jumpsToResolve.add(new Resolvable(target, bytesWritten));
						bytes.write(OFFSET_DUMMY, 0, 2);
						bytesWritten += 2;
					} else if (argTypes[argumentCnt] == ArgumentType.INDEX) {
						/*
						 * This argument is a table index. If it belongs to an INVOKEVIRTUAL instruction, we process it
						 * like a jump offset by writing out a dummy and adding it to a list of invoked methods (because
						 * the method table does not yet exist). If it is a LDC instruction, we lookup the correct index
						 * in the constant table. For an ERR instruction, the error message table is searched through.
						 * Otherwise, we search for it in the table for parameters and local variables.
						 */
						if (arg.identifier() == null) {
							throw new EjvmCompilationException(Reason.WRONG_ARG, instruction.name(),
									argTypes[argumentCnt].name(), arg.getText());
						}
						String name = arg.identifier().getText();
						if (instruction == EjvmInstruction.INVOKEVIRTUAL) {
							currentMethod.invokationsToResolve.add(new Resolvable(name, bytesWritten));
							bytes.write(0x00);
							bytesWritten++;
						} else if (instruction == EjvmInstruction.LDC) {
							for (int i = 0; i < constantTable.size(); i++) {
								Constant c = constantTable.get(i);
								if (c.name.equals(name)) {
									bytes.write(i);
									bytesWritten++;
									break;
								}
								if (i == constantTable.size() - 1) {
									// The constant is not in the constant table.
									throw new EjvmCompilationException(Reason.NO_SUCH_CONSTANT, name,
											currentMethod.name);
								}
							}
						} else if (instruction == EjvmInstruction.ERR) {
							for (int i = 0; i < errorMessageTable.size(); i++) {
								ErrorMessage e = errorMessageTable.get(i);
								if (e.name.equals(name)) {
									bytes.write(i);
									bytesWritten++;
									break;
								}
								if (i == errorMessageTable.size() - 1) {
									// The constant is not in the constant table.
									throw new EjvmCompilationException(Reason.NO_SUCH_ERROR, name, currentMethod.name);
								}
							}
						} else if (instruction == EjvmInstruction.SETOUT) {
							// Retrieve and write out the mode byte for the requested output format.
							String modeName = arg.identifier().getText();
							try {
								EjvmProcess.OutputFormatter formatter = EjvmProcess.OutputFormatter.valueOf(modeName);
								int modeByte = formatter.ordinal();
								bytes.write(modeByte);
								bytesWritten++;
							} catch (IllegalArgumentException e) {
								throw new EjvmCompilationException(Reason.NO_SUCH_OUTPUT_FORMAT, modeName);
							}
						} else {
							// It is a parameter or local variable.
							int index = currentMethod.localValues.indexOf(name);
							if (index == -1) {
								throw new EjvmCompilationException(Reason.NO_SUCH_VARIABLE, currentMethod.name, name);
							}
							bytes.write(index);
							bytesWritten++;
							if (bytesWritten > 65535) {
								// Method can't be longer than 2^16 - 1 byte, so we have to abort the compilation.
								throw new EjvmCompilationException(Reason.TOO_LONG_METHOD, currentMethod.name);
							}
						}
					}
					argumentCnt++;
				}
				if (argumentCnt < instruction.getArgumentTypes().length) {
					throw new EjvmCompilationException(Reason.NOT_ENOUGH_ARGS, instruction.name(),
							instruction.getArgumentTypes().length, argumentCnt);
				}
				codeLine++;
			}
			if (codeLine == 0) {
				throw new EjvmCompilationException(Reason.EMPTY_METHOD, currentMethod.name);
			}
			if (!foundReturnStatement) {
				throw new EjvmCompilationException(Reason.NO_RETURN_STMT, currentMethod.name);
			}
			currentMethod.compiledMethodSize = bytesWritten;
			currentMethod.compiledCode = bytes.toByteArray();
		}

		/**
		 * <p>
		 * Resolved jumps to labels inside of the method (invocations of other methods will only be resolved before the
		 * whole program is dumped into the output stream of this compiler) and adds the completed method to the list of
		 * methods.
		 * </p>
		 * 
		 * @param ctx
		 * @throws EjvmCompilationException
		 *             If a jump to a label can not be resolved because the target label does not exist.
		 */
		@Override
		public void exitMethod(MethodContext ctx) {
			// The code is compiled, but we still have to resolve the jump offsets.
			for (Resolvable r: currentMethod.jumpsToResolve) {
				Integer targetPosition = currentMethod.labelsToOffset.get(r.target);
				if (targetPosition == null) {
					throw new EjvmCompilationException(Reason.NO_SUCH_LABEL, currentMethod.name, r.target);
				}
				int offset = targetPosition - (r.position + 2); // Add 2 to compensate for argument length.
				currentMethod.compiledCode[r.position] = (byte) ((offset & 0xFF00) >> 8);
				currentMethod.compiledCode[r.position + 1] = (byte) (offset & 0xFF);
			}
			// We are done, put it into the deque.
			if (currentMethod.name.equals("main")) {
				// It's the main method, put it into the first position.
				methods.addFirst(currentMethod);
			} else {
				methods.addLast(currentMethod);
			}
			// Null class fields to provoke a NPE if anything goes wrong.
			this.currentMethod = null;
		}

		/**
		 * <p>
		 * Does some final checks, resolves invocations of methods and then dumps the whole executable into the output
		 * stream for this compiler.
		 * </p>
		 * 
		 * @param ctx
		 */
		@Override
		public void exitProgram(ProgramContext ctx) {
			// Check: Is there a main method?
			if (methods.size() == 0 || !methods.peekFirst().name.equals("main")) {
				throw new EjvmCompilationException(Reason.NO_MAIN_METHOD);
			}
			// Resolve method indices.
			for (Method method: methods) {
				for (Resolvable r: method.invokationsToResolve) {
					boolean found = false;
					int i = 0;
					for (Method m: methods) {
						if (m.name.equals(r.target)) {
							method.compiledCode[r.position] = (byte) i;
							found = true;
						}
						i++;
					}
					if (!found) {
						throw new EjvmCompilationException(Reason.NO_SUCH_METHOD, method.name, r.target);
					}
				}
			}
			try {
				// Calculate the offsets we definitely need.
				//@formatter:off
				/*
				 * 6: # of bytes written before (magic number, version, program name size).
				 * 4: # of bytes for program name offset.
				 * 1: # of bytes for method count.
				 * 1: # of bytes for constant count.
				 * 1: # of bytes for error message count.
				 */
				//@formatter:on
				int methodTableOffset = 6 + 4 + 1 + 1 + 1;
				// methods.size() * MTBL_ENTRY_SIZE: # of bytes needed for the method table.
				int constantTableOffset = methodTableOffset + (methods.size() * EjvmProgram.MTBL_ENTRY_SIZE);
				// constantTable.size() * 2: # of bytes needed for the constant table.
				int textSectionOffset = constantTableOffset + (constantTable.size() * 2);
				int textSectionSize = 0;
				int debugSectionSize = 0;
				for (Method method: methods) {
					textSectionSize += method.compiledMethodSize;
					if (!this.omitDebugInformation) {
						int nameLength = method.name.getBytes("UTF-16BE").length;
						if (nameLength > 255)
							nameLength = 255;
						debugSectionSize += 3 + nameLength; // 3 for dbgpool preamble (mnsize + msize).
					}
				}
				int errorTableSize = 0;
				for (ErrorMessage e: errorMessageTable) {
					errorTableSize += 1 + e.msg.length; // 1 for size byte before actual message.
				}
				int programNameOffset = textSectionOffset + textSectionSize + debugSectionSize + errorTableSize;
				// Start writing with the preamble.
				out.write(EjvmProgram.MAGIC_NUMBER);
				out.write(EjvmVersion.currentVersion().getVersionByte());
				out.write(this.nameBytes.length);
				out.write(ByteUtil.intToBytes(programNameOffset));
				out.write(methods.size());
				out.write(constantTable.size());
				out.write(errorMessageTable.size());
				// Write method table.
				int currentMethodAddress = textSectionOffset;
				int currentDebugAddress = omitDebugInformation ? 0 : textSectionOffset + textSectionSize;
				for (Method method: methods) {
					// Deque guarantees that first entry is first element of iteration => main method.
					out.write(ByteUtil.intToBytes(currentMethodAddress));
					out.write(method.parameterCount);
					out.write(method.localVariableCount);
					out.write(ByteUtil.intToBytes(currentDebugAddress));
					currentMethodAddress += method.compiledMethodSize; // Adjust starting address for next method.
					int nameLength = method.name.getBytes("UTF-16BE").length;
					if (nameLength > 255)
						nameLength = 255;
					if (!omitDebugInformation)
						currentDebugAddress += 3 + nameLength; // 3 for dbgpool preamble (mnsize + msize).
				}
				// Write constant table.
				for (Constant c: constantTable) {
					out.write(ByteUtil.shortToBytes(c.value));
				}
				/*
				 * Write the text section, aka the actual compiled code. We do not have to make sure it does not exceed
				 * the size limit for methods because this is done when the code is compiled.
				 */
				for (Method method: methods) {
					out.write(method.compiledCode);
				}
				// Write debug information.
				if (!this.omitDebugInformation) {
					for (Method method: methods) {
						byte[] nameBytes = method.name.getBytes(Charset.forName("UTF-16BE"));
						// Now we can abbreviate too long method names.
						if (nameBytes.length > 255) {
							nameBytes = Arrays.copyOf(nameBytes, 255);
							msgOut.println("Warning: Too long method name. Abbreviated to "
									+ new String(nameBytes, Charset.forName("UTF16-BE")));
						}
						out.write(nameBytes.length);
						out.write(ByteUtil.shortToBytes((short) method.compiledCode.length));
						out.write(nameBytes);
					}
				}
				// Write error message table.
				for (ErrorMessage e: errorMessageTable) {
					out.write(e.msg.length);
					out.write(e.msg);
				}
				// Write program name as last element.
				out.write(this.nameBytes);
				// We're done! Huzzah!
			} catch (IOException e) {
				throw new RuntimeException("I/O error while writing the executable.", e);
			}
		}
	}

	private static final class Method {

		private int parameterCount;
		private int localVariableCount;
		private int compiledMethodSize;
		private String name;
		private List<Resolvable> jumpsToResolve = new LinkedList<>();
		private List<Resolvable> invokationsToResolve = new LinkedList<>();
		// Stores the names of the method parameters and variables of the current method for lookup.
		private List<String> localValues = new LinkedList<>();
		// labelsToOffset contains labels and their byte offset from the method start for the current method.
		private Map<String, Integer> labelsToOffset = new HashMap<>();

		private byte[] compiledCode;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Method other = (Method) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}
	}

	private static final class Resolvable {

		private final String target;
		private final int position;

		public Resolvable(String target, int position) {
			this.target = target;
			this.position = position;
		}
	}

	private static final class Constant {

		private final String name;
		private final short value;

		public Constant(String name, short value) {
			this.name = name;
			this.value = value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Constant other = (Constant) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}
	}

	private static final class ErrorMessage {

		private final String name;
		private final byte[] msg;

		public ErrorMessage(String name, byte[] msg) {
			this.name = name;
			this.msg = msg;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ErrorMessage other = (ErrorMessage) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}
	}
}