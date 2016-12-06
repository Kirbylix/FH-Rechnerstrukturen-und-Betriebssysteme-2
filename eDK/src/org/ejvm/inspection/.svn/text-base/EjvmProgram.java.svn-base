package org.ejvm.inspection;

import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.ejvm.execution.EjvmProcess;
import org.ejvm.util.ByteUtil;

/**
 * <p>
 * Represents a compiled eJVM program and offers methods to retrieve information about said program from its compiled
 * form, e.g. the number of methods or constants.
 * </p>
 * 
 * @author Sebastian Nicolai Kaupe
 * @since 1.0
 * @see EjvmProcess
 */
public final class EjvmProgram {

	/**
	 * <p>
	 * Magic number (ASCII-encoded 'eJVM') found at the beginning of every compiled eJVM program file.
	 * </p>
	 */
	public static final byte[] MAGIC_NUMBER = new byte[] { 0x65, 0x4A, 0x56, 0x4D };

	/**
	 * <p>
	 * Number of bytes used for offsets in the executable.
	 * </p>
	 */
	public static final int ADDRESS_SIZE = 4;

	/**
	 * <p>
	 * Number of bytes an entry of the method table requires.
	 * </p>
	 */
	public static final int MTBL_ENTRY_SIZE = 10;

	/**
	 * <p>
	 * Address of the vno field in the executable, containing the required minimum version of the eJVM language needed
	 * to execute this program.
	 * </p>
	 */
	public static final int VNO_ADDRESS = 4;

	/**
	 * <p>
	 * Address of the pnsize field in the executable, containing the length of the program's name (in bytes).
	 * </p>
	 */
	public static final int PNSIZE_ADDRESS = 5;

	/**
	 * <p>
	 * Address of the pnoff field in the executable, containing the byte offset to the first byte of the program's name.
	 * </p>
	 */
	public static final int PNADDR_ADDRESS = 6;

	/**
	 * <p>
	 * Address of the mcnt field in the executable, containing the number of methods in the method table.
	 * </p>
	 */
	public static final int MCNT_ADDRESS = 10;

	/**
	 * <p>
	 * Address of the ccnt field in the executable, containing the number of entries in the program's constant table.
	 * </p>
	 */
	public static final int CCNT_ADDRESS = 11;

	/**
	 * <p>
	 * Address of the errcnt field in the executable, containing the number of entries in the program's error message
	 * table.
	 * </p>
	 */
	public static final int ERRCNT_ADDRESS = 12;

	/**
	 * <p>
	 * Address of the method table in the executable, containing an entry for every compiled method of the program.
	 * </p>
	 */
	public static final int MTBL_ADDRESS = 13;

	/**
	 * <p>
	 * Static part of the address of the ctbl field in the executable, containing all constants defined in this program.
	 * Adding the byte size of the method table yields the real offset to the constant table.
	 * </p>
	 */
	public static final int CTBL_BASE = 13;

	/**
	 * <p>
	 * Static part of the address of the text section of the executable, containing the compiled method code. Adding the
	 * byte size of the method table and the constant table yields the real offset to the text section.
	 * </p>
	 */
	public static final int TEXT_BASE = 13;

	/**
	 * <p>
	 * The bytes of the executable.
	 * </p>
	 */
	private final ByteBuffer bytes;

	/**
	 * <p>
	 * Private cache for {@link MethodInformation} objects to avoid the unnecessary creation of a new object for every
	 * invocation of {@link #getMethodInformation(int)}.
	 * </p>
	 */
	private final List<MethodInformation> methodInformations;

	/**
	 * <p>
	 * Creates a new {@code eJVMProgram} with {@code bytes} containing the executable's bytes. Checks for formal
	 * validity are performed.
	 * </p>
	 * <p>
	 * <strong>Important:</strong> The limit of {@code bytes} may be used for length checks, so it is important that it
	 * is set in a way that {@code bytes} does not to contain any bytes that do not belong to the executable.
	 * </p>
	 * 
	 * @param bytes
	 *            The byte content of the executable file.
	 */
	public EjvmProgram(ByteBuffer bytes) {
		this(bytes, true);
	}

