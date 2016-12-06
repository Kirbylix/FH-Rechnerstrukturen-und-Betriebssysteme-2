package org.ejvm.util;

import java.util.Comparator;

/**
 * <p>
 * Utility class for array handling.
 * </p>
 * 
 * @author Sebastian Nicolai Kaupe
 * @since 1.0
 */
public class ArrayUtil {

	/**
	 * <p>
	 * Returns the first index of the element of {@code arr} that is equal to {@code e}, with equality being determined
	 * by the {@code equals()}-Method of {@code T}.
	 * </p>
	 * 
	 * @param arr
	 *            The array to search through.
	 * @param e
	 *            The comparison element.
	 * @return The index of the first element that fulfills the requirements or -1 if no such element was found.
	 */
	public static <T> int indexOf(T[] arr, T e) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].equals(e))
				return i;
		}
		return -1;
	}

	/**
	 * <p>
	 * Returns the first index of the element of {@code arr} for which {@code comparator.compareTo(arr[i], e) == 0}
	 * evaluates to {@code true}, with {@code i} ranging from 0 to {@code arr.length}.
	 * </p>
	 * 
	 * @param arr
	 *            The array to search through.
	 * @param e
	 *            The comparison element.
	 * @param comparator
	 *            The {@link Comparator} used to compare the array elements to {@code e}.
	 * @return The index of the first element that fulfills the requirements or -1 if no such element was found.
	 */
	public static <T> int indexOf(T[] arr, T e, Comparator<T> comparator) {
		for (int i = 0; i < arr.length; i++) {
			if (comparator.compare(arr[i], e) == 0)
				return i;
		}
		return -1;
	}

	/**
	 * <p>
	 * Checks if an element equal to {@code e} can be found in {@code arr}.
	 * </p>
	 * 
	 * @param arr
	 *            The array to search through.
	 * @param e
	 *            The searched-for element.
	 * @return {@code true} if the array contains an element equal to {@code e}, otherwise {@code false}.
	 */
	public static <T> boolean contains(T[] arr, T e) {
		for (T t: arr) {
			if (t.equals(e))
				return true;
		}
		return false;
	}
}