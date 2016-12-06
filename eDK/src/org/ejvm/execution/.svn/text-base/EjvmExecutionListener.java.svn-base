package org.ejvm.execution;

import org.ejvm.inspection.EjvmProgram;

/**
 * <p>
 * Provides an interface for listeners informed about the steps of the execution of an {@link EjvmProgram}. Attach an
 * instance of this interface to an {@link EjvmProcess} to have its methods invoked before and after the execution of
 * the program's instructions.
 * </p>
 * 
 * @author Sebastian Nicolai Kaupe
 * @since 1.0
 */
public interface EjvmExecutionListener {

	/**
	 * <p>
	 * Invoked before the execution of every instruction of an {@link EjvmProcess}.
	 * </p>
	 * 
	 * @param process
	 *            The {@link EjvmProcess} in which the {@link EjvmProgram} is executed.
	 * @param instruction
	 *            The {@link EjvmInstruction} executed.
	 * @param args
	 *            The arguments for the instruction.
	 */
	void beforeExecution(EjvmProcess process, EjvmInstruction instruction, Number... args);

	/**
	 * <p>
	 * Invoked after the execution of every instruction of an {@link EjvmProcess}.
	 * </p>
	 * 
	 * @param process
	 *            The {@link EjvmProcess} in which the {@link EjvmProgram} is executed.
	 * @param instruction
	 *            The {@link EjvmInstruction} executed.
	 * @param args
	 *            The arguments for the instruction.
	 */
	void afterExecution(EjvmProcess process, EjvmInstruction instruction, Number... args);
}