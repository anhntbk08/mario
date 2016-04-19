package com.mario.cache.hazelcast;

import com.hazelcast.config.Config;

public interface HazelcastInitializer {

	void prepare(Config config);
}
