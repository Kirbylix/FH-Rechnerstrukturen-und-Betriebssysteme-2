package org.ejvm.execution;

import static org.ejvm.execution.EjvmInstruction.ArgumentType.INDEX;
import static org.ejvm.execution.EjvmInstruction.ArgumentType.NUMBER_LITERAL;
import static org.ejvm.execution.EjvmInstruction.ArgumentType.OFFSET;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.ejvm.execution.EjvmExecutionException.Reason;
import org.ejvm.execution.EjvmProcess.OutputFormatter;
import org.ejvm.execution.EjvmProcess.ProcessState;
import org.ejvm.inspection.MethodInformation;
import org.ejvm.util.ByteUtil;

/**
 * <p>
 * Contains all instructions executable by the {@link EjvmProcessor}. In fact, the actual execution of the instructions
 * is delegated to the {@link #execute(EjvmProcess, EjvmProcessor, Number...)} method provided by each element of this
 * {@link Enum}.
 * </p>
 * 
 * @author Sebastian Nicolai Kaupe
 * @since 1.0
 * @see ArgumentType
 */
public enum EjvmInstruction {

	/**
	 * <p>
	 * Pushes the 2-byte number literal provided as argument onto the process's stack.
	 * </p>
	 * 
	 * @since 1.0
	 */
	BIPUSH((byte) 0x10, NUMBER_LITERAL) {

		@Override
		public void execute(EjvmProcess process, EjvmProcessor processor, Number... args) {
			if (args.length != 1) {
				throw new IllegalArgumentException("BIPUSH expects one parameter, not " + args.length);
			}
			if (!(args[0] instanceof Short)) {
				throw new IllegalArgumentException("BIPUSH expects a single short as parameter, not a "
						+ args[0].getClass().getCanonicalName());
			}
			short s = args[0].shortValue();
			process.pushShortOnStack(s);
		}
	},
	/**
	 * <p>
	 * Duplicates the top-most two bytes of the process stack, effectively copying the top-most stack entry.
	 * </p>
	 * 
	 * @since 1.0
	 */
	DUP((byte) 0x59) {

		@Override
		public void execute(EjvmProcess process, EjvmProcessor processor, Number... args) {
			short s = process.popShortFromStack();
			process.pushShortOnStack(s);
			process.pushShortOnStack(s);
		}
	},
	/**
	 * <p>
	 * Removes the top-most stack value (two bytes) from the process stack.
	 * </p>
	 * 
	 * @since 1.0
	 * @see #BIPUSH
	 */
	POP((byte) 0x57) {

		@Override
		public void execute(EjvmProcess process, EjvmProcessor processor, Number... args) {
			if (!sufficientWorkingSectionSize(process, EjvmProcess.STACK_VALUE_SIZE))
				throw new EjvmExecutionException(Reason.NOT_ENOUGH_WORKING_VALUES, process);
			process.popShortFromStack();
		}
	},
	/**
	 * <p>
	 * Swaps the two 2-byte values of the process stack.
	 * </p>
	 * 
	 * @since 1.0
	 */
	SWAP((byte) 0x5F) {

		@Override
		public void execute(EjvmProcess process, EjvmProcessor processor, Number... args) {
			if (!sufficientWorkingSectionSize(process, 2 * EjvmProcess.STACK_VALUE_SIZE))
				throw new EjvmExecutionException(Reason.NOT_ENOUGH_WORKING_VALUES, process);
			short s1 = process.popShortFromStack();
			short s2 = process.popShortFromStack();
			process.pushShortOnStack(s2);
			process.pushShortOnStack(s1);
		}
	},
	/**
	 * <p>
	 * Retrieves the 2-byte value at the index provided as argument from the local variable table (which is itself on
	 * the stack) for the currently executed method and pushes it onto the process stack.
	 * </p>
	 * 
	 * @since 1.0
	 * @see #ISTORE
	 */
	ILOAD((byte) 0x15, INDEX) {

		@Override
		public void execute(EjvmProcess process, EjvmProcessor processor, Number... args) {
			if (args.length != 1) {
				throw new IllegalArgumentException("ILOAD expects one parameter, not " + args.length);
			}
			if (!(args[0] instanceof Byte)) {
				throw new IllegalArgumentException("ILOAD expects a single byte as parameter, not a "
						+ args[0].getClass().getCanonicalName());
			}
			int index = args[0].byteValue() & 0xFF;
			short value = process.getLocalVariable(index);
			process.pushShortOnStack(value);
		}
	},
	/**
	 * <p>
	 * Pops the top-most 2-byte value from the process stack and writes it into the method's local variable table at the
	 * index provided as argument.
	 * </p>
	 * 
	 * @since 1.0
	 * @see #ILOAD
	 */
	ISTORE((byte) 0x36, INDEX) {

		@Override
		public void execute(EjvmProcess process, EjvmProcessor processor, Number... args) {
			if (args.length != 1) {
				throw new IllegalArgumentException("ISTORE expects one parameter, not " + args.length);
			}
			if (!(args[0] instanceof Byte)) {
				throw new IllegalArgumentException("ISTORE expects a single byte as parameter, not a "
						+ args[0].getClass().getCanonicalName());
			}
			int index = args[0].byteValue() & 0xFF;
			short value = process.popShortFromStack();
			process.setLocalVariable(index, value);
		}
	},
	/**
	 * <p>
	 * Retrieves the constant at the provided index from the process's constant pool and push it onto the the process
	 * stack.
	 * </p>
	 * 
	 * @since 1.0
	 */
	LDC((byte) 0x12, INDEX) {

		@Override
		public void execute(EjvmProcess process, EjvmProcessor processor, Number... args) {
			if (args.length != 1) {
				throw new IllegalArgumentException("LDC expects one parameter, not " + args.length);
			}
			if (!(args[0] instanceof Byte)) {
				throw new IllegalArgumentException("LDC expects a single byte as parameter, not a "
						+ args[0].getClass().getCanonicalName());
			}
			// AND-ing with 0xFF enforces type enlargement with only the lowest byte set.
			short constant = process.getProgram().getConstant(args[0].byteValue() & 0xFF);
			process.pushShortOnStack(constant);
		}
	},
	/**
	 * <p>
	 * Pops two shorts from the process stack, adds them together and pushes the result back onto the stack.
	 * </p>
	 * 
	 * @since 1.0
	 * @see #ISUB
	 * @see #IINC
	 */
	IADD((byte) 0x60) {

		@Override
		public void execute(EjvmProcess process, EjvmProcessor processor, Number... args) {
			if (!sufficientWorkingSectionSize(process, 2 * EjvmProcess.STACK_VALUE_SIZE))
				throw new EjvmExecutionException(Reason.NOT_ENOUGH_WORKING_VALUES, process);
			short s0 = process.popShortFromStack();
			short s1 = process.popShortFromStack();
			process.pushShortOnStack((short) (s1 + s0));
		}
	},
	/**
	 * <p>
	 * Pops two shorts from the process stack, subtracts the second from the first and pushes the result back onto the
	 * stack.
	 * </p>
	 * 
	 * @since 1.0
	 * @see #IADD
	 * @see #IINC
	 */
	ISUB((byte) 0x64) {

		@Override
		public void execute(EjvmProcess process, EjvmProcessor processor, Number... args) {
			if (!sufficientWorkingSectionSize(process, 2 * EjvmProcess.STACK_VALUE_SIZE))
				throw new EjvmExecutionException(Reason.NOT_ENOUGH_WORKING_VALUES, process);
			short s0 = process.popShortFromStack();
			short s1 = process.popShortFromStack();
			process.pushShortOnStack((short) (s1 - s0));
		}
	},
	/**
	 * <p>
	 * Adds its second argument (short literal) onto the local variable/parameter at the index denoted by its first
	 * argument.
	 * </p>
	 * 
	 * @since 1.0
	 * @see #IADD
	 * @see #ISUB
	 */
	IINC((byte) 0x84, INDEX, NUMBER_LITERAL) {

		@Override
		public void execute(EjvmProcess process, EjvmProcessor processor, Number... args) {
			if (args.length != 2) {
				throw new IllegalArgumentException("IINC expects two parameters, not " + args.length);
			}
			if (!(args[0] instanceof Byte) && !(args[1] instanceof Short)) {
				throw new IllegalArgumentException("IINC expects a byte and a short as parameter,s not a "
						+ args[0].getClass().getCanonicalName() + " and a " + args[1].getClass().getCanonicalName());
			}
			int index = ((Byte) args[0]).byteValue() & 0xFF;
			short increment = args[1].shortValue();
			short value = process.getLocalVariable(index);
			process.setLocalVariable(index, (short) (value + increment));
		}
	},
	/**
	 * <p>
	 * Pops two shorts from the process stack, applies a binary AND to them and pushes the result back onto the stack.
	 * </p>
	 * 
	 * @since 1.0
	 * @see #IOR
	 */
	IAND((byte) 0x7E) {

		@Override
		public void execute(EjvmProcess process, EjvmProcessor processor, Number... args) {
			if (!sufficientWorkingSectionSize(process, 2 * EjvmProcess.STACK_VALUE_SIZE))
				throw new EjvmExecutionException(Reason.NOT_ENOUGH_WORKING_VALUES, process);
			short s1 = process.popShortFromStack();
			short s2 = process.popShortFromStack();
			process.pushShortOnStack((short) (s1 & s2));
		}
	},
	/**
	 * <p>
	 * Pops two shorts from the process stack, applies a binary OR to them and pushes the result back onto the stack.
	 * </p>
	 * 
	 * @since 1.0
	 * @see #IAND
	 */
	IOR((byte) 0x80) {

		@Override
		public void execute(EjvmProcess process, EjvmProcessor processor, Number... args) {
			if (!sufficientWorkingSectionSize(process, 2 * EjvmProcess.STACK_VALUE_SIZE))
				throw new EjvmExecutionException(Reason.NOT_ENOUGH_WORKING_VALUES, process);
			short s1 = process.popShortFromStack();
			short s2 = process.popShortFromStack();
			process.pushShortOnStack((short) (s1 | s2));
		}
	},
	/**
	 * <p>
	 * Moves the program counter number of bytes it receives as first argument.
	 * </p>
	 * 
	 * @since 1.0
	 * @see #IFEQ
	 * @see #IFLT
	 * @see #IF_ICMPEQ
	 */
	GOTO((byte) 0xA7, OFFSET) {

		@Override
		public void execute(EjvmProcess process, EjvmProcessor processor, Number... args) {
			if (args.length != 1) {
				throw new IllegalArgumentException("GOTO expects one parameter, not " + args.length);
			}
			if (!(args[0] instanceof Short)) {
				throw new IllegalArgumentException("GOTO expects a single short as parameter, not a "
						+ args[0].getClass().getCanonicalName());
			}
			process.moveProgramCounter(args[0].shortValue());
		}
	},
	/**
	 * <p>
	 * Removes the top-most stack value and checks its value. If it is 0, the instruction moves the program counter of
	 * the process the number of bytes it receives as first argument.
	 * </p>
	 * 
	 * @since 1.0
	 * @see #GOTO
	 * @see #IFLT
	 * @see #IF_ICMPEQ
	 */
	IFEQ((byte) 0x99, OFFSET) {

		@Override
		public void execute(EjvmProcess process, EjvmProcessor processor, Number... args) {
			if (args.length != 1) {
				throw new IllegalArgumentException("IFEQ expects one parameter, not " + args.length);
			}
			if (!(args[0] instanceof Short)) {
				throw new IllegalArgumentException("IFEQ expects a single short as parameter, not a "
						+ args[0].getClass().getCanonicalName());
			}
			if (!sufficientWorkingSectionSize(process, EjvmProcess.STACK_VALUE_SIZE))
				throw new EjvmExecutionException(Reason.NOT_ENOUGH_WORKING_VALUES, process);
			short s = process.popShortFromStack();
			if (s == 0) {
				process.setProgramCounter(process.getProgramCounter() + args[0].shortValue());
			}
		}
	},
	/**
	 * <p>
	 * Removes the top-most stack value and checks its value. If it lesser than 0, the instruction moves the program
	 * counter of the process the number of bytes it receives as first argument.
	 * </p>
	 * 
	 * @since 1.0
	 * @see #GOTO
	 * @see #IFEQ
	 * @see #IF_ICMPEQ
	 */
	IFLT((byte) 0x9B, OFFSET) {

		@Override
		public void execute(EjvmProcess process, EjvmProcessor processor, Number... args) {
			if (args.length != 1) {
				throw new IllegalArgumentException("IFLT expects one parameter, not " + args.length);
			}
			if (!(args[0] instanceof Short)) {
				throw new IllegalArgumentException("IFLT expects a single short as parameter, not a "
						+ args[0].getClass().getCanonicalName());
			}
			if (!sufficientWorkingSectionSize(process, EjvmProcess.STACK_VALUE_SIZE))
				throw new EjvmExecutionException(Reason.NOT_ENOUGH_WORKING_VALUES, process);
			short s = process.popShortFromStack();
			if (s < 0) {
				process.setProgramCounter(process.getProgramCounter() + args[0].shortValue());
			}
		}
	},
	/**
	 * <p>
	 * Removes the two top-most stack values and compares them. If they are equal, the instruction moves the program
	 * counter of the process the number of bytes it receives as first argument.
	 * </p>
	 * 
	 * @since 1.0
	 * @see #GOTO
	 * @see #IFEQ
	 * @see #IFLT
	 */
	IF_ICMPEQ((byte) 0x9F, OFFSET) {

		@Override
		public void execute(EjvmProcess process, EjvmProcessor processor, Number... args) {
			if (args.length != 1) {
				throw new IllegalArgumentException("IF_ICMPEQ expects one parameter, not " + args.length);
			}
			if (!(args[0] instanceof Short)) {
				throw new IllegalArgumentException("IF_ICMPEQ expects a single short as parameter, not a "
						+ args[0].getClass().getCanonicalName());
			}
			if (!sufficientWorkingSectionSize(process, 2 * EjvmProcess.STACK_VALUE_SIZE))
				throw new EjvmExecutionException(Reason.NOT_ENOUGH_WORKING_VALUES, process);
			short s1 = process.popShortFromStack();
			short s2 = process.popShortFromStack();
			if (s1 == s2) {
				process.setProgramCounter(process.getProgramCounter() + args[0].shortValue());
			}
		}
	},
	/**
	 * <p>
	 * Invokes the method denoted by their index in the method table. A new stack frame is added to the process for the
	 * execution of the invoked method.
	 * </p>
	 * 
	 * @since 1.0
	 * @see #RETURN
	 * @see #IRETURN
	 */
	INVOKEVIRTUAL((byte) 0xB6, INDEX) {

		@Override
		public void execute(EjvmProcess process, EjvmProcessor processor, Number... args) {
			if (args.length != 1) {
				throw new IllegalArgumentException("INVOKEVIRTUAL expects one parameter, not " + args.length);
			}
			if (!(args[0] instanceof Byte)) {
				throw new IllegalArgumentException("INVOKEVIRTUAL expects a single byte as parameter, not a "
						+ args[0].getClass().getCanonicalName());
			}
			int index = ((Byte) args[0]).byteValue() & 0xFF;
			MethodInformation method = process.getProgram().getMethodInformation(index);
			// Check if there is a working value for every parameter available.
			if (!sufficientWorkingSectionSize(process, method.getParameterCount() * EjvmProcess.STACK_VALUE_SIZE))
				throw new EjvmExecutionException(Reason.NOT_ENOUGH_WORKING_VALUES, process);
			// Add stack frame for the new method, then set PC to point to the invoked method's first instruction.
			process.addStackFrame(index);
			process.setProgramCounter(method.getMethodAddress());
		}
	},
	/**
	 * <p>
	 * Returns the control flow to a method invoked earlier, popping a single {@code short} value from the current stack
	 * frame and places it as new stack value onto the stack of the underlying stack frame. If their is no such method
	 * (i.e. {@code main} is complete), the {@link EjvmProcess} changes into the {@link ProcessState#DONE} state.
	 * </p>
	 * 
	 * @since 1.0
	 * @see #INVOKEVIRTUAL
	 */
	IRETURN((byte) 0xAC) {

		@Override
		public void execute(EjvmProcess process, EjvmProcessor processor, Number... args) {
			if (!sufficientWorkingSectionSize(process, EjvmProcess.STACK_VALUE_SIZE))
				throw new EjvmExecutionException(Reason.NOT_ENOUGH_WORKING_VALUES, process);
			short returnValue = process.popShortFromStack();
			process.removeStackFrame();
			process.pushShortOnStack(returnValue);
		}
	},
	/**
	 * <p>
	 * Returns the control flow to a method invoked earlier without returning a value. If their is no such method (i.e.
	 * {@code main} is complete), the {@link EjvmProcess} changes into the {@link ProcessState#DONE} state.
	 * </p>
	 * 
	 * @since 1.0
	 * @see #INVOKEVIRTUAL
	 */
	RETURN((byte) 0xB1) {

		@Override
		public void execute(EjvmProcess process, EjvmProcessor processor, Number... args) {
			process.removeStackFrame();
		}
	},
	/**
	 * <p>
	 * Attempts to read a single character from the input provided by the processor's {@link EjvmMachine}. If no input
	 * is available, the instruction sets the process state to {@link EjvmProcess.ProcessState#BLOCKED}.
	 * </p>
	 * 
	 * @since 1.0
	 * @see EjvmInstruction#OUT
	 */
	IN((byte) 0xF0) {

		@Override
		public void execute(EjvmProcess process, EjvmProcessor processor, Number... args) {
			Reader input = process.getInputAsReader();
			try {
				if (input.ready()) {
					// We have input, read a single char and push it onto the process stack.
					char inputCharacter = (char) input.read();
					process.pushShortOnStack((short) inputCharacter);
					process.setProcessState(ProcessState.RUNNING);
				} else {
					// Nothing to read, block the process.
					process.setProcessState(ProcessState.BLOCKED);
				}
			} catch (IOException e) {
				throw new RuntimeException("Unexpected error during eJVM virtual machine read.", e);
			}
		}
	},
	/**
	 * <p>
	 * Removes the top-most stack value and formats it according to the processes output format defined by
	 * {@link EjvmInstruction#SETOUT}, writing it out to the output channel provided by the processor's
	 * {@link EjvmMachine}.
	 * </p>
	 * 
	 * @since 1.0
	 * @see EjvmInstruction#IN
	 */
	OUT((byte) 0xF1) {

		@Override
		public void execute(EjvmProcess process, EjvmProcessor processor, Number... args) {
			if (!sufficientWorkingSectionSize(process, EjvmProcess.STACK_VALUE_SIZE))
				throw new EjvmExecutionException(Reason.NOT_ENOUGH_WORKING_VALUES, process);
			String output = process.getOutputFormatter().format(process.popShortFromStack());
			process.getOutput().print(output);
		}
	},
	/**
	 * <p>
	 * Sets the output formatter for a process. The formatter is used to process any output into the format desired by
	 * the program's programmer (i.e., numerical or textual output).
	 * </p>
	 * 
	 * @since 1.0
	 * @see EjvmInstruction#OUT
	 */
	SETOUT((byte) 0xFA, INDEX) {
		@Override
		public void execute(EjvmProcess process, EjvmProcessor processor, Number... args) {
			if (args.length != 1) {
				throw new IllegalArgumentException("SETOUT expects one parameter, not " + args.length);
			}
			if (!(args[0] instanceof Byte)) {
				throw new IllegalArgumentException("SETOUT expects a single byte as parameter, not a "
						+ args[0].getClass().getCanonicalName());
			}
			int formatterIndex = args[0].intValue();
			OutputFormatter formatter = EjvmProcess.OutputFormatter.values()[formatterIndex];
			process.setOutputFormatter(formatter);
		}
	},
	/**
	 * <p>
	 * Halts the {@link EjvmProcessor}, referring to an error message via its sole argument. Retrieve the message using
	 * an implementation of {@link EjvmExecutionListener#afterExecution(EjvmProcess, EjvmInstruction, Number...)}.
	 * </p>
	 * 
	 * @since 1.0
	 * @see #HALT
	 */
	ERR((byte) 0xF2, INDEX) {

		@Override
		public void execute(EjvmProcess process, EjvmProcessor processor, Number... args) {
			if (args.length != 1) {
				throw new IllegalArgumentException("ERR expects one parameter, not " + args.length);
			}
			if (!(args[0] instanceof Byte)) {
				throw new IllegalArgumentException("ERR expects a single byte as parameter, not a "
						+ args[0].getClass().getCanonicalName());
			}
			process.setProcessState(ProcessState.HALTED);
			/*
			 * In order to retrieve the error message, use an EjvmExecutionListener and react to ERR instructions in it
			 * afterExecution() method. The index of the message is given as argument to the method and the EjvmProgram
			 * is available via the EjvmProcess object.
			 */
		}
	},
	/**
	 * <p>
	 * Halts the {@link EjvmProcess}. This is <em>not</em> an error halt, but one deliberately requested by the program.
	 * </p>
	 * 
	 * @since 1.0
	 * @see #ERR
	 */
	HALT((byte) 0xFF) {

		@Override
		public void execute(EjvmProcess process, EjvmProcessor processor, Number... args) {
			process.setProcessState(ProcessState.HALTED);
		}
	},
	/**
	 * <p>
	 * This instruction does nothing.
	 * </p>
	 * 
	 * @since 1.0
	 */
	NOP((byte) 0x00) {

		@Override
		public void execute(EjvmProcess process, EjvmProcessor processor, Number... args) {
			return;
		}
	},
	/**
	 * <p>
	 * This instruction was meant for machines working with single-byte values and let the following instruction with 16
	 * instead of 8 bit values. Since this implementation already works with 16 bit {@code short} values, this
	 * instruction has been deprecated and acts as if {@link #NOP} would have been invoked.
	 * </p>
	 * 
	 * @deprecated
	 * @since 1.0
	 * @see #NOP
	 */
	@Deprecated
	WIDE((byte) 0xC4) {

		@Override
		public void execute(EjvmProcess process, EjvmProcessor processor, Number... args) {
			// WIDE needs to do nothing since we expanded variable size to 16 bit.
			return;
		}
	};

