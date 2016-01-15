package com.nhb.common.cache;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.redisson.Redisson;
import org.redisson.core.RBucket;

import com.nhb.common.BaseLoggable;
import com.nhb.common.utils.ArrayUtils;
import com.nhb.common.utils.ObjectUtils;
import com.nhb.common.utils.ObjectUtils.Setter;
import com.nhb.common.utils.PrimitiveTypeUtils;

public class RedisCache extends BaseLoggable {

	private static final String REFERENCE_SUBFIX = "*";
	private static final String LIST_SUBFIX = "~";
	private static final String MAP_SUBFIX = "@";
	private static final String SEPARATOR = ":";

	private KeyGenerator keyGenerator;
	private Redisson redisson;

	public RedisCache(Redisson redisson) {
		this.setRedisson(redisson);
	}

	private String _write(Object pojo, final Redisson redisson, String mapKey) {
		if (redisson == null) {
			throw new RuntimeException("redisson could be null");
		}
		if (this.keyGenerator == null) {
			this.keyGenerator = new BaseKeyGenerator();
		}

		String key = "";
		// if (prefixKey != null) {
		// key = prefixKey + SEPARATOR + this.keyGenerator.generateKey(pojo);
		// } else {
		key = this.keyGenerator.generateKey(pojo);
		// }

		final String _key = key;
		Map<String, Object> map = ObjectUtils.toMap(pojo);

		for (final Entry<String, Object> entry : map.entrySet()) {
			if (entry.getValue() != null) {
				if (PrimitiveTypeUtils.isPrimitiveOrWrapperType(entry.getValue().getClass())) {
					RBucket<String> bucket = redisson.getBucket(key + SEPARATOR + entry.getKey());
					bucket.set(PrimitiveTypeUtils.getStringValueFrom(entry.getValue()));
					redisson.getMap(key).fastPut(entry.getKey(), key + SEPARATOR + entry.getKey());

					// redisson.hset(key, entry.getKey(), key + SEPARATOR +
					// entry.getKey());
				} else if (ArrayUtils.isArrayOrCollection(entry.getValue().getClass())) {
					String fieldListName = key + SEPARATOR + entry.getKey();
					final List<String> itemsKey = new ArrayList<String>();
					ArrayUtils.foreach(entry.getValue(), new ArrayUtils.ForeachCallback<Object>() {

						@Override
						public void apply(Object element) {
							if (PrimitiveTypeUtils.isPrimitiveOrWrapperType(element.getClass())) {
								// jedis.lpush(fieldListName,
								// element.toString());
								itemsKey.add(element.toString());
							} else {
								String childKey = _key + SEPARATOR + entry.getKey();
								String childListElementKey = _write(element, redisson, childKey) + REFERENCE_SUBFIX; //
								itemsKey.add(childListElementKey);
							}
						}
					});
					RBucket<String> bucket = redisson.getBucket(fieldListName);
					bucket.set(itemsKey.toString() + LIST_SUBFIX);
					itemsKey.clear();
					redisson.getMap(key).fastPut(entry.getKey() + LIST_SUBFIX, fieldListName);

				} else if (entry.getValue() instanceof Map) {

					final String fieldMapName = (entry.getKey() + MAP_SUBFIX);
					String entryMmapKey = _write(entry.getValue(), redisson, key + entry.getKey());
					redisson.getMap(key).fastPut(fieldMapName, key + SEPARATOR + entry.getKey());
					redisson.getBucket(key + SEPARATOR + entry.getKey()).set(entryMmapKey + MAP_SUBFIX);

				} else if (entry.getValue() instanceof Object) {
					String objKey = _write(entry.getValue(), redisson, key);
					redisson.getMap(key).fastPut(entry.getKey(), key + SEPARATOR + entry.getKey() + REFERENCE_SUBFIX);
					redisson.getBucket(key + SEPARATOR + entry.getKey()).set(objKey + REFERENCE_SUBFIX);
				}
			}
		}

		// @SuppressWarnings("unused")
		// Response<String> response = jedis.save();

		return key;
	}

