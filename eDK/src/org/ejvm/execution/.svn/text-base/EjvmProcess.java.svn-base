package org.ejvm.execution;

import static org.ejvm.inspection.EjvmProgram.ADDRESS_SIZE;
import static org.ejvm.inspection.EjvmProgram.MTBL_ADDRESS;
import static org.ejvm.inspection.EjvmProgram.MTBL_ENTRY_SIZE;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

import org.ejvm.inspection.EjvmProgram;
import org.ejvm.inspection.MethodInformation;
import org.ejvm.util.ByteUtil;

/**
 * <p>
 * Represents a process in an {@link EjvmMachine} to execute an {@link EjvmProgram} and manages the associated
 * resources.
 * </p>
 * 
 * @author Sebastian Nicolai Kaupe
 * @since 1.0
 * @see EjvmMachine
 * @see EjvmProcessor
 * @see EjvmProgram
 */
public class EjvmProcess {

	/**
	 * <p>
	 * The value stored in the field for the return address in the stack frame for the main method.
	 * </p>
	 */
	public static final int MAIN_METHOD_FRAME_RETURN_ADDRESS = -1;

	/**
	 * <p>
	 * Size of the stack frame preamble, made up out of the method index byte, the old program counter and the old frame
	 * pointer.
	 * </p>
	 */
	public static final int STACK_FRAME_PREAMBLE_SIZE = (2 * EjvmProgram.ADDRESS_SIZE) + 1;

	/**
	 * <p>
	 * Size of a stack value, e.g. a local variable or working value.
	 * </p>
	 */
	public static final int STACK_VALUE_SIZE = 2;

	/**
	 * <p>
	 * This {@link ByteBuffer} contains the working memory of the system assigned to this process.
	 * </p>
	 */
	private final ByteBuffer workingMemory;

	/**
	 * <p>
	 * The program executed by this process.
	 * </p>
	 */
	private final EjvmProgram program;

	/**
	 * <p>
	 * The program counter for this process. At object creation, it is set to point to the first instruction of the
	 * executable's main method using the value of the executable field mainoff.
	 * </p>
	 */
	private int programCounter;

	/**
	 * <p>
	 * The process's stack pointer, pointing to the upper byte of the top-most 2-byte value on the process stack.
	 * </p>
	 */
	private int stackPointer;

	/**
	 * <p>
	 * The process's frame pointer, pointing to the start of the current stack frame (which is the byte with the highest
	 * memory address belonging to this stack frame).
	 * </p>
	 */
	private int framePointer;

	/**
	 * <p>
	 * The state of the process. After creation, the process is {@link ProcessState#READY}. Once it's first instruction
	 * has been fetched, the process is {@link ProcessState#RUNNING}. If the process is detached from a processor, its
	 * state has to be set to {@link ProcessState#READY} by the processor. Requesting input while non is available will
	 * set the process state to {@link ProcessState#BLOCKED}. Once the last instruction has been executed (i.e. the
	 * stack frame of the main method has been left), the state is changed to {@link ProcessState#DONE}.
	 * </p>
	 */
	private ProcessState processState;

	/**
	 * <p>
	 * The process' input channel.
	 * </p>
	 */
	private InputStream input;

	/**
	 * <p>
	 * The process' input channel as a {@link Reader} to make extraction of characters more easy.
	 * </p>
	 */
	private InputStreamReader inputAsReader;

	/**
	 * <p>
	 * The output channel for this process.
	 * </p>
	 */
	private PrintStream output;

	/**
	 * <p>
	 * The output formatter for this process. Any output of a process should be send through the formatter's
	 * {@link OutputFormatter#format(short)} method to guarantee that the output is formatted in the way requested by
	 * the programmer.
	 * </p>
	 * <p>
	 * The default formatter used is {@link OutputFormatter#CHAR}.
	 * </p>
	 */
	private OutputFormatter outputFormatter;

	/**
	 * <p>
	 * A {@link Set} of {@link EjvmExecutionListener} objects that are to be informed about the execution of every
	 * instruction of this process being executed.
	 * </p>
	 */
	private Set<EjvmExecutionListener> executionListeners;

