package com.nhb.messaging.kafka.serialization;

import com.nhb.common.data.PuElement;

import kafka.serializer.Encoder;

public class KafkaMsgpackEncoder extends MsgpackCodec implements Encoder<PuElement> {

	@Override
	public byte[] toBytes(PuElement data) {
		if (data == null) {
			return null;
		}
		return data.toBytes();
	}

}
