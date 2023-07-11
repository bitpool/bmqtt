/**
 * Copyright 2019 bitpool, All Rights Reserved.
 */
package com.bitpool.mqtt;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.tridium.json.JSONArray;
import com.tridium.json.JSONException;
import com.tridium.json.JSONObject;
import com.tridium.ndriver.BNDevice;
import com.tridium.ndriver.poll.BINPollable;
import com.tridium.ndriver.util.SfUtil;
import com.tridium.sys.Nre;
import com.tridium.util.EscUtil;
import com.bitpool.mqtt.point.*;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import javax.baja.driver.util.BPollFrequency;
import javax.baja.naming.BOrd;
import javax.baja.naming.SlotPath;
import javax.baja.nre.annotations.Facet;
import javax.baja.nre.annotations.NiagaraAction;
import javax.baja.nre.annotations.NiagaraProperty;
import javax.baja.nre.annotations.NiagaraType;
import javax.baja.security.BPassword;
import javax.baja.status.*;
import javax.baja.sys.*;
import javax.baja.units.BUnit;
import javax.baja.util.BFormat;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import static javax.baja.sys.Sys.getStation;

/**
 *  BMqttSubDevice models a single device
 *
 *  @author   Admin
 *  @creation 21-Feb-19
 */
@NiagaraType
@NiagaraProperty(name = "pollFrequency", type = "BPollFrequency", defaultValue = "BPollFrequency.normal")
@NiagaraProperty(name = "points", type = "BMqttPointDeviceExt", defaultValue = "new BMqttPointDeviceExt()")

@NiagaraProperty(name = "driverVersion", type = "BString", defaultValue = "", facets = {@Facet(name = "BFacets.FIELD_WIDTH", value = "10")}, flags = Flags.READONLY)

@NiagaraProperty(name = "driverStatus", type = "BString", defaultValue = "", facets = {
        @Facet(name = "BFacets.FIELD_WIDTH", value = "75")
}, flags = Flags.READONLY)
@NiagaraProperty(name = "brokerConnectionType", type = "BMqttConnectionType", defaultValue = "BMqttConnectionType.make(2)")

@NiagaraProperty(name = "brokerAddress", type = "BString", defaultValue = "", facets = {
    @Facet(name = "BFacets.FIELD_WIDTH", value = "75")
})
@NiagaraProperty(name = "brokerPort", type = "BInteger", defaultValue = "BInteger.make(1883)",
    facets = {
            @Facet(name = "BFacets.MIN", value = "BInteger.make(0)"),
            @Facet(name = "BFacets.MAX", value = "BInteger.make(65535)")
    }
)
@NiagaraProperty(name = "brokerUsingTls", type = "BBoolean", defaultValue = "BBoolean.make(\"false\")", flags = Flags.SUMMARY)
@NiagaraProperty(name = "brokerUsername", type = "BString", defaultValue = "", facets = {
        @Facet(name = "BFacets.FIELD_WIDTH", value = "75")
})
@NiagaraProperty(name = "brokerPassword", type = "BPassword", defaultValue = "BPassword.make(\"\")", facets = {
        @Facet(name = "BFacets.FIELD_WIDTH", value = "75")
})
@NiagaraProperty(name = "subscribeTopicPath", type = "BString", defaultValue = "/", facets = {
        @Facet(name = "BFacets.FIELD_WIDTH", value = "75")
})
@NiagaraProperty(name = "subscribeBufferSize", type = "BInteger", defaultValue = "BInteger.make(32768)",
        facets = {
                @Facet(name = "BFacets.MIN", value = "BInteger.make(32768)"),
                @Facet(name = "BFacets.MAX", value = "BInteger.make(524288)")
        }
)
@NiagaraProperty(name = "subscribeAutoDiscover", type = "BMqttAutoDiscover", defaultValue = "BMqttAutoDiscover.make(0)")
@NiagaraProperty(name = "dataInputType", type = "BMqttInputType", defaultValue = "BMqttInputType.make(0)", flags = Flags.HIDDEN)
@NiagaraProperty(name = "deviceStatusAsPoints", type = "BBoolean", defaultValue = "BBoolean.make(\"false\")", flags = Flags.HIDDEN)
@NiagaraProperty(name = "pointForceUpdate", type = "BBoolean", defaultValue = "BBoolean.make(\"false\")", flags = Flags.SUMMARY)
@NiagaraProperty(name = "pointPriorityLevel", type = "BMqttPriorityType", defaultValue = "BMqttPriorityType.make(16)")
@NiagaraProperty(
        name = "pointIn8Duration",
        type = "BRelTime",
        defaultValue = "BRelTime.make(60000L)"
)
@NiagaraProperty(name = "pointPriorityLevelPrev", type = "BInteger", defaultValue = "BInteger.make(17)", flags = Flags.HIDDEN)

@NiagaraProperty(name = "debugToConsole", type = "BBoolean", defaultValue = "BBoolean.make(\"false\")", flags = Flags.SUMMARY)
@NiagaraProperty(name = "debugLabel", type = "BString", defaultValue = "MQTT-SUB", facets = {
        @Facet(name = "BFacets.FIELD_WIDTH", value = "75")
})


@NiagaraAction(name = "Enable")
@NiagaraAction(name = "Disable")
@NiagaraAction(name = "Restart")
@NiagaraAction(name = "ClearAllInputs")

