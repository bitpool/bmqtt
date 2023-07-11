package com.bitpool.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.baja.sys.Sys;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class MqttPublish implements Runnable  {

    public static final int MQTT_QUEUE_SIZE = 4096;

    private final MqttException m_Ex = null;
    private String m_LastKnownError = null;
    private final String m_BrokerUrl;
    private String m_UserName;
    private String m_Password;
    private String m_clientId;
    private final Boolean m_UserCredentials;
    private Boolean m_UseSSL    = false;

    private MqttClient m_MqttClient;
    private MemoryPersistence m_Persistence;

    private final BlockingQueue<MqttPayload> m_BlockingQueue = new LinkedBlockingDeque<>(MQTT_QUEUE_SIZE);

    public MqttPublish(String brokerUrl, Boolean useSSL, String ClientId){
        m_UseSSL = useSSL;
        m_clientId = ClientId;
        if(useSSL){
            m_BrokerUrl         = "ssl://" + brokerUrl;
        }else{
            m_BrokerUrl         = "tcp://" + brokerUrl;
        }
        m_UserCredentials = false;
    }
    public MqttPublish(String brokerUrl, String userName, String password, Boolean useSSL, String ClientId){
        m_UseSSL = useSSL;
        m_clientId = ClientId;
        if(useSSL){
            m_BrokerUrl         = "ssl://" + brokerUrl;
        }else{
            m_BrokerUrl         = "tcp://" + brokerUrl;
        }
        m_UserName = userName;
        m_Password = password;
        m_UserCredentials = true;
        System.nanoTime();
    }
    public boolean connect(String lwtTopic, String lwtMsg){
        try {
            String cId = m_clientId;
            if (cId == null || cId.isEmpty()) {
                //cId = "BP-S-" + ((Long) currentTimeMillis()) + "-" + generateRandomIntStr();
                cId = Sys.getStation().getStationName();
            }
            m_Persistence = new MemoryPersistence();
            m_MqttClient = new MqttClient(m_BrokerUrl, cId, m_Persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true); // True = remove all topics, False = leave topics in place between connects
            if(m_UserCredentials) {
                connOpts.setPassword(m_Password.toCharArray());
                connOpts.setUserName(m_UserName);
            }
            if(m_UseSSL) {
                // Use standard JSSE available in the runtime and
                // Use TLSv1.2 which is the default for a secured mosquitto
                SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
                sslContext.init(null, new TrustManager[]{new AlwaysTrustManager()}, new java.security.SecureRandom());
                SSLSocketFactory socketFactory = sslContext.getSocketFactory();
                connOpts.setSocketFactory(socketFactory);
            }
            // Dont define the LWT until further tested
            connOpts.setWill(lwtTopic, lwtMsg.getBytes(), 2, true);

            m_MqttClient.connect(connOpts);

        }catch (MqttException me){
            return false;
        }catch (NoSuchAlgorithmException e) {
            //e.printStackTrace();
            return false;
        } catch (KeyManagementException e) {
            //e.printStackTrace();
            return false;
        } catch (Exception e) {
            //e.printStackTrace();
            return false;
        }
        return m_MqttClient.isConnected();
    }
    public boolean hasThrownRuntimeException(){
        return m_Ex != null;
    }
    public boolean connect(){
        try {
            String cId = m_clientId;
            if (cId == null || cId.isEmpty()) {
                //cId = "BP-S-" + ((Long) currentTimeMillis()) + "-" + generateRandomIntStr();
                cId = Sys.getStation().getStationName();
            }
            m_Persistence = new MemoryPersistence();
            m_MqttClient = new MqttClient(m_BrokerUrl, cId, m_Persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true); // True = remove all topics, False = leave topics in place between connects
            if(m_UserCredentials) {
                connOpts.setPassword(m_Password.toCharArray());
                connOpts.setUserName(m_UserName);
            }
            connOpts.setConnectionTimeout(10);
            connOpts.setKeepAliveInterval(30);

            m_MqttClient.connect(connOpts);
        }catch (MqttException me){
            return false;
        }
        return m_MqttClient.isConnected();
    }
    public void disconnect() {
        try {
            m_BlockingQueue.clear();
            m_MqttClient.disconnect(5000);
        }catch (Exception e){}
    }
    private String generateRandomIntStr(){
        Random random = new Random();
        return ((Integer)random.nextInt(100)).toString();
    }
    public boolean isOnline(){
        if(m_MqttClient == null)return false;
        else return m_MqttClient.isConnected();
    }
    public Integer getQueueSize(){
        return m_BlockingQueue.size();
    }

    public Integer queueMessage (String topic, String message) {
        // https://www.eclipse.org/paho/files/javadoc/org/eclipse/paho/client/mqttv3/MqttMessage.html
        // 0 - message should be delivered at most once (zero or one times), not persisted, fastest.
        // 1 - message should be delivered at least once (one or more times), persisted
        // 2 -  indicates that a message should be delivered once, persisted
        // By default qos=0, retain=false
        return queueMessage(topic, message, 0, false);
    }
    public Integer queueMessage (String topic, String message, int qos) {
        return queueMessage(topic, message, qos, false);
    }
    public Integer queueMessage (String topic, String message, int qos, boolean retain){
        try{
            MqttMessage msg = new MqttMessage(message.getBytes());
            msg.setQos(qos);        // default 0
            msg.setRetained(retain);// default false

            MqttPayload objMqtt = new MqttPayload();
            objMqtt.topic = topic;
            objMqtt.message = msg;
            m_BlockingQueue.add(objMqtt);
            return m_BlockingQueue.size();

        }catch(IllegalStateException ie){
            // Message that could not be queued (queue size) are thrown away
            m_LastKnownError = "MQTT discarded, exceeded queue size: " + topic + "/" + message;
        }
        return -1;
    }
    @Override
    public void run() {
        try{
            MqttPayload objMqtt;
            while((objMqtt = m_BlockingQueue.poll()) != null) {
                if(m_MqttClient.isConnected()) {
                    synchronized (this) {
                        m_MqttClient.publish(objMqtt.topic, objMqtt.message);
                    }
                }
            }
        }catch (MqttException me){
            me.printStackTrace();
        }
    }
    public String getLastError() {
        return m_LastKnownError;
    }
}