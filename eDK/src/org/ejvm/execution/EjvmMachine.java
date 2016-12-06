package org.ejvm.execution;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;

import org.ejvm.inspection.EjvmProgram;
import org.ejvm.util.EjvmVersion;

/**
 * <p>
 * Represents a machine capable of executing eJVM bytecode and acts as the machine's operating system.
 * </p>
 * 
 * @author Sebastian Nicolai Kaupe
 * @since 1.0
 * @see EjvmProcessor
 * @see EjvmProcess
 */
public final class EjvmMachine {

	/**
	 * <p>
	 * The working memory available to the machine.
	 * </p>
	 * 
	 * @see #loadIntoWorkingMemory(byte[], int, int)
	 * @see #loadProgram(Path)
	 * @see #readMemoryAt(int, byte[])
	 * @see #getMemoryView(int, int, boolean)
	 */
	private ByteBuffer workingMemory;

	/**
	 * <p>
	 * The array for the {@link EjvmProcessor} instances belonging to the machine.
	 * </p>
	 */
	private final EjvmProcessor[] cores;

	/**
	 * <p>
	 * Creates a new {@code EjvmMachine} with a single core and a working memory of {@code memorySize} byte. However,
	 * {@code memorySize} is expected to be at least 64 byte.
	 * </p>
	 * 
	 * @param memorySize
	 *            The size of the working memory available to the machine. 64 byte minimum.
	 * @throws IllegalArgumentException
	 *             If {@code memorySize} is less than 64 byte.
	 */
	public EjvmMachine(int memorySize) {
		this(memorySize, 1);
	}

	/**
	 * <p>
	 * Creates a new {@link EjvmMachine} with {@code numberOfCores} cores and a working memory of {@code memorySize}
	 * byte. However, {@code memorySize} is expected to be at least 64 byte.
	 * </p>
	 * 
	 * @param memorySize
	 *            The size of the working memory available to the machine. 64 byte minimum.
	 * @param numberOfCores
	 *            The number of {@link EjvmProcessor}s this machine has.
	 * @throws IllegalArgumentException
	 *             If {@code memorySize} is less than 64 byte.
	 */
	private EjvmMachine(int memorySize, int numberOfCores) {
		if (memorySize < 64) {
			throw new IllegalArgumentException("The memory size must be atleast 64 byte!");
		}
		this.workingMemory = ByteBuffer.allocate(memorySize).order(ByteOrder.BIG_ENDIAN);
		this.cores = new EjvmProcessor[numberOfCores];
		for (int i = 0; i < numberOfCores; i++) {
			this.cores[i] = new EjvmProcessor(this);
		}
	}

	/**
	 * <p>
	 * Resets the virtual machine. All processes are detached from their processors and the working memory is replaced,
	 * making sure old processes can not interfere with the machine after its reset.
	 * </p>
	 */
	public void reset() {
		for (EjvmProcessor processor: cores) {
			processor.idle();
		}
		this.workingMemory = ByteBuffer.allocate(workingMemory.capacity());
	}

	/**
	 * <p>
	 * Loads the program from {@code executable} into the machine's working memory and creates a new {@link EjvmProcess}
	 * for the execution of the program. The new process is then assigned to the machine's processor. Once this method
	 * returns, execution of the loaded program is possible using the appropriate methods of the {@link EjvmProcessor}.
	 * </p>
	 * <p>
	 * Any data contained previously in the working memory may be overwritten, if needed. Portions unaffected by the
	 * loading of the program do <i>not</i> get wiped.
	 * </p>
	 * <p>
	 * Please note that the first version of the virtual machine of the eJVM Development Kit does not support parallel
	 * (or pseudo-parallel) execution of programs: it is a strict single-program machine.
	 * </p>
	 * 
	 * @param executable
	 *            The file to read the program from.
	 * @return The {@link EjvmProcessor} to whom the newly-created process was assigned.
	 * @throws IllegalArgumentException
	 *             If {@code executable} points to a non-existent file.
	 * @throws NotExecutableException
	 *             If {@code executable} cannot be executed, e.g. because the required eJVM version is not supported.
	 * @see #loadIntoWorkingMemory(byte[], int, int)
	 */
	public EjvmProcessor loadProgram(Path executable) {
		return loadProgram(EjvmProgram.fromFile(executable, workingMemory, 0));

	}

