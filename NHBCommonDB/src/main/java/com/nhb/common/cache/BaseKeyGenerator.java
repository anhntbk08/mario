
package com.nhb.common.cache;

import org.redisson.Redisson;
import org.redisson.core.RBucket;

public class BaseKeyGenerator implements KeyGenerator {

	private static final String SEPARATOR = ":";

	@Override
	public String generateKey(Object obj) {
		RBucket<String> bucket;
		bucket = Redisson.create().getBucket(obj.getClass().getName());
		String str = bucket.get();
		if (str == null) {
			bucket.set("0");
			return obj.getClass().getName() + SEPARATOR + "0";
		}
		long id = Long.valueOf(str);
		id++;
		bucket.set(Long.toString(id));
		return obj.getClass().getName() + SEPARATOR + id;
	}

}

