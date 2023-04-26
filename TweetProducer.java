package com.bloomberg.kafkaproducerconsumer.service;

import com.google.common.collect.Lists;  
import com.twitter.hbc.ClientBuilder;  
import com.twitter.hbc.core.Client;  
import com.twitter.hbc.core.Constants;  
import com.twitter.hbc.core.Hosts;  
import com.twitter.hbc.core.HttpHosts;  
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;  
import com.twitter.hbc.core.processor.StringDelimitedProcessor;  
import com.twitter.hbc.httpclient.auth.Authentication;  
import com.twitter.hbc.httpclient.auth.OAuth1;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;  
import org.slf4j.Logger;  
import org.slf4j.LoggerFactory;  
  
import java.util.List;  
import java.util.Properties;  
import java.util.concurrent.BlockingQueue;  
import java.util.concurrent.LinkedBlockingQueue;  
import java.util.concurrent.TimeUnit;  

public class TweetProducer {
	 Logger logger = LoggerFactory.getLogger(TweetProducer.class.getName());  
	    String consumerKey = "Ow0TsWDmChC3MHqKIb8PWjDTA";//specify the consumer key from the twitter app  
	    String consumerSecret = "uksuy0ZcHD9EXd7YMTeR9outcPQzboHNrpfX3jEMfuDcu7NAPm";//specify the consumerSecret key from the twitter app  
	    String token = "iF0sqayBq3gYppYfKznX4TDjYBYt2RT";//specify the token key from the twitter app  
	    String secret = "6EnUjs8mDrVxkTF4cbDw6FzX11kIYcOHj5NUwuMi7HbOS";//specify the secret key from the twitter app  
	  
	    public TweetProducer() {}//constructor to invoke the producer function  
	  
	    public static void main(String[] args) {  
	        new TweetProducer().run();  
	    }  
	  
	    public void run() {  
	        logger.info("Setup");  
	  
	        BlockingQueue<String> msgQueue = new           
	    LinkedBlockingQueue<String>(1000);//Specify the size accordingly.  
	        Client client = tweetclient(msgQueue);  
	        client.connect(); //invokes the connection function  
	        KafkaProducer<String,String> producer=createKafkaProducer();  
	  
	        // on a different thread, or multiple different threads....  
	        while (!client.isDone()) {  
	            String msg = null;  
	            try {  
	                msg = msgQueue.poll(5, TimeUnit.SECONDS);//specify the time  
	            } catch (InterruptedException e) {  
	                e.printStackTrace();  
	                client.stop();  
	            }  
	            if (msg != null) {  
	                logger.info(msg);  
	                producer.send(new ProducerRecord<>("Cricket", null, msg), new Callback() {  
	                    @Override  
	                    public void onCompletion(RecordMetadata recordMetadata, Exception e) {  
	                        if(e!=null){  
	                            logger.error("Something went wrong",e);  
	                        }  
	                    }  
	                });  
	            }  
	  
	        }//Specify the topic name, key value, msg  
	  
	        logger.info("This is the end");//When the reading is complete, inform logger  
	    }  
	  
	    public Client tweetclient(BlockingQueue<String> msgQueue) {  
	  
	        Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);  
	        StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();  
	        List<String> terms = Lists.newArrayList("Cricket data");//describe   
	//anything for which we want to read the tweets.  
	        hosebirdEndpoint.trackTerms(terms);  
	        Authentication hosebirdAuth = new        OAuth1(consumerKey,consumerSecret,token,secret);  
	        ClientBuilder builder = new ClientBuilder()  
	                .name("Hosebird-Client-01")     // optional: mainly for the logs  
	                .hosts(hosebirdHosts)  
	                .authentication(hosebirdAuth)  
	                .endpoint(hosebirdEndpoint)  
	                .processor(new StringDelimitedProcessor(msgQueue));  
	  
	  
	        Client hosebirdClient = builder.build();  
	        return hosebirdClient; // Attempts to establish a connection.  
	 }  
	    public KafkaProducer<String,String> createKafkaProducer(){  
	 //creating kafka producer     
	//creating producer properties     
	        String bootstrapServers="127.0.0.1:9092";  
	        Properties properties= new Properties();  
	        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,    bootstrapServers);  
	        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());  
	        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());  
	  
	        KafkaProducer<String,String> first_producer = new KafkaProducer<String, String>(properties);  
	        return first_producer;  
	  
	    }  
}