	/**
	 * <p>
	 * Creates a new process with {@code workingMemory} containing the part of the machine's working memory assigned to
	 * it. The process does <i>not</i> retrieve the content of the executable file; this task lies within the
	 * responsibility of the {@link EjvmMachine} executing this process. Moreover, it is expected that the executable
	 * has already been loaded into the memory part assigned to the new process prior to the construction of said
	 * process.
	 * </p>
	 * 
	 * @param program
	 *            The {@link EjvmProgram} executed by this process.
	 * @param workingMemory
	 *            The part of the executing {@link EjvmMachine}'s working memory assigned to this process.
	 */
	protected EjvmProcess(EjvmProgram program, ByteBuffer workingMemory) {
		this.workingMemory = workingMemory;
		this.program = program;
		this.executionListeners = new HashSet<>();
		this.outputFormatter = OutputFormatter.CHAR;
		setupForExecution();
	}

	/**
	 * <p>
	 * Prepares the process for execution by setting up the stack, the stack and frame pointers and the program counter.
	 * After execution of this method, the process is in the {@link ProcessState#READY} state.
	 * </p>
	 */
	private void setupForExecution() {
		// Setup stack by let the SP and FP point at the highest memory position available to the process.
		this.stackPointer = this.workingMemory.limit();
		this.framePointer = this.workingMemory.limit();
		// Set PC to return address reserved for the main method's stack frame.
		this.programCounter = MAIN_METHOD_FRAME_RETURN_ADDRESS;
		// Add frame for main().
		addStackFrame(0);
		/*
		 * First entry in the method table is always the main method, therefore, we can use its first for byte to set
		 * the PC to its initial value.
		 * 
		 * IMPORTANT: Do NOT move this assignment in front of the call to addStackFrame(0) statement to create the stack
		 * frame for the main method! Otherwise, the return address in the main method stack frame will be incorrect.
		 */
		this.programCounter = program.getMainMethodOffset();
		this.processState = ProcessState.READY;
	}

	/**
	 * <p>
	 * Resets the process, restoring its state as it was just after creation. The contents of the stack are entirely
	 * overwritten with zeroes.
	 * </p>
	 */
	protected void reset() {
		int currentAddress = this.stackPointer;
		while (currentAddress < this.workingMemory.limit()) {
			workingMemory.position(currentAddress);
			workingMemory.put((byte) 0);
		}
		setupForExecution();
	}

	/**
	 * <p>
	 * Fetches the next OpCode from the running process to be executed and increases the program counter of this
	 * process.
	 * </p>
	 * 
	 * @return The next instruction to be executed.
	 * @throws IllegalStateException
	 *             If the process is not in state {@link ProcessState#RUNNING}.
	 */
	protected byte fetchNextOpCode() {
		// TODO Implement better state handling.
		switch (processState) {
		case READY: // Fall through.
		case HALTED:
		case DONE:
			throw new IllegalStateException("Process must be RUNNING in order to fetch an instruction.");
		default:
			// State is RUNNING or BLOCKED, so feel free to go.
			return workingMemory.get(programCounter++);
		}
	}

	/**
	 * <p>
	 * Moves the program counter {@code numberOfBytes} further.
	 * </p>
	 * 
	 * @param numberOfBytes
	 *            The amount of bytes to move the program counter.
	 * @see #setProgramCounter(int)
	 */
	protected void moveProgramCounter(int numberOfBytes) {
		this.programCounter += numberOfBytes;
	}

	/**
	 * <p>
	 * Sets this process's program counter to {@code offset}.
	 * </p>
	 * 
	 * @param pc
	 *            The new value for the PC.
	 * @throws IllegalArgumentException
	 *             If {@code pc} is smaller than 0 or bigger than the size of the working memory assigned to the
	 *             process.
	 * @see #moveProgramCounter(int)
	 */
	protected void setProgramCounter(int pc) {
		if (pc < 0) {
			throw new IllegalArgumentException("The program counter cannot be negative.");
		}
		if (pc >= this.workingMemory.limit()) {
			throw new IllegalArgumentException("The program counter cannot be bigger than the working memory.");
		}
		this.programCounter = pc;
	}

	/**
	 * <p>
	 * Pushes {@code src.length} bytes onto the process stack, moving the stack pointer accordingly.
	 * </p>
	 * 
	 * @param src
	 *            The array containing the bytes to be pushed onto the stack.
	 * @see #pushShortOnStack(short)
	 */
	protected void pushOnStack(byte[] src) {
		workingMemory.position((stackPointer -= src.length));
		workingMemory.put(src);
	}

	/**
	 * <p>
	 * Pushes the bytes of {@code s} onto the process stack.
	 * </p>
	 * 
	 * @param s
	 *            The {@code short} to push onto the stack.
	 * @see #pushOnStack(byte[])
	 */
	protected void pushShortOnStack(short s) {
		pushOnStack(ByteUtil.shortToBytes(s));
	}

