package org.ejvm.execution;

import org.ejvm.execution.EjvmExecutionException.Reason;
import org.ejvm.execution.EjvmInstruction.ArgumentType;
import org.ejvm.execution.EjvmProcess.ProcessState;
import org.ejvm.inspection.EjvmProgram;

/**
 * <p>
 * Represents a processor capable of executing eJVM bytecode instructions. This class is the frontend used to execute
 * the steps of an {@link EjvmProgram} using the associated methods ({@link #step()}, {@link #execute()}).
 * </p>
 * 
 * @author Sebastian Nicolai Kaupe
 * @since 1.0
 * @see EjvmMachine
 * @see EjvmProcess
 * @see EjvmInstruction
 */
public final class EjvmProcessor {

	/**
	 * <p>
	 * Default sleep interval between steps used when invoking the {@link #execute()} method.
	 * </p>
	 */
	public static final long DEFAULT_EXECUTION_INTERVAL = 5L;

	/**
	 * <p>
	 * The {@link EjvmMachine} this processor belongs to.
	 * </p>
	 */
	private final EjvmMachine machine;
	/**
	 * <p>
	 * The {@link EjvmProcess} currently assigned to this processor. If this is {@code null}, the processor should be in
	 * the {@link ProcessorState#IDLE} state.
	 * </p>
	 */
	private EjvmProcess assignedProcess;

	/**
	 * <p>
	 * The current state of the processor. If no {@link EjvmProcess} is assigned to it, the processor is in
	 * {@link ProcessorState#IDLE} mode. Once a process is assigned to it, it changes state to
	 * {@link ProcessorState#WORKING}. If the process is detached or completed (which results in automatic detachment),
	 * the processor returns to the idling. If the processor executes a {@link EjvmInstruction#HALT} or
	 * {@link EjvmInstruction#ERR}, its state is changed to {@link ProcessorState#HALTED}. Upon release of the HATL
	 * state, it returns to IDLE or WORKING, depending on whether their still is a process assigned to it or not.
	 * </p>
	 */
	private ProcessorState processorState = ProcessorState.IDLE;

	/**
	 * <p>
	 * Creates a new processor for {@code machine} to run an {@link EjvmProcess} on.
	 * </p>
	 * 
	 * @param machine
	 *            The {@link EjvmMachine} containing the processor.
	 */
	protected EjvmProcessor(final EjvmMachine machine) {
		this.machine = machine;
	}

	/**
	 * <p>
	 * Assigns {@code process} to this processor for execution and sets the processor state to
	 * {@link ProcessorState#WORKING}. If {@code process} is {@code null}, invoking this method will be equivalent to
	 * invoking {@link #idle()}.
	 * </p>
	 * 
	 * @param process
	 *            The {@link EjvmProcess} to be executed on this processor.
	 * @return The formerly assigned process (set to process state {@link EjvmProcess.ProcessState#READY} or
	 *         {@code null} if the processor was idling.
	 * @see #idle()
	 */
	protected EjvmProcess assignProcess(EjvmProcess process) {
		EjvmProcess formerProcess = idle();
		if (process != null) {
			this.assignedProcess = process;
			this.assignedProcess.setProcessState(ProcessState.RUNNING);
			this.processorState = ProcessorState.WORKING;
		}
		return formerProcess;
	}

	/**
	 * <p>
	 * Removes the currently assigned process and sets the processor state to {@link ProcessorState#IDLE}. The process's
	 * state is set to {@link EjvmProcess.ProcessState#READY}.
	 * </p>
	 * 
	 * @return The formerly assigned process or {@code null} if the processor was idling.
	 * @see #assignProcess(EjvmProcess)
	 * @see EjvmProcess.ProcessState
	 */
	protected EjvmProcess idle() {
		EjvmProcess formerProcess = this.assignedProcess;
		this.assignedProcess = null;
		if (formerProcess != null)
			formerProcess.setProcessState(ProcessState.READY);
		this.processorState = ProcessorState.IDLE;
		return formerProcess;
	}

	/**
	 * <p>
	 * Releases the halt status of the processor without change to the state of its process.
	 * </p>
	 */
	public void releaseHalt() {
		this.processorState = this.assignedProcess == null ? ProcessorState.IDLE : ProcessorState.WORKING;
	}

	/**
	 * <p>
	 * Resets the processor and the assigned process.
	 * </p>
	 */
	public void reset() {
		if (this.assignedProcess != null) {
			this.assignedProcess.reset();
			this.assignedProcess.setProcessState(ProcessState.RUNNING);
		}
		releaseHalt();
	}

