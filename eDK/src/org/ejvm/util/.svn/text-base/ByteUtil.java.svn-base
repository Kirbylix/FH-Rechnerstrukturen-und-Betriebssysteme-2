package org.ejvm.util;

/**
 * <p>
 * Utility class that contains generic helper methods for the handling of single bytes and byte arrays.
 * </p>
 * 
 * @author Sebastian Nicolai Kaupe
 * @since 1.0
 */
public class ByteUtil {

	/**
	 * <p>
	 * Converts the content of {@code bytes} into a single {@code short}.
	 * </p>
	 * 
	 * @param bytes
	 *            A byte array to be interpreted as a {@code short}.
	 * @return The {@code short} representation of the contents of {@code bytes}.
	 * @throws IllegalArgumentException
	 *             If {@code bytes} is not exactly two bytes long.
	 * @since 1.0
	 */
	public static short shortFromBytes(byte[] bytes) {
		if (bytes.length != 2) {
			throw new IllegalArgumentException("A byte[] containing a short must only be 2 bytes long!");
		}
		return (short) ((bytes[0] << 8) | (bytes[1] & 0xFF));
	}

	/**
	 * <p>
	 * Converts two bytes of the content of {@code bytes} into a single {@code short}, with the first byte being at
	 * index {@code offset}.
	 * </p>
	 * 
	 * @param bytes
	 *            A byte array to be interpreted as a {@code short}.
	 * @param offset
	 *            The index offset to the first byte.
	 * @return The {@code short} representation of the contents of {@code bytes}.
	 * @throws IllegalArgumentException
	 *             If {@code bytes} is shorter than {@code offset + 2} elements.
	 * @since 1.0
	 */
	public static short shortFromBytes(byte[] bytes, int offset) {
		if (bytes.length <= offset + 1) {
			throw new IllegalArgumentException("Not enough bytes to convert into a short!");
		}
		return (short) ((bytes[offset] << 8) | (bytes[offset + 1] & 0xFF));
	}

	/**
	 * <p>
	 * Converts the content of {@code bytes} into a single {@code int}.
	 * </p>
	 * 
	 * @param bytes
	 *            A byte array to be interpreted as a {@code int}.
	 * @return The {@code int} representation of the contents of {@code bytes}.
	 * @throws IllegalArgumentException
	 *             If {@code bytes} is not exactly four bytes long.
	 * @since 1.0
	 */
	public static int intFromBytes(byte[] bytes) {
		if (bytes.length != 4) {
			throw new IllegalArgumentException("A byte[] containing an int must only be 4 bytes long!");
		}
		return ((bytes[0] << 24) | (bytes[1] << 16) | (bytes[2] << 8) | (bytes[3] & 0xFF));
	}

	/**
	 * <p>
	 * Converts four bytes of the content of {@code bytes} into a single {@code int}, with the first byte being at index
	 * {@code offset}.
	 * </p>
	 * 
	 * @param bytes
	 *            A byte array to be interpreted as a {@code int}.
	 * @param offset
	 *            The index offset to the first byte.
	 * @return The {@code int} representation of the contents of {@code bytes}.
	 * @throws IllegalArgumentException
	 *             If {@code bytes} is shorter than {@code offset + 4} elements.
	 * @since 1.0
	 */
	public static int intFromBytes(byte[] bytes, int offset) {
		if (bytes.length <= offset + 3) {
			throw new IllegalArgumentException("Not enough bytes to convert into an int!");
		}
		return ((bytes[offset] << 24) | (bytes[offset + 1] << 16) | (bytes[offset + 2] << 8) | (bytes[offset + 3] & 0xFF));
	}

	/**
	 * <p>
	 * Converts the {@code short s} into a {@code byte[]} containing its value in big-endian order.
	 * </p>
	 * 
	 * @param s
	 *            The {@code short} to convert.
	 * @return A {@code byte[]} representation of {@code s}.
	 * @since 1.0
	 */
	public static byte[] shortToBytes(short s) {
		byte[] bytes = new byte[2];
		bytes[0] = (byte) ((s >> 8) & 0xFF);
		bytes[1] = (byte) (s & 0xFF);
		return bytes;
	}

	/**
	 * <p>
	 * Converts the {@code int i} into a {@code byte[]} containing its value in big-endian order.
	 * </p>
	 * 
	 * @param i
	 *            The {@code int} to convert.
	 * @return A {@code byte[]} representation of {@code i}.
	 * @since 1.0
	 */
	public static byte[] intToBytes(int i) {
		byte[] bytes = new byte[4];
		bytes[0] = (byte) ((i >> 24) & 0xFF);
		bytes[1] = (byte) ((i >> 16) & 0xFF);
		bytes[2] = (byte) ((i >> 8) & 0xFF);
		bytes[3] = (byte) (i & 0xFF);
		return bytes;
	}

	/**
	 * <p>
	 * Converts the {@code short s} into its byte representation and stores it in {@code dst}, beginning at
	 * {@code offset} .
	 * </p>
	 * 
	 * @param s
	 *            The {@code short} to convert.
	 * @param dst
	 *            The array to store the conversion into.
	 * @param offset
	 *            The starting index for storing in {@code dst}.
	 * @since 1.0
	 */
	public static void shortIntoBytes(int s, byte[] dst, int offset) {
		if (offset < 0 || offset + 1 >= dst.length) {
			throw new IndexOutOfBoundsException();
		}
		dst[offset] = (byte) ((s >> 8) & 0xFF);
		dst[offset + 1] = (byte) (s & 0xFF);
	}

	/**
	 * <p>
	 * Converts the {@code int i} into its byte representation and stores it in {@code dst}, beginning at {@code offset}
	 * .
	 * </p>
	 * 
	 * @param i
	 *            The {@code int} to convert.
	 * @param dst
	 *            The array to store the conversion into.
	 * @param offset
	 *            The starting index for storing in {@code dst}.
	 * @since 1.0
	 */
	public static void intIntoBytes(int i, byte[] dst, int offset) {
		if (offset < 0 || offset + 3 >= dst.length) {
			throw new IndexOutOfBoundsException();
		}
		dst[offset] = (byte) ((i >> 24) & 0xFF);
		dst[offset + 1] = (byte) ((i >> 16) & 0xFF);
		dst[offset + 2] = (byte) ((i >> 8) & 0xFF);
		dst[offset + 3] = (byte) (i & 0xFF);
	}

	/**
	 * <p>
	 * Creates a new {@code byte} array containing the bytes the {@code short} values in {@code src} are made of.
	 * </p>
	 * 
	 * @param src
	 *            The array containing the {@code short} values to be stripped into their single bytes.
	 * @return A {@code byte} array containing two {@code byte} values for every {@code short} in {@code src}.
	 * @since 1.0
	 */
	public static byte[] bytesFromShorts(short[] src) {
		byte[] bytes = new byte[src.length * 2];
		for (int i = 0; i < src.length; i++) {
			bytes[i * 2] = (byte) ((src[i] >> 8) & 0xFF);
			bytes[i * 2 + 1] = (byte) (src[i] & 0xFF);
		}
		return bytes;
	}
}