package com.bitpool.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.baja.sys.Sys;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class MqttSubscribe implements MqttCallback, Runnable  {

    public static final int MQTT_QUEUE_SIZE = 32768; //131072;

    private String m_LastKnownError = "";
    private final String m_BrokerUrl;
    private String m_UserName;
    private String m_Password;
    private String m_clientId;
    private final Boolean m_UserCredentials;
    private Integer m_QueueSize = MQTT_QUEUE_SIZE;
    private Boolean m_UseSSL    = false;

    private MqttClient m_MqttClient;
    private MemoryPersistence m_Persistence;

    private final BlockingQueue<MqttPayload> m_BlockingQueue;// = new LinkedBlockingDeque<>(MQTT_QUEUE_SIZE);

    public MqttSubscribe(String brokerUrl, Integer queueSize, Boolean useSSL, String ClientId){
        m_UseSSL = useSSL;
        m_clientId = ClientId;
        if(useSSL){
            m_BrokerUrl         = "ssl://" + brokerUrl;
        }else{
            m_BrokerUrl         = "tcp://" + brokerUrl;
        }
        m_UserCredentials = false;
        m_QueueSize = queueSize;
        m_BlockingQueue = new LinkedBlockingDeque<>(queueSize);
    }
    public MqttSubscribe(String brokerUrl, String userName, String password, Integer queueSize, Boolean useSSL,  String ClientId){
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
        m_QueueSize = queueSize;
        m_BlockingQueue = new LinkedBlockingDeque<>(queueSize);
    }
    public boolean connect(String subscribeTopic){
        try {
            String cId = m_clientId;
            if (cId == null || cId.isEmpty()) {
                //cId = "BP-S-" + ((Long) currentTimeMillis()) + "-" + generateRandomIntStr();
                cId = Sys.getStation().getStationName();
            }

            m_Persistence = new MemoryPersistence();
            m_MqttClient = new MqttClient(m_BrokerUrl, cId, m_Persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
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
            connOpts.setConnectionTimeout(10);
            connOpts.setKeepAliveInterval(30);

            m_MqttClient.connect(connOpts);
            if("".equals(subscribeTopic)){
                subscribeTopic += "#";
            }else{
                subscribeTopic += "/#";
            }
            subscribeTopic = subscribeTopic.replaceAll("/+", "/");
            m_MqttClient.subscribe(subscribeTopic);

        }catch (MqttException me){
//            me.printStackTrace();
            return false;
        }catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
            return false;
        } catch (KeyManagementException e) {
//            e.printStackTrace();
            return false;
        } catch (Exception e) {
//            e.printStackTrace();
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
    public Integer getInflightMsgCount(){
        return m_QueueSize - m_BlockingQueue.remainingCapacity();
    }
    public ArrayList<MqttPayload> dequeueMessages (){
        ArrayList<MqttPayload> listTopics = new ArrayList<>();
        MqttPayload objMqtt;
        while((objMqtt = m_BlockingQueue.poll()) != null) {
            listTopics.add(objMqtt);
        }
        return listTopics;
    }
    public ArrayList<MqttPayload> dequeueMessages (Integer limit){
        ArrayList<MqttPayload> listTopics = new ArrayList<>();
        MqttPayload objMqtt;
        Integer cnt = 0;
        while((objMqtt = m_BlockingQueue.poll()) != null) {
            listTopics.add(objMqtt);
            cnt++;
            if(cnt >= limit )break;
        }
        return listTopics;
    }

    public ArrayList<MqttPayload> dequeueUniqueMessages (){
        ArrayList<MqttPayload> listTopics = new ArrayList<>();
        Map< String, MqttPayload > messages = new HashMap<>();
        MqttPayload objMqtt;
        while((objMqtt = m_BlockingQueue.poll()) != null) {
            if (messages.containsKey(objMqtt.topic)) {
                messages.replace(objMqtt.topic, objMqtt);
            }else{
                messages.put(objMqtt.topic, objMqtt);
            }
        }
        for (Map.Entry < String, MqttPayload > entry: messages.entrySet()) {
            listTopics.add(entry.getValue());
        }

        return listTopics;
    }
    public ArrayList<MqttPayload> dequeueUniqueMessages (Integer limit){
        ArrayList<MqttPayload> listTopics = new ArrayList<>();
        Map< String, MqttPayload > messages = new HashMap<>();
        MqttPayload objMqtt;
        Integer cnt = 0;
        while((objMqtt = m_BlockingQueue.poll()) != null) {
            if (messages.containsKey(objMqtt.topic)) {
                messages.replace(objMqtt.topic, objMqtt);
            }else{
                messages.put(objMqtt.topic, objMqtt);
            }
            cnt++;
            if(cnt >= limit )break;
        }
        for (Map.Entry < String, MqttPayload > entry: messages.entrySet()) {
            listTopics.add(entry.getValue());
        }

        return listTopics;
    }

    public boolean  injectMessage (String topic, MqttMessage message){
        try{
            MqttPayload objMqtt = new MqttPayload();
            objMqtt.topic = topic;
            objMqtt.message = message;
            m_BlockingQueue.add(objMqtt);

        }catch(IllegalStateException ie){
            return false;
        }
        return true;
    }
    private void stackMessage (String topic, MqttMessage message){
        try{
            MqttPayload objMqtt = new MqttPayload();
            objMqtt.topic = topic;
            objMqtt.message = message;
            m_BlockingQueue.add(objMqtt);

        }catch(IllegalStateException ie){
            // Message that could not be queued (queue size) are thrown away
            m_LastKnownError = "MQTT discarded, exceeded queue size: " + topic + "/" + message;
        }
    }

    @Override
    public void run() {
        m_MqttClient.setCallback(this);
    }
    public String getLastError() {
        return m_LastKnownError;
    }

    @Override
    public void connectionLost(Throwable throwable) {}

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        stackMessage(s, mqttMessage);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {}
}
