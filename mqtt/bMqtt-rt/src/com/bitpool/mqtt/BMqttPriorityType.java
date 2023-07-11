package com.bitpool.mqtt;
import javax.baja.nre.annotations.NiagaraEnum;
import javax.baja.nre.annotations.NiagaraType;
import javax.baja.sys.BFrozenEnum;
import javax.baja.sys.Sys;
import javax.baja.sys.Type;

@NiagaraType
@NiagaraEnum(
        range={
                @javax.baja.nre.annotations.Range("In1EO"),
                @javax.baja.nre.annotations.Range("In2"),
                @javax.baja.nre.annotations.Range("In3"),
                @javax.baja.nre.annotations.Range("In4"),
                @javax.baja.nre.annotations.Range("In5"),
                @javax.baja.nre.annotations.Range("In6"),
                @javax.baja.nre.annotations.Range("In7"),
                @javax.baja.nre.annotations.Range("In8MO"),
                @javax.baja.nre.annotations.Range("In9"),
                @javax.baja.nre.annotations.Range("In10"),
                @javax.baja.nre.annotations.Range("In11"),
                @javax.baja.nre.annotations.Range("In12"),
                @javax.baja.nre.annotations.Range("In13"),
                @javax.baja.nre.annotations.Range("In14"),
                @javax.baja.nre.annotations.Range("In15"),
                @javax.baja.nre.annotations.Range("In16"),
                @javax.baja.nre.annotations.Range("Fallback"),
                @javax.baja.nre.annotations.Range("Disabled"),
        }
)
public final class BMqttPriorityType extends BFrozenEnum {
/*+ ------------ BEGIN BAJA AUTO GENERATED CODE ------------ +*/
/*@ $com.bitpool.mqtt.BMqttPriorityType(2107109527)1.0$ @*/
/* Generated Sat Jan 16 14:09:37 AEST 2021 by Slot-o-Matic (c) Tridium, Inc. 2012 */
  
  /** Ordinal value for In1EO. */
  public static final int IN_1EO = 0;
  /** Ordinal value for In2. */
  public static final int IN_2 = 1;
  /** Ordinal value for In3. */
  public static final int IN_3 = 2;
  /** Ordinal value for In4. */
  public static final int IN_4 = 3;
  /** Ordinal value for In5. */
  public static final int IN_5 = 4;
  /** Ordinal value for In6. */
  public static final int IN_6 = 5;
  /** Ordinal value for In7. */
  public static final int IN_7 = 6;
  /** Ordinal value for In8MO. */
  public static final int IN_8MO = 7;
  /** Ordinal value for In9. */
  public static final int IN_9 = 8;
  /** Ordinal value for In10. */
  public static final int IN_10 = 9;
  /** Ordinal value for In11. */
  public static final int IN_11 = 10;
  /** Ordinal value for In12. */
  public static final int IN_12 = 11;
  /** Ordinal value for In13. */
  public static final int IN_13 = 12;
  /** Ordinal value for In14. */
  public static final int IN_14 = 13;
  /** Ordinal value for In15. */
  public static final int IN_15 = 14;
  /** Ordinal value for In16. */
  public static final int IN_16 = 15;
  /** Ordinal value for Fallback. */
  public static final int FALLBACK = 16;
  /** Ordinal value for Disabled. */
  public static final int DISABLED = 17;
  
  /** BMqttPriorityType constant for In1EO. */
  public static final BMqttPriorityType In1EO = new BMqttPriorityType(IN_1EO);
  /** BMqttPriorityType constant for In2. */
  public static final BMqttPriorityType In2 = new BMqttPriorityType(IN_2);
  /** BMqttPriorityType constant for In3. */
  public static final BMqttPriorityType In3 = new BMqttPriorityType(IN_3);
  /** BMqttPriorityType constant for In4. */
  public static final BMqttPriorityType In4 = new BMqttPriorityType(IN_4);
  /** BMqttPriorityType constant for In5. */
  public static final BMqttPriorityType In5 = new BMqttPriorityType(IN_5);
  /** BMqttPriorityType constant for In6. */
  public static final BMqttPriorityType In6 = new BMqttPriorityType(IN_6);
  /** BMqttPriorityType constant for In7. */
  public static final BMqttPriorityType In7 = new BMqttPriorityType(IN_7);
  /** BMqttPriorityType constant for In8MO. */
  public static final BMqttPriorityType In8MO = new BMqttPriorityType(IN_8MO);
  /** BMqttPriorityType constant for In9. */
  public static final BMqttPriorityType In9 = new BMqttPriorityType(IN_9);
  /** BMqttPriorityType constant for In10. */
  public static final BMqttPriorityType In10 = new BMqttPriorityType(IN_10);
  /** BMqttPriorityType constant for In11. */
  public static final BMqttPriorityType In11 = new BMqttPriorityType(IN_11);
  /** BMqttPriorityType constant for In12. */
  public static final BMqttPriorityType In12 = new BMqttPriorityType(IN_12);
  /** BMqttPriorityType constant for In13. */
  public static final BMqttPriorityType In13 = new BMqttPriorityType(IN_13);
  /** BMqttPriorityType constant for In14. */
  public static final BMqttPriorityType In14 = new BMqttPriorityType(IN_14);
  /** BMqttPriorityType constant for In15. */
  public static final BMqttPriorityType In15 = new BMqttPriorityType(IN_15);
  /** BMqttPriorityType constant for In16. */
  public static final BMqttPriorityType In16 = new BMqttPriorityType(IN_16);
  /** BMqttPriorityType constant for Fallback. */
  public static final BMqttPriorityType Fallback = new BMqttPriorityType(FALLBACK);
  /** BMqttPriorityType constant for Disabled. */
  public static final BMqttPriorityType Disabled = new BMqttPriorityType(DISABLED);
  
  /** Factory method with ordinal. */
  public static BMqttPriorityType make(int ordinal)
  {
    return (BMqttPriorityType)In1EO.getRange().get(ordinal, false);
  }
  
  /** Factory method with tag. */
  public static BMqttPriorityType make(String tag)
  {
    return (BMqttPriorityType)In1EO.getRange().get(tag);
  }
  
  /** Private constructor. */
  private BMqttPriorityType(int ordinal)
  {
    super(ordinal);
  }
  
  public static final BMqttPriorityType DEFAULT = In1EO;

////////////////////////////////////////////////////////////////
// Type
////////////////////////////////////////////////////////////////
  
  @Override
  public Type getType() { return TYPE; }
  public static final Type TYPE = Sys.loadType(BMqttPriorityType.class);

/*+ ------------ END BAJA AUTO GENERATED CODE -------------- +*/


}