/*
 * Copyright 2019 bitpool, All Rights Reserved.
 */
package com.bitpool.mqtt.point;

import javax.baja.sys.*;
import javax.baja.nre.annotations.*;

import com.tridium.ndriver.discover.*;

@NiagaraType
public class BMqttPointDiscoveryPreferences
  extends BNDiscoveryPreferences
{

/*+ ------------ BEGIN BAJA AUTO GENERATED CODE ------------ +*/
/*@ $com.bitpool.mqtt.point.BMqttPointDiscoveryPreferences(2979906276)1.0$ @*/
/* Generated Mon Feb 25 16:23:58 AEST 2019 by Slot-o-Matic (c) Tridium, Inc. 2012 */

////////////////////////////////////////////////////////////////
// Type
////////////////////////////////////////////////////////////////
  
  @Override
  public Type getType() { return TYPE; }
  public static final Type TYPE = Sys.loadType(BMqttPointDiscoveryPreferences.class);

/*+ ------------ END BAJA AUTO GENERATED CODE -------------- +*/
  
  public Type getDiscoveryLeafType()
  {
    return BMqttPointDiscoveryLeaf.TYPE;
  }

}
