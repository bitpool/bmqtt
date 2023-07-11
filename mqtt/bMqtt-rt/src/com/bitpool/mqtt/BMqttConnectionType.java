package com.bitpool.mqtt;

import javax.baja.nre.annotations.NiagaraEnum;
import javax.baja.nre.annotations.NiagaraType;
import javax.baja.sys.BFrozenEnum;
import javax.baja.sys.Sys;
import javax.baja.sys.Type;

@NiagaraType
@NiagaraEnum(
        range={
                @javax.baja.nre.annotations.Range("Anonymous"),
                @javax.baja.nre.annotations.Range("UsingCredentials"),
        }
)
public final class BMqttConnectionType extends BFrozenEnum {
/*+ ------------ BEGIN BAJA AUTO GENERATED CODE ------------ +*/
/*@ $com.bitpool.mqtt.BMqttConnectionType(1105264539)1.0$ @*/
/* Generated Wed Feb 27 11:12:21 AEST 2019 by Slot-o-Matic (c) Tridium, Inc. 2012 */
  
  /** Ordinal value for Anonymous. */
  public static final int ANONYMOUS = 1;
  /** Ordinal value for UsingCredentials. */
  public static final int USING_CREDENTIALS = 2;
  
  /** BMqttConnectionType constant for Anonymous. */
  public static final BMqttConnectionType Anonymous = new BMqttConnectionType(ANONYMOUS);
  /** BMqttConnectionType constant for UsingCredentials. */
  public static final BMqttConnectionType UsingCredentials = new BMqttConnectionType(USING_CREDENTIALS);
  
  /** Factory method with ordinal. */
  public static BMqttConnectionType make(int ordinal)
  {
    return (BMqttConnectionType)Anonymous.getRange().get(ordinal, false);
  }
  
  /** Factory method with tag. */
  public static BMqttConnectionType make(String tag)
  {
    return (BMqttConnectionType)Anonymous.getRange().get(tag);
  }
  
  /** Private constructor. */
  private BMqttConnectionType(int ordinal)
  {
    super(ordinal);
  }
  
  public static final BMqttConnectionType DEFAULT = Anonymous;

////////////////////////////////////////////////////////////////
// Type
////////////////////////////////////////////////////////////////
  
  @Override
  public Type getType() { return TYPE; }
  public static final Type TYPE = Sys.loadType(BMqttConnectionType.class);

/*+ ------------ END BAJA AUTO GENERATED CODE -------------- +*/
}