	/**
	 * <p>
	 * Creates a new {@code eJVMProgram} with {@code bytes} containing the executable's bytes. Checks if the executable
	 * is formally valid are optional.
	 * </p>
	 * <p>
	 * <strong>Important:</strong> The limit of {@code bytes} may be used for length checks, so it is important that it
	 * is set in a way that {@code bytes} does not to contain any bytes that do not belong to the executable.
	 * </p>
	 * 
	 * @param bytes
	 *            The byte content of the executable file.
	 * @param performChecks
	 *            Perform checks after instantiation to make sure this is a (formally) valid eJVM program.
	 */
	public EjvmProgram(ByteBuffer bytes, boolean performChecks) {
		this.bytes = bytes;
		this.methodInformations = readMethodInformation();
		if (performChecks) {
			// Check magic number.
			byte[] magic = new byte[4];
			bytes.position(0);
			bytes.get(magic);
			if (!Arrays.equals(magic, MAGIC_NUMBER)) {
				throw new InvalidEjvmProgramException("Wrong magic number: "
						+ new String(magic, Charset.forName("UTF-16BE")));
			}
			// Check executable size. Name is at the end, so offset to name + length of name == executable size.
			int executableSize = getProgramNameLength() + getProgramNameOffset();
			if (bytes.limit() != executableSize) {
				throw new InvalidEjvmProgramException("Wrong executable size. Expected: " + executableSize
						+ ", found: " + bytes.limit());
			}
		}
	}

	/**
	 * <p>
	 * Returns the length of the executed program's name (in bytes) as described by the executable itself.
	 * </p>
	 * 
	 * @return The length of the program name in bytes.
	 */
	public int getProgramNameLength() {
		return bytes.get(PNSIZE_ADDRESS);
	}

	/**
	 * <p>
	 * Returns the byte offset from the first byte of the executable to the first byte of it's program's name.
	 * </p>
	 * 
	 * @return The name offset in bytes.
	 */
	public int getProgramNameOffset() {
		return bytes.getInt(PNADDR_ADDRESS);
	}

	/**
	 * <p>
	 * Extracts and returns the program name from the executable.
	 * </p>
	 * 
	 * @return The name of the program.
	 */
	public String getProgramName() {
		int pnsize = bytes.get(PNSIZE_ADDRESS);
		byte[] nameBytes = new byte[pnsize];
		bytes.position(bytes.getInt(PNADDR_ADDRESS));
		bytes.get(nameBytes);
		return new String(nameBytes, Charset.forName("UTF-16BE"));
	}

	/**
	 * <p>
	 * Returns the minimal version number of the eJVM language needed to execute this program. The upper four bit
	 * represent the major, the lower four bit the minor component of the version number.
	 * </p>
	 * <p>
	 * This version number can be converted into an easier to read format using the following code:
	 * </p>
	 * 
	 * <pre>
	 * {@code
	 * 	byte versionByte = program.getRequiredeJVMVersion();
	 * String versionNumber = (versionByte >> 4) + "." + versionByte & 0x0F;
	 * }</pre>
	 * 
	 * @return The required eJVM version for execution of this executable's program.
	 */
	public byte getRequiredEjvmVersion() {
		return bytes.get(VNO_ADDRESS);
	}

	/**
	 * <p>
	 * Returns the number of constants contained in the executable's constant table.
	 * </p>
	 * 
	 * @return The number of constants (up to 255).
	 */
	public byte getNumberOfConstants() {
		return bytes.get(CCNT_ADDRESS);
	}

	/**
	 * <p>
	 * Returns the value of the constant at {@code index}. Please note that it is the index inside the constant table,
	 * <i>not</i> the offset from the beginning of the executable or the constant table.
	 * </p>
	 * 
	 * @param index
	 *            The index of the constant in the constant table.
	 * @return The value of the constant at {@code index}.
	 */
	public short getConstant(int index) {
		return bytes.getShort(CTBL_BASE + (bytes.get(MCNT_ADDRESS) * MTBL_ENTRY_SIZE) + (index * 2));
	}

	/**
	 * <p>
	 * Returns the number of entries in the method table.
	 * </p>
	 * 
	 * @return The number of methods.
	 */
	public int getNumberOfMethods() {
		return bytes.get(MCNT_ADDRESS);
	}

	/**
	 * <p>
	 * Returns the offset from the beginning of the executable's bytes to the first byte of the compiled code of the
	 * method at {@code index} in the method table.
	 * </p>
	 * 
	 * @param index
	 *            The index of the method in the method table.
	 * @return The offset to the first byte of the compiled method code.
	 * @throws IndexOutOfBoundsException
	 *             If index is smaller 0 or bigger than the actual method table.
	 */
	public int getMethodOffset(int index) {
		// First four byte of the method table entry is the method address, so get them as a single int.
		return bytes.getInt(MTBL_ADDRESS + (MTBL_ENTRY_SIZE * index));
	}

	/**
	 * <p>
	 * Returns the offste to the main method's first instruction.
	 * </p>
	 * 
	 * @return The offset of the main method.
	 */
	public int getMainMethodOffset() {
		// main() is defined to be at index 0 of the method table.
		return getMethodOffset(0);
	}

