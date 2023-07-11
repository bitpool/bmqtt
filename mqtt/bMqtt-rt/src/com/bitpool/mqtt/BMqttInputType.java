package com.bitpool.mqtt;
import javax.baja.nre.annotations.NiagaraEnum;
import javax.baja.nre.annotations.NiagaraType;
import javax.baja.sys.BFrozenEnum;
import javax.baja.sys.Sys;
import javax.baja.sys.Type;

@NiagaraType
@NiagaraEnum(
        range={
                @javax.baja.nre.annotations.Range("SubscribeAllChangeOfValues"),
                @javax.baja.nre.annotations.Range("SubscribeOnlyLastChangeOfValue"),
        }
)
public final class BMqttInputType  extends BFrozenEnum {
    
/*+ ------------ BEGIN BAJA AUTO GENERATED CODE ------------ +*/
/*@ $com.bitpool.mqtt.BMqttInputType(3041811415)1.0$ @*/
/* Generated Fri May 10 14:40:28 AEST 2019 by Slot-o-Matic (c) Tridium, Inc. 2012 */
  
  /** Ordinal value for SubscribeAllChangeOfValues. */
  public static final int SUBSCRIBE_ALL_CHANGE_OF_VALUES = 0;
  /** Ordinal value for SubscribeOnlyLastChangeOfValue. */
  public static final int SUBSCRIBE_ONLY_LAST_CHANGE_OF_VALUE = 1;
  
  /** BMqttInputType constant for SubscribeAllChangeOfValues. */
  public static final BMqttInputType SubscribeAllChangeOfValues = new BMqttInputType(SUBSCRIBE_ALL_CHANGE_OF_VALUES);
  /** BMqttInputType constant for SubscribeOnlyLastChangeOfValue. */
  public static final BMqttInputType SubscribeOnlyLastChangeOfValue = new BMqttInputType(SUBSCRIBE_ONLY_LAST_CHANGE_OF_VALUE);
  
  /** Factory method with ordinal. */
  public static BMqttInputType make(int ordinal)
  {
    return (BMqttInputType)SubscribeAllChangeOfValues.getRange().get(ordinal, false);
  }
  
  /** Factory method with tag. */
  public static BMqttInputType make(String tag)
  {
    return (BMqttInputType)SubscribeAllChangeOfValues.getRange().get(tag);
  }
  
  /** Private constructor. */
  private BMqttInputType(int ordinal)
  {
    super(ordinal);
  }
  
  public static final BMqttInputType DEFAULT = SubscribeAllChangeOfValues;

////////////////////////////////////////////////////////////////
// Type
////////////////////////////////////////////////////////////////
  
  @Override
  public Type getType() { return TYPE; }
  public static final Type TYPE = Sys.loadType(BMqttInputType.class);

/*+ ------------ END BAJA AUTO GENERATED CODE -------------- +*/
}
