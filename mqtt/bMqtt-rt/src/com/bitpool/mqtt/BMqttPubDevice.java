/**
 * Copyright 2019 bitpool, All Rights Reserved.
 */
package com.bitpool.mqtt;

import com.bitpool.mqtt.point.BMqttPointDeviceExt;
import com.tridium.json.JSONArray;
import com.tridium.json.JSONException;
import com.tridium.json.JSONObject;
import com.tridium.ndriver.BNDevice;
import com.tridium.ndriver.poll.BINPollable;
import com.tridium.ndriver.util.SfUtil;
import com.tridium.sys.Nre;

import javax.baja.control.*;
import javax.baja.control.ext.BAbstractProxyExt;
import javax.baja.control.util.BBooleanOverride;
import javax.baja.control.util.BEnumOverride;
import javax.baja.control.util.BNumericOverride;
import javax.baja.control.util.BStringOverride;
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
import javax.baja.tag.Tag;
import javax.baja.util.BFormat;
import javax.baja.util.Lexicon;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 *  BMqttPubDevice models a single device
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
@NiagaraProperty(name = "publishTopicPath", type = "BString", defaultValue = "/", facets = {
        @Facet(name = "BFacets.FIELD_WIDTH", value = "75")
})
@NiagaraProperty(name = "publishPointsFolder", type = "BOrd", defaultValue = "BOrd.make(\"station:|slot:/Drivers\")", facets = {
        @Facet(name = "BFacets.FIELD_WIDTH", value = "100")
})
@NiagaraProperty(name = "threadCount", type = "BInteger", defaultValue = "BInteger.make(2)",
        facets = {
                @Facet(name = "BFacets.MIN", value = "BInteger.make(1)"),
                @Facet(name = "BFacets.MAX", value = "BInteger.make(25)")
        }
)
@NiagaraProperty(name = "pointPriorityLevel", type = "BMqttPriorityType", defaultValue = "BMqttPriorityType.make(16)")
@NiagaraProperty(
        name = "pointIn8Duration",
        type = "BRelTime",
        defaultValue = "BRelTime.make(60000L)"
)
@NiagaraProperty(name = "dataOutputType", type = "BMqttOutputType", defaultValue = "BMqttOutputType.make(0)")
@NiagaraProperty(name = "dataOutputFormat", type = "BMqttOutputFormat", defaultValue = "BMqttOutputFormat.make(1)")
@NiagaraProperty(name = "dataOutputCompressed", type = "BBoolean", defaultValue = "BBoolean.make(\"true\")", flags = Flags.SUMMARY)


@NiagaraProperty(name = "debugToConsole", type = "BBoolean", defaultValue = "BBoolean.make(\"false\")", flags = Flags.SUMMARY)
@NiagaraProperty(name = "debugLabel", type = "BString", defaultValue = "MQTT-PUB", facets = {
        @Facet(name = "BFacets.FIELD_WIDTH", value = "75")
})

@NiagaraAction(name = "Enable")
@NiagaraAction(name = "Disable")
@NiagaraAction(name = "Initialise")