public class BMqttSubDevice
        extends BNDevice
        implements BINPollable {

    private MqttPublish m_MqttPubConn = null;
    private MqttSubscribe m_MqttSubConn = null;
    private boolean m_IsAllowedToPoll = false;
    private boolean m_IsClearingInputsRequired = false;
    private MqttSubQuery m_SubOrdLookUp = new MqttSubQuery();
    private MqttSubPubQuery m_SubPubOrdLookUp = new MqttSubPubQuery();
    private final SimpleDateFormat m_OutputDateFormat = new SimpleDateFormat("MMM-dd HH:mm:ss");

    // Add facet to include following in auto manager view
    public static final Property status = newProperty(Flags.TRANSIENT | Flags.READONLY | Flags.SUMMARY | Flags.DEFAULT_ON_CLONE, BStatus.ok, SfUtil.incl(SfUtil.MGR_EDIT_READONLY));







/*+ ------------ BEGIN BAJA AUTO GENERATED CODE ------------ +*/
/*@ $com.bitpool.mqtt.BMqttSubDevice(1337272754)1.0$ @*/
/* Generated Wed Mar 10 09:13:37 AEST 2021 by Slot-o-Matic (c) Tridium, Inc. 2012 */

////////////////////////////////////////////////////////////////
// Property "pollFrequency"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code pollFrequency} property.
   * @see #getPollFrequency
   * @see #setPollFrequency
   */
  public static final Property pollFrequency = newProperty(0, BPollFrequency.normal, null);
  
  /**
   * Get the {@code pollFrequency} property.
   * @see #pollFrequency
   */
  public BPollFrequency getPollFrequency() { return (BPollFrequency)get(pollFrequency); }
  
  /**
   * Set the {@code pollFrequency} property.
   * @see #pollFrequency
   */
  public void setPollFrequency(BPollFrequency v) { set(pollFrequency, v, null); }

////////////////////////////////////////////////////////////////
// Property "points"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code points} property.
   * @see #getPoints
   * @see #setPoints
   */
  public static final Property points = newProperty(0, new BMqttPointDeviceExt(), null);
  
  /**
   * Get the {@code points} property.
   * @see #points
   */
  public BMqttPointDeviceExt getPoints() { return (BMqttPointDeviceExt)get(points); }
  
  /**
   * Set the {@code points} property.
   * @see #points
   */
  public void setPoints(BMqttPointDeviceExt v) { set(points, v, null); }

////////////////////////////////////////////////////////////////
// Property "driverVersion"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code driverVersion} property.
   * @see #getDriverVersion
   * @see #setDriverVersion
   */
  public static final Property driverVersion = newProperty(Flags.READONLY, "", BFacets.make(BFacets.FIELD_WIDTH, 10));
  
  /**
   * Get the {@code driverVersion} property.
   * @see #driverVersion
   */
  public String getDriverVersion() { return getString(driverVersion); }
  
  /**
   * Set the {@code driverVersion} property.
   * @see #driverVersion
   */
  public void setDriverVersion(String v) { setString(driverVersion, v, null); }

////////////////////////////////////////////////////////////////
// Property "driverStatus"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code driverStatus} property.
   * @see #getDriverStatus
   * @see #setDriverStatus
   */
  public static final Property driverStatus = newProperty(Flags.READONLY, "", BFacets.make(BFacets.FIELD_WIDTH, 75));
  
  /**
   * Get the {@code driverStatus} property.
   * @see #driverStatus
   */
  public String getDriverStatus() { return getString(driverStatus); }
  
  /**
   * Set the {@code driverStatus} property.
   * @see #driverStatus
   */
  public void setDriverStatus(String v) { setString(driverStatus, v, null); }

////////////////////////////////////////////////////////////////
// Property "brokerConnectionType"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code brokerConnectionType} property.
   * @see #getBrokerConnectionType
   * @see #setBrokerConnectionType
   */
  public static final Property brokerConnectionType = newProperty(0, BMqttConnectionType.make(2), null);
  
  /**
   * Get the {@code brokerConnectionType} property.
   * @see #brokerConnectionType
   */
  public BMqttConnectionType getBrokerConnectionType() { return (BMqttConnectionType)get(brokerConnectionType); }
  
  /**
   * Set the {@code brokerConnectionType} property.
   * @see #brokerConnectionType
   */
  public void setBrokerConnectionType(BMqttConnectionType v) { set(brokerConnectionType, v, null); }

////////////////////////////////////////////////////////////////
// Property "brokerAddress"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code brokerAddress} property.
   * @see #getBrokerAddress
   * @see #setBrokerAddress
   */
  public static final Property brokerAddress = newProperty(0, "", BFacets.make(BFacets.FIELD_WIDTH, 75));
  
  /**
   * Get the {@code brokerAddress} property.
   * @see #brokerAddress
   */
  public String getBrokerAddress() { return getString(brokerAddress); }
  
  /**
   * Set the {@code brokerAddress} property.
   * @see #brokerAddress
   */
  public void setBrokerAddress(String v) { setString(brokerAddress, v, null); }

////////////////////////////////////////////////////////////////
// Property "brokerPort"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code brokerPort} property.
   * @see #getBrokerPort
   * @see #setBrokerPort
   */
  public static final Property brokerPort = newProperty(0, ((BInteger.make(1883))).getInt(), BFacets.make(BFacets.make(BFacets.MIN, BInteger.make(0)), BFacets.make(BFacets.MAX, BInteger.make(65535))));
  
  /**
   * Get the {@code brokerPort} property.
   * @see #brokerPort
   */
  public int getBrokerPort() { return getInt(brokerPort); }
  
  /**
   * Set the {@code brokerPort} property.
   * @see #brokerPort
   */
  public void setBrokerPort(int v) { setInt(brokerPort, v, null); }

////////////////////////////////////////////////////////////////
// Property "brokerUsingTls"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code brokerUsingTls} property.
   * @see #getBrokerUsingTls
   * @see #setBrokerUsingTls
   */
  public static final Property brokerUsingTls = newProperty(Flags.SUMMARY, ((BBoolean.make("false"))).getBoolean(), null);
  
  /**
   * Get the {@code brokerUsingTls} property.
   * @see #brokerUsingTls
   */
  public boolean getBrokerUsingTls() { return getBoolean(brokerUsingTls); }
  
  /**
   * Set the {@code brokerUsingTls} property.
   * @see #brokerUsingTls
   */
  public void setBrokerUsingTls(boolean v) { setBoolean(brokerUsingTls, v, null); }

////////////////////////////////////////////////////////////////
// Property "brokerUsername"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code brokerUsername} property.
   * @see #getBrokerUsername
   * @see #setBrokerUsername
   */
  public static final Property brokerUsername = newProperty(0, "", BFacets.make(BFacets.FIELD_WIDTH, 75));
  
  /**
   * Get the {@code brokerUsername} property.
   * @see #brokerUsername
   */
  public String getBrokerUsername() { return getString(brokerUsername); }
  
  /**
   * Set the {@code brokerUsername} property.
   * @see #brokerUsername
   */
  public void setBrokerUsername(String v) { setString(brokerUsername, v, null); }

////////////////////////////////////////////////////////////////
// Property "brokerPassword"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code brokerPassword} property.
   * @see #getBrokerPassword
   * @see #setBrokerPassword
   */
  public static final Property brokerPassword = newProperty(0, BPassword.make(""), BFacets.make(BFacets.FIELD_WIDTH, 75));
  
  /**
   * Get the {@code brokerPassword} property.
   * @see #brokerPassword
   */
  public BPassword getBrokerPassword() { return (BPassword)get(brokerPassword); }
  
  /**
   * Set the {@code brokerPassword} property.
   * @see #brokerPassword
   */
  public void setBrokerPassword(BPassword v) { set(brokerPassword, v, null); }

  ////////////////////////////////////////////////////////////////
// Property "clientId"
////////////////////////////////////////////////////////////////

  /**
   * Slot for the {@code brokerUsername} property.
   * @see #getBrokerUsername
   * @see #setBrokerUsername
   */
  public static final Property clientId = newProperty(0, "", BFacets.make(BFacets.FIELD_WIDTH, 75));

  /**
   * Get the {@code brokerUsername} property.
   * @see #brokerUsername
   */
  public String getClientId() { return getString(clientId); }

  /**
   * Set the {@code brokerUsername} property.
   * @see #brokerUsername
   */
  public void setClientId(String v) { setString(clientId, v, null); }


////////////////////////////////////////////////////////////////
// Property "subscribeTopicPath"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code subscribeTopicPath} property.
   * @see #getSubscribeTopicPath
   * @see #setSubscribeTopicPath
   */
  public static final Property subscribeTopicPath = newProperty(0, "/", BFacets.make(BFacets.FIELD_WIDTH, 75));
  
  /**
   * Get the {@code subscribeTopicPath} property.
   * @see #subscribeTopicPath
   */
  public String getSubscribeTopicPath() { return getString(subscribeTopicPath); }
  
  /**
   * Set the {@code subscribeTopicPath} property.
   * @see #subscribeTopicPath
   */
  public void setSubscribeTopicPath(String v) { setString(subscribeTopicPath, v, null); }

////////////////////////////////////////////////////////////////
// Property "subscribeBufferSize"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code subscribeBufferSize} property.
   * @see #getSubscribeBufferSize
   * @see #setSubscribeBufferSize
   */
  public static final Property subscribeBufferSize = newProperty(0, ((BInteger.make(32768))).getInt(), BFacets.make(BFacets.make(BFacets.MIN, BInteger.make(32768)), BFacets.make(BFacets.MAX, BInteger.make(524288))));
  
  /**
   * Get the {@code subscribeBufferSize} property.
   * @see #subscribeBufferSize
   */
  public int getSubscribeBufferSize() { return getInt(subscribeBufferSize); }
  
  /**
   * Set the {@code subscribeBufferSize} property.
   * @see #subscribeBufferSize
   */
  public void setSubscribeBufferSize(int v) { setInt(subscribeBufferSize, v, null); }

////////////////////////////////////////////////////////////////
// Property "subscribeAutoDiscover"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code subscribeAutoDiscover} property.
   * @see #getSubscribeAutoDiscover
   * @see #setSubscribeAutoDiscover
   */
  public static final Property subscribeAutoDiscover = newProperty(0, BMqttAutoDiscover.make(0), null);
  
  /**
   * Get the {@code subscribeAutoDiscover} property.
   * @see #subscribeAutoDiscover
   */
  public BMqttAutoDiscover getSubscribeAutoDiscover() { return (BMqttAutoDiscover)get(subscribeAutoDiscover); }
  
  /**
   * Set the {@code subscribeAutoDiscover} property.
   * @see #subscribeAutoDiscover
   */
  public void setSubscribeAutoDiscover(BMqttAutoDiscover v) { set(subscribeAutoDiscover, v, null); }

////////////////////////////////////////////////////////////////
// Property "dataInputType"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code dataInputType} property.
   * @see #getDataInputType
   * @see #setDataInputType
   */
  public static final Property dataInputType = newProperty(Flags.HIDDEN, BMqttInputType.make(0), null);
  
  /**
   * Get the {@code dataInputType} property.
   * @see #dataInputType
   */
  public BMqttInputType getDataInputType() { return (BMqttInputType)get(dataInputType); }
  
  /**
   * Set the {@code dataInputType} property.
   * @see #dataInputType
   */
  public void setDataInputType(BMqttInputType v) { set(dataInputType, v, null); }

////////////////////////////////////////////////////////////////
// Property "deviceStatusAsPoints"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code deviceStatusAsPoints} property.
   * @see #getDeviceStatusAsPoints
   * @see #setDeviceStatusAsPoints
   */
  public static final Property deviceStatusAsPoints = newProperty(Flags.HIDDEN, ((BBoolean.make("false"))).getBoolean(), null);
  
  /**
   * Get the {@code deviceStatusAsPoints} property.
   * @see #deviceStatusAsPoints
   */
  public boolean getDeviceStatusAsPoints() { return getBoolean(deviceStatusAsPoints); }
  
  /**
   * Set the {@code deviceStatusAsPoints} property.
   * @see #deviceStatusAsPoints
   */
  public void setDeviceStatusAsPoints(boolean v) { setBoolean(deviceStatusAsPoints, v, null); }

////////////////////////////////////////////////////////////////
// Property "pointForceUpdate"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code pointForceUpdate} property.
   * @see #getPointForceUpdate
   * @see #setPointForceUpdate
   */
  public static final Property pointForceUpdate = newProperty(Flags.SUMMARY, ((BBoolean.make("false"))).getBoolean(), null);
  
  /**
   * Get the {@code pointForceUpdate} property.
   * @see #pointForceUpdate
   */
  public boolean getPointForceUpdate() { return getBoolean(pointForceUpdate); }
  
  /**
   * Set the {@code pointForceUpdate} property.
   * @see #pointForceUpdate
   */
  public void setPointForceUpdate(boolean v) { setBoolean(pointForceUpdate, v, null); }

////////////////////////////////////////////////////////////////
// Property "pointPriorityLevel"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code pointPriorityLevel} property.
   * @see #getPointPriorityLevel
   * @see #setPointPriorityLevel
   */
  public static final Property pointPriorityLevel = newProperty(0, BMqttPriorityType.make(16), null);
  
  /**
   * Get the {@code pointPriorityLevel} property.
   * @see #pointPriorityLevel
   */
  public BMqttPriorityType getPointPriorityLevel() { return (BMqttPriorityType)get(pointPriorityLevel); }
  
  /**
   * Set the {@code pointPriorityLevel} property.
   * @see #pointPriorityLevel
   */
  public void setPointPriorityLevel(BMqttPriorityType v) { set(pointPriorityLevel, v, null); }

////////////////////////////////////////////////////////////////
// Property "pointIn8Duration"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code pointIn8Duration} property.
   * @see #getPointIn8Duration
   * @see #setPointIn8Duration
   */
  public static final Property pointIn8Duration = newProperty(0, BRelTime.make(60000L), null);
  
  /**
   * Get the {@code pointIn8Duration} property.
   * @see #pointIn8Duration
   */
  public BRelTime getPointIn8Duration() { return (BRelTime)get(pointIn8Duration); }
  
  /**
   * Set the {@code pointIn8Duration} property.
   * @see #pointIn8Duration
   */
  public void setPointIn8Duration(BRelTime v) { set(pointIn8Duration, v, null); }

////////////////////////////////////////////////////////////////
// Property "pointPriorityLevelPrev"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code pointPriorityLevelPrev} property.
   * @see #getPointPriorityLevelPrev
   * @see #setPointPriorityLevelPrev
   */
  public static final Property pointPriorityLevelPrev = newProperty(Flags.HIDDEN, ((BInteger.make(17))).getInt(), null);
  
  /**
   * Get the {@code pointPriorityLevelPrev} property.
   * @see #pointPriorityLevelPrev
   */
  public int getPointPriorityLevelPrev() { return getInt(pointPriorityLevelPrev); }
  
  /**
   * Set the {@code pointPriorityLevelPrev} property.
   * @see #pointPriorityLevelPrev
   */
  public void setPointPriorityLevelPrev(int v) { setInt(pointPriorityLevelPrev, v, null); }

////////////////////////////////////////////////////////////////
// Property "debugToConsole"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code debugToConsole} property.
   * @see #getDebugToConsole
   * @see #setDebugToConsole
   */
  public static final Property debugToConsole = newProperty(Flags.SUMMARY, ((BBoolean.make("false"))).getBoolean(), null);
  
  /**
   * Get the {@code debugToConsole} property.
   * @see #debugToConsole
   */
  public boolean getDebugToConsole() { return getBoolean(debugToConsole); }
  
  /**
   * Set the {@code debugToConsole} property.
   * @see #debugToConsole
   */
  public void setDebugToConsole(boolean v) { setBoolean(debugToConsole, v, null); }

////////////////////////////////////////////////////////////////
// Property "debugLabel"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code debugLabel} property.
   * @see #getDebugLabel
   * @see #setDebugLabel
   */
  public static final Property debugLabel = newProperty(0, "MQTT-SUB", BFacets.make(BFacets.FIELD_WIDTH, 75));
  
  /**
   * Get the {@code debugLabel} property.
   * @see #debugLabel
   */
  public String getDebugLabel() { return getString(debugLabel); }
  
  /**
   * Set the {@code debugLabel} property.
   * @see #debugLabel
   */
  public void setDebugLabel(String v) { setString(debugLabel, v, null); }

////////////////////////////////////////////////////////////////
// Action "Enable"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code Enable} action.
   * @see #Enable()
   */
  public static final Action Enable = newAction(0, null);
  
  /**
   * Invoke the {@code Enable} action.
   * @see #Enable
   */
  public void Enable() { invoke(Enable, null, null); }

////////////////////////////////////////////////////////////////
// Action "Disable"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code Disable} action.
   * @see #Disable()
   */
  public static final Action Disable = newAction(0, null);
  
  /**
   * Invoke the {@code Disable} action.
   * @see #Disable
   */
  public void Disable() { invoke(Disable, null, null); }

////////////////////////////////////////////////////////////////
// Action "Restart"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code Restart} action.
   * @see #Restart()
   */
  public static final Action Restart = newAction(0, null);
  
  /**
   * Invoke the {@code Restart} action.
   * @see #Restart
   */
  public void Restart() { invoke(Restart, null, null); }

////////////////////////////////////////////////////////////////
// Action "ClearAllInputs"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code ClearAllInputs} action.
   * @see #ClearAllInputs()
   */
  public static final Action ClearAllInputs = newAction(0, null);
  
  /**
   * Invoke the {@code ClearAllInputs} action.
   * @see #ClearAllInputs
   */
  public void ClearAllInputs() { invoke(ClearAllInputs, null, null); }

////////////////////////////////////////////////////////////////
// Type
////////////////////////////////////////////////////////////////
  
  @Override
  public Type getType() { return TYPE; }
  public static final Type TYPE = Sys.loadType(BMqttSubDevice.class);

/*+ ------------ END BAJA AUTO GENERATED CODE -------------- +*/

    public Type getNetworkType() {
        return BMqttNetwork.TYPE;
    }
    public void started() throws Exception {
        super.started();
        setDriverVersion(getDrvVersion());
        getMqttSubNetwork().getPollScheduler().subscribe(this);
        setFlags(getSlot("status"), Flags.HIDDEN);
        setFlags(getSlot("faultCause"), Flags.HIDDEN);

       
    }
    public void stopped() throws Exception {
        // unregister device with poll scheduler
        getMqttSubNetwork().getPollScheduler().unsubscribe(this);
        super.stopped();
    }
    public void doPing() {
        // Ping does not work correctly since there are two devices under a single network
        // Check BMqttNetwork -
        //      public Type getDeviceType(){return BMqttPubDevice.TYPE;}
        //
        // Here we must return a single type, which we set to pub.
        // The issue is that the ping async does not work for sub!
        //
        // HACK: check the device is online during doPoll() then restart if connection to Broker is dead
        if(m_MqttSubConn != null && m_MqttSubConn.isOnline()) {
          this.getHealth().pingOk();
        }
        else{
          this.getHealth().pingFail("Broker Connection Down");
        }
    }
    public void doPoll() {

        if(m_IsAllowedToPoll ){
            Integer newPriorityInput = getPointPriorityLevel().getOrdinal();
            Integer oldPriorityInput = getPointPriorityLevelPrev();
            if(newPriorityInput !=  oldPriorityInput && oldPriorityInput != BMqttPriorityType.DISABLED) {
                debugOut("Clearing all component inputs: Setting " + getInputName(oldPriorityInput) + " to NULL" , 1, false);
                clearAllPointsPriorityInputs(oldPriorityInput);
                debugOut("All cleared OK" , 2, false);
                debugOut("");
            }
            if(m_IsClearingInputsRequired){
                debugOut("User request to clear all component inputs", 1, true);
                clearAllPointsPriorityInputs(0, true);
                debugOut("All cleared OK" , 2, true);
                debugOut("");
                m_IsClearingInputsRequired = false;
            }
            setPointPriorityLevelPrev(newPriorityInput);

            subPoll();
        }

    }

    private String getInputName(Integer componentPriorityIndex){
        String inputName = "unknown input";
        if(componentPriorityIndex ==      BMqttPriorityType.IN_1EO){inputName = "Input 1 (In1)";}
        else if(componentPriorityIndex == BMqttPriorityType.IN_2){inputName = "Input 2 (In2)";}
        else if(componentPriorityIndex == BMqttPriorityType.IN_3){inputName = "Input 3 (In3)";}
        else if(componentPriorityIndex == BMqttPriorityType.IN_4){inputName = "Input 4 (In4)";}
        else if(componentPriorityIndex == BMqttPriorityType.IN_5){inputName = "Input 5 (In5)";}
        else if(componentPriorityIndex == BMqttPriorityType.IN_6){inputName = "Input 6 (In6)";}
        else if(componentPriorityIndex == BMqttPriorityType.IN_7){inputName = "Input 7 (In7)";}
        else if(componentPriorityIndex == BMqttPriorityType.IN_8MO){inputName = "Input 8 (In8)";}
        else if(componentPriorityIndex == BMqttPriorityType.IN_9){inputName = "Input 9 (In9)";}
        else if(componentPriorityIndex == BMqttPriorityType.IN_10){inputName = "Input 10 (In10)";}
        else if(componentPriorityIndex == BMqttPriorityType.IN_11){inputName = "Input 11 (In11)";}
        else if(componentPriorityIndex == BMqttPriorityType.IN_12){inputName = "Input 12 (In12)";}
        else if(componentPriorityIndex == BMqttPriorityType.IN_13){inputName = "Input 13 (In13)";}
        else if(componentPriorityIndex == BMqttPriorityType.IN_14){inputName = "Input 14 (In14)";}
        else if(componentPriorityIndex == BMqttPriorityType.IN_15){inputName = "Input 15 (In15)";}
        else if(componentPriorityIndex == BMqttPriorityType.IN_16){inputName = "Input 16 (In16)";}
        else if(componentPriorityIndex == BMqttPriorityType.FALLBACK){inputName = "Fallback";}
        return inputName;
    }
    private void updateComponentInput(BComponent bComp, BValue testValue, String slotName){
        if(testValue.getType() == BStatusString.TYPE){
            bComp.set(slotName, new BStatusString("", BStatus.nullStatus));
        }else if(testValue.getType() == BStatusNumeric.TYPE){
            bComp.set(slotName, new BStatusNumeric(0.0D, BStatus.nullStatus));
        }else if(testValue.getType() == BStatusEnum.TYPE){
            BStatusEnum bE = (BStatusEnum) bComp.get(slotName);
            bE.setStatus(BStatus.nullStatus);
            bComp.set(slotName, bE);
        }else if(testValue.getType() == BStatusBoolean.TYPE){
            bComp.set(slotName, new BStatusBoolean(false, BStatus.nullStatus));
        }
    }
    private void clearAllPointsPriorityInputs(Integer pointLevelIndex) {
        clearAllPointsPriorityInputs(pointLevelIndex, false);
    }
    private void clearAllPointsPriorityInputs(Integer pointLevelIndex, Boolean forceAllCleared){
        m_SubPubOrdLookUp.buildOrdLookup(getPoints().getNavOrd().toString());
        for (Map.Entry < String, String > entry: m_SubPubOrdLookUp.m_Ords.entrySet()) {
            String pointOrd = entry.getValue();
            //debugOut(pointOrd, 2, true);
            try {
                BComponent bComp = BOrd.make(escapeOrd(pointOrd)).resolve().getComponent(); // Resolve the component
                BValue testValue =  bComp.get("in1");                                       // Use In1 to discover the component derived class (Str, Num, Bool, Enum) of any N4 component.

                if(forceAllCleared) {
                    if(testValue.getType() == BStatusString.TYPE){
                        try{
                            for(int idx = 1; idx <= 16; idx++) {bComp.set("in" + idx , new BStatusString("", BStatus.nullStatus));}
                            bComp.set("fallback", new BStatusString("", BStatus.nullStatus));
                        }catch (Exception exp){}
                    }else if(testValue.getType() == BStatusNumeric.TYPE){
                        try{
                            for(int idx = 1; idx <= 16; idx++) {
                                bComp.set("in" + idx, new BStatusNumeric(0.0D, BStatus.nullStatus));
                            }
                            bComp.set("fallback", new BStatusNumeric(0.0D, BStatus.nullStatus));
                        }catch (Exception exp){}
                    }else if(testValue.getType() == BStatusEnum.TYPE){
                        try{
                            for(int idx = 1; idx <= 16; idx++) {
                                BStatusEnum bE = (BStatusEnum) bComp.get("in" + idx);
                                bE.setStatus(BStatus.nullStatus);
                                bComp.set("in" + idx, bE);
                            }
                            BStatusEnum bE = (BStatusEnum) bComp.get("fallback");
                            bE.setStatus(BStatus.nullStatus);
                            bComp.set("fallback", bE);
                        }catch (Exception exp){}

                    }else if(testValue.getType() == BStatusBoolean.TYPE){
                        try{
                            for(int idx = 1; idx <= 16; idx++) {
                                // TODO Issue here where these call do clear the inputs, but does not change the Out status to null.
                                // However works for all other types!
                                BStatusBoolean bB = (BStatusBoolean) bComp.get("in" + idx);
                                bB.setStatus(BStatus.nullStatus);
                                bComp.set("in" + idx, bB);
                                //bComp.set("in" + idx , new BStatusBoolean(false, BStatus.nullStatus));
                            }
                            bComp.set("fallback", new BStatusBoolean(false, BStatus.nullStatus));
                        }catch (Exception exp){}
                    }
                }else {
                    if (pointLevelIndex == BMqttPriorityType.IN_1EO) {
                        updateComponentInput(bComp, testValue, "in1");
                    } else if (pointLevelIndex == BMqttPriorityType.IN_2) {
                        updateComponentInput(bComp, testValue, "in2");
                    } else if (pointLevelIndex == BMqttPriorityType.IN_3) {
                        updateComponentInput(bComp, testValue, "in3");
                    } else if (pointLevelIndex == BMqttPriorityType.IN_4) {
                        updateComponentInput(bComp, testValue, "in4");
                    } else if (pointLevelIndex == BMqttPriorityType.IN_5) {
                        updateComponentInput(bComp, testValue, "in5");
                    } else if (pointLevelIndex == BMqttPriorityType.IN_6) {
                        updateComponentInput(bComp, testValue, "in6");
                    } else if (pointLevelIndex == BMqttPriorityType.IN_7) {
                        updateComponentInput(bComp, testValue, "in7");
                    } else if (pointLevelIndex == BMqttPriorityType.IN_8MO) {
                        updateComponentInput(bComp, testValue, "in8");
                    } else if (pointLevelIndex == BMqttPriorityType.IN_9) {
                        updateComponentInput(bComp, testValue, "in9");
                    } else if (pointLevelIndex == BMqttPriorityType.IN_10) {
                        updateComponentInput(bComp, testValue, "in10");
                    } else if (pointLevelIndex == BMqttPriorityType.IN_11) {
                        updateComponentInput(bComp, testValue, "in11");
                    } else if (pointLevelIndex == BMqttPriorityType.IN_12) {
                        updateComponentInput(bComp, testValue, "in12");
                    } else if (pointLevelIndex == BMqttPriorityType.IN_13) {
                        updateComponentInput(bComp, testValue, "in13");
                    } else if (pointLevelIndex == BMqttPriorityType.IN_14) {
                        updateComponentInput(bComp, testValue, "in14");
                    } else if (pointLevelIndex == BMqttPriorityType.IN_15) {
                        updateComponentInput(bComp, testValue, "in15");
                    } else if (pointLevelIndex == BMqttPriorityType.IN_16) {
                        updateComponentInput(bComp, testValue, "in16");
                    } else if (pointLevelIndex == BMqttPriorityType.FALLBACK) {
                        updateComponentInput(bComp, testValue, "fallback");
                    }
                }
            }catch (Exception ex){}
        }

    }
    private String escapeOrd(String ord){
        // Escape characters in the ORD after the 'slot:/' path
        Integer index = ord.indexOf("slot:/") + 6;
        String slot = ord.substring(index);
        String escdSlot = ord.substring(0, index);
        try {
            String[] aryOrdPath = slot.split("/");
            if(aryOrdPath.length > 0){
                for (String path : aryOrdPath) {
                    if(path != null) {
                        escdSlot += SlotPath.escape(path) + "/";
                    }
                }
                escdSlot = escdSlot.replaceAll("\\/$", ""); // Remove trailing slash
                return escdSlot;
            }

        } catch (Exception ex){
            return null;
        }
        return null;
    }

    public void atSteadyState() throws Exception {
        super.atSteadyState();
        showTime();
    }
    public void doEnable() {
        this.setEnabled(true);
    }
    public void doDisable() {
        debugOut("User request to disable device", 1, true);
        this.setEnabled(false);
    }
    public void doRestart(){
        debugOut("User requested action, restart in progress", 1, true);
        setEnabled(false);
        setEnabled(true);
    }
    public void doClearAllInputs() {
        m_IsClearingInputsRequired = true;
    }
    public void changed(Property p, Context cx) {
        super.changed(p, cx);
        if(!isRunning()) return;

        if(p == enabled) {
            if(!isDisabled()) {
                showTime();
                debugOut("Device enabled", 1, false);
            } else {
                debugOut("Device stopped", 1, false);
                setDriverStatus("Device stopped");

                mqttDisconnect();
                m_IsAllowedToPoll = false;
            }
        }

        if( p == brokerUsername ||
            p == brokerPassword ||
            p == clientId ||
            p == brokerAddress ||
            p == brokerPort ||
            p == subscribeTopicPath ||
            p == brokerConnectionType ||
            p == brokerUsingTls ||
            p == subscribeBufferSize) {

            setDriverStatus("Restarting connection");
            debugOut("MQTT configuration has changed, restarting connection", 1, true);
            mqttDisconnect();
            setEnabled(true);
            showTime();

        }
        if(p == debugToConsole) {
            if(getDebugToConsole()) {
                debugOut("Debugging to console enabled", 1, true);
            } else {
                debugOut("Debugging to console disabled", 1, true);
            }
        }
    }
    private void showTime() {
        debugOut("MQTT SUBSCRIBE DRIVER [" + getDrvVersion() + "]" , 1, true);
        m_SubOrdLookUp = new MqttSubQuery();
        m_SubPubOrdLookUp = new MqttSubPubQuery();
        m_SubPubOrdLookUp.buildOrdLookup(getPoints().getNavOrd().toString());
        mqttConnect();
        m_IsAllowedToPoll = true;
        m_IsClearingInputsRequired = false;
    }
    private void injectDeviceStatusIntoQueue(){

        String hostName = Sys.getHostName();
        String hostId = Sys.getHostId();
        String stationName = getStation().getStationName();
        String deviceName = this.getName();

        String topicPrefix = "/mqttDeviceStatus/" + stationName + "/" + deviceName;

        MqttMessage mqttMsg = new MqttMessage();
        mqttMsg.setPayload(hostName.getBytes());
        m_MqttSubConn.injectMessage(topicPrefix + "/hostname/", mqttMsg);

        mqttMsg = new MqttMessage();
        mqttMsg.setPayload(hostId.getBytes());
        m_MqttSubConn.injectMessage(topicPrefix + "/hostid/", mqttMsg);

        Integer cpuUsage =  Nre.getPlatform().getCpuUsage();
        Integer memUsage =  Nre.getPlatform().getMemoryUsage();
        Integer memTotalUsage =  Nre.getPlatform().getTotalMemory();
        Integer iMemPctUsed = 0 ;
        float memPctUsed;

        mqttMsg = new MqttMessage();
        mqttMsg.setPayload(cpuUsage.toString().getBytes());
        m_MqttSubConn.injectMessage(topicPrefix + "/stationPercentCpuUsed/", mqttMsg);

        if(memTotalUsage>0 && memUsage <= memTotalUsage){
            BFloat a = BFloat.make(memUsage.toString());
            BFloat b = BFloat.make(memTotalUsage.toString());
            memPctUsed = (a.getFloat() / b.getFloat()) * 100;
            iMemPctUsed = BFloat.make(memPctUsed).getInt();
        }

        mqttMsg = new MqttMessage();
        mqttMsg.setPayload(iMemPctUsed.toString().getBytes());
        m_MqttSubConn.injectMessage(topicPrefix + "/stationPercentMemoryUsed/", mqttMsg);

        // Get all th slots of the mqtt driver and output as topic/value pairs into points tree
        Property[] aryProps = this.getFrozenPropertiesArray();
        if (aryProps.length > 0) {
            for (int i = 0; i < aryProps.length; i++) {
                Property prop = aryProps[i];
                String propName = SlotPath.unescape(prop.getName());
                BValue bV = this.get(prop);
                String text = bV.toDataValue().toString();
                mqttMsg = new MqttMessage();
                mqttMsg.setPayload(text.getBytes());
                m_MqttSubConn.injectMessage(topicPrefix + "/" + propName + "/", mqttMsg);
            }
        }
    }
    private void subPoll() {
        BMqttPointDeviceExt points = this.getPoints();

        if(niagIsNetworkAndDriverEnabled()) {
            if(m_MqttSubConn != null && m_MqttSubConn.isOnline()) {
                pingOk();
                // Inject a this driver's own status points is defined in configuration
                if(getDeviceStatusAsPoints()) {
                    injectDeviceStatusIntoQueue();
                }

                debugOut("Polling <SUB>", 1, false);


                debugOut("Subscribed Broker Topic: <" + resolveBFormatToPath(getSubscribeTopicPath()) + ">", 2);

                // Get all queued messages
                Integer brokerQueueSize = 0;
                ArrayList<MqttPayload> listTopics;
                String outputTypeMsg;
                Integer cntInflightMsgsStillToProcess = 0;
                Integer cntInflightMsgs = m_MqttSubConn.getInflightMsgCount();
                if (getDataInputType().getOrdinal() == BMqttInputType.SUBSCRIBE_ALL_CHANGE_OF_VALUES) {
                    listTopics = m_MqttSubConn.dequeueMessages(3000);
                    brokerQueueSize = listTopics.size();
                    outputTypeMsg = "<Receive ALL values>";
                    cntInflightMsgsStillToProcess = cntInflightMsgs - brokerQueueSize;
                    debugOut("All Points: " + brokerQueueSize + " (still in queue " + cntInflightMsgsStillToProcess + ")", 3);
                    debugOut("Output Type:  " + outputTypeMsg, 3);
                }else{
                    listTopics = m_MqttSubConn.dequeueUniqueMessages(3000);
                    brokerQueueSize = listTopics.size();
                    outputTypeMsg = "<Receive ONLY most recent values>";
                    cntInflightMsgsStillToProcess = cntInflightMsgs - brokerQueueSize;
                    debugOut("Changed Points: " + brokerQueueSize + " (still in queue " + cntInflightMsgsStillToProcess + ")" , 3);
                    debugOut("Output Type:  " + outputTypeMsg, 3);
                }

                if (brokerQueueSize > 0) {

                    Boolean forcePointUpdate =  getPointForceUpdate();

                    // Get all out.values under the points folder
                    if (m_SubPubOrdLookUp.buildOrdLookup(getPoints().getNavOrd().toString())) {


                        // ---------------------------------------------------------------------------------------------
                        // Clean each MQTT object first before creating a new list. Caters for duplicates
                        ArrayList<MqttPayload> listObjMqttCooked = new ArrayList();
                        for (int queueIdx = 0; queueIdx < brokerQueueSize; queueIdx++) {
                            MqttPayload objMqttRaw = listTopics.get(queueIdx);
                            String topicRaw = removeNonPrintables(objMqttRaw.topic);    // e.g. '/monitor/manager/os/processes'
                            listObjMqttCooked.add(objMqttRaw);

                            // Get a list of points where the 'Topic' is the same, add back in so can processed as individuals
                            // This allows the User the ability to create a duplicate point with the same 'Topic'
                            // Duplicate 'Topics' are used with JSON blocks to further break down each value
                            ArrayList listTopicDuplicates =  m_SubPubOrdLookUp.getOrdsWithProxyExtTopic(topicRaw);
                            Integer countOrdDuplicates = listTopicDuplicates.size();
                            if(countOrdDuplicates>0){
                                // Add multiple messages with of the same topic
                                for (int duplicateIdx = 0; duplicateIdx < countOrdDuplicates; duplicateIdx++) {
                                    MqttPayload objMqttCooked = new MqttPayload();
                                    objMqttCooked.topic = listTopicDuplicates.get(duplicateIdx).toString();
                                    objMqttCooked.message = objMqttRaw.message;
                                    listObjMqttCooked.add(objMqttCooked);
                                }
                            }
                        }

                        // Find the size again as we may have added back in duplicates
                        brokerQueueSize = listObjMqttCooked.size();

                        // ---------------------------------------------------------------------------------------------
                        // Walk through each MQTT object and find matching point in the Station
                        for (int x = 0; x < brokerQueueSize; x++) {
                            try {
                                MqttPayload payload = listObjMqttCooked.get(x);

                                // Broker Topics may have not leading
                                String topic = formatTopic(payload.topic);
                                String value = removeNonPrintables(payload.message.toString());

                                // Is this topic a SYSTEM command '$SYS'. The '$' is illegal (escape character) so parse out.
                                if (topic.charAt(0) == '$' && topic.charAt(1) == 'S' && topic.charAt(2) == 'Y' && topic.charAt(3) == 'S') {
                                    topic = topic.substring(1);
                                }

                                // Process if topic format OK
                                if (topicFormatIsValid(topic)) {

                                    // This resolves short ORD to long ORD
                                    String stationOrd = m_SubPubOrdLookUp.getStationOrdIfTopicExists(topic);
                                    // NULL is a valid station ORD; it means there is no reference in the station and so
                                    // needs to created as a new point
                                    if (stationOrd != null) { // UPDATE EXISTING ---------------------------------------
                                        BComponent bComp = null;
                                        try {
                                            bComp = BOrd.make(escapeOrd(stationOrd)).resolve().getComponent();
                                        }catch (Exception ex){
                                        }

                                        if(bComp != null) {
                                            // Check the point object type 1st then convert to this
                                            // A string is a special case as it coould be string, json object string, number, or bool
                                            if(bComp.getType() == BMqttStringWritable.TYPE){
                                                // Could be a JSON object so try and determine
                                                if (isJsonObject(value) || isJsonArray(value) && (!isDouble(value) || !isBoolean(value))) {
                                                    // If a JSON array/object and not a numeric, then update as a String
                                                    // This could also be an Enum

                                                    if (forcePointUpdate || m_SubPubOrdLookUp.hasStringValueChanged(topic, value)) {
                                                        updateStringWritable(bComp, value);
                                                    }
                                                } else if (isDouble(value)) {
                                                    // Else must be a numeric value
                                                    if (forcePointUpdate || m_SubPubOrdLookUp.hasNumericValueChanged(topic, value)) {
                                                        updateNumericWritable(bComp, value);
                                                    }
                                                } else if (isBoolean(value)) {
                                                    if (forcePointUpdate || m_SubPubOrdLookUp.hasBooleanValueChanged(topic, value)) {
                                                        updateBooleanWritable(bComp, value);
                                                    }
                                                } else {
                                                    if (forcePointUpdate || m_SubPubOrdLookUp.hasStringValueChanged(topic, value)) {
                                                        updateStringWritable(bComp, value);
                                                    }
                                                }
                                            }else if(bComp.getType() == BMqttNumericWritable.TYPE){
                                                if (forcePointUpdate || m_SubPubOrdLookUp.hasNumericValueChanged(topic, value)) {
                                                    updateNumericWritable(bComp, value);
                                                }
                                            }else if(bComp.getType() == BMqttBooleanWritable.TYPE){
                                                if (forcePointUpdate || m_SubPubOrdLookUp.hasBooleanValueChanged(topic, value)) {
                                                    updateBooleanWritable(bComp, value);
                                                }
                                            }else if(bComp.getType() == BMqttEnumWritable.TYPE){
                                                BMqttEnumWritable ew = (BMqttEnumWritable)bComp;
                                                BFacets bFacet = ew.getFacets();
                                                updateEnumWritable(bComp, value, bFacet);
                                            }
                                        }
                                    } else { // ADD NEW ----------------------------------------------------------------

                                        String[] folderNames = getFolderNamesFromTopicPath(topic);

                                        if (folderNames.length > 0 && getSubscribeAutoDiscover().getOrdinal() == BMqttAutoDiscover.YES_ADD_NEW_DISCOVERED_TOPICS) {
                                            String mainFolder = folderNames[0];
                                            Integer topicLeafLen = folderNames.length;
                                            Integer topicLastLeafIndex = topicLeafLen - 1;

                                            // Check this could be a single leaf. Just add extension
                                            if (topicLeafLen == 1) {
                                                // Boolean can only be 'true' or 'false', else seen as numeric (1 or 0)
                                                if (isJsonObject(value) || isJsonArray(value) && (!isDouble(value) || !isBoolean(value))) {
                                                    addStringWritable(points, mainFolder, value, topic, false);
                                                } else if (isDouble(value)) {
                                                    addNumericWritable(points, mainFolder, null, value,topic, makeDefaultNumericFacets(value), false);
                                                } else if (isBoolean(value)) {
                                                    addBooleanWritable(points, mainFolder, null, value, topic,  false);
                                                } else {
                                                    addStringWritable(points, mainFolder, value, topic, false);
                                                }

                                            } else {
                                                BMqttPointFolder parentFolder = createParentFolder(points, mainFolder);

                                                for (int leafIdx = 1; leafIdx < topicLeafLen; leafIdx++) {
                                                    String childFolderName = folderNames[leafIdx];

                                                    // Keeping adding the child folders till the last leaf
                                                    if (leafIdx != topicLastLeafIndex) {
                                                        parentFolder = createSubFolder(parentFolder, childFolderName);

                                                    } else { // Is the last leaf, so add the point extension
                                                        // Boolean can only be 'true' or 'false', else seen as numeric (1 or 0)
                                                        if (isJsonObject(value) || isJsonArray(value) && (!isDouble(value) || !isBoolean(value))) {
                                                            // Is either a json object or json array, and not a double or boolean
                                                            // This could also be an Enum
                                                            addStringWritable(parentFolder, childFolderName, value, topic, false);
                                                        } else if (isDouble(value)) {
                                                            addNumericWritable(parentFolder, childFolderName, null, value, topic, makeDefaultNumericFacets(value), false);
                                                        } else if (isBoolean(value)) {
                                                            addBooleanWritable(parentFolder, childFolderName, null, value, topic, false);
                                                        } else {
                                                            addStringWritable(parentFolder, childFolderName, value, topic, false);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    debugOut("Skipping topic as invalid format: " + topic, 5, false);
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                }
            setDriverStatus("Polled OK [" + m_OutputDateFormat.format(new Date()) + "]");
            saPolledOk("Polled OK", 1);
            debugOut("", 1, false);
            }else{
                // Attempt to restart the connection
                debugOut("Broker is Offline - restarting connection..." , 1, true);
                debugOut("" , 1, true);
                pingFail("MQTT broker offline");
                mqttDisconnect();
                showTime();
            }
        }else{
            // Make sure the threads are shut down correctly
            mqttDisconnect();
        }
    }
    private boolean addBooleanWritable(BObject parentFolder, String childName, String childDisplayName, String value, String mqttTopic,  boolean isPoints){
        BMqttBooleanWritable bw = new BMqttBooleanWritable();
        BMqttProxyExt pExt = new BMqttProxyExt();
        pExt.setTopic(mqttTopic);
        bw.setProxyExt(pExt);

        try {
            bw.setFacets(BFacets.makeBoolean());
        } catch (Exception e) {
        }

        try {
            if(isPoints) {
                ((BMqttPointDeviceExt)parentFolder).add(SlotPath.escape(childName), bw);
            }else{
                ((BMqttPointFolder)parentFolder).add(SlotPath.escape(childName), bw);
            }

            if(childDisplayName != null || "".equals(childDisplayName) == false){
                setComponentDisplayName(bw, childDisplayName);
            }

            if(pExt.getEnabled()) {
                int pLevel = getPointPriorityLevel().getOrdinal();
                if (pLevel == BMqttPriorityType.DISABLED) {
                    // Send value directly to Out
                    pExt.readOk(new BStatusBoolean(Boolean.parseBoolean(value)));
                    pExt.writeOk(new BStatusBoolean(Boolean.parseBoolean(value)));
                } else {
                    // Use priority queue, dont publish to mqtt on create
                    bw.setMqttValue(value, pLevel);
                }
            }


        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean addNumericWritable(BObject parentFolder, String childName, String childDisplayName, String value, String mqttTopic, BFacets facets, boolean isPoints){

        BMqttNumericWritable nw = new BMqttNumericWritable();
        BMqttProxyExt pExt = new BMqttProxyExt();
        pExt.setTopic(mqttTopic);
        nw.setProxyExt(pExt);

        try {nw.setFacets(facets);} catch (Exception e) {}
        try {
            if(isPoints) {
                ((BMqttPointDeviceExt)parentFolder).add(SlotPath.escape(childName), nw);
            }else{
                ((BMqttPointFolder)parentFolder).add(SlotPath.escape(childName), nw);
            }

            if(childDisplayName != null || "".equals(childDisplayName) == false){
                setComponentDisplayName(nw, childDisplayName);
            }

            if(pExt.getEnabled()){
                int pLevel = getPointPriorityLevel().getOrdinal();
                if(pLevel ==  BMqttPriorityType.DISABLED){
                    // Send value directly to Out
                    pExt.readOk(new BStatusNumeric(Double.parseDouble(value)));
                    pExt.writeOk(new BStatusNumeric(Double.parseDouble(value)));
                }else{
                    // Use priority queue
                    nw.setMqttValue(Double.parseDouble(value), pLevel);
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    private boolean updateStringWritable(BComponent bComp, String value){

        Boolean isStdJsonPayload = false;
        BMqttStringWritable sw = (BMqttStringWritable) bComp;
        BMqttProxyExt pExt;
        try {
            pExt = (BMqttProxyExt) sw.getProxyExt();
        }catch (Exception e){
            e.printStackTrace();
            pExt = null;
        }

        if(isJsonObject(value)){
            JSONObject jSchema = new JSONObject(value);
            if(jSchema.has("schema")){
                if(jSchema.get("schema").equals(JsonSchema.MQTTPS.toString()) || jSchema.get("schema").equals(JsonSchema.MQTTPS2.toString())){
                    if(jSchema.has("data")){

                        JSONObject jData;
                        // If compression exist then decompress
                        if(jSchema.has("compression")){
                            String sDd = jSchema.getString("data");
                            byte[] bMsgC = Base64.getDecoder().decode(sDd);
                            String bMsg = decompress(bMsgC);
                            jData = new JSONObject( bMsg);
                        }else{
                            jData = new JSONObject(jSchema.getString("data"));
                        }

                        if(jData.has("props")){

                            JSONArray jAProps = jData.getJSONArray("props");
                            Map<String, SlotProperties> properties = new HashMap();

                            // Parse out the properties json array into a hashmap using the name as the key
                            for (int i = 0; i < jAProps.length(); i++) {
                                JSONObject jProp = jAProps.getJSONObject(i);
                                if(jProp.has("n") && jProp.has("t") && jProp.has("v")){
                                    SlotProperties sProp = new SlotProperties();
                                    sProp.name = jProp.getString("n");
                                    sProp.type = jProp.getString("t");
                                    sProp.value = jProp.getString("v");
                                    properties.putIfAbsent(sProp.name, sProp);
                                }
                            }
                            String oVal = "";
                            String oFacet = "";
                            BFacets bFacet = BFacets.make(BFacets.NULL);
                            String oType = "";
                            SlotProperties sPropOut;

                            if(properties.size()>0){
                                if(properties.containsKey("facets")){
                                    sPropOut = properties.get("facets");
                                    oFacet = sPropOut.value;
                                    try {
                                        bFacet =  BFacets.make(oFacet);
                                    }catch (Exception e){}
                                }
                                if(properties.containsKey("outValue")){
                                    sPropOut = properties.get("outValue");
                                    oVal = sPropOut.value;
                                }
                                if(properties.containsKey("type")){
                                    sPropOut = properties.get("type");
                                    oType = sPropOut.value;
                                }

                                if(oType.contains("Numeric")){
                                    updateNumericWritable(bComp, oVal, bFacet);
                                }else if(oType.contains("Boolean")){
                                    updateBooleanWritable(bComp, oVal, bFacet);
                                }else if(oType.contains("Enum")){
                                    updateEnumWritable(bComp, oVal, bFacet);
                                }else{
                                    sw.setFacets(bFacet);
                                    if(pExt.getEnabled()) {
                                        int pLevel = getPointPriorityLevel().getOrdinal();
                                        if (pLevel == BMqttPriorityType.DISABLED) {
                                            // Send value directly to Out
                                            pExt.readOk(new BStatusString(oVal).getStatusValue());
                                            pExt.writeOk(new BStatusString(oVal).getStatusValue());
                                        } else {
                                            // Use priority queue
                                            sw.setMqttValue(value, pLevel);
                                        }
                                    }
                                }
                            }
                        }

                        if (jData.has("tags")) {
                            JSONArray jATags = jData.getJSONArray("tags");
                            Map<String, SlotProperties> tags = new HashMap();
                            // Parse out the tags json array into a hashmap using the name as the key
                            for (int i = 0; i < jATags.length(); i++) {
                                JSONObject jProp = jATags.getJSONObject(i);
                                if (jProp.has("n") && jProp.has("t") && jProp.has("v")) {
                                    SlotProperties sTags = new SlotProperties();
                                    sTags.name = jProp.getString("n");
                                    sTags.type = jProp.getString("t");
                                    sTags.value = jProp.getString("v");
                                    tags.putIfAbsent(sTags.name, sTags);
                                }
                            }
                        }
                    }else{
                        return false;
                    }
                }else{
                    isStdJsonPayload = true;
                }
            }else{
                isStdJsonPayload = true;
            }

            // With a standard JSON object payload, check user may have entered a key to parse out JSON value
            // If a JSON payload (not this schema), check for filter
            if(isStdJsonPayload) {
                String jFilterStr = pExt.getJsonFilter();
                pExt.setJsonFault("");
                pExt.setJson(new JSONObject(value).toString(4));    // Update the JSON
                if("".equals(jFilterStr)){    // If no user entered json key, add the JSON block as a string
                    if(pExt.getEnabled()) {
                        int pLevel = getPointPriorityLevel().getOrdinal();
                        if (pLevel == BMqttPriorityType.DISABLED) {
                            // Send value directly to Out
                            pExt.readOk(new BStatusString(new JSONObject(value).toString()).getStatusValue());
                            pExt.writeOk(new BStatusString(new JSONObject(value).toString()).getStatusValue());
                        } else {
                            // Use priority queue
                            sw.setMqttValue(value, pLevel);
                        }
                    }

                }else{  // If user entered json key, attempt to find and display value
                    JSONObject jO =  new JSONObject(value);
                    try {
                        DocumentContext jsonContext = JsonPath.parse(jO.toString());
                        String keyValue = "";
                        Object keyValueRead =  jsonContext.read(jFilterStr);

                        if(keyValueRead.getClass() == java.lang.String.class) {
                            keyValue = keyValueRead.toString();

                        }else if(keyValueRead.getClass() == java.util.LinkedHashMap.class){
                            Map lhm = new LinkedHashMap((Map) keyValueRead);
                            keyValue = String.valueOf(lhm.toString());

                        }else if(keyValueRead.getClass() == net.minidev.json.JSONArray.class){
                            String jStr = ((net.minidev.json.JSONArray)keyValueRead).toJSONString();
                            JSONArray jAval = new JSONArray(jStr);
                            if(jAval.length() > 0){
                                if(jAval.length() == 1){
                                    keyValue = jAval.get(0).toString();
                                }else{
                                    keyValue = jAval.toString();
                                }
                            }
                        }else{
                            // Just try and get the string of the object. Could be Int, Float, Double....
                            keyValue = String.valueOf(keyValueRead);
                        }

                        if ("".equals(keyValue) == false) {
                            if(pExt.getEnabled()) {
                                int pLevel = getPointPriorityLevel().getOrdinal();
                                if (pLevel == BMqttPriorityType.DISABLED) {
                                    // Send value directly to Out
                                    pExt.readOk(new BStatusString(keyValue).getStatusValue());
                                    pExt.writeOk(new BStatusString(keyValue).getStatusValue());
                                } else {
                                    // Use priority queue
                                    sw.setMqttValue(value, pLevel);
                                }
                            }

                        } else {
                            pExt.setJsonFault("Cannot evaluate JSON path expression (CLASS=" + keyValueRead.getClass() + ")");
                        }
                    }catch (Exception e){
                        pExt.setJsonFault(e.toString());
                    }
                }
            }

        }else if(isJsonArray(value)) {
            String jFilterStr = pExt.getJsonFilter();
            pExt.setJsonFault("");
            pExt.setJson(new JSONArray(value).toString(4));
            if("".equals(jFilterStr)){    // If no user entered json key, add the JSON block as a string
                if(pExt.getEnabled()) {
                    int pLevel = getPointPriorityLevel().getOrdinal();
                    if (pLevel == BMqttPriorityType.DISABLED) {
                        // Send value directly to Out
                        pExt.readOk(new BStatusString(new JSONArray(value).toString()).getStatusValue());
                        pExt.writeOk(new BStatusString(new JSONArray(value).toString()).getStatusValue());
                    } else {
                        // Use priority queue
                        sw.setMqttValue(value, pLevel);
                    }
                }

            }else {
                JSONArray jO = new JSONArray(value);
                try {
                    DocumentContext jsonContext = JsonPath.parse(jO.toString());
                    String keyValue = "";
                    Object keyValueRead =  jsonContext.read(jFilterStr);

                    if(keyValueRead.getClass() == java.lang.String.class) {
                        keyValue = keyValueRead.toString();

                    }else if(keyValueRead.getClass() == java.util.LinkedHashMap.class){
                        Map lhm = new LinkedHashMap((Map) keyValueRead);
                        keyValue = String.valueOf(lhm.toString());
                    }else if(keyValueRead.getClass() == net.minidev.json.JSONArray.class){
                        String jStr = ((net.minidev.json.JSONArray)keyValueRead).toJSONString();
                        JSONArray jAval = new JSONArray(jStr);
                        if(jAval.length() > 0){
                            if(jAval.length() == 1){
                                keyValue = jAval.get(0).toString();
                            }else{
                                keyValue = jAval.toString();
                            }
                        }
                    }else{
                        // Just try and get the string of the object. Could be Int, Float, Double....
                        keyValue = String.valueOf(keyValueRead);
                    }

                    if ("".equals(keyValue) == false) {
                        if(pExt.getEnabled()) {
                            int pLevel = getPointPriorityLevel().getOrdinal();
                            if (pLevel == BMqttPriorityType.DISABLED) {
                                // Send value directly to Out
                                pExt.readOk(new BStatusString(keyValue).getStatusValue());
                                pExt.writeOk(new BStatusString(keyValue).getStatusValue());
                            } else {
                                // Use priority queue
                                sw.setMqttValue(value, pLevel);
                            }
                        }


                    } else {
                        pExt.setJsonFault("Cannot evaluate JSON path expression (CLASS=" + keyValueRead.getClass() + ")");
                    }
                }catch (Exception e){
                    pExt.setJsonFault(e.toString());
                }
            }
        }else{
            if(pExt.getEnabled()) {
                int pLevel = getPointPriorityLevel().getOrdinal();
                if (pLevel == BMqttPriorityType.DISABLED) {
                    // Send value directly to Out
                    pExt.readOk(new BStatusString(value).getStatusValue());
                    pExt.writeOk(new BStatusString(value).getStatusValue());
                } else {
                    // Use priority queue
                    sw.setMqttValue(value, pLevel);
                }
            }

        }
        return true;
    }
    private void updateNumericWritable(BComponent bComp, String value) {
        try {
            BMqttNumericWritable nw = (BMqttNumericWritable) bComp;
            BMqttProxyExt pExt = (BMqttProxyExt) nw.getProxyExt();
            if (pExt.getEnabled()) {
                int pLevel = getPointPriorityLevel().getOrdinal();
                if(pLevel ==  BMqttPriorityType.DISABLED){
                    // Send value directly to Out
                    pExt.readOk(new BStatusNumeric(Double.parseDouble(value)));
                    pExt.writeOk(new BStatusNumeric(Double.parseDouble(value)));
                }else{
                    // Use priority queue
                    nw.setMqttValue(Double.parseDouble(value), pLevel);
                }
            }
        } catch (Exception e) {
        }
    }
    private void updateNumericWritable(BComponent bComp, String value, BFacets bFacet){
        try {
            BMqttNumericWritable nw = (BMqttNumericWritable) bComp;
            nw.setFacets(bFacet);
            BMqttProxyExt pExt = (BMqttProxyExt) nw.getProxyExt();
            if(pExt.getEnabled()){
                int pLevel = getPointPriorityLevel().getOrdinal();
                if(pLevel ==  BMqttPriorityType.DISABLED){
                    // Send value directly to Out
                    pExt.readOk(new BStatusNumeric(Double.parseDouble(value)));
                    pExt.writeOk(new BStatusNumeric(Double.parseDouble(value)));
                }else{
                    // Use priority queue
                    nw.setMqttValue(Double.parseDouble(value), pLevel);
                }
            }
        }catch (Exception e){
        }
    }
    private void updateBooleanWritable(BComponent bComp, String value){
        try {
            BMqttBooleanWritable bw = (BMqttBooleanWritable) bComp;
            BMqttProxyExt pExt = (BMqttProxyExt) bw.getProxyExt();
            if(pExt.getEnabled()) {
                int pLevel = getPointPriorityLevel().getOrdinal();
                if (pLevel == BMqttPriorityType.DISABLED) {
                    // Send value directly to Out
                    pExt.readOk(new BStatusBoolean(Boolean.parseBoolean(value)));
                    pExt.writeOk(new BStatusBoolean(Boolean.parseBoolean(value)));
                } else {
                    // Use priority queue
                    bw.setMqttValue(value, pLevel);
                }
            }
        }catch (Exception e){
        }
    }
    private void updateBooleanWritable(BComponent bComp, String value, BFacets bFacet){
        try {
            BMqttBooleanWritable bw = (BMqttBooleanWritable) bComp;
            bw.setFacets(bFacet);
            BMqttProxyExt pExt = (BMqttProxyExt) bw.getProxyExt();
            if(pExt.getEnabled()) {
                int pLevel = getPointPriorityLevel().getOrdinal();
                if (pLevel == BMqttPriorityType.DISABLED) {
                    // Send value directly to Out
                    pExt.readOk(new BStatusBoolean(Boolean.parseBoolean(value)));
                    pExt.writeOk(new BStatusBoolean(Boolean.parseBoolean(value)));
                } else {
                    // Use priority queue
                    bw.setMqttValue(value, pLevel);
                }
            }
        }catch (Exception e){
        }
    }
    private void setComponentDisplayName(BComponent thisComp, String newDisplayName){
        try {
            BComponent myParent = thisComp.getParent().getParentComponent();
            myParent.setDisplayName(thisComp.getPropertyInParent(), BFormat.make(newDisplayName), null);
        }catch (Exception e){}
    }
    private boolean addEnumWritable(BObject parentFolder, String childName, String childDisplayName, String value, String mqttTopic, BFacets bFacet, boolean isPoints){

        BMqttEnumWritable ew = new BMqttEnumWritable();
        BMqttProxyExt pExt = new BMqttProxyExt();
        pExt.setTopic(mqttTopic);
        ew.setProxyExt(pExt);

        try {ew.setFacets(bFacet);
        } catch (Exception e) {}

        try {
            if(isPoints) {
                ((BMqttPointDeviceExt)parentFolder).add(SlotPath.escape(childName), ew);
            }else{
                ((BMqttPointFolder)parentFolder).add(SlotPath.escape(childName), ew);
            }
            if(childDisplayName != null || "".equals(childDisplayName) == false){
                setComponentDisplayName(ew, childDisplayName);
            }

            if(pExt.getEnabled()) {
                if ("".equals(value) == false) {
                    BEnum enumValue = getEnumValue(value, bFacet);
                    if(enumValue != null){
                        int pLevel = getPointPriorityLevel().getOrdinal();
                        if (pLevel == BMqttPriorityType.DISABLED) {
                            pExt.readOk(new BStatusEnum(enumValue).getStatusValue());
                            pExt.writeOk(new BStatusEnum(enumValue).getStatusValue());
                        } else {
                            // Use priority queue
                            BEnumRange eRange = getEnumRange(bFacet);
                            ew.setMqttValue(enumValue,eRange, pLevel);
                        }
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    private BEnum getEnumValue(String value, BFacets bFacet) throws Exception{
        value = SlotPath.escape(value);
        String facetStr = bFacet.toString();
        String enumStr = facetStr.substring(facetStr.indexOf('{') + 1, facetStr.indexOf('}'));
        String[] pairsList = enumStr.split(",");
        ArrayList<Integer> intList = new ArrayList<Integer>();
        ArrayList<String> strList = new ArrayList<String>();
        for (String pair : pairsList) {
            String[] pairList = pair.split("=");
            Integer index = new Integer(Integer.parseInt(pairList[1]));
            String indexV = pairList[0];
            intList.add(index);
            strList.add(indexV);
        }
        int[] enumInts = convertIntegers(intList);
        String[] enumStrs = convertStrings(strList);
        BEnumRange enumRangeX = BEnumRange.make(enumInts, enumStrs);
        return enumRangeX.get(value);
    }
    private BEnumRange getEnumRange( BFacets bFacet) throws Exception{
        String facetStr = bFacet.toString();
        String enumStr = facetStr.substring(facetStr.indexOf('{') + 1, facetStr.indexOf('}'));
        String[] pairsList = enumStr.split(",");
        ArrayList<Integer> intList = new ArrayList<Integer>();
        ArrayList<String> strList = new ArrayList<String>();
        for (String pair : pairsList) {
            String[] pairList = pair.split("=");
            Integer index = new Integer(Integer.parseInt(pairList[1]));
            String indexV = pairList[0];
            intList.add(index);
            strList.add(indexV);
        }
        int[] enumInts = convertIntegers(intList);
        String[] enumStrs = convertStrings(strList);
        BEnumRange enumRangeX = BEnumRange.make(enumInts, enumStrs);
        return enumRangeX;
    }
    private void updateEnumWritable(BComponent bComp, String value, BFacets bFacet){
        try {
            BMqttEnumWritable ew = (BMqttEnumWritable) bComp;
            ew.setFacets(bFacet);
            BMqttProxyExt pExt = (BMqttProxyExt) ew.getProxyExt();
            if(pExt.getEnabled()) {
                if ("".equals(value) == false) {
                    BEnum enumValue = getEnumValue(value, bFacet);
                    if(enumValue != null){
                        int pLevel = getPointPriorityLevel().getOrdinal();
                        if (pLevel == BMqttPriorityType.DISABLED) {
                            pExt.readOk(new BStatusEnum(enumValue).getStatusValue());
                            pExt.writeOk(new BStatusEnum(enumValue).getStatusValue());
                        } else {
                            // Use priority queue
                            BEnumRange eRange = getEnumRange(bFacet);
                            ew.setMqttValue(enumValue,eRange, pLevel);
                        }
                    }
                }
            }
        }catch (Exception e){}
    }
    private boolean addStringWritable(BObject parentFolder, String childFolderName, String value, String mqttTopic, boolean isPoints) {
        if(isJsonObject(value)){
            JSONObject jSchema = new JSONObject(value);
            if(jSchema.has("schema")){
                if( jSchema.get("schema").equals(JsonSchema.MQTTPS.toString()) || jSchema.get("schema").equals(JsonSchema.MQTTPS2.toString()) ){
                    if(jSchema.has("data")){

                        JSONObject jData;
                        // If compression exist then decompress
                        if(jSchema.has("compression")){
                            String sDd = jSchema.getString("data");
                            byte[] bMsgC = Base64.getDecoder().decode(sDd);
                            String bMsg = decompress(bMsgC);
                            jData = new JSONObject( bMsg);
                        }else{
                            jData = new JSONObject(jSchema.getString("data"));
                        }

                        //JSONObject jData = new JSONObject(jSchema.getString("data"));

                        String displayName = null;
                        if (jData.has("tags")) {
                            JSONArray jATags = jData.getJSONArray("tags");
                            Map<String, SlotProperties> tags = new HashMap();
                            // Parse out the tags json array into a hashmap using the name as the key
                            for (int i = 0; i < jATags.length(); i++) {
                                JSONObject jProp = jATags.getJSONObject(i);
                                if (jProp.has("n") && jProp.has("t") && jProp.has("v")) {
                                    SlotProperties sTags = new SlotProperties();
                                    sTags.name = jProp.getString("n");
                                    sTags.type = jProp.getString("t");
                                    sTags.value = jProp.getString("v");
                                    tags.putIfAbsent(sTags.name, sTags);
                                }
                            }
                            if(tags.size()>0) {
                                if (tags.containsKey("n:displayName")) {
                                    SlotProperties tDisplayName = tags.get("n:displayName");
                                    displayName = tDisplayName.value;
                                }
                            }
                        }


                        if(jData.has("props")){
                            JSONArray jAProps = jData.getJSONArray("props");
                            Map<String, SlotProperties> properties = new HashMap();

                            // Parse out the properties json array into a hashmap using the name as the key
                            for (int i = 0; i < jAProps.length(); i++) {
                                JSONObject jProp = jAProps.getJSONObject(i);
                                if(jProp.has("n") && jProp.has("t") && jProp.has("v")){
                                    SlotProperties sProp = new SlotProperties();
                                    sProp.name = jProp.getString("n");
                                    sProp.type = jProp.getString("t");
                                    sProp.value = jProp.getString("v");
                                    properties.putIfAbsent(sProp.name, sProp);
                                }
                            }

                            String oVal = "";
                            String oFacet = "";
                            BFacets bFacet = BFacets.make(BFacets.NULL);
                            String oType = "";

                            if(properties.size()>0){
                                if(properties.containsKey("facets")){
                                    SlotProperties sPropOut = properties.get("facets");
                                    oFacet = sPropOut.value;
                                    try {
                                        bFacet =  BFacets.make(oFacet);
                                     }catch (Exception e){}
                                }
                                if(properties.containsKey("outValue")){
                                    SlotProperties sPropOut = properties.get("outValue");
                                    oVal = sPropOut.value;
                                }
                                if(properties.containsKey("type")){
                                    SlotProperties sPropOut = properties.get("type");
                                    oType = sPropOut.value;
                                }

                                // e.g. oType = "v": "mqtt:MqttBooleanWritable"
                                // Search for marker in string, however could use type
                                if(oType.contains("String")){
                                    return addStringWritableV(parentFolder, childFolderName, displayName, oVal, mqttTopic, oFacet, isPoints);
                                }else if(oType.contains("Numeric")){
                                    return addNumericWritable(parentFolder, childFolderName, displayName, oVal, mqttTopic, bFacet, isPoints);
                                }else if(oType.contains("Boolean")){
                                    return addBooleanWritable(parentFolder, childFolderName, displayName, oVal, mqttTopic, isPoints);
                                }else if(oType.contains("Enum")){
                                    return addEnumWritable(parentFolder, childFolderName, displayName, oVal, mqttTopic, bFacet, isPoints);
                                }else{
                                    return addStringWritableV(parentFolder, childFolderName, displayName, oVal, mqttTopic, oFacet, isPoints);
                                }
                            }
                        }
                    }
                }
            }
        }
        return addStringWritableV(parentFolder, childFolderName, null,  value, mqttTopic, null, isPoints);
    }
    private boolean addStringWritableV(BObject parentFolder, String childName, String childDisplayName, String value, String mqttTopic, String facetStr, boolean isPoints){
        BMqttStringWritable sw = new BMqttStringWritable();
        BMqttProxyExt pExt = new BMqttProxyExt();

        if (isJsonObject(value)) {
            pExt.showJsonProperty();
            pExt.setJson(new JSONObject(value).toString(4));
        } else if (isJsonArray(value)) {
            pExt.showJsonProperty();
            pExt.setJson(new JSONArray(value).toString(4));
        }

        pExt.setTopic(mqttTopic);
        sw.setProxyExt(pExt);
        if(facetStr != null) {
            try {
                BFacets bf =  BFacets.make(facetStr);
                sw.setFacets(bf);
            } catch (Exception e) {}
        }

        try {
            if (isPoints) {
                ((BMqttPointDeviceExt) parentFolder).add(SlotPath.escape(childName), sw);
            } else {
                ((BMqttPointFolder) parentFolder).add(SlotPath.escape(childName), sw);
            }

            if(childDisplayName != null || "".equals(childDisplayName) == false){
                setComponentDisplayName(sw, childDisplayName);
            }

            if(pExt.getEnabled()) {
                int pLevel = getPointPriorityLevel().getOrdinal();
                if (pLevel == BMqttPriorityType.DISABLED) {
                    // Send value directly to Out
                    pExt.readOk(new BStatusString(value).getStatusValue());
                    pExt.writeOk(new BStatusString(value).getStatusValue());
                } else {
                    // Use priority queue
                    sw.setMqttValue(value, pLevel);
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // NAVIGATION TREE
    // -----------------------------------------------------------------------------------------------------------------
    public static byte[] compress(String text) {
        //http://www.txtwizard.net/compression
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            OutputStream out = new DeflaterOutputStream(baos);
            out.write(text.getBytes(StandardCharsets.UTF_8));
            out.close();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return baos.toByteArray();
    }
    public static String decompress(byte[] bytes) {
        InputStream in = new InflaterInputStream(new ByteArrayInputStream(bytes));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[8192];
            int len;
            while((len = in .read(buffer)) > 0)
                baos.write(buffer, 0, len);
            return baos.toString(String.valueOf(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
    private BMqttPointFolder createParentFolder(BMqttPointDeviceExt points, String folderName) {
        try {
            getFolderIfExists(points.getChildComponents(), folderName);
        } catch (Exception e) {
            return null;
        }
        BMqttPointFolder folder = getFolderIfExists(points.getChildComponents(), folderName);
        try {
            if(folder == null) {
                try {
                    folderName = SlotPath.unescape(folderName);
                    points.add(SlotPath.escape(folderName), new BMqttPointFolder());
                } catch (Exception e) {}
                return getFolderIfExists(points.getChildComponents(), folderName);
            }
        } catch (Exception e) {}
        return folder;
    }
    private BMqttPointFolder createSubFolder(BMqttPointFolder parentFolder, String childFolderName) {

        try {
            String parentPath = parentFolder.getNavOrd().toString();
            parentPath = parentPath + "/" + childFolderName;
            parentPath = SlotPath.unescape(parentPath); // Clean it up
            BComponent bComp = null;
            try {
                bComp = BOrd.make(escapeOrd(parentPath)).resolve().getComponent();
            }catch (Exception e0) {
                // means there is no folder - so create it
            }
            if(bComp != null) {
                // Did find a folder so return with folder component
                return (BMqttPointFolder) bComp;
            }else{
                try {
                    // Add new folder to parent
                    parentFolder.add(SlotPath.escape(childFolderName), new BMqttPointFolder());

                    // Check that it exist and return
                    bComp = BOrd.make(escapeOrd(parentPath)).resolve().getComponent();
                    if(bComp != null) {
                        // All good
                        return (BMqttPointFolder) bComp;
                    }

                } catch (Exception e1) {
                    // Means that the creation did not work
                }
            }
        } catch (Exception e2) {
            // Means something else went wrong
        }
        return null;
    }

    private BMqttPointFolder getFolderIfExists(BComponent[] folders, String name) {
        try {
            name = SlotPath.escape(name);
            Integer fLen = folders.length;
            for(int i = 0; i < fLen; i++) {
                if(name.equalsIgnoreCase(folders[i].getName())) {
                    return (BMqttPointFolder) folders[i];
                }
            }
        } catch (Exception e) {}
        return null;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // MQTT
    // -----------------------------------------------------------------------------------------------------------------
    // Create a connection with MQTT broker
    private void mqttConnect() {
        if(niagIsNetworkAndDriverEnabled()) {

            debugOut("Configuring MQTT client", 1, false);

            Integer connectionType = getBrokerConnectionType().getOrdinal();

            Boolean areMqttCredentialsOK = true;
            String faultCause = "";

            String brokerAddress        = getUrlDomainName(getBrokerAddress());
            Integer brokerPort          = getBrokerPort();
            String mqttUserName         = getBrokerUsername();
            Boolean brokerUseTLS        = getBrokerUsingTls();
            String brokerPassword       = AccessController.doPrivileged((PrivilegedAction<String>) ()-> getBrokerPassword().getValue());
            String subscribeTopicPath   = resolveBFormatToPath(getSubscribeTopicPath());
            String ClientId             = getClientId();

            if(brokerAddress.isEmpty() && connectionType == BMqttConnectionType.USING_CREDENTIALS) {
                faultCause = "Address not provided!";
                areMqttCredentialsOK = false;
            }
            if(brokerPort < 0 && brokerPort > 65535) {
                faultCause = "Port number invalid! (0-65535)";
                areMqttCredentialsOK = false;
            }
            if(mqttUserName.isEmpty() && connectionType == BMqttConnectionType.USING_CREDENTIALS) {
                faultCause = "Username not provided!";
                areMqttCredentialsOK = false;
            }
            if(brokerPassword.isEmpty() && connectionType == BMqttConnectionType.USING_CREDENTIALS) {
                faultCause = "Password not provided!";
                areMqttCredentialsOK = false;
            }

            if(areMqttCredentialsOK) {
                mqttDisconnect();
                Integer bufferSize = getSubscribeBufferSize();
                if(brokerUseTLS) {
                    debugOut("Connecting to Broker (TLS): <" + brokerAddress + "><" + bufferSize + " bytes buffer>", 2, false);
                }else{
                    debugOut("Connecting to Broker: <" + brokerAddress + "><" + bufferSize + " bytes buffer>", 2, false);
                }
                if(connectionType == BMqttConnectionType.USING_CREDENTIALS) {

                     m_MqttSubConn = new MqttSubscribe(brokerAddress + ":" + brokerPort, mqttUserName, brokerPassword, bufferSize, brokerUseTLS, ClientId);

                    // Create in publish instance but dont connect. Connect only when the data is sent
                    m_MqttPubConn = new MqttPublish(brokerAddress + ":" + brokerPort, mqttUserName, brokerPassword, brokerUseTLS, ClientId);
                    debugOut("Using credentials for user: " + mqttUserName, 2, false);

                } else if(connectionType == BMqttConnectionType.ANONYMOUS) {

                    m_MqttSubConn = new MqttSubscribe(brokerAddress + ":" + brokerPort, bufferSize, brokerUseTLS, ClientId);
                    // Create in publish instance but dont connect. Connect only when the data is sent
                    m_MqttPubConn = new MqttPublish(brokerAddress + ":" + brokerPort, brokerUseTLS, ClientId);

                    debugOut("Not using credentials, logging in as Anonymous user", 2, false);
                }

                if(m_MqttSubConn.connect(subscribeTopicPath)) {
                    debugOut("Connected OK", 3, false);
                    debugOut("Using Topic Source: <" + subscribeTopicPath + ">", 4, false);
                    debugOut("", 1, false);
                    setDriverStatus("Connected OK");

                    this.postAsync(m_MqttSubConn);

                }
            } else {
                setEnabled(false);
                debugOut(faultCause, 2, false);
                setDriverStatus(faultCause);
            }
        }
    }
    public void mqttPublishMessage(String topic, String value) {

        // Called mostly from BMqttProxyExt
        // Assume if the subscriber is online then publisher will be the same as has same credentials etc
        if(getEnabled()) {
            if(this.getNetwork().getEnabled()) { // Cant send MQTT messages if disabled, they rely on Network worker thread pool
                if(m_MqttSubConn != null && m_MqttSubConn.isOnline()) {
                    Boolean doPublishMessage = false;

                    // If online then send message else attempt to connect first
                    if(m_MqttPubConn.isOnline()){
                        doPublishMessage = true;
                    }else {
                        if (m_MqttPubConn.connect()) {
                            doPublishMessage = true;
                        } else {
                            debugOut("Cannot publish data as not connected to broker", 2, false);
                        }
                    }
                    if(doPublishMessage){
                        m_MqttPubConn.queueMessage(topic, value);
                        this.postAsync(m_MqttPubConn);
                    }
                }
            }
        }
    }
    private void mqttDisconnect() {
        if(m_MqttSubConn != null) {
            m_MqttSubConn.disconnect();
            m_MqttSubConn = null;
        }
    }
    // -----------------------------------------------------------------------------------------------------------------
    // HELPERS
    // -----------------------------------------------------------------------------------------------------------------
    private String formatTopic(String topic){
        // TOPIC FORMAT = '/dddd/ddddd/ddd/dddddd/d'
        String topicF = removeNonPrintables(topic);

        // 16/12/2020
        // v1.34.1433
        // ISSUE    - Adding the / prefix to the topic means that publishing message always get sent with /
        //          - If the subscribed point does is not prefixed with a / then this causes a problem on publish
        // FIX      - If we remove this / here then the query logic cannot match topics to ords and all fails
        //          - This can be changed but requires solid testing
//        topicF = "/" + topicF;                                        // Always add a prefix

        topicF = topicF.replaceAll("/+", "/");      // Remove any suspect double slashes
        topicF = topicF.replaceAll("\\/$", "");     // Remove the last trailing slash if exists
        return topicF;
    }
    private int[] convertIntegers(List<Integer> integers){
        int[] ret = new int[integers.size()];
        for (int i=0; i < ret.length; i++){
            ret[i] = integers.get(i).intValue();
        }
        return ret;
    }
    private String[] convertStrings(List<String> strings){
        String[] ret = new String[strings.size()];
        for (int i=0; i < ret.length; i++){
            ret[i] = strings.get(i);
        }
        return ret;
    }
    public String getUrlDomainName(String url) {
        String domainName = url;

        int index = domainName.indexOf("://");
        if(index != -1) {
            // keep everything after the "://"
            domainName = domainName.substring(index + 3);
        }

        index = domainName.indexOf('/');
        if(index != -1) {
            // keep everything before the '/'
            domainName = domainName.substring(0, index);
        }
        domainName = domainName.replaceFirst("^www.*?\\.", "");
        if(domainName == null) {
            return url;
        } else {
            return domainName;
        }
    }
    private Boolean topicFormatIsValid(String ord) {
        try {
            // Attempt to catch illegal ORD
            EscUtil.ord.unescape(ord);
            return true;
        } catch (Exception e) {
            // Catch strings with illegal chars eg: 'myhome/s_out/r7/$format'
            return false;
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // HELPERS
    // -----------------------------------------------------------------------------------------------------------------


    private String resolveBFormatToPath(String path){ return BFormat.format(path, this);}
    
    private BFacets makeDefaultNumericFacets(String value){
        ////units=null,precision=1,min=-inf,max=+inf
        try {
            Integer cntDps = countDecPlaces(Double.parseDouble(value));
            return BFacets.makeNumeric(BUnit.NULL, cntDps);
        }catch (Exception e){
            return BFacets.makeNumeric(BUnit.NULL, 1);
        }
    }
    private String[] getFolderNamesFromTopicPath(String path){
        // Remove and starting '/'
        String pathN = path.replaceFirst("^/", "");
        return pathN.split("/");
    }
    private String removeNonPrintables(String value) {
        //return value.replaceAll("[^\\x0A\\x0D\\x20-\\x7E]", "").trim();
        String newString = value.replaceAll("[^\\p{Print}]", "");   // Remove non printables
        return newString.replaceAll("\\p{Cntrl}", "").trim();       // Remove control codes
    }
    private boolean niagIsNetworkAndDriverEnabled() {
        try {
            if(this.getNetwork().getEnabled() == false || this.getEnabled() == false) {
                if(this.getEnabled() == false && this.getNetwork().getEnabled() == false) {
                    m_IsAllowedToPoll = false;
                    saPolledFailed("Network and Device disabled", 1);
                } else {
                    if(this.getEnabled() == false) {
                        m_IsAllowedToPoll = false;
                        saPolledFailed("Device disabled", 1);
                    }
                    if(this.getNetwork().getEnabled() == false) {
                        m_IsAllowedToPoll = false;
                        saPolledFailed("Network disabled", 1);
                    }
                }
            } else {
                m_IsAllowedToPoll = true;
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    // On good POLL do housework
    private void saPolledOk(String mesg, int tabOut) {
        //System.out.print("Setting LastOkTime");
        debugOut(mesg, tabOut, false);
        //getHealth().setLastOkTime(BAbsTime.make());
        this.getHealth().pingOk();
    }
    // On failed POLL do housework
    private void saPolledFailed(String mesg, int tabOut) {
        debugOut(mesg, tabOut, false);
    }
    public void debugOut(String msg) {
        debugOut(msg, 1, false);
    }
    public void debugOut(String msg, int tabOut) {
        debugOut(msg, tabOut, false);
    }
    public void debugOut(String msg, int tabOut, boolean override) {

        if(override == false && getDebugToConsole() == false) return;

        String message;
        String ts = "[" + m_OutputDateFormat.format(new Date()) + "]";

        // Add the label if user supplied
        String debugLabel = getDebugLabel();
        if("".equals(debugLabel) == false) {
            ts += "[" + debugLabel + "]";
        }

        if(tabOut > 0) {
            String padIt = padLeftZeros(msg, tabOut);
            message = ts + padIt;
            System.out.println(message);
        }
    }
    private boolean isDouble(String strVal){
        try{
            Double.parseDouble(strVal);
            return true;
        }catch (Exception e){
            return false;
        }
    }
    private boolean isBoolean(String strVal){
        try{
            Boolean.parseBoolean(strVal);
          return "true".equalsIgnoreCase(strVal) || "false".equalsIgnoreCase(strVal);
        }catch (Exception e){
            return false;
        }
    }
    private Integer countDecPlaces(Double d){
        String s = new BigDecimal(d.toString()).stripTrailingZeros().toPlainString();
        Integer integerPlaces = s.indexOf('.');
        if(integerPlaces < 0){
            integerPlaces = s.length();
        }else{
            integerPlaces += 1;
        }
        return s.length() - integerPlaces;
    }
    private boolean isJsonObject(String value) {
        try {
            new JSONObject(value);
        }catch (JSONException ex) {
            return false;
        }
        return true;
    }
    private boolean isJsonArray(String value) {
        try {
            new JSONArray(value);
        } catch (JSONException ex) {
            return false;
        }
        return true;
    }
    private String padLeftZeros(String inputString, int length) {
        String tab = "";
        if(length > 0) {
            for(int i = 0; i < length; i++) {
                tab += " ";
            }
        }
        return tab + inputString;
    }
    private String getDrvVersion(){
        return BuildVersion.VERSION_NUMBER + "." + BuildVersion.FEATURE_NUMBER + "." + BuildVersion.BUILD_NUMBER;
    }

    public final BMqttNetwork getMqttSubNetwork() {
        return (BMqttNetwork) getNetwork();
    }


}