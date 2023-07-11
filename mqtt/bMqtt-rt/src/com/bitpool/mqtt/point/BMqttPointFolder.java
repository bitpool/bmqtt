/**
 * Copyright 2019 bitpool, All Rights Reserved.
 */
package com.bitpool.mqtt.point;

import com.bitpool.mqtt.BMqttNetwork;
import com.bitpool.mqtt.BMqttPubDevice;
import com.tridium.ndriver.point.BNPointFolder;

import javax.baja.nre.annotations.NiagaraType;
import javax.baja.sys.Sys;
import javax.baja.sys.Type;

/**
 * BMqttPointFolder
 *
 * @author   Admin
 * @creation 25-Feb-19  
 */
@NiagaraType
public class BMqttPointFolder
  extends BNPointFolder
{            

/*+ ------------ BEGIN BAJA AUTO GENERATED CODE ------------ +*/
/*@ $com.bitpool.mqtt.point.BMqttPointFolder(2979906276)1.0$ @*/
/* Generated Mon Feb 25 16:23:58 AEST 2019 by Slot-o-Matic (c) Tridium, Inc. 2012 */

////////////////////////////////////////////////////////////////
// Type
////////////////////////////////////////////////////////////////
  
  @Override
  public Type getType() { return TYPE; }
  public static final Type TYPE = Sys.loadType(BMqttPointFolder.class);

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
   * Get the device cast to a BMqttPubDevice.
   * @return device as a BMqttPubDevice.
   */
  public final BMqttPubDevice getBMqttPubDevice()
  {
    return (BMqttPubDevice)getDevice();
  }


}
