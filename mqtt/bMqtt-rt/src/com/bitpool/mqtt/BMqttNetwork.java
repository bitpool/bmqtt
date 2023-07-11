/**
 * Copyright 2019 bitpool, All Rights Reserved.
 */
package com.bitpool.mqtt;

import com.tridium.ndriver.BNNetwork;
import com.tridium.ndriver.poll.BNPollScheduler;

import javax.baja.driver.BDevice;
import javax.baja.driver.point.BTuningPolicy;
import javax.baja.nre.annotations.Facet;
import javax.baja.nre.annotations.NiagaraAction;
import javax.baja.nre.annotations.NiagaraProperty;
import javax.baja.nre.annotations.NiagaraType;
import javax.baja.sys.*;
import javax.baja.util.Lexicon;

/**
 *  BMqttNetwork models a network of devices
 *
 *  @author   Admin
 *  @creation 25-Feb-19
 */
@NiagaraAction(name = "Enable")
@NiagaraAction(name = "Disable")
@NiagaraType
@NiagaraProperty(name = "pollScheduler", type = "BNPollScheduler", defaultValue = "new BNPollScheduler()")

@NiagaraProperty(name = "driverVersion", type = "BString", defaultValue = "", facets = {@Facet(name = "BFacets.FIELD_WIDTH", value = "10")}, flags = Flags.READONLY)
@NiagaraProperty(name = "productKey", type = "BString", defaultValue = "", facets = {@Facet(name = "BFacets.FIELD_WIDTH", value = "100")}, flags = Flags.READONLY)


public class BMqttNetwork
        extends BNNetwork {

    private final static String MODULE_NAME                 = "MQTT";

    
/*+ ------------ BEGIN BAJA AUTO GENERATED CODE ------------ +*/
/*@ $com.bitpool.mqtt.BMqttNetwork(1592957483)1.0$ @*/
/* Generated Tue Jul 16 15:34:14 AEST 2019 by Slot-o-Matic (c) Tridium, Inc. 2012 */

////////////////////////////////////////////////////////////////
// Property "pollScheduler"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code pollScheduler} property.
   * @see #getPollScheduler
   * @see #setPollScheduler
   */
  public static final Property pollScheduler = newProperty(0, new BNPollScheduler(), null);
  
  /**
   * Get the {@code pollScheduler} property.
   * @see #pollScheduler
   */
  public BNPollScheduler getPollScheduler() { return (BNPollScheduler)get(pollScheduler); }
  
  /**
   * Set the {@code pollScheduler} property.
   * @see #pollScheduler
   */
  public void setPollScheduler(BNPollScheduler v) { set(pollScheduler, v, null); }

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
// Property "productKey"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code productKey} property.
   * @see #getProductKey
   * @see #setProductKey
   */
  public static final Property productKey = newProperty(Flags.READONLY, "", BFacets.make(BFacets.FIELD_WIDTH, 100));

  /**
   * Get the {@code productKey} property.
   * @see #productKey
   */
  public String getProductKey() { return getString(productKey); }

  /**
   * Set the {@code productKey} property.
   * @see #productKey
   */
  public void setProductKey(String v) { setString(productKey, v, null); }

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
// Type
////////////////////////////////////////////////////////////////
  
  @Override
  public Type getType() { return TYPE; }
  public static final Type TYPE = Sys.loadType(BMqttNetwork.class);

/*+ ------------ END BAJA AUTO GENERATED CODE -------------- +*/

    public String getNetworkName() {
        return "MqttNetwork";
    }

    @Override
    public Type getDeviceFolderType() {
        return BMqttDeviceFolder.TYPE;
    }

    @Override
    public Type getDeviceType() {
        return BMqttPubDevice.TYPE;
    }

    public void doEnable() {
        this.setEnabled(true);
    }
    public void doDisable() {
        this.setEnabled(false);
    }

    @Override
    public void started() throws Exception {
        super.started();
        setDriverVersion(getDrvVersion());
        String hostId = Sys.getHostId();
        String prodKey = hostId + ":" + MODULE_NAME;
        setProductKey(prodKey.toUpperCase());

        // Clear out just' the default tuning. A user can override by creating a new policy
        // Reason: Pub/Sub device will receive a 'write' to point ext.
        //  - will send an MQTT message
        //  - will cause overhead on each event type (start, up, enable)
        //  - may not be favourable on large sites
        BTuningPolicy bTP =  this.getTuningPolicies().getDefaultPolicy();
        bTP.setWriteOnStart(false);
        bTP.setWriteOnUp(false);
        bTP.setWriteOnEnabled(false);
        this.getTuningPolicies().setDefaultPolicy(bTP);

    }
    @Override
    public void changed(Property p, Context cx) {
        super.changed(p, cx);
        if(!isRunning()) return;
        if(p == enabled) {
            Boolean doEnableDevice;
            doEnableDevice = !isDisabled();
            BComponent[] devices = getChildComponents();
            for(int i = 0; i < devices.length; i++) {
                if(devices[i].getType() == BMqttPubDevice.TYPE) {
                    ((BMqttPubDevice) devices[i]).setEnabled(doEnableDevice);
                } else if(devices[i].getType() == BMqttSubDevice.TYPE) {
                    ((BMqttSubDevice) devices[i]).setEnabled(doEnableDevice);
                }
            }
        }
        // Check the user has entered a valid key
            setEnabled(true);
            devicesRestartEnabledOnly();
    }

    private void devicesRestartEnabledOnly(){
        BDevice[] lstDevices =  getDevices();
        for(BDevice device: lstDevices){
            if(device.getEnabled()){
                device.setEnabled(false);
                device.setEnabled(true);
            }
        }
    }
    private String getDrvVersion(){
        return BuildVersion.VERSION_NUMBER + "." + BuildVersion.FEATURE_NUMBER + "." + BuildVersion.BUILD_NUMBER;
    }
    private void devicesUpdateStatusMessage(String statusMessage){
        BDevice[] lstDevices =  getDevices();
        for(BDevice device: lstDevices){
            if(device.getEnabled()){
                if(device.getType() == BMqttSubDevice.TYPE){
                    ((BMqttSubDevice)device).setDriverStatus(statusMessage);
                }
                if(device.getType() == BMqttPubDevice.TYPE){
                    ((BMqttPubDevice)device).setDriverStatus(statusMessage);
                }
            }
        }
    }

    public static Lexicon LEX = Lexicon.make(BMqttNetwork.class);
}