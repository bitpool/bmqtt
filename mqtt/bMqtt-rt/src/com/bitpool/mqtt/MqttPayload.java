package com.bitpool.mqtt;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttPayload {
    public MqttMessage message;
    public String topic;
    public String value;
}