	/**
	 * <p>
	 * The OpCode for the instruction.
	 * </p>
	 */
	private final byte opCode;
	/**
	 * <p>
	 * A list detailing the types of all arguments the instruction needs.
	 * </p>
	 */
	private final ArgumentType[] argumentTypes;

	/**
	 * Adds a new instruction.
	 * 
	 * @param opCode
	 *            The OpCode for the instruction.
	 * @param argumentTypes
	 *            The types of the arguments expected by the instruction as {@link ArgumentType} element.
	 */
	private EjvmInstruction(final byte opCode, final ArgumentType... argumentTypes) {
		this.opCode = opCode;
		this.argumentTypes = argumentTypes;
		MapHolder.instructionForOpCode.put(Byte.valueOf(opCode), this);
	}

	/**
	 * <p>
	 * Returns the opCode associated with this instruction.
	 * </p>
	 * 
	 * @return The opCode for the instruction.
	 */
	public byte getOpCode() {
		return opCode;
	}

	/**
	 * <p>
	 * Returns an array containing the types of the arguments this instruction expects.
	 * </p>
	 * 
	 * @return The arguments types for this instruction.
	 */
	public EjvmInstruction.ArgumentType[] getArgumentTypes() {
		// Make sure to apply defensive copying.
		return argumentTypes.clone();
	}

