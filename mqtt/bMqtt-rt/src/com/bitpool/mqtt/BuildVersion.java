package com.bitpool.mqtt;

public class BuildVersion {
    //https://stackoverflow.com/questions/29265750/intellij-idea-can-i-have-automatically-incremented-build-version-number
    public static final String VERSION_NUMBER   = "1";
    public static final String FEATURE_NUMBER   = "002";
    public static final String BUILD_NUMBER = "004";

    // --------------------------------------------------------------------------------
    // UPDATE *-rt.gradle on changed version number so it can be seen in Software Manager
    // SubDevice - driver that subscribes to a MQTT topic to create Niagara point(s).
    // PubDevice - driver that publishes Niagara point(s) to create an MQTT topic.
    // --------------------------------------------------------------------------------
    // Features / Changes
    //
    // 001.002.000  10/07/2023 - Initial commit to Github
}