	@SuppressWarnings("unchecked")
	private Object _read(String key, Class<?> clazz, final Redisson redisson) throws Exception {
		Object result = clazz.newInstance();

		Map<String, String> map = redisson.getMap(key);
		Map<String, Setter> classSetter = ObjectUtils.findAllClassSetters(clazz);

		for (Entry<String, String> entry : map.entrySet()) {
			if (entry.getKey().contains(LIST_SUBFIX)) {
				String[] elements = entry.getValue().split(SEPARATOR);
				Setter setter = classSetter
						.get(entry.getKey().substring(0, entry.getKey().length() - REFERENCE_SUBFIX.length()));
				Collection<Object> list = new ArrayList<>();

				Class<?> childParamType = setter.getComponentType();

				if (elements.length > 0) {
					RBucket<String> bucket = redisson.getBucket(entry.getValue());
					String valueList = bucket.get().replace(" ", "");
					int startIndex = valueList.indexOf('[') + 1;
					int endIndex = valueList.indexOf(']');
					List<String> keyRange = new ArrayList<String>(
							Arrays.asList(valueList.substring(startIndex, endIndex).split(",")));

					// List<String> keyRange = jedis.lrange(listKey, 0, 10);
					for (String elementValue : keyRange) {
						if (elementValue.contains(REFERENCE_SUBFIX)) {
							String[] components = elementValue
									.substring(0, elementValue.length() - REFERENCE_SUBFIX.length()).split(SEPARATOR);
							String childKey = elementValue.substring(0,
									elementValue.length() - REFERENCE_SUBFIX.length());
							if (components.length >= 2 && childKey != "") {
								String className = components[components.length - 2];
								Object value = _read(childKey, Class.forName(className), redisson);
								list.add(value);
							}
						} else {
							list.add(PrimitiveTypeUtils.getValueFrom(childParamType, elementValue));
						}
					}

					if (setter.getParamType().isArray()) {
						Object array = Array.newInstance(setter.getComponentType(), list.size());
						int i = 0;
						for (Object child : list) {
							if (PrimitiveTypeUtils.isPrimitiveOrWrapperType(child.getClass())) {
								Array.set(array, i, PrimitiveTypeUtils.getValueFrom(setter.getComponentType(), child));
							} else {
								Array.set(array, i, child);
							}
							i++;
						}
						setter.set(result, array);
					} else {
						setter.set(result, list);
					}
				}
			} else if (entry.getKey().contains(MAP_SUBFIX)) {
				RBucket<String> bucket = redisson.getBucket(entry.getValue());
				String mapKeyValue = bucket.get();
				String[] components = mapKeyValue.split(SEPARATOR);
				String fieldName = entry.getKey().substring(0, entry.getKey().length() - MAP_SUBFIX.length());
				if (components.length >= 2) {
					String mapClassName = components[0];
					String valueKey = mapKeyValue.replace(MAP_SUBFIX, "");
					Object value = _read(valueKey, Class.forName(mapClassName), redisson);
					if (result instanceof Map) {
						((Map<Object, Object>) result).put(fieldName, value);
					} else {
						classSetter.get(fieldName).set(result, value);
					}
				}
			} else {
				if (entry.getValue().contains(REFERENCE_SUBFIX)) {
					String objectKey = entry.getValue().substring(0,
							entry.getValue().length() - REFERENCE_SUBFIX.length());
					RBucket<String> bucket = redisson.getBucket(objectKey);
					String objectKeyValue = bucket.get().replace(REFERENCE_SUBFIX, "");
					String[] elements = objectKeyValue.split(SEPARATOR);
					if (elements.length > 1) {
						String className = elements[0];
						Object value = _read(objectKeyValue, Class.forName(className), redisson);
						if (result instanceof Map) {
							((Map<Object, Object>) result).put(entry.getKey(), value);
						} else {
							classSetter.get(entry.getKey()).set(result, value);
						}
					}
				} else {
					RBucket<String> bucket = redisson.getBucket(entry.getValue());
					String value = bucket.get();
					if (result instanceof Map) {
						((Map<Object, Object>) result).put(entry.getKey(), value);
					} else {
						Setter setter = classSetter.get(entry.getKey());
						if (value == null) {
							setter.set(result, null);
						} else if (PrimitiveTypeUtils.isPrimitiveOrWrapperType(value.getClass())) {
							setter.set(result, PrimitiveTypeUtils.getValueFrom(setter.getParamType(), value));
						}
					}
				}
			}
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public <T> T read(Object key, Class<T> clazz) {
		if (key != null && clazz != null) {
			if (this.redisson != null) {

				try {
					return (T) _read((String) key, clazz, redisson);
				} catch (Exception ex) {
					ex.printStackTrace();
				} finally {
					if (redisson != null) {
						// this.jedisPool.returnResourceObject(jedis);

					}
				}
			}
		}
		return null;
	}

	private <T> Object _update(String key, String path, Object value, final Redisson redisson) {

		String tempKey = new BaseKeyTranslator().translate(key, path, redisson);
		// case reference or primitive type
		if (PrimitiveTypeUtils.isPrimitiveOrWrapperType(value.getClass())) {
			redisson.getBucket(tempKey).set(value.toString());
			return value;
		} else if (value instanceof Map || value instanceof Object) {
			Map<String, Object> map = ObjectUtils.toMap(value);
			for (Entry<String, Object> entry : map.entrySet()) {
				if (PrimitiveTypeUtils.isPrimitiveOrWrapperType(entry.getValue().getClass())) {
					redisson.getBucket(tempKey + SEPARATOR + entry.getKey()).set(entry.getValue().toString());
				} else {
					_update(tempKey, entry.getKey(), entry.getValue(), redisson);
				}
			}
		}
		if (ArrayUtils.isArrayOrCollection(value.getClass())) {
			String valueList = redisson.getBucket(tempKey).get().toString().replace(" ", "");
			int startIndex = valueList.indexOf('[') + 1;
			int endIndex = valueList.indexOf(']');
			final List<String> keyRange = new ArrayList<String>(
					Arrays.asList(valueList.substring(startIndex, endIndex).split(",")));
			ArrayUtils.foreach(value, new ArrayUtils.ForeachCallback<Object>() {
				int count = 0;

				@Override
				public void apply(Object element) {
					if (PrimitiveTypeUtils.isPrimitiveOrWrapperType(element.getClass())) {
						keyRange.set(count, element.toString());
					} else {
						_update(keyRange.get(count).substring(0, keyRange.get(count).length() - 1), null, element,
								redisson);
					}
					count++;
				}

			});
			redisson.getBucket(tempKey).set(keyRange.toString());
		}
		return value;
	}

	public String write(Object pojo) {
		if (pojo != null) {
			if (this.redisson != null) {

				try {

					return _write(pojo, redisson, null);

				} finally {
					if (redisson != null) {

					}
				}
			}
		}
		return null;
	}

	public Object update(String key, String path, Object value) {

		if (value != null) {
			if (this.redisson != null) {

				try {
					return _update(key, path, value, redisson);

				} catch (Exception ex) {
					System.out.println(ex.getMessage());
				} finally {
					if (redisson != null) {

					}
				}
			}
		}
		return null;
	}

	public Redisson getRedisson() {
		return redisson;
	}

	public void setRedisson(Redisson redisson) {
		this.redisson = redisson;
	}
}