	/**
	 * <p>
	 * Returns the byte offset to the beginning of the program's text section.
	 * </p>
	 * 
	 * @return The offset to the text section.
	 */
	public int getTextSectionOffset() {
		return TEXT_BASE + (bytes.get(MCNT_ADDRESS) * MTBL_ENTRY_SIZE) + (bytes.get(CCNT_ADDRESS) * 2);
	}

	/**
	 * <p>
	 * Returns the size of the program's text section (i.e. the compiled instructions).
	 * </p>
	 * 
	 * @return The size of the text section.
	 */
	public int getTextSectionSize() {
		int textSectionSize = getMethodInformation(0).getDebugInformationAddress();
		if (textSectionSize != 0) {
			textSectionSize = textSectionSize - getTextSectionOffset();
		} else {
			textSectionSize = getProgramNameOffset() - getTextSectionOffset();
		}
		return textSectionSize;
	}

	/**
	 * <p>
	 * Returns the number of error messages in the executable.
	 * </p>
	 * 
	 * @return The number of error messages.
	 */
	public int getNumberOfErrorMessages() {
		return bytes.get(ERRCNT_ADDRESS);
	}

	/**
	 * <p>
	 * Returns the offset to the first byte of the first entry in the error message table. If the table contains no
	 * error messages, -1 is returned.
	 * </p>
	 * 
	 * @return The offset to the start of the error message table or -1 if no error messages were found in the
	 *         executable.
	 */
	public int getErrorMessageTableOffset() {
		if (bytes.get(ERRCNT_ADDRESS) == 0) {
			return -1;
		}
		MethodInformation lastMethod = getMethodInformation(bytes.get(MCNT_ADDRESS) - 1);
		int address = lastMethod.getDebugInformationAddress();
		if (address != 0) {
			address += lastMethod.getMethodNameSize() + 3; // Add three to skip mnsize and msize fields.
		} else {
			// No debug information compiled into executable, calculate error message table position.
			address = getTextSectionOffset() + getTextSectionSize();
		}
		return address;
	}

	/**
	 * <p>
	 * Retrieves the error message from {@code index} in the error message table and returns it.
	 * </p>
	 * 
	 * @param index
	 * @return The error message from index {@code index} or an empty string if the executable does not contain any
	 *         error messages.
	 * @throws IndexOutOfBoundsException
	 */
	public String getErrorMessage(int index) {
		if (index < 0 || index >= bytes.get(ERRCNT_ADDRESS)) {
			throw new IndexOutOfBoundsException();
		}
		int currentMessageAddress = getErrorMessageTableOffset();
		if (currentMessageAddress == -1) {
			return "";
		}
		int currentIndex = 0;
		while (currentIndex < index) {
			int messageLength = bytes.get(currentMessageAddress++);
			currentMessageAddress += messageLength;
			currentIndex++;
		}
		int messageLength = bytes.get(currentMessageAddress++);
		byte[] messageBytes = new byte[messageLength];
		bytes.position(currentMessageAddress);
		bytes.get(messageBytes);
		return new String(messageBytes, Charset.forName("UTF-16BE"));
	}

	/**
	 * <p>
	 * Returns an <em>unmodifiable</em> {@link List} of {@link MethodInformation} objects containing all available
	 * information for a method included in the executable. If the executable was compiled without debug information,
	 * the corresponding fields in the {@link MethodInformation} object will be filled with default values.
	 * </p>
	 * 
	 * @return An unmodifiable {@link List} of {@link MethodInformation} objects.
	 * @see #getMethodInformation(int)
	 */
	public List<MethodInformation> getMethodInformation() {
		return Collections.unmodifiableList(methodInformations);
	}

	/**
	 * <p>
	 * Returns a {@link MethodInformation} object containing all available information for the method found at method
	 * table index {@code index} included in the executable. If the executable was compiled without debug information,
	 * the corresponding fields in the {@link MethodInformation} object will be filled with default values.
	 * </p>
	 * 
	 * @param index
	 *            The index of the method in the method table.
	 * @return A {@link MethodInformation} object for the method at {@code index}.
	 * @throws IndexOutOfBoundsException
	 *             If {@code index} is negative or bigger than the highest method index in the method table.
	 * @see #getMethodInformation()
	 */
	public MethodInformation getMethodInformation(int index) {
		return methodInformations.get(index);
	}

	/**
	 * <p>
	 * Returns the length of the complete program in bytes.
	 * </p>
	 * 
	 * @return The byte length of the program.
	 */
	public int getProgramLength() {
		return bytes.limit();
	}

