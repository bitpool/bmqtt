package com.bitpool.mqtt;

import javax.baja.sys.Sys;
import javax.baja.sys.Type;
import javax.baja.nre.annotations.NiagaraEnum;
import javax.baja.nre.annotations.NiagaraType;
import javax.baja.nre.annotations.Range;
import javax.baja.sys.BFrozenEnum;


@NiagaraType
@NiagaraEnum(
        range = {
                @Range(value="Value", ordinal=1),
                @Range(value="Json", ordinal=2),
                @Range(value="JsonSimple", ordinal=3)
        },
        defaultValue = "Value"
)
public final class BMqttOutputFormat extends BFrozenEnum{
/*+ ------------ BEGIN BAJA AUTO GENERATED CODE ------------ +*/
/*@ $com.bitpool.mqtt.BMqttOutputFormat(267752857)1.0$ @*/
/* Generated Fri Jul 12 11:49:18 AEST 2019 by Slot-o-Matic (c) Tridium, Inc. 2012 */
  
  /** Ordinal value for Value. */
  public static final int VALUE = 1;
  /** Ordinal value for Json. */
  public static final int JSON = 2;
  /** Ordinal value for JsonSimple. */
  public static final int JSON_SIMPLE = 3;
  
  /** BMqttOutputFormat constant for Value. */
  public static final BMqttOutputFormat Value = new BMqttOutputFormat(VALUE);
  /** BMqttOutputFormat constant for Json. */
  public static final BMqttOutputFormat Json = new BMqttOutputFormat(JSON);
  /** BMqttOutputFormat constant for JsonSimple. */
  public static final BMqttOutputFormat JsonSimple = new BMqttOutputFormat(JSON_SIMPLE);
  
  /** Factory method with ordinal. */
  public static BMqttOutputFormat make(int ordinal)
  {
    return (BMqttOutputFormat)Value.getRange().get(ordinal, false);
  }
  
  /** Factory method with tag. */
  public static BMqttOutputFormat make(String tag)
  {
    return (BMqttOutputFormat)Value.getRange().get(tag);
  }
  
  /** Private constructor. */
  private BMqttOutputFormat(int ordinal)
  {
    super(ordinal);
  }
  
  public static final BMqttOutputFormat DEFAULT = Value;

////////////////////////////////////////////////////////////////
// Type
////////////////////////////////////////////////////////////////
  
  @Override
  public Type getType() { return TYPE; }
  public static final Type TYPE = Sys.loadType(BMqttOutputFormat.class);

/*+ ------------ END BAJA AUTO GENERATED CODE -------------- +*/
}
