/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * @date 2018-03-27    (Lei Wang) Initial release.
 */
package com.lei.kafka;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.safs.IndependantLog;

/**
 * @author Lei Wang
 *
 */
public class SimpleProducer {
    private String groupId = Constants.DEFAULT_GROUP_ID;
    private String topic = Constants.DEFAULT_TOPIC;
    private String servers = Constants.DEFAULT_BOOTSTRAP_SERVERS;

    private static Producer<String, String> producer = null;

    public SimpleProducer(){
    	producer = createProducer();
    };

    public SimpleProducer(String groupId) {
		this.groupId = groupId;
		producer = createProducer();
    }

	public SimpleProducer(String groupId, String topic) {
		this.groupId = groupId;
		this.topic = topic;
		producer = createProducer();
	}

    /**
	 * @param groupId
	 * @param topic
	 * @param servers
	 */
	public SimpleProducer(String groupId, String topic, String servers) {
		this.groupId = groupId;
		this.topic = topic;
		this.servers = servers;
		producer = createProducer();
	}

	private Producer<String, String> createProducer() {
    	Properties props = new Properties();
    	props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
    	props.put(ProducerConfig.CLIENT_ID_CONFIG, groupId);
    	props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    	props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    	return new KafkaProducer<>(props);
    }

    public void queue(String entity, String jsonData) throws Exception {
    	long time = System.currentTimeMillis();

    	try {
    		final ProducerRecord<String, String> record = new ProducerRecord<>(topic, entity, jsonData);
    		RecordMetadata metadata = producer.send(record).get();

    		long elapsedTime = System.currentTimeMillis() - time;
    		IndependantLog.debug(String.format("sent record(key=%s value=%s) " +
    				"meta(partition=%d, offset=%d) time=%d\n",
    				record.key(), record.value(), metadata.partition(),
    				metadata.offset(), elapsedTime));

    	} finally {
    		producer.flush();
    		producer.close();
    	}
    }

    public static void main(String... args) throws Exception {
    	SimpleProducer p = new SimpleProducer();
    	p.queue("user", "{'id':'userID'}");
    }
}