	/**
	 * <p>
	 * Pops {@code dst.length} bytes from the stack, writes them into {@code dst} and moves the stack pointer
	 * accordingly.
	 * </p>
	 * 
	 * @param dst
	 *            The byte array to write the popped bytes into.
	 * @see #popShortFromStack()
	 */
	protected void popFromStack(byte[] dst) {
		workingMemory.position(stackPointer);
		workingMemory.get(dst);
		stackPointer += dst.length;
	}

	/**
	 * <p>
	 * Pops the top-most two bytes from the process stack and converts them into a {@code short}.
	 * </p>
	 * 
	 * @return The top-most {@code short} value from the process stack.
	 * @see #popFromStack(byte[])
	 */
	protected short popShortFromStack() {
		byte[] bytes = new byte[2];
		popFromStack(bytes);
		return ByteUtil.shortFromBytes(bytes);
	}

	/**
	 * <p>
	 * Adds a new stack frame to the process stack, providing enough space for the parameters and local variables for
	 * the method found in the method table at {@code methodIndex}.
	 * </p>
	 * 
	 * @param methodIndex
	 *            The index of the method to create a stack frame for.
	 */
	protected void addStackFrame(int methodIndex) {
		// Retrieve parameters from current stack frame.
		int numberOfParameters = workingMemory.get(MTBL_ADDRESS + (MTBL_ENTRY_SIZE * methodIndex) + 4) & 0xFF;
		short[] parameters = new short[numberOfParameters];
		for (int i = 0; i < numberOfParameters; i++) {
			parameters[i] = popShortFromStack();
		}
		// Create preamble for new stack frame.
		pushOnStack(new byte[] { (byte) (methodIndex & 0xFF) });
		pushOnStack(ByteUtil.intToBytes(programCounter));
		pushOnStack(ByteUtil.intToBytes(framePointer));
		this.framePointer = stackPointer + STACK_FRAME_PREAMBLE_SIZE;
		// Push parameters onto stack.
		byte[] p = ByteUtil.bytesFromShorts(parameters);
		pushOnStack(p);
		// Reserve space for local variables.
		int numberOfLocals = workingMemory.get(MTBL_ADDRESS + (MTBL_ENTRY_SIZE * methodIndex) + 5) & 0xFF;
		pushOnStack(new byte[(numberOfLocals) * 2]);
	}

	/**
	 * <p>
	 * Removes the top-most stack frame from the process stack. If the removed stack frame was the last one, the process
	 * state is set to {@link ProcessState#DONE}.
	 * </p>
	 * <p>
	 * Please note that the content of the stack is <i>not</i> modified - only the program counter, the frame pointer
	 * and the stack pointer are returned to their earlier values.
	 * </p>
	 */
	protected void removeStackFrame() {
		// Subtract an additional byte for the mindex byte at the begin of each stack frame.
		this.programCounter = workingMemory.getInt(this.framePointer - ADDRESS_SIZE - 1);
		this.stackPointer = this.framePointer;
		// Subtract an additional byte for the mindex byte at the begin of each stack frame.
		this.framePointer = workingMemory.getInt(this.framePointer - STACK_FRAME_PREAMBLE_SIZE);
		// Check if we have reached the end of the program.
		if (this.programCounter == MAIN_METHOD_FRAME_RETURN_ADDRESS) {
			this.processState = ProcessState.DONE;
		}
	}

	/**
	 * <p>
	 * Retrieves the value of the local variable at {@code index} from the local variable section of the stack frame.
	 * </p>
	 * <p>
	 * Please remember that method parameters are also stored in the local variable section.
	 * </p>
	 * 
	 * @param index
	 *            The variable index.
	 * @throws IndexOutOfBoundsException
	 *             If the variable index if negative or bigger than the number of local variables allows.
	 */
	public short getLocalVariable(int index) {
		int currentMethodIndex = workingMemory.get(getFramePointer() - 1);
		MethodInformation info = program.getMethodInformation(currentMethodIndex);
		if (index < 0) {
			throw new IndexOutOfBoundsException("The local variable index must not be negative.");
		} else if (index > info.getLocalVariableCount() + info.getParameterCount()) {
			// Parameters & variables are stored together, so remember to add the number of parameters.
			throw new IndexOutOfBoundsException(index + ">="
					+ program.getMethodInformation(currentMethodIndex).getLocalVariableCount());
		}
		byte[] bytes = new byte[2];
		// Subtract 3 to make up for the mindex byte and the size of the short we have to read.
		workingMemory.position(framePointer - (ADDRESS_SIZE * 2) - (index * 2) - 3);
		workingMemory.get(bytes);
		return ByteUtil.shortFromBytes(bytes);
	}

