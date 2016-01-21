package com.nhb.messaging.kafka.serialization;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.msgpack.unpacker.Unpacker;

import com.nhb.common.data.PuElement;
import com.nhb.common.data.msgpkg.PuElementTemplate;

import kafka.serializer.Decoder;

public class KafkaMsgpackDecoder extends MsgpackCodec implements Decoder<PuElement> {

	@Override
	public PuElement fromBytes(byte[] bytes) {
		Unpacker unpacker = this.getMsgpack().createUnpacker(new ByteArrayInputStream(bytes));
		try {
			return PuElementTemplate.getInstance().read(unpacker, null);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
