package com.bitpool.mqtt.point;

import javax.baja.control.BStringPoint;
import javax.baja.nre.annotations.NiagaraType;
import javax.baja.sys.*;

@NiagaraType
public class BMqttStringPoint extends BStringPoint{

/*+ ------------ BEGIN BAJA AUTO GENERATED CODE ------------ +*/
/*@ $com.bitpool.mqtt.point.BMqttStringPoint(2979906276)1.0$ @*/
/* Generated Mon Apr 15 17:18:17 AEST 2019 by Slot-o-Matic (c) Tridium, Inc. 2012 */

////////////////////////////////////////////////////////////////
// Type
////////////////////////////////////////////////////////////////
  
  @Override
  public Type getType() { return TYPE; }
  public static final Type TYPE = Sys.loadType(BMqttStringPoint.class);

/*+ ------------ END BAJA AUTO GENERATED CODE -------------- +*/

    @Override
    public void started() throws Exception {
        super.started();
    }
}
