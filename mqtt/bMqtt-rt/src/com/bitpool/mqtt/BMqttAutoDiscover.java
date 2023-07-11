package com.bitpool.mqtt;

import javax.baja.nre.annotations.NiagaraEnum;
import javax.baja.nre.annotations.NiagaraType;
import javax.baja.sys.BFrozenEnum;
import javax.baja.sys.Sys;
import javax.baja.sys.Type;

@NiagaraType
@NiagaraEnum(
        range={
                @javax.baja.nre.annotations.Range("YesAddNewDiscoveredTopics"),
                @javax.baja.nre.annotations.Range("NoDisableAddingNewTopics"),
        }
)

public final class BMqttAutoDiscover  extends BFrozenEnum {
/*+ ------------ BEGIN BAJA AUTO GENERATED CODE ------------ +*/
/*@ $com.bitpool.mqtt.BMqttAutoDiscover(1633118101)1.0$ @*/
/* Generated Thu Apr 11 07:42:32 AEST 2019 by Slot-o-Matic (c) Tridium, Inc. 2012 */
  
  /** Ordinal value for YesAddNewDiscoveredTopics. */
  public static final int YES_ADD_NEW_DISCOVERED_TOPICS = 0;
  /** Ordinal value for NoDisableAddingNewTopics. */
  public static final int NO_DISABLE_ADDING_NEW_TOPICS = 1;
  
  /** BMqttAutoDiscover constant for YesAddNewDiscoveredTopics. */
  public static final BMqttAutoDiscover YesAddNewDiscoveredTopics = new BMqttAutoDiscover(YES_ADD_NEW_DISCOVERED_TOPICS);
  /** BMqttAutoDiscover constant for NoDisableAddingNewTopics. */
  public static final BMqttAutoDiscover NoDisableAddingNewTopics = new BMqttAutoDiscover(NO_DISABLE_ADDING_NEW_TOPICS);
  
  /** Factory method with ordinal. */
  public static BMqttAutoDiscover make(int ordinal)
  {
    return (BMqttAutoDiscover)YesAddNewDiscoveredTopics.getRange().get(ordinal, false);
  }
  
  /** Factory method with tag. */
  public static BMqttAutoDiscover make(String tag)
  {
    return (BMqttAutoDiscover)YesAddNewDiscoveredTopics.getRange().get(tag);
  }
  
  /** Private constructor. */
  private BMqttAutoDiscover(int ordinal)
  {
    super(ordinal);
  }
  
  public static final BMqttAutoDiscover DEFAULT = YesAddNewDiscoveredTopics;

////////////////////////////////////////////////////////////////
// Type
////////////////////////////////////////////////////////////////
  
  @Override
  public Type getType() { return TYPE; }
  public static final Type TYPE = Sys.loadType(BMqttAutoDiscover.class);

/*+ ------------ END BAJA AUTO GENERATED CODE -------------- +*/
}
