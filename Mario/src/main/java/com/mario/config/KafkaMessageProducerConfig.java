package com.mario.config;

import java.util.Collection;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import com.mario.config.gateway.GatewayType;
import com.nhb.common.vo.HostAndPort;
import com.nhb.messaging.kafka.config.KafkaCompressionCodec;
import com.nhb.messaging.kafka.config.KafkaProducerConfig;
import com.nhb.messaging.kafka.config.KafkaProducerType;
import com.nhb.messaging.kafka.serialization.KafkaMsgpackDecoder;
import com.nhb.messaging.kafka.serialization.KafkaMsgpackEncoder;

public class KafkaMessageProducerConfig extends MessageProducerConfig {

	{
		this.setGatewayType(GatewayType.KAFKA);
	}

	private final KafkaProducerConfig source = new KafkaProducerConfig();

	{
		this.source.setSerializerClass(KafkaMsgpackEncoder.class);
		this.source.setDeserializerClass(KafkaMsgpackDecoder.class);
	}

	public Collection<HostAndPort> getBrokers() {
		return source.getBrokers();
	}

	public void addBrokers(HostAndPort... endpoints) {
		source.addBrokers(endpoints);
	}

	public void addBrokers(Collection<HostAndPort> endpoints) {
		source.addBrokers(endpoints);
	}

	public void addBrokers(String brokers) {
		source.addBrokers(brokers);
	}

	public KafkaProducerType getProducerType() {
		return source.getProducerType();
	}

	public void setProducerType(KafkaProducerType producerType) {
		source.setProducerType(producerType);
	}

	public void setProducerType(String producerType) {
		source.setProducerType(producerType);
	}

	public KafkaCompressionCodec getCompressionCodec() {
		return source.getCompressionCodec();
	}

	public void setCompressionCodec(KafkaCompressionCodec compressionCodec) {
		source.setCompressionCodec(compressionCodec);
	}

	public void setCompressionCodec(String compressionCodec) {
		source.setCompressionCodec(compressionCodec);
	}

	public String getSerializerClass() {
		return source.getSerializerClass();
	}

	public void setSerializerClass(String serializerClass) {
		source.setSerializerClass(serializerClass);
	}

	public void setSerializerClass(Class<? extends Serializer<?>> serializerClass) {
		source.setSerializerClass(serializerClass);
	}

	public String getDeserializerClass() {
		return source.getDeserializerClass();
	}

	public void setDeserializerClass(String deserializerClass) {
		source.setDeserializerClass(deserializerClass);
	}

	public void setDeserializerClass(Class<? extends Deserializer<?>> deserializerClass) {
		source.setDeserializerClass(deserializerClass);
	}

	public int getQueueBufferTime() {
		return source.getQueueBufferTime();
	}

	public void setQueueBufferTime(int queueBufferTime) {
		source.setQueueBufferTime(queueBufferTime);
	}

	public int getQueueBufferSize() {
		return source.getQueueBufferSize();
	}

	public void setQueueBufferSize(int queueBufferSize) {
		source.setQueueBufferSize(queueBufferSize);
	}

	public int getEnqueueTimeout() {
		return source.getEnqueueTimeout();
	}

	public void setEnqueueTimeout(int enqueueTimeout) {
		source.setEnqueueTimeout(enqueueTimeout);
	}

	public int getBatchNumMessage() {
		return source.getBatchNumMessage();
	}

	public void setBatchNumMessage(int batchNumMessage) {
		source.setBatchNumMessage(batchNumMessage);
	}

	public boolean isCompressedTopics() {
		return source.isCompressedTopics();
	}

	public void setCompressedTopics(boolean compressedTopics) {
		source.setCompressedTopics(compressedTopics);
	}
}