	/**
	 * <p>
	 * Executes the next step (i.e. instruction) of the currently assigned {@link EjvmProcess}. If the instruction
	 * executed is the last instruction of the program, the processor sets itself to idle mode. If the processor is
	 * already idling (i.e. has no assigned process) or the process is halted, this method immediately returns.
	 * </p>
	 */
	public void step() {
		if (this.processorState == ProcessorState.IDLE)
			return;
		if (assignedProcess == null)
			// Should never happen because we check for idling beforehand..
			throw new IllegalStateException("assignedProcess is null even though the processor is NOT idling!");
		if (assignedProcess.getProcessState() == ProcessState.HALTED)
			return;
		/*
		 * We store a reference to the assigned process because we need it later on when invoking the afterExecution()
		 * method of the process' execution listeners and assignedProcess might be null at that time (if idle() was
		 * invoked).
		 */
		EjvmProcess process = assignedProcess;
		int oldProgramCounter = process.getProgramCounter();
		EjvmInstruction instruction = null;
		try {
			instruction = EjvmInstruction.forOpcode(process.fetchNextOpCode());
		} catch (IllegalArgumentException e) {
			// We've found an unknown OpCode - add additional debug information to the exception.
			throw new EjvmExecutionException(Reason.UNKNOWN_OPCODE, process, e);
		}
		// Read the instruction's arguments from memory and convert them to their corresponding Java type.
		ArgumentType[] argumentTypes = instruction.getArgumentTypes();
		Number[] arguments = new Number[argumentTypes.length];
		for (int i = 0; i < argumentTypes.length; i++) {
			byte[] argument = new byte[argumentTypes[i].getArgumentSize()];
			machine.readMemoryAt(process.getProgramCounter(), argument);
			// Adjust PC to skip the read bytes.
			assignedProcess.moveProgramCounter(argument.length);
			arguments[i] = argumentTypes[i].fromBytes(argument);
		}
		for (EjvmExecutionListener listener: process.getExecutionListeners()) {
			listener.beforeExecution(process, instruction, arguments);
		}
		try {
			instruction.execute(process, this, arguments);
		} catch (IllegalArgumentException e) {
			throw new EjvmExecutionException(Reason.WRONG_ARG_TYPE, process, e);
		}
		if (process.getProcessState() == ProcessState.BLOCKED) {
			// The process is now blocked, return the PC to its old value and try anew the next step.
			process.setProgramCounter(oldProgramCounter);
		} else if (process.getProcessState() == ProcessState.DONE) {
			idle();
		}
		for (EjvmExecutionListener listener: process.getExecutionListeners()) {
			listener.afterExecution(process, instruction, arguments);
		}
	}

	/**
	 * <p>
	 * Executes the currently assigned process until it is done. Afterwards, the processor is in
	 * {@link ProcessorState#IDLE} mode. Between each program step, the processor waits
	 * {@link #DEFAULT_EXECUTION_INTERVAL} milliseconds.
	 * </p>
	 */
	public void execute() {
		execute(DEFAULT_EXECUTION_INTERVAL);
	}

	/**
	 * <p>
	 * Executes the currently assigned process until it is done. Afterwards, the processor is in
	 * {@link ProcessorState#IDLE} mode. Between each program step, the processor waits {@code interval} milliseconds.
	 * </p>
	 * 
	 * @param interval
	 *            The sleep interval between program steps.
	 */
	public void execute(long interval) {
		while (canStep()) {
			step();
			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				throw new RuntimeException("Execution sleep phase interrupted!", e);
			}
		}
		// We do not need to explicitly set the processor to idle mode because step() is already doing it for us.
	}

	/**
	 * <p>
	 * Checks if processor and process allow for the execution of another step.
	 * </p>
	 * 
	 * @return {@code true} if another step can be executed, otherwise {@code false}.
	 */
	public boolean canStep() {
		return this.assignedProcess != null && this.assignedProcess.getProcessState() != ProcessState.DONE
				&& this.assignedProcess.getProcessState() != ProcessState.HALTED;
	}

	/**
	 * <p>
	 * Returns whether or not the processor is idling, i.e. has no process assigned to it.
	 * </p>
	 * 
	 * @return {@code true} if this processor has no assigned process, otherwise {@code false}.
	 */
	public boolean isIdle() {
		return processorState == ProcessorState.IDLE;
	}

	/**
	 * <p>
	 * Returns the {@link EjvmMachine} this processor belongs to.
	 * </p>
	 * 
	 * @return This processor's {@link EjvmMachine}.
	 */
	public EjvmMachine getMachine() {
		return this.machine;
	}

	/**
	 * <p>
	 * Returns the {@link EjvmProcess} currently assigned to this processor.
	 * </p>
	 * 
	 * @return The currently assigned process.
	 */
	public EjvmProcess getAssignedProcess() {
		return this.assignedProcess;
	}

	/**
	 * <p>
	 * Represents the state of the processor.
	 * </p>
	 * 
	 * @author Sebastian Nicolai Kaupe
	 * @since 1.0
	 */
	public static enum ProcessorState {
		/**
		 * <p>
		 * Signals that the processor is idling and may be assigned a new process without interruption of an existing
		 * one.
		 * </p>
		 */
		IDLE,
		/**
		 * <p>
		 * Signals that the processor currently has a process assigned. Assigning a new process stops the execution of
		 * the then-formerly assigned process until it is assigned to a processor once more.
		 * </p>
		 */
		WORKING
	}
}