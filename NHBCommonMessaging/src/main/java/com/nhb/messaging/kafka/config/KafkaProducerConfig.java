package com.nhb.messaging.kafka.config;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import com.nhb.common.vo.HostAndPort;

public class KafkaProducerConfig {

	private final Collection<HostAndPort> brokers = new HashSet<>();
	private KafkaProducerType producerType = KafkaProducerType.SYNC;
	private KafkaCompressionCodec compressionCodec = KafkaCompressionCodec.NONE;
	private String serializerClass = "kafka.serializer.DefaultEncoder";
	private String deserializerClass = "kafka.serializer.DefaultDecoder";
	private boolean compressedTopics = false;

	// Async producer config
	private int queueBufferTime;
	private int queueBufferSize;
	private int enqueueTimeout;
	private int batchNumMessage;

	public Collection<HostAndPort> getBrokers() {
		return brokers;
	}

	public void addBrokers(HostAndPort... endpoints) {
		if (endpoints != null) {
			for (HostAndPort endpoint : endpoints) {
				if (endpoint == null) {
					continue;
				}
				this.getBrokers().add(endpoint);
			}
		}
	}

	public void addBrokers(Collection<HostAndPort> endpoints) {
		if (endpoints != null) {
			this.getBrokers().addAll(endpoints);
		}
	}

	public void addBrokers(String brokers) {
		if (brokers != null) {
			String[] arr = brokers.split("[^a-zA-Z0-9:\\.]");
			for (String str : arr) {
				if (str.trim().length() == 0) {
					continue;
				}
				this.getBrokers().add(HostAndPort.fromString(str));
			}
		}
	}

	public KafkaProducerType getProducerType() {
		return producerType;
	}

	public void setProducerType(KafkaProducerType producerType) {
		this.producerType = producerType;
	}

	public void setProducerType(String producerType) {
		this.producerType = KafkaProducerType.fromName(producerType);
	}

	public KafkaCompressionCodec getCompressionCodec() {
		return compressionCodec;
	}

	public void setCompressionCodec(KafkaCompressionCodec compressionCodec) {
		this.compressionCodec = compressionCodec;
	}

	public void setCompressionCodec(String compressionCodec) {
		this.compressionCodec = KafkaCompressionCodec.fromName(compressionCodec);
	}

	public String getSerializerClass() {
		return serializerClass;
	}

	public void setSerializerClass(String serializerClass) {
		this.serializerClass = serializerClass;
	}

	public void setSerializerClass(Class<? extends Serializer<?>> serializerClass) {
		this.serializerClass = serializerClass.getName();
	}

	public String getDeserializerClass() {
		return deserializerClass;
	}

	public void setDeserializerClass(String deserializerClass) {
		this.deserializerClass = deserializerClass;
	}

	public void setDeserializerClass(Class<? extends Deserializer<?>> deserializerClass) {
		this.deserializerClass = deserializerClass.getName();
	}

	public int getQueueBufferTime() {
		return queueBufferTime;
	}

	public void setQueueBufferTime(int queueBufferTime) {
		this.queueBufferTime = queueBufferTime;
	}

	public int getQueueBufferSize() {
		return queueBufferSize;
	}

	public void setQueueBufferSize(int queueBufferSize) {
		this.queueBufferSize = queueBufferSize;
	}

	public int getEnqueueTimeout() {
		return enqueueTimeout;
	}

	public void setEnqueueTimeout(int enqueueTimeout) {
		this.enqueueTimeout = enqueueTimeout;
	}

	public int getBatchNumMessage() {
		return batchNumMessage;
	}

	public void setBatchNumMessage(int batchNumMessage) {
		this.batchNumMessage = batchNumMessage;
	}

	private String getBrokerListAsString() {
		StringBuffer sb = new StringBuffer();
		if (this.brokers != null) {
			for (HostAndPort endpoint : this.brokers) {
				if (sb.length() > 0) {
					sb.append(",");
				}
				sb.append(endpoint.toString());
			}
		}
		return sb.toString();
	}

	public boolean isCompressedTopics() {
		return compressedTopics;
	}

	public void setCompressedTopics(boolean compressedTopics) {
		this.compressedTopics = compressedTopics;
	}

	public Properties toProperties() {

		Properties props = new Properties();
		props.setProperty("metadata.broker.list", getBrokerListAsString());
		props.setProperty("producer.type", this.getProducerType().name().toLowerCase());
		props.setProperty("compression.codec", this.getCompressionCodec().name().toLowerCase());
		props.setProperty("serializer.class", this.getSerializerClass());
		props.setProperty("deserializer.class", this.getDeserializerClass());
		props.setProperty("compressed.topics", String.valueOf(this.isCompressedTopics()));

		if (this.getQueueBufferTime() > 0) {
			props.setProperty("queue.buffering.max.ms", String.valueOf(this.getQueueBufferTime()));
		}
		if (this.getQueueBufferSize() > 0) {
			props.setProperty("queue.buffering.max.message", String.valueOf(this.getQueueBufferSize()));
		}
		if (this.getEnqueueTimeout() > 0) {
			props.setProperty("queue.enqueue.timeout.ms", String.valueOf(this.getEnqueueTimeout()));
		}
		if (this.getBatchNumMessage() > 0) {
			props.setProperty("batch.num.messages", String.valueOf(this.getBatchNumMessage()));
		}

		return props;
	}

	public static KafkaProducerConfig fromProperties(Properties props) {
		KafkaProducerConfig config = new KafkaProducerConfig();
		Enumeration<Object> it = props.keys();
		while (it.hasMoreElements()) {
			String key = (String) it.nextElement();
			String value = props.getProperty(key);
			if (key.equals("metadata.broker.list")) {
				config.addBrokers(value);
			} else if (key.equals("producer.type")) {
				config.setProducerType(value);
			} else if (key.equals("compression.codec")) {
				config.setCompressionCodec(value);
			} else if (key.equals("serializer.class")) {
				config.setSerializerClass(value);
			} else if (key.equals("deserializer.class")) {
				config.setDeserializerClass(value);
			} else if (key.equals("compressed.topics")) {
				config.setCompressedTopics(Boolean.valueOf(value));
			} else if (key.equals("queue.buffering.max.ms")) {
				config.setQueueBufferTime(Integer.valueOf(value));
			} else if (key.equals("queue.buffering.max.message")) {
				config.setQueueBufferSize(Integer.valueOf(value));
			} else if (key.equals("queue.enqueue.timeout.ms")) {
				config.setEnqueueTimeout(Integer.valueOf(value));
			} else if (key.equals("batch.num.messages")) {
				config.setBatchNumMessage(Integer.valueOf(value));
			}
		}
		return config;
	}
}
