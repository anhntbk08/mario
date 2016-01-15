package com.nhb.common.cache;

import org.redisson.Redisson;

public interface KeyTranslator {
	String translate(String key, String path, Redisson redisson);
}
