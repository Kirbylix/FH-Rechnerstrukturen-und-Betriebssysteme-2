package org.ejvm.util;

import java.util.Deque;
import java.util.LinkedList;

/**
 * <p>
 * Utility class to work with eJVM version numbers.
 * </p>
 * 
 * @author Sebastian Nicolai Kaupe
 * @since 1.0
 */
public final class EjvmVersion {

	/**
	 * <p>
	 * Holds the versions known to this eJVM machine implementation. It is organized as a stack, with the current
	 * version on the top.
	 * </p>
	 */
	private static final Deque<EjvmVersion> knownVersions = new LinkedList<>();
	static {
		knownVersions.push(new EjvmVersion((byte) 0x10));
	}

	/**
	 * <p>
	 * The version byte, as it is also compiled into any eJVM executable file.
	 * </p>
	 */
	private final byte version;

	/**
	 * <p>
	 * A list of eJVM version that can also be executed on a machine of this version. A machine can always execute
	 * programs that require its exact version; therefore, it is <em>not</em> part of this list.
	 * </p>
	 */
	private final EjvmVersion[] executables;

	/**
	 * <p>
	 * Creates a new eJVM version object.
	 * </p>
	 * 
	 * @param version
	 *            The version in byte form.
	 * @param executables
	 *            The list of versions that can also be executed on this machine.
	 */
	private EjvmVersion(final byte version, EjvmVersion... executables) {
		this.version = version;
		this.executables = executables;
	}

	/**
	 * <p>
	 * Returns the version byte for this version number.
	 * </p>
	 * 
	 * @return The version byte.
	 */
	public byte getVersionByte() {
		return version;
	}

	/**
	 * <p>
	 * Returns the major component of the version number.
	 * </p>
	 * 
	 * @return The major version.
	 */
	public int getMajor() {
		return (version >> 4) & 0x0F;
	}

	/**
	 * <p>
	 * Returns the minor component of the version number.
	 * </p>
	 * 
	 * @return The minor version.
	 */
	public int getMinor() {
		return version & 0x0F;
	}

	@Override
	public String toString() {
		return getMajor() + "." + getMinor();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + version;
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
		EjvmVersion other = (EjvmVersion) obj;
		if (version != other.version)
			return false;
		return true;
	}

	/**
	 * <p>
	 * Returns the version of this eJVM machine implementation.
	 * </p>
	 * 
	 * @return This implementation's eJVM version.
	 */
	public static EjvmVersion currentVersion() {
		return knownVersions.peek();
	}

	/**
	 * <p>
	 * Creates, if needed, a new {@link EjvmVersion} from {@code version} by interpreting it as an eJVM version number.
	 * If the version number is already known, the existing object is returned.
	 * </p>
	 * 
	 * @param version
	 *            The version byte to interpret.
	 * @return A new {@link EjvmVersion} for every unknown eJVM version, the already existing object for a known
	 *         version.
	 */
	public static EjvmVersion fromByte(final byte version) {
		for (EjvmVersion v: knownVersions) {
			if (v.version == version)
				return v;
		}
		return new EjvmVersion(version);
	}

	/**
	 * <p>
	 * Checks if a program with the eJVM version number {@code version} can be executed on this eJVM machine
	 * implementation.
	 * </p>
	 * 
	 * @param version
	 *            The eJVM version the program requires.
	 * @return {@code true} if the program can be executed on this implementation, otherwise {@code false}.
	 */
	public static boolean canExecute(final byte version) {
		return canExecute(fromByte(version));
	}

	/**
	 * <p>
	 * Checks if a program with the eJVM version number {@code version} can be executed on this eJVM machine
	 * implementation.
	 * </p>
	 * 
	 * @param version
	 *            The {@link EjvmVersion} the program requires.
	 * @return {@code true} if the program can be executed on this implementation, otherwise {@code false}.
	 */
	public static boolean canExecute(final EjvmVersion version) {
		return currentVersion().equals(version) || ArrayUtil.contains(currentVersion().executables, version);
	}

	/**
	 * <p>
	 * Converts the version byte {@code version} into a {@link String} representation.
	 * </p>
	 * 
	 * @param version
	 *            The version byte.
	 * @return A readable String-version of the version byte.
	 */
	public static String asString(final byte version) {
		return (version >> 4) + "." + (version & 0x0F);
	}
}