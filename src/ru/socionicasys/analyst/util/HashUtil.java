package ru.socionicasys.analyst.util;

import java.lang.reflect.Array;

public class HashUtil {
	private static final int DEFAULT_SEED = 17;
	private static final int PRIME = 31;

	private int hashCode;

	public HashUtil() {
		hashCode = DEFAULT_SEED;
	}

	public HashUtil(int seed) {
		hashCode = seed;
	}

	public int getComputedHash() {
		return hashCode;
	}

	public void hash(boolean value) {
		hashCode = PRIME * hashCode + (value ? 1 : 0);
	}

	public void hash(char value) {
		hashCode = PRIME * hashCode + (int)value;
	}

	public void hash(int value) {
		hashCode = PRIME * hashCode + value;
	}

	public void hash(long value) {
		hashCode = PRIME * hashCode + (int)(value ^ (value >>> 32));
	}

	public void hash(float value) {
		hash(Float.floatToIntBits(value));
	}

	public void hash(double value) {
		hash(Double.doubleToLongBits(value));
	}

	public void hash(Object value) {
		if (value == null) {
			hash(0);
		} else if (!value.getClass().isArray()) {
			hash(value.hashCode());
		} else {
			int arrayLength = Array.getLength(value);
			for (int i = 0; i < arrayLength; ++i) {
				Object item = Array.get(value, i);
				hash(item);
			}
		}
	}
}
