import com.twitter.bijection.Injection;
import com.twitter.bijection.avro.GenericAvroCodecs;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Created by lowery on 7/20/16.
 */
public class Consumer {
    private Properties props = new Properties();
    private KafkaConsumer<String, byte[]> consumer;

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        Consumer test = new Consumer();
        test.createConsumer();

        test.poll();
    }

    public void createConsumer() {
        props.put("bootstrap.servers", "localhost:9092");

        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        props.put("group.id", "test");

        consumer = new KafkaConsumer<String, byte[]>(props);
        consumer.subscribe(Collections.singletonList("mytopic"));
    }

    public void poll() throws IOException {
        while (true) {
            ConsumerRecords<String, byte[]> records = consumer.poll(100);
            for (ConsumerRecord<String, byte[]> record : records) {

                Schema.Parser parser = new Schema.Parser();
                Schema schema = parser.parse(getClass().getResourceAsStream("test.avsc"));

                Injection<GenericRecord, byte[]> recordInjection = GenericAvroCodecs.toBinary(schema);
                GenericRecord datum = recordInjection.invert(record.value()).get();

                System.out.printf("offset = %d, key = %s, value = %s", record.offset(), record.key(), record.value());
            }
        }
    }

}
