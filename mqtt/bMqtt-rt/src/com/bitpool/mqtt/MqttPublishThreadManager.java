package com.bitpool.mqtt;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class MqttPublishThreadManager implements Runnable{

    protected Thread       m_RunningThread= null;

    public static final int MQTT_QUEUE_SIZE             = 32768;
    public static final int MQTT_DEFAULT_THREAD_COUNT   = 2;

    protected String m_LastKnownError                   = null;
    protected String m_BrokerUrl                        = null;
    protected String m_UserName                         = null;
    protected String m_Password                         = null;
    protected Boolean m_UserCredentials                 = true;
    private String m_clientId;
    protected Integer m_ThreadCount                     = MQTT_DEFAULT_THREAD_COUNT;
    protected Boolean m_UseSSL                          = false;

    protected BlockingQueue<MqttPayload> m_BlockingQueue= new LinkedBlockingDeque<>(MQTT_QUEUE_SIZE);
    HashMap<String, Thread> m_Threads  = new HashMap();

    public MqttPublishThreadManager(String brokerUrl, Integer threadCount, Boolean useSSL, String clientId){
        m_UseSSL = useSSL;
        if(useSSL){
            m_BrokerUrl         = "ssl://" + brokerUrl;
        }else{
            m_BrokerUrl         = "tcp://" + brokerUrl;
        }
        m_UserCredentials   = false;
        m_ThreadCount = threadCount;
        m_clientId = clientId;
    }

    public MqttPublishThreadManager(String brokerUrl, String userName, String password, Integer threadCount, Boolean useSSL, String clientId){
        m_UseSSL = useSSL;
        m_clientId = clientId;
        if(useSSL){
            m_BrokerUrl         = "ssl://" + brokerUrl;
        }else{
            m_BrokerUrl         = "tcp://" + brokerUrl;
        }
        m_UserName          = userName;
        m_Password          = password;
        m_UserCredentials   = true;
        m_ThreadCount = threadCount;
    }

    public void run(){
        synchronized(this){
            m_RunningThread = Thread.currentThread();
        }
        // Start up the x worker threads with/without credentials
        Thread th;
        String thName ;
        if(m_UserCredentials){
            for (int i = 1; i <= m_ThreadCount; i++) {
                thName = "bp-" + i;
                if(m_UseSSL){
                    th = new Thread(new MqttPublishThreadWorker(thName, m_BrokerUrl, m_UserName, m_Password, m_BlockingQueue, true, m_clientId));
                }else{
                    th = new Thread(new MqttPublishThreadWorker(thName, m_BrokerUrl, m_UserName, m_Password, m_BlockingQueue, false, m_clientId));
                }

                th.setPriority(Thread.MAX_PRIORITY);
                th.start();
                m_Threads.put(thName, th);
            }
        }else{
            for (int i = 1; i < m_ThreadCount; i++) {
                thName = "bp-" + i;
                if(m_UseSSL) {
                    th = new Thread(new MqttPublishThreadWorker(thName, m_BrokerUrl, m_BlockingQueue, true, m_clientId));
                }else{
                    th = new Thread(new MqttPublishThreadWorker(thName, m_BrokerUrl, m_BlockingQueue, false, m_clientId));
                }
                th.setPriority(Thread.MAX_PRIORITY);
                th.start();
                m_Threads.put(thName, th);
            }
        }
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
    public synchronized void disconnect() {
        if(m_Threads.size()>0){
            for (Map.Entry<String, Thread> entry : m_Threads.entrySet()) {
                entry.getValue().interrupt();
            }
        }
        m_Threads = null;
    }
    public synchronized boolean isOnline(){
        if(m_Threads.size()>0){
            // Just get 1st thread  - assume this represents all threads
            // TODO - iterate all threads to check if any are online
            Thread hWorker = m_Threads.get("bp-1");
            return hWorker.isAlive() && !hWorker.isInterrupted();
        }
        return false;
    }

}