	/**
	 * <p>
	 * Executes the instruction with {@code args} providing the arguments needed. Arguments should have been converted
	 * into their appropriate form using {@link ArgumentType#fromBytes(byte[])}.
	 * </p>
	 * 
	 * @param process
	 *            The process from which the instruction originates.
	 * @param processor
	 *            The {@link EjvmProcessor} {@code process} is executed on.
	 * @param args
	 *            The arguments needed for the instruction.
	 * @throws IllegalArgumentException
	 *             In any case where something is wrong with the parameters, e.g. if their is a wrong number of those or
	 *             they are of the wrong type.
	 */
	public abstract void execute(EjvmProcess process, EjvmProcessor processor, Number... args);

	/**
	 * <p>
	 * Fetches the instruction {@code opCode} is assigned to.
	 * </p>
	 * 
	 * @param opCode
	 *            The OpCode for which the associated instruction is to be fetched.
	 * @return The instruction for {@code opCode}.
	 * @throws IllegalArgumentException
	 *             If there is no matching instruction.
	 */
	public static EjvmInstruction forOpcode(byte opCode) {
		EjvmInstruction instruction = MapHolder.instructionForOpCode.get(Byte.valueOf(opCode));
		if (instruction == null) {
			throw new IllegalArgumentException(String.format("No instruction for OpCode 0x%02X", opCode));
		}
		return instruction;
	}

