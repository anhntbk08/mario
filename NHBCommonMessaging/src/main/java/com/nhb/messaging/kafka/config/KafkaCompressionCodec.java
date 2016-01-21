package com.nhb.messaging.kafka.config;

public enum KafkaCompressionCodec {

	NONE(0), GZIP(1), SNAPPY(2), LZ4(3);

	private int id;

	private KafkaCompressionCodec(int index) {
		this.id = index;
	}

	public int getId() {
		return this.id;
	}

	public static final KafkaCompressionCodec fromId(int id) {
		for (KafkaCompressionCodec codec : values()) {
			if (codec.getId() == id) {
				return codec;
			}
		}
		return null;
	}

	public static final KafkaCompressionCodec fromName(String name) {
		if (name != null) {
			for (KafkaCompressionCodec codec : values()) {
				if (codec.name().equalsIgnoreCase(name.trim())) {
					return codec;
				}
			}
			try {
				return fromId(Integer.valueOf(name));
			} catch (Exception ex) {
				// do nothing
			}
		}
		return null;
	}
}
