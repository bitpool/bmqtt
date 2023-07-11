package com.bitpool.mqtt.point;

import com.bitpool.mqtt.BMqttPriorityType;
import com.bitpool.mqtt.BMqttSubDevice;

import javax.baja.control.BEnumWritable;
import javax.baja.control.util.BEnumOverride;
import javax.baja.nre.annotations.NiagaraType;
import javax.baja.status.BStatusEnum;
import javax.baja.sys.BAbsTime;
import javax.baja.sys.BDynamicEnum;
import javax.baja.sys.BEnum;
import javax.baja.sys.BEnumRange;
import javax.baja.sys.BRelTime;
import javax.baja.sys.Clock;
import javax.baja.sys.Sys;
import javax.baja.sys.Type;

@NiagaraType
public class BMqttEnumWritable extends BEnumWritable {
    private boolean m_SuppressUserPublishedEcho = false;
/*+ ------------ BEGIN BAJA AUTO GENERATED CODE ------------ +*/
/*@ $com.bitpool.mqtt.point.BMqttEnumWritable(2979906276)1.0$ @*/
/* Generated Tue May 21 11:40:48 AEST 2019 by Slot-o-Matic (c) Tridium, Inc. 2012 */

////////////////////////////////////////////////////////////////
// Type
////////////////////////////////////////////////////////////////
  
  @Override
  public Type getType() { return TYPE; }
  public static final Type TYPE = Sys.loadType(BMqttEnumWritable.class);

/*+ ------------ END BAJA AUTO GENERATED CODE -------------- +*/
    public void setEchoSuppression(Boolean isSuppressed){
        m_SuppressUserPublishedEcho = isSuppressed;
    }
    public Boolean getEchoSuppression(){
        return m_SuppressUserPublishedEcho;
    }
    public void setMqttValue(BEnum value, BEnumRange eRange,  Integer priorityLevel) {

        if(this.getEchoSuppression()){
            this.setEchoSuppression(false);
            return;
        }

        // Make sure the ProxyExt does not publish out the mqtt sub message you just received
        BMqttProxyExt mPE = (BMqttProxyExt) this.getProxyExt();
        mPE.setMqttPubAllowed(false);

        Integer mP =  priorityLevel;
        if(mP ==      BMqttPriorityType.IN_1EO){this.setIn1(new BStatusEnum(value));}
        else if(mP == BMqttPriorityType.IN_2){this.setIn2(new BStatusEnum(value));}
        else if(mP == BMqttPriorityType.IN_3){this.setIn3(new BStatusEnum(value));}
        else if(mP == BMqttPriorityType.IN_4){this.setIn4(new BStatusEnum(value));}
        else if(mP == BMqttPriorityType.IN_5){this.setIn5(new BStatusEnum(value));}
        else if(mP == BMqttPriorityType.IN_6){this.setIn6(new BStatusEnum(value));}
        else if(mP == BMqttPriorityType.IN_7){this.setIn7(new BStatusEnum(value));}
        else if(mP == BMqttPriorityType.IN_8MO){
            // Dont just set the priority and let it latch, use the timer to clear as expected for In8
            BMqttSubDevice device =(BMqttSubDevice)((BMqttProxyExt)this.getProxyExt()).getDevice();
            if (device != null) {
                long mSecAdd = device.getPointIn8Duration().getMillis();        // Get the user delay
                BEnumOverride ovrRideVal = new BEnumOverride();           // Create the override instance
                ovrRideVal.set("duration", BRelTime.make(mSecAdd));             // Set the duration in milliseconds

                // Set the value to be overrridden
                ovrRideVal.setValue(BDynamicEnum.make(value.getOrdinal(), eRange));
                super.doOverride(ovrRideVal);                                    // Do override
                long mSecTarget = Clock.millis() + mSecAdd;                     // Add to the current time now
                this.setOverrideExpiration(BAbsTime.make(mSecTarget));          // Update the UI
            }
        }
        else if(mP == BMqttPriorityType.IN_9){this.setIn9(new BStatusEnum(value));}
        else if(mP == BMqttPriorityType.IN_10){this.setIn10(new BStatusEnum(value));}
        else if(mP == BMqttPriorityType.IN_11){this.setIn11(new BStatusEnum(value));}
        else if(mP == BMqttPriorityType.IN_12){this.setIn12(new BStatusEnum(value));}
        else if(mP == BMqttPriorityType.IN_13){this.setIn13(new BStatusEnum(value));}
        else if(mP == BMqttPriorityType.IN_14){this.setIn14(new BStatusEnum(value));}
        else if(mP == BMqttPriorityType.IN_15){this.setIn15(new BStatusEnum(value));}
        else if(mP == BMqttPriorityType.IN_16){this.setIn16(new BStatusEnum(value));}
        else if(mP == BMqttPriorityType.FALLBACK){this.setFallback(new BStatusEnum(value));}
    }
    @Override
    public void doOverride(BEnumOverride v) {
        // Allow the ProxyExt to publish out an mmqtt message when use interacts with point
        // Since we are also subscribing then need to cancel an published echo messages
        BMqttProxyExt mPE = (BMqttProxyExt) this.getProxyExt();
        mPE.setMqttPubAllowed(true);
        this.setEchoSuppression(true);
        super.doOverride(v);
    }
    @Override
    public void doSet(BDynamicEnum v) {
        BMqttProxyExt mPE = (BMqttProxyExt) this.getProxyExt();
        mPE.setMqttPubAllowed(true);
        this.setEchoSuppression(true);
        super.doSet(v);
    }
    @Override
    public void doEmergencyOverride(BDynamicEnum v) {
        BMqttProxyExt mPE = (BMqttProxyExt) this.getProxyExt();
        mPE.setMqttPubAllowed(true);
        this.setEchoSuppression(true);
        super.doEmergencyOverride(v);
    }
    @Override
    public void doEmergencyAuto(){
        BMqttProxyExt mPE = (BMqttProxyExt) this.getProxyExt();
        mPE.setMqttPubAllowed(true);
        this.setEchoSuppression(true);
        super.doEmergencyAuto();
    }
    @Override
    public void doAuto(){
        BMqttProxyExt mPE = (BMqttProxyExt) this.getProxyExt();
        mPE.setMqttPubAllowed(true);
        this.setEchoSuppression(true);
        super.doAuto();
    }

}
