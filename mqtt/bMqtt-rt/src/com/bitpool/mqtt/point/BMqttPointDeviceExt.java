/**
 * Copyright 2019 bitpool, All Rights Reserved.
 */
package com.bitpool.mqtt.point;

import com.bitpool.mqtt.BMqttNetwork;
import com.bitpool.mqtt.BMqttSubDevice;
import com.tridium.ndriver.discover.BINDiscoveryObject;
import com.tridium.ndriver.discover.BNDiscoveryPreferences;
import com.tridium.ndriver.point.BNPointDeviceExt;

import javax.baja.nre.annotations.NiagaraProperty;
import javax.baja.nre.annotations.NiagaraType;
import javax.baja.sys.Property;
import javax.baja.sys.Sys;
import javax.baja.sys.Type;

/**
 * BMqttPointDeviceExt is a container for mqtt proxy points.
 *
 * @author   Admin
 * @creation 25-Feb-19 
 */
@NiagaraType
@NiagaraProperty(
        name = "discoveryPreferences",
        type = "BMqttPointDiscoveryPreferences",
        defaultValue = "new BMqttPointDiscoveryPreferences()",
        override = false
)

public class BMqttPointDeviceExt
  extends BNPointDeviceExt
{            

/*+ ------------ BEGIN BAJA AUTO GENERATED CODE ------------ +*/
/*@ $com.bitpool.mqtt.point.BMqttPointDeviceExt(1191135238)1.0$ @*/
/* Generated Fri May 29 14:34:03 AEST 2020 by Slot-o-Matic (c) Tridium, Inc. 2012 */

////////////////////////////////////////////////////////////////
// Property "discoveryPreferences"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code discoveryPreferences} property.
   * @see #getDiscoveryPreferences
   * @see #setDiscoveryPreferences
   */
  public static final Property discoveryPreferences = newProperty(0, new BMqttPointDiscoveryPreferences(), null);
  
  /**
   * Get the {@code discoveryPreferences} property.
   * @see #discoveryPreferences
   */
  public BMqttPointDiscoveryPreferences getDiscoveryPreferences() { return (BMqttPointDiscoveryPreferences)get(discoveryPreferences); }
  
  /**
   * Set the {@code discoveryPreferences} property.
   * @see #discoveryPreferences
   */
  public void setDiscoveryPreferences(BMqttPointDiscoveryPreferences v) { set(discoveryPreferences, v, null); }

////////////////////////////////////////////////////////////////
// Type
////////////////////////////////////////////////////////////////
  
  @Override
  public Type getType() { return TYPE; }
  public static final Type TYPE = Sys.loadType(BMqttPointDeviceExt.class);

/*+ ------------ END BAJA AUTO GENERATED CODE -------------- +*/



////////////////////////////////////////////////////////////////
// Access
////////////////////////////////////////////////////////////////

  /**
   * Get the network cast to a BMqttNetwork.
   * @return network as a BMqttNetwork.
   */
  public final BMqttNetwork getMqttNetwork()
  {
    return (BMqttNetwork)getNetwork();
  }

  /**
   * Get the device cast to a BMqttSubDevice.
   * @return device as a BMqttSubDevice.
   */
  public final BMqttSubDevice getMqttSubDevice()
  {
    return (BMqttSubDevice)getDevice();
  }

////////////////////////////////////////////////////////////////
// PointDeviceExt
////////////////////////////////////////////////////////////////
  
  /**
   * @return the Device type.
   */
  public Type getDeviceType()
  {
    return BMqttSubDevice.TYPE;
  }

  /**
   * @return the PointFolder type.
   */
  public Type getPointFolderType()
  {
    return BMqttPointFolder.TYPE;
  }
  
  /**
   * @return the ProxyExt type.
   */
  public Type getProxyExtType()
  {
    return BMqttProxyExt.TYPE;
  }
  

////////////////////////////////////////////////////////////////
//BINDiscoveryHost
////////////////////////////////////////////////////////////////

  /** Call back for discoveryJob to get an array of discovery objects.
   *  Override point for driver specific discovery. */
  public BINDiscoveryObject[] getDiscoveryObjects(BNDiscoveryPreferences prefs)throws Exception{
      return null;
  }

}
