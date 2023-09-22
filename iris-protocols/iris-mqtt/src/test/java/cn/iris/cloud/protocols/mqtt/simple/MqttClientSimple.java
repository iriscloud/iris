package cn.iris.cloud.protocols.mqtt.simple;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

/**
 * MqttClientSimple
 * @author wuhao
 */

public class MqttClientSimple {
	public static void main(String[] args) {
		try {
			MQTT mqtt = new MQTT();
			mqtt.setHost("localhost", 1883);
			mqtt.setUserName("testUser");
			BlockingConnection connection =mqtt.blockingConnection();
			connection.connect();
			System.out.println("connect ok");
			connection.publish("testPub", "hello".getBytes(), QoS.AT_LEAST_ONCE, false);
			System.out.println("publish ok");
			Topic[] topics = {new Topic("testSub", QoS.AT_LEAST_ONCE)};
			connection.subscribe(topics);
			System.out.println("subscribe ok");
			connection.disconnect();
			System.out.println("disconnect ok");
		} catch (Exception e){
			e.printStackTrace();
		}

	}
}