	/**
	 * <p>
	 * Sets the value of the local variable at {@code index} in the local variable section of the stack frame.
	 * </p>
	 * 
	 * @param index
	 *            The variable index.
	 * @param value
	 *            The new variable value.
	 * @throws IndexOutOfBoundsException
	 *             If the variable index if negative or bigger than the number of local variables allows.
	 */
	protected void setLocalVariable(int index, short value) {
		int currentMethodIndex = workingMemory.get(getFramePointer() - 1);
		MethodInformation info = program.getMethodInformation(currentMethodIndex);
		if (index < 0) {
			throw new IndexOutOfBoundsException("The local variable index must not be negative.");
		} else if (index > info.getLocalVariableCount() + info.getParameterCount()) {
			throw new IndexOutOfBoundsException(index + ">="
					+ program.getMethodInformation(currentMethodIndex).getLocalVariableCount());
		}
		byte[] bytes = ByteUtil.shortToBytes(value);
		// Subtract 3 to make up for the mindex byte and the size of the short we have to read.
		workingMemory.position(framePointer - (ADDRESS_SIZE * 2) - (index * 2) - 3);
		workingMemory.put(bytes);
	}

	/**
	 * <p>
	 * Returns the current value of the process's program counter.
	 * </p>
	 * 
	 * @return The current PC value.
	 */
	public int getProgramCounter() {
		return programCounter;
	}

	/**
	 * <p>
	 * Returns the current value of the process's stack pointer.
	 * </p>
	 * 
	 * @return The current SP value.
	 */
	public int getStackPointer() {
		return stackPointer;
	}

	/**
	 * <p>
	 * Returns the current value of the process's frame pointer.
	 * </p>
	 * 
	 * @return The current FP value.
	 */
	public int getFramePointer() {
		return framePointer;
	}

	/**
	 * <p>
	 * Returns the current status of the process.
	 * </p>
	 * 
	 * @return The current process state.
	 */
	public EjvmProcess.ProcessState getProcessState() {
		return processState;
	}

	/**
	 * <p>
	 * Changes this process's state to {@code state}.
	 * </p>
	 * 
	 * @param state
	 *            The new state of the process.
	 */
	protected void setProcessState(EjvmProcess.ProcessState state) {
		this.processState = state;
	}

	/**
	 * <p>
	 * Returns a view of the complete executable as a read-only {@link ByteBuffer}, i.e. the process memory minus heap
	 * and stack.
	 * </p>
	 * 
	 * @return A {@link ByteBuffer} offering read-only access to the complete memory area inhabited by the executable.
	 */
	public ByteBuffer getExecutableView() {
		int executableLength = program.getProgramNameOffset() + program.getProgramNameLength();
		this.workingMemory.position(0);
		int oldLimit = this.workingMemory.limit();
		this.workingMemory.limit(executableLength);
		ByteBuffer view = this.workingMemory.slice().asReadOnlyBuffer();
		this.workingMemory.limit(oldLimit);
		return view;
	}

	/**
	 * <p>
	 * Reads the method table index for the currently executed method from the stack and returns it.
	 * </p>
	 * 
	 * @return The index of the currently executed method in the method table.
	 */
	public int getCurrentMethodIndex() {
		// Method index is the first byte of the current stack frame.
		return workingMemory.get(framePointer - 1);
	}

	/**
	 * <p>
	 * Returns a view of the complete stack as a read-only {@link ByteBuffer}.
	 * </p>
	 * 
	 * @return A {@link ByteBuffer} ranging from the position of the stack pointer to the end of the working memory
	 *         assigned to this process.
	 */
	public ByteBuffer getStackView() {
		this.workingMemory.position(this.stackPointer);
		ByteBuffer stackView = this.workingMemory.slice();
		return stackView.asReadOnlyBuffer();
	}

	/**
	 * <p>
	 * Returns the program executed in this process.
	 * </p>
	 * 
	 * @return The {@link EjvmProgram} executed in this process.
	 */
	public EjvmProgram getProgram() {
		return this.program;
	}

	/**
	 * <p>
	 * Returns the {@link InputStream} that is used as the process' input channel.
	 * </p>
	 * 
	 * @return The {@link InputStream} delivering input to this process.
	 */
	public InputStream getInput() {
		return input;
	}

