package com.bitpool.mqtt;
import javax.baja.nre.annotations.NiagaraEnum;
import javax.baja.nre.annotations.NiagaraType;
import javax.baja.sys.BFrozenEnum;
import javax.baja.sys.Sys;
import javax.baja.sys.Type;

@NiagaraType
@NiagaraEnum(
        range={
                @javax.baja.nre.annotations.Range("PublishAllValues"),
                @javax.baja.nre.annotations.Range("PublishOnlyChangedValues"),
        }
)
public final class BMqttOutputType  extends BFrozenEnum {
/*+ ------------ BEGIN BAJA AUTO GENERATED CODE ------------ +*/
/*@ $com.bitpool.mqtt.BMqttOutputType(3410667224)1.0$ @*/
/* Generated Thu Apr 11 10:23:44 AEST 2019 by Slot-o-Matic (c) Tridium, Inc. 2012 */
  
  /** Ordinal value for PublishAllValues. */
  public static final int PUBLISH_ALL_VALUES = 0;
  /** Ordinal value for PublishOnlyChangedValues. */
  public static final int PUBLISH_ONLY_CHANGED_VALUES = 1;
  
  /** BMqttOutputType constant for PublishAllValues. */
  public static final BMqttOutputType PublishAllValues = new BMqttOutputType(PUBLISH_ALL_VALUES);
  /** BMqttOutputType constant for PublishOnlyChangedValues. */
  public static final BMqttOutputType PublishOnlyChangedValues = new BMqttOutputType(PUBLISH_ONLY_CHANGED_VALUES);
  
  /** Factory method with ordinal. */
  public static BMqttOutputType make(int ordinal)
  {
    return (BMqttOutputType)PublishAllValues.getRange().get(ordinal, false);
  }
  
  /** Factory method with tag. */
  public static BMqttOutputType make(String tag)
  {
    return (BMqttOutputType)PublishAllValues.getRange().get(tag);
  }
  
  /** Private constructor. */
  private BMqttOutputType(int ordinal)
  {
    super(ordinal);
  }
  
  public static final BMqttOutputType DEFAULT = PublishAllValues;

////////////////////////////////////////////////////////////////
// Type
////////////////////////////////////////////////////////////////
  
  @Override
  public Type getType() { return TYPE; }
  public static final Type TYPE = Sys.loadType(BMqttOutputType.class);

/*+ ------------ END BAJA AUTO GENERATED CODE -------------- +*/
}
