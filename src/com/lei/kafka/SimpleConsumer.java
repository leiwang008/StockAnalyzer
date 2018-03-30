/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * @date 2018-03-27    (Lei Wang) Initial release.
 */
package com.lei.kafka;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.safs.IndependantLog;


/**
 * @author Lei Wang
 */
public class SimpleConsumer {
    private String groupId = Constants.DEFAULT_GROUP_ID;
    private String topic = Constants.DEFAULT_TOPIC;
    private String servers = Constants.DEFAULT_BOOTSTRAP_SERVERS;

    private static Consumer<String, String> consumer = null;

    public SimpleConsumer(){
    	consumer = createConsumer();
    };

    public SimpleConsumer(String groupId) {
		this.groupId = groupId;
		consumer = createConsumer();
    }

	public SimpleConsumer(String groupId, String topic) {
		this.groupId = groupId;
		this.topic = topic;
		consumer = createConsumer();
	}

    /**
	 * @param groupId
	 * @param topic
	 * @param servers
	 */
	public SimpleConsumer(String groupId, String topic, String servers) {
		this.groupId = groupId;
		this.topic = topic;
		this.servers = servers;
		consumer = createConsumer();
	}

    private Consumer<String, String> createConsumer() {
    	final Properties props = new Properties();
    	props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
    	props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    	props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    	props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    	// Create the consumer using props.
    	final Consumer<String, String> consumer = new KafkaConsumer<>(props);

    	IndependantLog.debug("All Topics:\n");
    	Map<String,List<PartitionInfo>> topics = consumer.listTopics();
    	topics.keySet().forEach(topic->{
    		IndependantLog.debug(topic+"\n");
    		List<PartitionInfo> partitions = topics.get(topic);
    		IndependantLog.debug(partitions+"\n");
    	});


    	// Subscribe to the topic.
    	consumer.subscribe(Collections.singletonList(topic));

    	consumer.seekToBeginning(consumer.assignment());

    	return consumer;
    }

    private void consume() throws InterruptedException {
        final int giveUp = 100;   int noRecordsCount = 0;
        while (true) {
            final ConsumerRecords<String, String> consumerRecords = consumer.poll(1000);
            if (consumerRecords.count()==0) {
                noRecordsCount++;
                if (noRecordsCount > giveUp) break;
                else continue;
            }
            consumerRecords.forEach(record -> {
            	handleRecord(record);
            });
            consumer.commitAsync();
        }
        consumer.close();
        IndependantLog.debug("DONE");
    }

    private void handleRecord(ConsumerRecord<?, ?> record){
    	IndependantLog.debug( String.format("Consumer Record:("+record.key()+", "+record.value()+", %d, %d)\n", record.partition(), record.offset()));
    }


    public static void main(String... args) throws Exception {
    	SimpleConsumer sp = new SimpleConsumer();
    	sp.consume();
    }

}