	/**
	 * <p>
	 * Returns a read-only {@link ByteBuffer} to access the program bytes.
	 * </p>
	 * 
	 * @return A read-only {@link ByteBuffer} for the program bytes.
	 */
	public ByteBuffer getByteView() {
		bytes.position(0);
		return bytes.slice().asReadOnlyBuffer();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bytes == null) ? 0 : bytes.hashCode());
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
		EjvmProgram other = (EjvmProgram) obj;
		if (bytes == null) {
			if (other.bytes != null)
				return false;
		} else if (!bytes.equals(other.bytes))
			return false;
		return true;
	}

	/**
	 * <p>
	 * Returns a {@link List} of {@link MethodInformation} objects containing all available information for a method
	 * included in the executable. If the executable was compiled without debug information, the corresponding fields in
	 * the {@link MethodInformation} object will be filled with default values.
	 * </p>
	 * 
	 * @return A {@link List} of {@link MethodInformation} objects.
	 * @see #getMethodInformation(int)
	 */
	private final List<MethodInformation> readMethodInformation() {
		List<MethodInformation> list = new ArrayList<>();
		int numberOfMethods = bytes.get(MCNT_ADDRESS);
		for (int index = 0; index < numberOfMethods; index++) {
			byte[] mtblEntry = new byte[MTBL_ENTRY_SIZE];
			bytes.position(MTBL_ADDRESS + (index * MTBL_ENTRY_SIZE));
			bytes.get(mtblEntry);
			int debugAddress = ByteUtil.intFromBytes(mtblEntry, 6);
			byte[] dbgInfo = null;
			if (debugAddress != 0) {
				int methodNameSize = bytes.get(debugAddress);
				dbgInfo = new byte[3 + methodNameSize];
				bytes.position(debugAddress);
				bytes.get(dbgInfo);
			}
			list.add(new MethodInformation(index, mtblEntry, dbgInfo));
		}
		return list;
	}

	/**
	 * <p>
	 * Creates a new {@link EjvmProgram} with the content from {@code executable}.
	 * </p>
	 * 
	 * @param executable
	 *            The file to read the program from.
	 * @return An {@link EjvmProgram} instance representing the eJVM program read from {@code executable}.
	 * @throws IllegalArgumentException
	 *             If {@code executable} does not exist.
	 * @throws RuntimeException
	 *             In case of any IO error.
	 */
	public static EjvmProgram fromFile(Path executable) {
		if (!Files.exists(executable)) {
			throw new IllegalArgumentException(new NoSuchFileException(executable.toString()));
		}
		try {
			return new EjvmProgram(ByteBuffer.wrap(Files.readAllBytes(executable)));
		} catch (IOException e) {
			throw new RuntimeException("Unable to read executable file.", e);
		}
	}

	/**
	 * <p>
	 * Creates a new {@link EjvmProgram} with the content from {@code executable}. Instead of creating a new
	 * {@link ByteBuffer} for the program, {@code buffer} is used to store the program bytes, starting at {@code offset}
	 * .
	 * </p>
	 * <p>
	 * <strong>Important: </strong> This method is <em>not</em> free of side effects. Not only the content of
	 * {@code buffer} is changed, but limit and position may also be changed during execution of the method. However,
	 * they limit are restored before this method returns.
	 * </p>
	 * 
	 * @param executable
	 *            The file to read the program from.
	 * @param buffer
	 *            The {@link ByteBuffer} to store the program bytes in.
	 * @param offset
	 *            The starting index.
	 * @return An {@link EjvmProgram} instance representing the eJVM program read from {@code executable}.
	 * @throws IllegalArgumentException
	 *             If {@code executable} does not exist.
	 * @throws BufferOverflowException
	 *             If the capacity or limit of {@code buffer} are insufficient to store the program.
	 * @throws RuntimeException
	 *             In case of any IO error.
	 */
	public static EjvmProgram fromFile(Path executable, ByteBuffer buffer, int offset) {
		if (!Files.exists(executable)) {
			throw new IllegalArgumentException(new NoSuchFileException(executable.toString()));
		}
		int programLength = 0;
		int oldPosition = buffer.position();
		try (InputStream in = Files.newInputStream(executable)) {
			// Use a magic number instead of a constant - we only need it this one time and the meaning is clear.
			byte[] arr = new byte[1024];
			int bytesRead;
			while ((bytesRead = in.read(arr)) > 0) {
				buffer.position(offset + programLength);
				buffer.put(arr, 0, bytesRead);
				programLength += bytesRead;
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to read executable file.", e);
		}
		int oldLimit = buffer.limit();
		ByteBuffer programBuffer = ((ByteBuffer) buffer.position(0).limit(programLength)).slice().asReadOnlyBuffer();
		buffer.limit(oldLimit);
		buffer.position(oldPosition);
		return new EjvmProgram(programBuffer);
	}
}