	/**
	 * <p>
	 * Private helper method to check the size of the working value section on the stack. If the size is smaller than
	 * {@code sizeNeeded}, this method returns {@code false}.
	 * </p>
	 * 
	 * @param process
	 *            The {@link EjvmProcess}.
	 * @param sizeNeeded
	 *            The minimum size the working value section needs to have in order for the instruction to be executed
	 *            successfully.
	 * @return {@code true} if the working value section of the processes' stack is at least {@code sizeNeeded} bytes
	 *         big; {@code false} otherwise.
	 */
	private static boolean sufficientWorkingSectionSize(EjvmProcess process, int sizeNeeded) {
		int parameterCount = process.getProgram().getMethodInformation(process.getCurrentMethodIndex())
				.getParameterCount();
		int localVariableCount = process.getProgram().getMethodInformation(process.getCurrentMethodIndex())
				.getLocalVariableCount();
		int upperWorkingValuesBound = process.getFramePointer() + EjvmProcess.STACK_FRAME_PREAMBLE_SIZE
				+ (parameterCount + localVariableCount) * 2;
		return upperWorkingValuesBound - process.getStackPointer() >= sizeNeeded;
	}

	/**
	 * <p>
	 * Private class used only to hold the map that provides the mapping from opCode to {@link EjvmInstruction}
	 * instance.
	 * </p>
	 * 
	 * @author Sebastian Nicolai Kaupe
	 * @since 1.0
	 */
	private static final class MapHolder {

