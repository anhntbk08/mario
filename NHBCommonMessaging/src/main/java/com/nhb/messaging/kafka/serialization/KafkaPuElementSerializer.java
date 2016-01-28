package com.nhb.messaging.kafka.serialization;

import java.util.Map;

import org.apache.kafka.common.serialization.Serializer;

import com.nhb.common.data.PuElement;

public class KafkaPuElementSerializer extends MsgpackCodec implements Serializer<PuElement> {

	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {

	}

	@Override
	public byte[] serialize(String topic, PuElement data) {
		if (data == null) {
			return null;
		}
		return data.toBytes();
	}

	@Override
	public void close() {

	}

}
