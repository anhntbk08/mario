package com.nhb.common.utils;

import java.nio.ByteBuffer;

import com.nhb.common.exception.UnsupportedTypeException;

public final class ByteArrayUtils {

	private ByteArrayUtils() {
		// private constructor
	}

	@SuppressWarnings("unchecked")
	public static final <T> T primitiveFromByteArray(Class<T> clazz, byte[] bytes) {
		if (clazz != null && bytes != null) {
			if (clazz == Boolean.class || clazz == Boolean.TYPE) {
				return (T) Boolean.valueOf(Long.valueOf(ByteBuffer.wrap(bytes).getLong()) != 0);
			} else if (clazz == Byte.class || clazz == Byte.TYPE) {
				return (T) Byte.valueOf(bytes.length > 0 ? bytes[0] : 0);
			} else if (clazz == Short.class || clazz == Short.TYPE) {
				return (T) Short.valueOf(ByteBuffer.wrap(bytes).getShort());
			} else if (clazz == Integer.class || clazz == Integer.TYPE) {
				return (T) Integer.valueOf(ByteBuffer.wrap(bytes).getInt());
			} else if (clazz == Float.class || clazz == Float.TYPE) {
				return (T) Float.valueOf(ByteBuffer.wrap(bytes).getFloat());
			} else if (clazz == Long.class || clazz == Long.TYPE) {
				return (T) Long.valueOf(ByteBuffer.wrap(bytes).getLong());
			} else if (clazz == Double.class || clazz == Double.TYPE) {
				return (T) Double.valueOf(ByteBuffer.wrap(bytes).getDouble());
			} else if (clazz == String.class) {
				return (T) new String(bytes);
			} else if (clazz == Character.class || clazz == Character.TYPE) {
				return (T) Character.valueOf(ByteBuffer.wrap(bytes).getChar());
			}
			throw new UnsupportedTypeException();
		}
		return null;
	}
}