		/**
		 * Provides the mapping from OpCode to {@link EjvmInstruction} instance.
		 */
		private static Map<Byte, EjvmInstruction> instructionForOpCode = new HashMap<>();

		private MapHolder() {}
	}

	/**
	 * <p>
	 * Describes the types arguments to an eJVM instruction may have.
	 * </p>
	 * 
	 * @author Sebastian Nicolai Kaupe
	 * @since 1.0
	 * @see EjvmInstruction
	 */
	public static enum ArgumentType {

		/**
		 * <p>
		 * Represents a numerical literal. Numerical literals are 16-bit signed values.
		 * </p>
		 * 
		 * @since 1.0
		 */
		NUMBER_LITERAL(2, Short.class) {
			@Override
			public Short fromBytes(byte[] bytes) {
				return Short.valueOf(ByteUtil.shortFromBytes(bytes));
			}
		},
		/**
		 * <p>
		 * Represents an offset, usually used for jumps.
		 * </p>
		 * 
		 * @since 1.0
		 */
		OFFSET(2, Short.class) {

			@Override
			public Number fromBytes(byte[] bytes) {
				return Short.valueOf(ByteUtil.shortFromBytes(bytes));
			}
		},
		/**
		 * <p>
		 * Represents a relative address, with 0 being set at the first byte of the executable as stored in the memory.
		 * </p>
		 * 
		 * @since 1.0
		 */
		ADDRESS(4, Integer.class) {
			@Override
			public Integer fromBytes(byte[] bytes) {
				return Integer.valueOf(ByteUtil.intFromBytes(bytes));
			}
		},
		/**
		 * <p>
		 * Represents an index for one of the tables contained in a compiled eJVM program, such as the method table.
		 * </p>
		 * 
		 * @since 1.0
		 */
		INDEX(1, Byte.class) {
			@Override
			public Byte fromBytes(byte[] bytes) {
				return Byte.valueOf(bytes[0]);
			}
		};

