package com.bitpool.mqtt;


import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.baja.sys.Sys;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class MqttPublishThreadWorker implements Runnable{

    public static final int THREAD_SLEEP_MILLISECS      = 10;
    public static final int THREAD_FORCE_BLOCK_CNT      = 100;

    protected boolean m_Exit                            = false;
    protected String m_ThreadName                       = null;
    protected String m_BrokerUrl                        = null;
    protected String m_UserName                         = null;
    protected String m_Password                         = null;
    public    MqttClient m_MqttClient                   = null;
    protected MemoryPersistence m_Persistence           = null;
    protected Boolean m_UserCredentials                 = false;
    protected Boolean m_UseSSL                          = false;
    private String m_clientId;
    protected BlockingQueue<MqttPayload> m_BlockingQueue= null;

    public MqttPublishThreadWorker(String threadName, String brokerUrl, BlockingQueue blockingQ, Boolean useSSL, String ClientId ) {
        m_ThreadName    = threadName;
        m_BrokerUrl     = brokerUrl;
        m_BlockingQueue = blockingQ;
        m_UserCredentials = false;
        m_UseSSL = useSSL;
        m_clientId = ClientId;
    }
    public MqttPublishThreadWorker(String threadName, String brokerUrl, String userName, String password, BlockingQueue blockingQ, Boolean useSSL, String ClientId ) {
        m_ThreadName    = threadName;
        m_BrokerUrl     = brokerUrl;
        m_UserName      = userName;
        m_Password      = password;
        m_BlockingQueue = blockingQ;
        m_UserCredentials = true;
        m_UseSSL = useSSL;
        m_clientId = ClientId;

    }
    public synchronized void diconnectAndExit() {
        disconnect();
        m_Exit = true;
    }
    public void run() {
        Integer blockCount = 0;
        try {

            if(connect()){
                MqttPayload objMqtt;
                while(!Thread.interrupted() && !m_Exit){ //  run forever
                    Thread.sleep(THREAD_SLEEP_MILLISECS);
                    while((objMqtt = m_BlockingQueue.poll()) != null) {
                        if(m_MqttClient.isConnected()) {
                            m_MqttClient.publish(objMqtt.topic, objMqtt.message);
                            // Force the block out
                            if(blockCount % THREAD_FORCE_BLOCK_CNT == 0) Thread.sleep(THREAD_SLEEP_MILLISECS);
                            blockCount++;
                        }else{
                            m_Exit = true;
                        }
                    }
                }
            }else{
                disconnect(); //clean up
            }
        } catch (Exception e) {
            disconnect();
        }
        // When exiting here the thread is consider to be dead - ie Thread.isAlive() == false
    }
    public boolean connect(){
        try {
            String cId = m_clientId+"-"+m_ThreadName;
            if (cId == null || cId.isEmpty()) {
                //cId = "BP-P-" + ((Long) currentTimeMillis()) + "-" + generateRandomIntStr();
                cId = Sys.getStation().getStationName()+"-"+m_ThreadName;
            }

            m_Persistence = new MemoryPersistence();
            m_MqttClient = new MqttClient(m_BrokerUrl, cId, m_Persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true); // True = remove all topics, False = leave topics in place between connects
            if(m_UserCredentials) {
                connOpts.setPassword(m_Password.toCharArray());
                connOpts.setUserName(m_UserName);
            }
            if(m_UseSSL){

                // Use standard JSSE available in the runtime and
                // Use TLSv1.2 which is the default for a secured mosquitto
                SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
                sslContext.init(null, new TrustManager[] {new AlwaysTrustManager()}, new java.security.SecureRandom());
                SSLSocketFactory socketFactory = sslContext.getSocketFactory();
                connOpts.setSocketFactory(socketFactory);

                // -----------------------------------------------------------------------------------------------------
                // This code is the initial attempt to use TLS with pem files. Did not work due to Niagara permission
                // related issues in class SslUtil. Have used the above as an alternative.
                // -----------------------------------------------------------------------------------------------------
                //SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
                //sslContext.init(null, null, null);
                //connOpts.setSocketFactory(sslContext.getSocketFactory());
                //String caPEM    = "-----BEGIN CERTIFICATE-----";
                //String clientCrtPEM = "-----BEGIN CERTIFICATE-----";
                //String clientKey    = "-----BEGIN RSA PRIVATE KEY-----";
                //connOpts.setSocketFactory(SslUtil.getSocketFactory(caPEM, clientCrtPEM, clientKey, ""));
                // -----------------------------------------------------------------------------------------------------
            }

            m_MqttClient.connect(connOpts);

        }catch (MqttException me){
            System.out.println(me.getMessage()); // Show the connect issue here
            me.printStackTrace();
            return false;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        } catch (KeyManagementException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {

            e.printStackTrace();
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
}