public class BMqttPubDevice
        extends BNDevice
        implements BINPollable {

    private MqttPublishThreadManager  m_MqttPubThreadManager = null;
    private MqttSubscribe m_MqttSubConn = null;

    private Integer m_MqttInQueueCnt = 0;
    private final SimpleDateFormat m_OutputDateFormat = new SimpleDateFormat("MMM-dd HH:mm:ss");
    private MqttPubSubQuery m_PubSubOrdLookUp = new MqttPubSubQuery();
    private boolean m_IsAllowedToPoll = false;


    // Add facet to include following in auto manager view
    public static final Property status = newProperty(Flags.TRANSIENT | Flags.READONLY | Flags.SUMMARY | Flags.DEFAULT_ON_CLONE, BStatus.ok, SfUtil.incl(SfUtil.MGR_EDIT_READONLY));






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
  public static final Property clientId = newProperty(0, Sys.getHostId(), BFacets.make(BFacets.FIELD_WIDTH, 75));

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
// Property "publishTopicPath"
////////////////////////////////////////////////////////////////

  /**
   * Slot for the {@code publishTopicPath} property.
   * @see #getPublishTopicPath
   * @see #setPublishTopicPath
   */
  public static final Property publishTopicPath = newProperty(0, "/", BFacets.make(BFacets.FIELD_WIDTH, 75));

  /**
   * Get the {@code publishTopicPath} property.
   * @see #publishTopicPath
   */
  public String getPublishTopicPath() { return getString(publishTopicPath); }

  /**
   * Set the {@code publishTopicPath} property.
   * @see #publishTopicPath
   */
  public void setPublishTopicPath(String v) { setString(publishTopicPath, v, null); }

////////////////////////////////////////////////////////////////
// Property "publishPointsFolder"
////////////////////////////////////////////////////////////////

  /**
   * Slot for the {@code publishPointsFolder} property.
   * @see #getPublishPointsFolder
   * @see #setPublishPointsFolder
   */
  public static final Property publishPointsFolder = newProperty(0, BOrd.make("station:|slot:/Drivers"), BFacets.make(BFacets.FIELD_WIDTH, 100));

  /**
   * Get the {@code publishPointsFolder} property.
   * @see #publishPointsFolder
   */
  public BOrd getPublishPointsFolder() { return (BOrd)get(publishPointsFolder); }

  /**
   * Set the {@code publishPointsFolder} property.
   * @see #publishPointsFolder
   */
  public void setPublishPointsFolder(BOrd v) { set(publishPointsFolder, v, null); }

////////////////////////////////////////////////////////////////
// Property "useHierarchy"
////////////////////////////////////////////////////////////////

  /**
   * Slot for the {@code useHierarchy} property.
   * @see #getUseHierarchy
   * @see #setUseHierarchy
   */
  public static final Property useHierarchy = newProperty(Flags.SUMMARY, ((BBoolean.make("false"))).getBoolean(), null);

  /**
   * Get the {@code useHierarchy} property.
   * @see #useHierarchy
   */
  public boolean getUseHierarchy() { return getBoolean(useHierarchy); }

  /**
   * Set the {@code useHierarchy} property.
   * @see #useHierarchy
   */
  public void setUseHierarchy(boolean v) { setBoolean(useHierarchy, v, null); }

////////////////////////////////////////////////////////////////
// Property "hierarchy"
////////////////////////////////////////////////////////////////

  /**
   * Slot for the {@code hierarchy} property.
   * @see #getHierarchy
   * @see #setHierarchy
   */
  public static final Property hierarchy = newProperty(Flags.HIDDEN, BOrd.make(""), BFacets.make(BFacets.FIELD_WIDTH, 100));

  /**
   * Get the {@code hierarchy} property.
   * @see #hierarchy
   */
  public BOrd getHierarchy() { return (BOrd)get(hierarchy); }

  /**
   * Set the {@code hierarchy} property.
   * @see #hierarchy
   */
  public void setHierarchy(BOrd v) { set(hierarchy, v, null); }


////////////////////////////////////////////////////////////////
// Property "threadCount"
////////////////////////////////////////////////////////////////

  /**
   * Slot for the {@code threadCount} property.
   * @see #getThreadCount
   * @see #setThreadCount
   */
  public static final Property threadCount = newProperty(0, ((BInteger.make(2))).getInt(), BFacets.make(BFacets.make(BFacets.MIN, BInteger.make(1)), BFacets.make(BFacets.MAX, BInteger.make(25))));

  /**
   * Get the {@code threadCount} property.
   * @see #threadCount
   */
  public int getThreadCount() { return getInt(threadCount); }

  /**
   * Set the {@code threadCount} property.
   * @see #threadCount
   */
  public void setThreadCount(int v) { setInt(threadCount, v, null); }

////////////////////////////////////////////////////////////////
// Property "pointPriorityLevel"
////////////////////////////////////////////////////////////////

  /**
   * Slot for the {@code pointPriorityLevel} property.
   * @see #getPointPriorityLevel
   * @see #setPointPriorityLevel
   */
  public static final Property pointPriorityLevel = newProperty(0, BMqttPriorityType.make(17), null);

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
// Property "dataOutputType"
////////////////////////////////////////////////////////////////

  /**
   * Slot for the {@code dataOutputType} property.
   * @see #getDataOutputType
   * @see #setDataOutputType
   */
  public static final Property dataOutputType = newProperty(0, BMqttOutputType.make(0), null);

  /**
   * Get the {@code dataOutputType} property.
   * @see #dataOutputType
   */
  public BMqttOutputType getDataOutputType() { return (BMqttOutputType)get(dataOutputType); }

  /**
   * Set the {@code dataOutputType} property.
   * @see #dataOutputType
   */
  public void setDataOutputType(BMqttOutputType v) { set(dataOutputType, v, null); }

////////////////////////////////////////////////////////////////
// Property "dataOutputFormat"
////////////////////////////////////////////////////////////////

  /**
   * Slot for the {@code dataOutputFormat} property.
   * @see #getDataOutputFormat
   * @see #setDataOutputFormat
   */
  public static final Property dataOutputFormat = newProperty(0, BMqttOutputFormat.make(1), null);

  /**
   * Get the {@code dataOutputFormat} property.
   * @see #dataOutputFormat
   */
  public BMqttOutputFormat getDataOutputFormat() { return (BMqttOutputFormat)get(dataOutputFormat); }

  /**
   * Set the {@code dataOutputFormat} property.
   * @see #dataOutputFormat
   */
  public void setDataOutputFormat(BMqttOutputFormat v) { set(dataOutputFormat, v, null); }

////////////////////////////////////////////////////////////////
// Property "dataOutputCompressed"
////////////////////////////////////////////////////////////////

  /**
   * Slot for the {@code dataOutputCompressed} property.
   * @see #getDataOutputCompressed
   * @see #setDataOutputCompressed
   */
  public static final Property dataOutputCompressed = newProperty(Flags.SUMMARY, ((BBoolean.make("false"))).getBoolean(), null);

  /**
   * Get the {@code dataOutputCompressed} property.
   * @see #dataOutputCompressed
   */
  public boolean getDataOutputCompressed() { return getBoolean(dataOutputCompressed); }

  /**
   * Set the {@code dataOutputCompressed} property.
   * @see #dataOutputCompressed
   */
  public void setDataOutputCompressed(boolean v) { setBoolean(dataOutputCompressed, v, null); }

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
  public static final Property debugLabel = newProperty(0, "MQTT-PUB", BFacets.make(BFacets.FIELD_WIDTH, 75));

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
// Action "Initialise"
////////////////////////////////////////////////////////////////

  /**
   * Slot for the {@code Initialise} action.
   * @see #Initialise()
   */
  public static final Action Initialise = newAction(0, null);

  /**
   * Invoke the {@code Initialise} action.
   * @see #Initialise
   */
  public void Initialise() { invoke(Initialise, null, null); }

////////////////////////////////////////////////////////////////
// Type
////////////////////////////////////////////////////////////////

  @Override
  public Type getType() { return TYPE; }
  public static final Type TYPE = Sys.loadType(BMqttPubDevice.class);

/*+ ------------ END BAJA AUTO GENERATED CODE -------------- +*/


    public Type getNetworkType() {
        return BMqttNetwork.TYPE;
    }

    public void started() throws Exception {
        super.started();
        setDriverVersion(getDrvVersion());
        getMqttPubNetwork().getPollScheduler().subscribe(this);
        setFlags(getSlot("status"), Flags.HIDDEN);
        setFlags(getSlot("faultCause"), Flags.HIDDEN);
    }

    public void stopped() throws Exception {
        // unregister device with poll scheduler
        getMqttPubNetwork().getPollScheduler().unsubscribe(this);
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
            pubPoll();
        }
    }
    public void atSteadyState() throws Exception {
        super.atSteadyState();
        showTime();
    }
    private static final Lexicon lexicon = Lexicon.make("wbutil");
    public static String text(String s) { return lexicon.getText(s); }

  public void doEnable() {
        this.setEnabled(true);
    }
    public void doDisable() {
        debugOut("User request to disable device", 1, true);
        this.setEnabled(false);
    }
    public void doInitialise(){
        debugOut("User requested action, initialising...", 1, true);
        setEnabled(false);
        setEnabled(true);
    }
    public void changed(Property p, Context cx) {
        super.changed(p, cx);
        if(!isRunning()) return;

        if(p == enabled) {
            if(!isDisabled()) {
                showTime();
                debugOut("Device enabled");
            } else {
                mqttDisconnect();
                m_IsAllowedToPoll = false;
            }
        }
        if( p == brokerUsername ||
            p == brokerPassword ||
            p == clientId ||
            p == publishTopicPath ||
            p == brokerAddress ||
            p == brokerPort ||
            p == publishPointsFolder ||
            p == brokerConnectionType ||
            p == brokerUsingTls ||
            p == threadCount) {

          setDriverStatus("Initialising connection");
          debugOut("MQTT configuration has changed, initialising...", 1, true);
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

        if(p == useHierarchy) {
          if (getUseHierarchy()) {
            BOrd defaultPublishOrd = BOrd.make("station:|slot:/Drivers");
            setPublishPointsFolder(defaultPublishOrd);
            Slot s = this.getSlot("publishPointsFolder");
            Slot t = this.getSlot("hierarchy");
            this.setFlags(s, Flags.HIDDEN | getFlags(s));
            this.setFlags(t, ~Flags.HIDDEN & getFlags(t));
          }
          else if (!getUseHierarchy()) {
            Slot s = this.getSlot("publishPointsFolder");
            Slot t = this.getSlot("hierarchy");
            this.setFlags(s, ~Flags.HIDDEN & getFlags(s));
            this.setFlags(t, Flags.HIDDEN | getFlags(t));
          }
        }
    }
    private void showTime() {
        debugOut("MQTT PUBLISH DRIVER [" + getDrvVersion() + "]", 1, true);
        m_PubSubOrdLookUp = new MqttPubSubQuery();
        mqttConnect();
        m_IsAllowedToPoll = true;
    }
    private boolean isDouble(String strVal){
        try{
            Double.parseDouble(strVal);
            return true;
        }catch (Exception e){
            return false;
        }
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
    private String extractValueFromJson(String jsonBlock){
        if(isJsonObject(jsonBlock)) {
            JSONObject jSchema = new JSONObject(jsonBlock);
            if (jSchema.has("schema")) {
                if (jSchema.get("schema").equals(JsonSchema.MQTTPS.toString()) || jSchema.get("schema").equals(JsonSchema.MQTTPS2.toString())) {
                    if (jSchema.has("data")) {

//                        JSONObject jData = new JSONObject(jSchema.getString("data"));

                        JSONObject jData;
                        // If compression exist then decompress
                        if(jSchema.has("compression")){
                            String sDd = jSchema.getString("data");
                            byte[] bMsgC = Base64.getDecoder().decode(sDd);
                            String bMsg = decompress(bMsgC);
                            jData = new JSONObject( bMsg);
                        }else{
                          // Here we check the type of "data" value
                          Object dataObj = jSchema.get("data");
                          if(dataObj instanceof JSONObject){
                            // If "data" is already a JSON object, just cast it
                            jData = (JSONObject) dataObj;
                          } else if(dataObj instanceof String){
                            // If "data" is a string, parse it into a JSON object
                            jData = new JSONObject((String) dataObj);
                          } else {
                            // If "data" is neither a JSON object nor a string, print an error message
                            System.out.println("Unexpected type for 'data': " + dataObj.getClass().getName());
                            return null;
                          }
                        }


                        if (jData.has("props")) {
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

                            if(properties.size()>0){
                                if(properties.containsKey("outValue")){
                                    SlotProperties sPropOut = properties.get("outValue");
                                    return sPropOut.value;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    private void pubPoll() {



      if(niagIsNetworkAndDriverEnabled()) {
          Integer queueCnt = null;
          if (m_MqttPubThreadManager != null && m_MqttPubThreadManager.isOnline()) {
            pingOk();
            queueCnt = m_MqttPubThreadManager.getQueueSize();

            JSONObject mqttDetailsJson = new JSONObject();
            JSONObject mqttPublishDetailsJson = new JSONObject();
            mqttDetailsJson = deviceStatusJSON();

            // Make sure all messages are sent before processing
            if (queueCnt == 0) {
              // Clear the cache for the next poll
              m_PubSubOrdLookUp.prevOrdPollInitCache();

              // We have to publish data in this manner using polling - since we have not created the points. By checking
              // the new 'out.value' against the previous then we can determine what needs to be sent. If the user
              // selects the 'PUBLISH_ALL_VALUES' option then every value get sent on the poll cycle.
              //
              // A further user option 'JSON', prevents sending discrete values and provides a 'copy' mechanism
              // of point the value (and facets etc) on another system.

              String parentTopic = getPublishPointsFolder().toString();
              String topicPrefix = resolveBFormatToPath(getPublishTopicPath());
              Map<String, String> hierarchies = new HashMap<>();

              if(getUseHierarchy()){
                m_PubSubOrdLookUp.buildHierarchyLookup(getHierarchy());
              }

              debugOut("Polling <PUB>", 1);
              debugOut("Published Broker Topic: <" + topicPrefix + ">", 2);
              debugOut("Source ORD: <" + parentTopic + ">", 3);


              Boolean makeJsonPayload = false;
              Boolean makeJsonPayloadSimple = false;
              if (getDataOutputFormat().getOrdinal() == BMqttOutputFormat.JSON) {
                makeJsonPayload = true;
              } else if (getDataOutputFormat().getOrdinal() == BMqttOutputFormat.JSON_SIMPLE) {
                makeJsonPayload = true;
                makeJsonPayloadSimple = true;
              }

              if (m_PubSubOrdLookUp.buildOrdLookup(parentTopic)) {
                ArrayList<String> ordList = m_PubSubOrdLookUp.getOrdList();
                if(getUseHierarchy()) {
                  m_PubSubOrdLookUp.buildHierarchyLookup(getHierarchy());
                }
                //System.out.println("Hierarchy in main class: "+ hierarchies);
                Integer countOuts = ordList.size();

                boolean forceGetAllValues;
                // Only permit publish on all value of the priority queue is being used. Will cause feedback loop
                String outputTypeMsg;
                if (getDataOutputType().getOrdinal() == BMqttOutputType.PUBLISH_ALL_VALUES) {
                  // Get ALL values from the query even if their state has not changed - ie COV
                  if (getPointPriorityLevel().getOrdinal() == BMqttPriorityType.DISABLED) {
                    forceGetAllValues = true;
                    outputTypeMsg = "<Send ALL values>";
                  } else {
                    forceGetAllValues = false;
                    outputTypeMsg = "<Send ONLY changed values (priority point queue enabled)>";
                  }
                } else {
                  forceGetAllValues = false;
                  outputTypeMsg = "<Send ONLY changed values>";
                }

                Map<String, String> hashTopicsCov = m_PubSubOrdLookUp.getChanged(forceGetAllValues);
                Map<String, String> hashTopicsCovJson = new HashMap<>();
                Map<String, String> hashTopicsCovDiscrete = new HashMap<>();
                Map<String, String> mqttDetails = new HashMap<>();
                Integer countCovOuts = hashTopicsCov.size();

                debugOut("All Points: " + countOuts, 3);
                debugOut("Changed Points: " + countCovOuts, 3);
                debugOut("Output Type:  " + outputTypeMsg, 3);

                if (makeJsonPayload) { // Send in JSON format
                  if (countCovOuts > 0) {
                    if (makeJsonPayloadSimple) { // JSON simple - just time and value
                      for (Map.Entry<String, String> entry : hashTopicsCov.entrySet()) {
                        try {
                          String topic = entry.getKey();
                          String publishTopic;
                          if (getUseHierarchy()) {

                            String newTopicHandle = m_PubSubOrdLookUp.getExistingHandle(topic);
                            String newTopicOrd = m_PubSubOrdLookUp.getNewTopic(newTopicHandle);

                            if(newTopicOrd != null) {
                              publishTopic = newTopicOrd;
                            }
                            else{
                                publishTopic = null;
                            }
                          } else {
                              publishTopic = topic;
                              //System.out.println("Simple. Not Hierarchy.");
                          }

                          JSONObject jO = new JSONObject();
                          BControlPoint bComp = (BControlPoint) BOrd.make(escapeOrd(topic)).resolve().getComponent();
                          String pointValue = bComp.getOutStatusValue().getValueValue().toString();
                          m_PubSubOrdLookUp.prevOrdPollAddToCache(topic, pointValue);

                          jO.put("utcMSecs", new Date().getTime());

                          if (isDouble(pointValue)) {
                            jO.put("value", new BigDecimal(pointValue));
                          } else if (isJsonObject(pointValue)) {
                            jO.put("value", new JSONObject(pointValue).toString());
                          } else if (isJsonArray(pointValue)) {
                            jO.put("value", new JSONArray(pointValue).toString());
                          } else {
                            jO.put("value", pointValue);
                          }


                          if(publishTopic != null) {
                            hashTopicsCovJson.put(publishTopic, jO.toString());
                          }

                        } catch (Exception e) {//e.printStackTrace();
                        }
                      }
                      mqttPublishMessageBlock(hashTopicsCovJson, true, true);
                      debugOut("Published: " + hashTopicsCovJson.size() + " <" + getBrokerAddress() + "><OutputFormat=JSON-SIMPLE>", 4);

                      // hashTopicsCovJson.put(publishTopic, jO.toString());
                      //System.out.println("MQTT Details: "+mqttDetails);
                      try {


                        mqttPublishDetailsJson.put("Points to Publish", hashTopicsCovJson.size());
                        mqttPublishDetailsJson.put("Point Count", countOuts);

                        mqttDetailsJson.put("Publish Count", mqttPublishDetailsJson);
                        // Update in convertOrdToTopic to ensure correct format to broker
                        String sysTopic = getPublishTopicPath() + "/EDGE_DEVICES/" + Sys.getHostId() + "/SYSTEM";
                        //System.out.println("System topic is: " + sysTopic);
                        mqttDetails.put(formatTopic(sysTopic), mqttDetailsJson.toString());
                        // publish mqtt detail payload
                        mqttPublishMessageBlock(mqttDetails, true, false);
                      }
                      catch (Exception e) {
                        System.out.println(e);
                      }

                    } else {// JSON complex - all tags etc and value
                      for (Map.Entry<String, String> entry : hashTopicsCov.entrySet()) {
                        try {
                          String topic = entry.getKey();
                          JSONObject jO = new JSONObject();
                          JSONArray jAryTags = new JSONArray();
                          JSONArray jAryProps = new JSONArray();
                          BControlPoint bComp = (BControlPoint) BOrd.make(escapeOrd(topic)).resolve().getComponent();
                          String bHandle = bComp.getHandleOrd().toString();
                          Property[] aryProps = bComp.getFrozenPropertiesArray();
                          BFacets bPef = bComp.getFacets();
                          String pointFacetStr = null;
                          ArrayList<String> relArray = m_PubSubOrdLookUp.getRelations(bHandle);
                          try {
                            pointFacetStr = bPef.encodeToString();
                          } catch (Exception ex) {
                          }
                          String pointValue = bComp.getOutStatusValue().getValueValue().toString();
                          String pointStatus = bComp.getOutStatusValue().getStatus().toString();
                          m_PubSubOrdLookUp.prevOrdPollAddToCache(topic, pointValue);
                          if (aryProps.length > 0) {
                            for (int i = 0; i < aryProps.length; i++) {
                              Property prop = aryProps[i];
                              String propName = SlotPath.unescape(prop.getName());
                              JSONObject jP = new JSONObject();
                              jP.put("n", propName);
                              BValue bV = bComp.get(prop);
                              String tagType = prop.getType().toString();
                              if (prop.getType().equals(BFacets.TYPE)) {
                                jP.put("v", pointFacetStr);
                                jP.put("t", "String");
                              } else {
                                jP.put("v", SlotPath.unescape(bV.toString()));
                                jP.put("t", tagType.replace("baja:", ""));
                              }
                              if (prop.getType().equals(BAbstractProxyExt.TYPE) == false) {
                                jAryProps.put(jP);
                              }
                            }
                            JSONObject jX = new JSONObject();
                            jX.put("n", "navOrd");
                            jX.put("t", "String");
                            jX.put("v", SlotPath.unescape(bComp.getNavOrd().toString()));
                            jAryProps.put(jX);

                            JSONObject jT = new JSONObject();
                            jT.put("n", "type");
                            jT.put("t", "String");
                            jT.put("v", m_PubSubOrdLookUp.getOrdType(topic));
                            jAryProps.put(jT);

                            JSONObject jOV = new JSONObject();
                            jOV.put("n", "outValue");
                            jOV.put("t", "String");
                            jOV.put("v", pointValue);
                            jAryProps.put(jOV);

                            JSONObject jOS = new JSONObject();
                            jOS.put("n", "outStatus");
                            jOS.put("t", "String");
                            jOS.put("v", pointStatus);
                            jAryProps.put(jOS);

                            jO.put("props", jAryProps);
                          }
                          // Build Tags
                          Collection<Tag> colTags = bComp.tags().getAll();
                          if (colTags.size() > 0) {
                            Iterator itTags = colTags.iterator();
                            while (itTags.hasNext()) {
                              Tag newTag = (Tag) itTags.next();
                              String tagName = newTag.getId().toString();
                              String tagValue;
                              try {
                                tagValue = SlotPath.unescape(newTag.getValue().toString());
                              } catch (Exception ee) {
                                tagValue = newTag.getValue().toString();
                              }

                              String tagType = newTag.getValue().getType().toString();
                              JSONObject jT = new JSONObject();
                              jT.put("n", tagName);
                              jT.put("v", tagValue);
                              jT.put("t", tagType.replace("baja:", ""));
                              jAryTags.put(jT);
                            }
                            if(getUseHierarchy()) {
                              jO.put("meta", jAryTags);
                            }
                            else{
                              jO.put("tags", jAryTags);
                            }
                          }
                          //Build Relations
                          if(relArray != null && relArray.size() > 0){
                            Iterator itRel = relArray.iterator();
                            while (itRel.hasNext()){
                              String rel = (String) itRel.next();
                              String[] relAry = rel.split(",");
                              String direction = relAry[0];
                              String reference = relAry[1];
                              String endpoint = relAry[2];
                              BOrd relOrd = BOrd.make("station:|"+endpoint);
                              BComponent relComp = (BComponent)relOrd.get();
                              String relStr = relComp.getSlotPath().toString();
                              //Create JSONObject for each relation
                              JSONObject relObject = new JSONObject();
                              relObject.put("n", reference);   // n for reference
                              relObject.put("d", direction);   // d for direction
                              relObject.put("h", endpoint);    // h for endpoint
                              relObject.put("o", relStr);

                              jAryTags.put(relObject);
                            }
                            if(getUseHierarchy()) {
                              jO.put("meta", jAryTags);
                            }
                            else{
                              jO.put("tags", jAryTags);
                            }
                          }

                          String publishTopic;
                          if (getUseHierarchy()) {
                            String newTopicHandle = m_PubSubOrdLookUp.getExistingHandle(topic);
                            String newTopicOrd = m_PubSubOrdLookUp.getNewTopic(newTopicHandle);
                            //debugOut("New Hierarchy Topic: <" + newTopicOrd + ">", 4);
                            if(newTopicOrd != null) {
                              publishTopic = newTopicOrd;
                            }
                            else{
                              publishTopic = null;
                            }
                          } else {
                            publishTopic = topic;
                          }

                          if(publishTopic != null) {
                            hashTopicsCovJson.put(publishTopic, jO.toString());
                          }
                        } catch (Exception e) {//e.printStackTrace();
                        }
                      }
                      mqttPublishMessageBlock(hashTopicsCovJson, true, false);
                      debugOut("Published: " + hashTopicsCovJson.size() + " <" + getBrokerAddress() + "><OutputFormat=JSON>", 4);
                      // Build mqtt detail payload

                      // hashTopicsCovJson.put(publishTopic, jO.toString());
                      //System.out.println("MQTT Details: "+mqttDetails);
                      try {


                        mqttPublishDetailsJson.put("Points to Publish", hashTopicsCovJson.size());
                        mqttPublishDetailsJson.put("Point Count", countOuts);

                        mqttDetailsJson.put("Publish Count", mqttPublishDetailsJson);
                        // Update in convertOrdToTopic to ensure correct format to broker
                        String sysTopic = getPublishTopicPath() + "/EDGE_DEVICES/" + Sys.getHostId() + "/SYSTEM";
                        //System.out.println("System topic is: " + sysTopic);
                        mqttDetails.put(formatTopic(sysTopic), mqttDetailsJson.toString());
                        // publish mqtt detail payload
                        mqttPublishMessageBlock(mqttDetails, true, false);
                      }
                      catch (Exception e) {
                        System.out.println(e);
                      }

                    }




                  }
                } else { // Send in DISCRETE format
                  if (countCovOuts > 0) {

                    for (Map.Entry<String, String> entry : hashTopicsCov.entrySet()) {
                      String topic = entry.getKey();
                      String pointValue = entry.getValue();
                      m_PubSubOrdLookUp.prevOrdPollAddToCache(topic, pointValue);
                      String publishTopic;
                      if (getUseHierarchy()) {
                        String newTopicHandle = m_PubSubOrdLookUp.getExistingHandle(topic);
                        String newTopicOrd = m_PubSubOrdLookUp.getNewTopic(newTopicHandle);
                        //debugOut("New Hierarchy Topic: <" + newTopicOrd + ">", 4);
                        if(newTopicOrd != null) {
                          publishTopic = newTopicOrd;
                        }
                        else{
                          publishTopic = null;
                        }
                      } else {
                        publishTopic = topic;
                      }

                      if(publishTopic != null) {
                        hashTopicsCovDiscrete.put(publishTopic, pointValue);
                      }
                    }
                    mqttPublishMessageBlock(hashTopicsCovDiscrete, false, false);
                    debugOut("Published: " + countCovOuts + " <" + getBrokerAddress() + "><OutputFormat=VALUE>", 4);
                    // Build mqtt detail payload


                    // hashTopicsCovJson.put(publishTopic, jO.toString());
                    //System.out.println("MQTT Details: "+mqttDetails);
                    try {

                      mqttPublishDetailsJson.put("Points to Publish", hashTopicsCovDiscrete.size());
                      mqttPublishDetailsJson.put("Point Count", countCovOuts);

                      mqttDetailsJson.put("Publish Count", mqttPublishDetailsJson);
                      // Update in convertOrdToTopic to ensure correct format to broker
                      String sysTopic = getPublishTopicPath() + "/EDGE_DEVICES/" + Sys.getHostId() + "/SYSTEM";
                      //System.out.println("System topic is: " + sysTopic);
                      mqttDetails.put(formatTopic(sysTopic), mqttDetailsJson.toString());
                      // publish mqtt detail payload
                      mqttPublishMessageBlock(mqttDetails, true, false);
                    }
                    catch (Exception e) {
                      System.out.println(e);
                    }
//                    mqttPublishMessageBlock(hashTopicsCov, false, false);
//                    debugOut("Published: " + countCovOuts + " <" + getBrokerAddress() + "><OutputFormat=VALUE>", 4);
                  }
                }

                Integer brokerQueueSize = 0;
                ArrayList<MqttPayload> listTopics;

                // Get a list of messages echoed
                listTopics = m_MqttSubConn.dequeueMessages();
                brokerQueueSize = listTopics.size();

                // Pull subscribed message and create a list to handle later
                if (brokerQueueSize > 0) {
                  for (int queueIdx = 0; queueIdx < brokerQueueSize; queueIdx++) {
                    MqttPayload objMqttRaw = listTopics.get(queueIdx);
                    String brokerTopic = objMqttRaw.topic;
                    String brokerValue = objMqttRaw.message.toString();

                    // Need to match the mqtt topic with the published ORD
                    // pubSubTestout1/BooleanWritable -> true (pubSubTestout1 == this.getPublishTopicPath())
                    // local:|station:|slot:/Apps/Test/BooleanWritable - (ORD in cache)
                    String brokerTopicLimit = brokerTopic.replaceAll(this.getPublishTopicPath(), "");
                    String compareTopic = "local:|" + this.getPublishPointsFolder() + brokerTopicLimit;

                    // Is it a JSON Block, if so look for driver marker 'MQTTPS' or 'MQTTPS2'
                    if (isJsonObject(brokerValue)) {
                      brokerValue = extractValueFromJson(brokerValue);
                    }
                    Integer pointLevelIndex = getPointPriorityLevel().getOrdinal();
                    int pIndex = getPointPriorityLevel().getOrdinal();
                    String priorityInputName;

                    if (m_PubSubOrdLookUp.doseOrdExist(escapeOrd(compareTopic))) {
                      String currentValue = m_PubSubOrdLookUp.getOrdCurrentValue(escapeOrd(compareTopic));
                      if (currentValue.contentEquals(brokerValue) == false) {

                        BControlPoint bComp = (BControlPoint) BOrd.make(escapeOrd(compareTopic)).resolve().getComponent();
                        if (bComp.isWritablePoint() && pIndex != BMqttPriorityType.DISABLED) {
                          BValue testValue = bComp.get("in1");
                          if ((pIndex >= BMqttPriorityType.IN_1EO) && (pIndex <= BMqttPriorityType.IN_16)) {
                            priorityInputName = "in" + (pointLevelIndex + 1);
                          } else {
                            priorityInputName = "fallback";
                          }

                          if (testValue.getType() == BStatusString.TYPE) {
                            try {
                              if (getPointPriorityLevel().getOrdinal() == BMqttPriorityType.IN_8MO) {
                                long mSecAdd = getPointIn8Duration().getMillis();
                                BStringOverride ovrRideVal = new BStringOverride();                         // Create the override instance
                                ovrRideVal.set("duration", BRelTime.make(mSecAdd));                         // Set the duration in milliseconds
                                ovrRideVal.setValue(brokerValue);                                           // Set the value to be overridden
                                ((BStringWritable) bComp).doOverride(ovrRideVal);                            // Do override
                                long mSecTarget = Clock.millis() + mSecAdd;                                 // Add to the current time now
                                ((BNumericWritable) bComp).setOverrideExpiration(BAbsTime.make(mSecTarget)); // Update the UI

                              } else {
                                BStatusString bS = (BStatusString) bComp.get(priorityInputName);
                                bS.setStatus(BStatus.ok);
                                bS.setValue(brokerValue);
                                bComp.set(priorityInputName, bS);
                              }

                            } catch (Exception exp) {
                            }

                          } else if (testValue.getType() == BStatusNumeric.TYPE) {
                            try {
                              if (getPointPriorityLevel().getOrdinal() == BMqttPriorityType.IN_8MO) {
                                long mSecAdd = getPointIn8Duration().getMillis();
                                BNumericOverride ovrRideVal = new BNumericOverride();                       // Create the override instance
                                ovrRideVal.set("duration", BRelTime.make(mSecAdd));                         // Set the duration in milliseconds
                                ovrRideVal.setValue(BDouble.make(brokerValue).getDouble());                 // Set the value to be overridden
                                ((BNumericWritable) bComp).doOverride(ovrRideVal);                           // Do override
                                long mSecTarget = Clock.millis() + mSecAdd;                                 // Add to the current time now
                                ((BNumericWritable) bComp).setOverrideExpiration(BAbsTime.make(mSecTarget)); // Update the UI
                              } else {
                                BStatusNumeric bN = (BStatusNumeric) bComp.get(priorityInputName);
                                bN.setStatus(BStatus.ok);
                                bN.setValue(BDouble.make(brokerValue).getDouble());
                                bComp.set(priorityInputName, bN);
                              }
                            } catch (Exception exp) {
                            }

                          } else if (testValue.getType() == BStatusEnum.TYPE) {
                            try {
                              if (getPointPriorityLevel().getOrdinal() == BMqttPriorityType.IN_8MO) {
                                long mSecAdd = getPointIn8Duration().getMillis();
                                BEnumOverride ovrRideVal = new BEnumOverride();                             // Create the override instance
                                ovrRideVal.set("duration", BRelTime.make(mSecAdd));                         // Set the duration in milliseconds
                                BEnumRange eRange = getEnumRange(bComp.getFacets());
                                BEnum enumValue = getEnumValue(brokerValue, bComp.getFacets());
                                ovrRideVal.setValue(BDynamicEnum.make(enumValue.getOrdinal(), eRange));     // Set the value to be overridden
                                ((BEnumWritable) bComp).doOverride(ovrRideVal);                              // Do override
                                long mSecTarget = Clock.millis() + mSecAdd;                                 // Add to the current time now
                                ((BEnumWritable) bComp).setOverrideExpiration(BAbsTime.make(mSecTarget));    // Update the UI

                              } else {
                                BEnum enumValue = getEnumValue(brokerValue, bComp.getFacets());
                                BStatusEnum bE = (BStatusEnum) bComp.get(priorityInputName);
                                bE.setStatus(BStatus.ok);
                                bE.setValue(enumValue);
                              }
                            } catch (Exception exp) {
                            }

                          } else if (testValue.getType() == BStatusBoolean.TYPE) {
                            try {
                              if (getPointPriorityLevel().getOrdinal() == BMqttPriorityType.IN_8MO) {
                                long mSecAdd = getPointIn8Duration().getMillis();
                                BBooleanOverride ovrRideVal = new BBooleanOverride();                       // Create the override instance
                                ovrRideVal.set("duration", BRelTime.make(mSecAdd));                         // Set the duration in milliseconds
                                if (Boolean.parseBoolean(brokerValue) == true) {
                                  ((BBooleanWritable) bComp).doActive(ovrRideVal);
                                } else {
                                  ((BBooleanWritable) bComp).doInactive(ovrRideVal);
                                }
                                long mSecTarget = Clock.millis() + mSecAdd;                                 // Add to the current time now
                                ((BNumericWritable) bComp).setOverrideExpiration(BAbsTime.make(mSecTarget)); // Update the UI

                              } else {
                                BStatusBoolean bB = (BStatusBoolean) bComp.get(priorityInputName);
                                bB.setStatus(BStatus.ok);
                                bB.setValue(BBoolean.make(brokerValue).getBoolean());
                                bComp.set(priorityInputName, bB);
                              }

                            } catch (Exception exp) {
                            }
                          }
                          // Add  for echo cancellation
                          m_PubSubOrdLookUp.setOrdCurrentValue(escapeOrd(compareTopic), brokerValue);

                        }
                      }
                    }
                  }
                }
              }
              setDriverStatus("Polled OK [" + m_OutputDateFormat.format(new Date()) + "]");
              saPolledOk("Polled OK", 1);
            } else {
              saPolledFailed("Poll FAILED - topic invalid or does not exist: " + getPublishPointsFolder().toString(), 2);
            }
            debugOut("", 1, false);
          } else {

            debugOut("Polling <PUB>", 1);
            debugOut("Clearing outstanding messages in queue (" + queueCnt + ")", 2);
            debugOut("Thread Manager is: "+m_MqttPubThreadManager +"and is online? "+ m_MqttPubThreadManager.isOnline(), 2);
            debugOut("", 1);
          }
        }else{
                // Attempt to restart the connection
                debugOut("Broker is Offline - restarting connection..." , 1, true);
                debugOut("" , 1, true);
                pingFail("MQTT broker offline");
                mqttDisconnect();
                showTime();
            }
//        }else{
//            // Make sure the threads are shut down correctly
//            mqttDisconnect();
//        }

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
    // -----------------------------------------------------------------------------------------------------------------
    // MQTT
    // -----------------------------------------------------------------------------------------------------------------
    private void mqttConnect() {
        if(niagIsNetworkAndDriverEnabled()) {
            debugOut("Configuring MQTT client", 1, false);

            Integer connectionType = getBrokerConnectionType().getOrdinal();

            Boolean areMqttCredentialsOK = true;
            String faultCause = "";

            String brokerAddress    = getUrlDomainName(getBrokerAddress());
            Integer brokerPort      = getBrokerPort();
            String mqttUserName     = getBrokerUsername();
            Boolean brokerUseTLS    = getBrokerUsingTls();
            String brokerPassword   = AccessController.doPrivileged((PrivilegedAction<String>) ()-> getBrokerPassword().getValue());
            String publishTopicPath = resolveBFormatToPath(getPublishTopicPath());
            String ClientId         = getClientId();

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
                if(brokerUseTLS){
                    debugOut("Connecting to secure Broker (TLS): <" + brokerAddress + "> <Threads=" + getThreadCount() + ">", 2);
                }else{
                    debugOut("Connecting to Broker: <" + brokerAddress + "> <Threads=" + getThreadCount() + ">", 2);
                }

                if(connectionType == BMqttConnectionType.USING_CREDENTIALS) {
                    m_MqttPubThreadManager = new MqttPublishThreadManager(brokerAddress + ":" + brokerPort, mqttUserName, brokerPassword, getThreadCount(), brokerUseTLS, ClientId);
                    // Create in subscribe instance but dont connect yet.
                    m_MqttSubConn = new MqttSubscribe(brokerAddress + ":" + brokerPort, mqttUserName, brokerPassword, MqttSubscribe.MQTT_QUEUE_SIZE, brokerUseTLS, ClientId);

                    debugOut("Using credentials for user: " + getBrokerUsername(), 2);
                } else if(connectionType == BMqttConnectionType.ANONYMOUS) {

                    m_MqttPubThreadManager = new MqttPublishThreadManager(brokerAddress + ":" + brokerPort, getThreadCount(), brokerUseTLS, ClientId);
                    // Create in subscribe instance but dont connect yet.
                    m_MqttSubConn = new MqttSubscribe(brokerAddress + ":" + brokerPort, MqttSubscribe.MQTT_QUEUE_SIZE, brokerUseTLS, ClientId);

                    debugOut("Not using credentials, logging in as Anonymous user", 2);
                }

                // Start up the thread manager and workers
                this.postAsync(m_MqttPubThreadManager);

                debugOut("Using Topic Prefix: <" + publishTopicPath + ">", 4);
                // Connect the sub to the broker for receiving messages
                if (m_MqttSubConn.connect(publishTopicPath)) {
                    debugOut("Connected OK", 3, false);
                    setDriverStatus("Connected OK");
                    this.postAsync(m_MqttSubConn);
                }
                debugOut("", 1, false);

            } else {
                setEnabled(false);
                debugOut(faultCause, 2);
                setDriverStatus(faultCause);
            }
        }
    }
    private void mqttDisconnect() {
        if(m_MqttPubThreadManager != null) {
            m_MqttPubThreadManager.disconnect();
            m_MqttPubThreadManager = null;
        }
    }
    public void mqttPublishMessageBlock(Map < String, String > mapTopics, Boolean makeJsonPayload , Boolean isSimpleJson) {
        if(getEnabled()) {
            if(this.getNetwork().getEnabled()) { // Cant send MQTT messages if disabled, they rely on Network worker thread pool
                if(m_MqttPubThreadManager != null) {
                    if(mapTopics.size() > 0) {
                        for(Map.Entry < String, String > entry: mapTopics.entrySet()) {
                            String topic = convertOrdToTopic(entry.getKey());
                            String message = entry.getValue();

                            if(makeJsonPayload) {
                                String dataOut;
                                JSONObject jO = new JSONObject();
                                if(isSimpleJson){
                                    jO = new JSONObject(message);
                                    dataOut = jO.toString(4);
                                }else{
                                    jO.put("utcMSecs", new Date().getTime());
                                    jO.put("schema", JsonSchema.MQTTPS2);

                                    // Compress data value before sending and set new 'compression' key
                                    // Receiving even will only check to see of 'compression' key exists - not its value
                                    if(getDataOutputCompressed()){
                                        jO.put("compression", true);
                                        JSONObject jOC = new JSONObject(message);
                                        byte[] bMsg = compress(jOC.toString());
                                        jO.put("data", Base64.getEncoder().encodeToString(bMsg));
                                    }else{
                                        jO.put("data", new JSONObject(message));
                                    }

                                    dataOut = jO.toString(4);
                                }
                               m_MqttInQueueCnt = m_MqttPubThreadManager.queueMessage(topic, dataOut);
                            } else {
                               m_MqttInQueueCnt = m_MqttPubThreadManager.queueMessage(topic, message);
                            }
                        }
                    }
                }
            }
        }
    }
    // -----------------------------------------------------------------------------------------------------------------
    // HELPERS
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

    private String resolveBFormatToPath(String path){
        return BFormat.format(path, this);
    }

    private String convertOrdToTopic(String ord) {                                      // 'local:|station:|slot:/Drivers/MqttNetwork/bitpool2/points/tick/t1'
      String cleanedOrd = ord.replaceAll("/+", "/");
      //From system payload
      String targetPath = formatTopic(getPublishTopicPath() + "/EDGE_DEVICES/" + Sys.getHostId() + "/SYSTEM");

      if(!ord.startsWith("hierarchy:/")){
        if (cleanedOrd.equals(targetPath)) {
          return targetPath;
        }
        else {
          Integer indx = ord.indexOf("/points/") + 7;                                     // 57
          String stationTopic = ord.substring(indx);                                      // '/tick/t1'
          String findMe = getPublishPointsFolder().toString() + "/";                      // 'station:|slot:/Drivers/MqttNetwork/bitpool2/points/tick' + '/'
          findMe = findMe.replaceAll("/+", "/");                          // 'station:|slot:/Drivers/MqttNetwork/bitpool2/points/tick/'
          String brokerTopic = ord.substring(ord.indexOf(findMe) + findMe.length());      // 7 + 56 = 63 ( 't1')
          brokerTopic = resolveBFormatToPath(getPublishTopicPath()) + "/" + brokerTopic;  // '/tock/' + '/' + 't1'
          brokerTopic = brokerTopic.replaceAll("/+", "/");                // '/tock/t1'
          return SlotPath.unescape(brokerTopic);
        }
      }
      else {
        // Find the index of the start of the desired segment after the hierarchy
        int hierarchyIndex = ord.indexOf("/hierarchy:/") + 12;
        int startIndex = ord.indexOf("/", hierarchyIndex) + 1;

        // Find the index of the end of the segment, marked by "/station:|"
        int endIndex = ord.indexOf("/station:|");
        if (endIndex == -1) {
          endIndex = ord.length();
        }

        // Extract the desired segment from the input string
        String brokerTopic = ord.substring(startIndex, endIndex);

        String resolvedPath = resolveBFormatToPath(getPublishTopicPath());
        // Concatenate the resolvedPath only if it's not an empty string
        String cleanTopic = (resolvedPath.isEmpty() ? brokerTopic : resolvedPath + "/" + brokerTopic);

        // Replace one or more consecutive slashes with a single slash
        cleanTopic = cleanTopic.replaceAll("/+", "/");

        return cleanTopic;

      }
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
    private void saPolledOk(String mesg, int tabOut) {
        //System.out.print("Setting LastOkTime");
        debugOut(mesg, tabOut, false);
        //this.getHealth().setLastOkTime(BAbsTime.make());
        this.getHealth().pingOk();
    }
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

        String debugLabel = getDebugLabel();
        if("".equals(debugLabel) == false) {
            ts += "[" + debugLabel + "]";
        }
        if(tabOut > 0) {
            String padIt = padLeftZeros(msg, tabOut);
            message = ts + padIt;
            //System.out.println(message);
        }
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


    public final BMqttNetwork getMqttPubNetwork() {
        return (BMqttNetwork) getNetwork();
    }

  private JSONObject deviceStatusJSON(){

    JSONObject mqttDetailsJson = new JSONObject();
    JSONObject hostDetailsJson = new JSONObject();
    JSONObject systemDetailsJson = new JSONObject();
//    JSONObject driverDetailsJson = new JSONObject();


    String hostName = Sys.getHostName();
    String hostId = Sys.getHostId();
    String stationName = Sys.getStation().getStationName();
    String deviceName = this.getName();
    String sysTime = BAbsTime.now().toString();


    hostDetailsJson.put("Host Name", hostName);
    hostDetailsJson.put("Host ID", hostId);
    hostDetailsJson.put("Station Name", stationName);
    hostDetailsJson.put("System Time", sysTime);


    Integer cpuUsage =  Nre.getPlatform().getCpuUsage();
    Integer memUsage =  Nre.getPlatform().getMemoryUsage();
    Integer memTotalUsage =  Nre.getPlatform().getTotalMemory();
    Integer iMemPctUsed = 0 ;
    float memPctUsed;

    if(memTotalUsage>0 && memUsage <= memTotalUsage){
      BFloat a = BFloat.make(memUsage.toString());
      BFloat b = BFloat.make(memTotalUsage.toString());
      memPctUsed = (a.getFloat() / b.getFloat()) * 100;
      iMemPctUsed = BFloat.make(memPctUsed).getInt();
    }

    //String mem = iMemPctUsed.toString().getBytes();
    String mem = iMemPctUsed.toString();

    systemDetailsJson.put("CPU Usage", cpuUsage);
    systemDetailsJson.put("Memory Usage", memUsage);
    systemDetailsJson.put("Memory Toal Usage", memTotalUsage);
    systemDetailsJson.put("Memory Usage Percentage", mem);

    // Get all th slots of the mqtt driver and output as topic/value pairs into points tree
//    Property[] aryProps = this.getFrozenPropertiesArray();
//    if (aryProps.length > 0) {
//      for (int i = 0; i < aryProps.length; i++) {
//        Property prop = aryProps[i];
//        String propName = SlotPath.unescape(prop.getName());
//        BValue bV = this.get(prop);
//        String text = bV.toDataValue().toString();
////        mqttMsg = new MqttMessage();
////        mqttMsg.setPayload(text.getBytes());
////        m_MqttSubConn.injectMessage(topicPrefix + "/" + propName + "/", mqttMsg);
//
//        driverDetailsJson.put(propName, text);
//      }
    //}

    // hashTopicsCovJson.put(publishTopic, jO.toString());
    //System.out.println("MQTT Details: "+mqttDetails);
      mqttDetailsJson.put("Host Details", hostDetailsJson);
      mqttDetailsJson.put("Ssytem Details", systemDetailsJson);
      //mqttDetailsJson.put("Device Details", driverDetailsJson);

      return mqttDetailsJson;
  }

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
    private String removeNonPrintables(String value) {
      //return value.replaceAll("[^\\x0A\\x0D\\x20-\\x7E]", "").trim();
      String newString = value.replaceAll("[^\\p{Print}]", "");   // Remove non printables
      return newString.replaceAll("\\p{Cntrl}", "").trim();       // Remove control codes
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
}