	/**
	 * <p>
	 * Loads the program {@code program} into the machine's working memory and creates a new {@link EjvmProcess} for the
	 * execution of the program. The new process is then assigned to the machine's processor. Once this method returns,
	 * execution of the loaded program is possible using the appropriate methods of the {@link EjvmProcessor}. The input
	 * and output channels for the process are set to {@link System#in} and {@link System#out} and may be changed before
	 * execution using the appropriate methods.
	 * </p>
	 * <p>
	 * Any data contained previously in the working memory may be overwritten, if needed. Portions unaffected by the
	 * loading of the program do <i>not</i> get wiped.
	 * </p>
	 * <p>
	 * Please note that the first version of the virtual machine of the eJVM Development Kit does not support parallel
	 * (or pseudo-parallel) execution of programs: it is a strict single-process machine.
	 * </p>
	 * 
	 * @param program
	 *            The {@link EjvmProgram} to be loaded into the machine.
	 * @return The {@link EjvmProcessor} to whom the newly-created process was assigned.
	 * @throws NotExecutableException
	 *             If {@code executable} cannot be executed, e.g. because the required eJVM version is not supported.
	 * @see #loadIntoWorkingMemory(byte[], int, int)
	 */
	public EjvmProcessor loadProgram(EjvmProgram program) {
		EjvmVersion requiredVersion = EjvmVersion.fromByte(program.getRequiredEjvmVersion());
		if (!EjvmVersion.canExecute(program.getRequiredEjvmVersion()))
			throw new NotExecutableException("Required eJVM version not supported: " + requiredVersion, program);
		EjvmProcess process = new EjvmProcess(program, getMemoryView(0, workingMemory.capacity(), false));
		process.setInput(System.in);
		process.setOutput(System.out);
		cores[0].assignProcess(process);
		return cores[0];
	}

	/**
	 * <p>
	 * Inserts {@code numberOfBytes} bytes from {@code content} into the machine's working memory, starting insertion
	 * into working memory at index {@code offset}.
	 * </p>
	 * 
	 * @param content
	 *            The content to be loaded into the machine's memory.
	 * @param offset
	 *            The position from which to start from.
	 * @param numberOfBytes
	 *            The number of bytes to copy from {@code content} into the working memory.
	 * @throws IndexOutOfBoundsException
	 *             If the offset is smaller 0 or bigger than the working memory's capacity or if the length of
	 *             {@code content} combined with {@code offset} is bigger than the working memory's capacity.
	 * @throws IllegalArgumentException
	 *             If {@code numberOfBytes} is negative.
	 * @see #loadProgram(Path)
	 * @see #getMemoryView(int, int, boolean)
	 */
	public void loadIntoWorkingMemory(byte[] content, int offset, int numberOfBytes) {
		if (numberOfBytes < 0) {
			throw new IllegalArgumentException("numberOfBytes must not be negative.");
		}
		if (offset < 0 || offset >= this.workingMemory.capacity()) {
			throw new IndexOutOfBoundsException("Invalid offset: " + offset);
		}
		if (numberOfBytes + offset > this.workingMemory.capacity()) {
			throw new IndexOutOfBoundsException(String.format("Insufficient memory. Memory size: %d Byte. Needed: %d.",
					this.workingMemory.capacity(), content.length + offset));
		}
		workingMemory.position(offset);
		workingMemory.put(content, 0, numberOfBytes);
	}

	/**
	 * <p>
	 * Returns a {@link ByteBuffer} providing the caller with a view into the machine's working memory. The view starts
	 * at {@code offset} and contains {@code length} bytes. Depending on {@code readOnly}, the returned
	 * {@code ByteBuffer} may be read-only.
	 * </p>
	 * 
	 * @param offset
	 *            The starting byte offset for the returned view.
	 * @param length
	 *            The length of the view.
	 * @param readOnly
	 *            If {@code true}, the buffer will be read-only.
	 * @return A {@link ByteBuffer} acting as a view into the working memory.
	 * @throws IndexOutOfBoundsException
	 *             If {@code offset} is negative or the combination of {@code offset} and {@code length} exceeds the
	 *             capacity of the working memory.
	 */
	public ByteBuffer getMemoryView(int offset, int length, boolean readOnly) {
		if (offset < 0 || (offset + length) > workingMemory.capacity()) {
			throw new IndexOutOfBoundsException();
		}
		this.workingMemory.position(offset);
		ByteBuffer view = (ByteBuffer) workingMemory.slice().limit(length);
		return readOnly ? view.asReadOnlyBuffer() : view;
	}

	/**
	 * <p>
	 * Reads {@code dst.length} bytes from the machine's working memory into {@code dst}, starting at {@code offset}.
	 * </p>
	 * 
	 * @param offset
	 *            The offset in the working memory from which to start reading.
	 * @param dst
	 *            The {@code byte[]} to place the read bytes in.
	 */
	public void readMemoryAt(int offset, byte[] dst) {
		if (offset < 0 || (offset + dst.length) > workingMemory.capacity()) {
			throw new IndexOutOfBoundsException();
		}
		this.workingMemory.position(offset);
		this.workingMemory.get(dst);
	}

	/**
	 * <p>
	 * Returns the number of cores working in this machine.
	 * </p>
	 * 
	 * @return The number of {@link EjvmProcessor} instances available to the machine.
	 */
	public int getNumberOfCores() {
		return cores.length;
	}

	/**
	 * <p>
	 * Returns the {@link EjvmProcessor} number {@code core}.
	 * </p>
	 * 
	 * @param core
	 *            The index of the core in the core table.
	 * @return The {@link EjvmProcessor} from the core table at {@code core}.
	 */
	public EjvmProcessor getProcessor(int core) {
		return cores[core];
	}
}