import com.twitter.bijection.Injection;
import com.twitter.bijection.avro.GenericAvroCodecs;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Created by lowery on 7/20/16.
 */
public class Test {
    private Properties props = new Properties();
    private KafkaProducer<String, byte[]> producer;

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        Test test = new Test();
        test.createProducer();

        for (int i = 0; i < 2; i++) {
            test.send();
        }
    }

    public void createProducer() {
        props.put("bootstrap.servers", "localhost:9092");

        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");

        producer = new KafkaProducer<String, byte[]>(props);
    }

    public void send() throws ExecutionException, InterruptedException, IOException {
        Schema.Parser parser = new Schema.Parser();
        Schema schema = parser.parse(getClass().getResourceAsStream("test.avsc"));

        Injection<GenericRecord, byte[]> recordInjection = GenericAvroCodecs.toBinary(schema);

        GenericRecord datum = new GenericData.Record(schema);
        datum.put("name", "David Lowery");
        datum.put("favorite_number", 7);
        datum.put("favorite_color", "orange");

        byte[] bytes = recordInjection.apply(datum);

        ProducerRecord<String, byte[]> record = new ProducerRecord<String, byte[]>("mytopic", bytes);
        producer.send(record).get();

    }
}