	/**
	 * <p>
	 * Returns the input channel of this process as an {@link InputStreamReader} to ease the reading of character data.
	 * </p>
	 * 
	 * @return This process' input channel as {@link InputStreamReader}.
	 */
	public InputStreamReader getInputAsReader() {
		if (inputAsReader == null) {
			inputAsReader = new InputStreamReader(input);
		}
		return inputAsReader;
	}

	/**
	 * <p>
	 * Sets this process' input channel to {@code inputStream}.
	 * </p>
	 * 
	 * @param inputStream
	 *            The {@link InputStream} the process is meant to receive input from.
	 */
	public void setInput(InputStream inputStream) {
		this.input = inputStream;
		this.inputAsReader = null;
	}

	/**
	 * <p>
	 * Returns the {@link PrintStream} that is used as the process' output channel.
	 * </p>
	 * 
	 * @return The {@link PrintStream} receiving output from this process.
	 */
	public PrintStream getOutput() {
		return output;
	}

	/**
	 * <p>
	 * Sets this process' output channel to {@code writer}.
	 * </p>
	 * 
	 * @param writer
	 *            The {@link PrintStream} the process is meant to send output to.
	 */
	public void setOutput(PrintStream writer) {
		this.output = writer;
	}

	/**
	 * <p>
	 * Returns the {@link OutputFormatter} that is used for this process.
	 * </p>
	 * 
	 * @return The {@link OutputFormatter}.
	 */
	public OutputFormatter getOutputFormatter() {
		return outputFormatter;
	}

	/**
	 * <p>
	 * Sets this processes output formatter to {@code formatter}.
	 * </p>
	 * 
	 * @param formatter
	 *            The {@link OutputFormatter} for the process.
	 */
	public void setOutputFormatter(OutputFormatter formatter) {
		this.outputFormatter = formatter;
	}

	/**
	 * <p>
	 * Returns the {@link EjvmExecutionListener} instances registered for this process.
	 * </p>
	 * 
	 * @return The execution listeners for this process.
	 */
	protected Set<EjvmExecutionListener> getExecutionListeners() {
		return this.executionListeners;
	}

	/**
	 * <p>
	 * Add another {@link EjvmExecutionListener} to this process.
	 * </p>
	 * 
	 * @param listener
	 *            The new {@link EjvmExecutionListener}.
	 */
	public void addExecutionListener(EjvmExecutionListener listener) {
		if (!executionListeners.contains(listener)) {
			executionListeners.add(listener);
		}
	}

	/**
	 * <p>
	 * Removes the {@link EjvmExecutionListener} {@code listener} from this process.
	 * </p>
	 * 
	 * @param listener
	 *            The listener to be removed.
	 * @return {@code true} if the listener was removed. See {@link Set#remove(Object)} for details about the
	 *         requirements.
	 */
	public boolean removeExecutionListener(EjvmExecutionListener listener) {
		return executionListeners.remove(listener);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((processState == null) ? 0 : processState.hashCode());
		result = prime * result + ((program == null) ? 0 : program.hashCode());
		result = prime * result + programCounter;
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
		EjvmProcess other = (EjvmProcess) obj;
		if (processState != other.processState)
			return false;
		if (program == null) {
			if (other.program != null)
				return false;
		} else if (!program.equals(other.program))
			return false;
		if (programCounter != other.programCounter)
			return false;
		return true;
	}

	/**
	 * <p>
	 * Simple enum to represent the process state.
	 * </p>
	 * 
	 * @author Sebastian Nicolai Kaupe
	 * @since 1.0
	 */
	public static enum ProcessState {
		READY,
		RUNNING,
		BLOCKED,
		HALTED,
		DONE;
	}

	/**
	 * <p>
	 * Simple enum to represent the output mode set for a process.
	 * </p>
	 * 
	 * @author Sebastian Nicolai Kaupe
	 * @since 1.0
	 */
	public static enum OutputFormatter {
		/**
		 * <p>
		 * Returns the character representation of {@code value}.
		 * </p>
		 */
		CHAR {
			@Override
			public String format(short value) {
				return Character.toString((char) value);
			}
		},
		/**
		 * <p>
		 * Returns the String representation of the numerical value of {@code value}.
		 * </p>
		 */
		NUMBER() {
			@Override
			public String format(short value) {
				return String.valueOf(0x0000FFFF & value);
			}
		};

		public abstract String format(short value);
	}
}