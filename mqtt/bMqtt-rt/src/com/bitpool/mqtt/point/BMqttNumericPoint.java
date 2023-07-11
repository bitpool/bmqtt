package com.bitpool.mqtt.point;

import javax.baja.control.BNumericPoint;
import javax.baja.nre.annotations.NiagaraType;
import javax.baja.sys.Sys;
import javax.baja.sys.Type;

@NiagaraType
public class BMqttNumericPoint extends BNumericPoint {
/*+ ------------ BEGIN BAJA AUTO GENERATED CODE ------------ +*/
/*@ $com.bitpool.mqtt.point.BMqttNumericPoint(2979906276)1.0$ @*/
/* Generated Wed Apr 17 14:45:40 AEST 2019 by Slot-o-Matic (c) Tridium, Inc. 2012 */

////////////////////////////////////////////////////////////////
// Type
////////////////////////////////////////////////////////////////
  
  @Override
  public Type getType() { return TYPE; }
  public static final Type TYPE = Sys.loadType(BMqttNumericPoint.class);

/*+ ------------ END BAJA AUTO GENERATED CODE -------------- +*/
    @Override
    public void started() throws Exception {
        super.started();
    }
}