		/**
		 * <p>
		 * The size of the argument type in byte.
		 * </p>
		 */
		private final int size;
		/**
		 * <p>
		 * The Java type this argument type is mapped to.
		 * </p>
		 */
		private final Class<? extends Number> javaType;

		/**
		 * Add a new type of instruction arguments to the enum.
		 * 
		 * @param size
		 *            The byte size of the argument type in the executable.
		 * @param javaType
		 *            The Java type this argument type is mapped to.
		 */
		private ArgumentType(final int size, final Class<? extends Number> javaType) {
			this.size = size;
			this.javaType = javaType;
		}

		/**
		 * <p>
		 * Returns the value of given bytes as seen by the argument type.
		 * </p>
		 * 
		 * @param bytes
		 *            The bytes to convert into the concrete value.
		 * @return The value of the bytes as seen by the argument type.
		 * @throws IllegalArgumentException
		 *             If the number of bytes in {@code bytes} does not match the expected number (which is the argument
		 *             type's size).
		 * @see #getArgumentSize()
		 */
		public abstract Number fromBytes(byte[] bytes);

		/**
		 * <p>
		 * Returns a format string capable of formatting the argument type into a suitable, human-readable format (which
		 * is a hexadecimal number, in most cases). The format string does not contain any characters besides those
		 * needed for the proper conversion and, as such, may be concatenated with other strings without harm.
		 * </p>
		 * 
		 * @return A format string to convert the argument type into a human-readable format.
		 */
		public String getFormatString() {
			// Multiply argSize with 2 as we need two chars to represent a single byte.
			return "0x%0" + (getArgumentSize() * 2) + "X";
		}

		/**
		 * <p>
		 * Returns the number of bytes a single argument of the given type needs.
		 * </p>
		 * 
		 * @return The size of the argument type in byte.
		 */
		public int getArgumentSize() {
			return this.size;
		}

		/**
		 * <p>
		 * Returns the {@link Class} object of a numerical Java type suitable to represent the argument type.
		 * </p>
		 * 
		 * @return A numerical Java type suitable to represent the argument type.
		 */
		public Class<? extends Number> getJavaType() {
			return this.javaType;
		}
	}
}