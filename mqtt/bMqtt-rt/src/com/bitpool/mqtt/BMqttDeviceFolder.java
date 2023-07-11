/*
 * Copyright 2019 bitpool, All Rights Reserved.
 */
package com.bitpool.mqtt;

import com.tridium.ndriver.BNDeviceFolder;

import javax.baja.nre.annotations.NiagaraType;
import javax.baja.sys.BComponent;
import javax.baja.sys.Sys;
import javax.baja.sys.Type;

/**
 * BMqttDeviceFolder is a folder for BMqttPubDevice.
 *
 *  @author   Admin
 *  @creation 25-Feb-19 
 */
@NiagaraType
public class BMqttDeviceFolder
  extends BNDeviceFolder
{                       

/*+ ------------ BEGIN BAJA AUTO GENERATED CODE ------------ +*/
/*@ $com.bitpool.mqtt.BMqttDeviceFolder(2979906276)1.0$ @*/
/* Generated Mon Feb 25 16:23:57 AEST 2019 by Slot-o-Matic (c) Tridium, Inc. 2012 */

////////////////////////////////////////////////////////////////
// Type
////////////////////////////////////////////////////////////////
  
  @Override
  public Type getType() { return TYPE; }
  public static final Type TYPE = Sys.loadType(BMqttDeviceFolder.class);

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
   * @return true if parent is BMqttNetwork or BMqttDeviceFolder.
   */
  public boolean isParentLegal(BComponent parent)
  {
    return parent instanceof BMqttNetwork ||
           parent instanceof BMqttDeviceFolder;
  }


}
