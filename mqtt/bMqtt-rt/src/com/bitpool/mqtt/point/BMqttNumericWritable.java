package com.bitpool.mqtt.point;
import com.bitpool.mqtt.BMqttPriorityType;
import com.bitpool.mqtt.BMqttSubDevice;

import javax.baja.control.BNumericWritable;
import javax.baja.control.util.BNumericOverride;
import javax.baja.nre.annotations.NiagaraType;
import javax.baja.status.BStatusNumeric;
import javax.baja.sys.BAbsTime;
import javax.baja.sys.BDouble;
import javax.baja.sys.BRelTime;
import javax.baja.sys.Clock;
import javax.baja.sys.Sys;
import javax.baja.sys.Type;

@NiagaraType
public class BMqttNumericWritable extends BNumericWritable {
    private boolean m_SuppressUserPublishedEcho = false;

    /*+ ------------ BEGIN BAJA AUTO GENERATED CODE ------------ +*/
    /*@ $com.bitpool.mqtt.point.BMqttNumericWritable(2979906276)1.0$ @*/
    /* Generated Tue Jan 12 08:49:14 AEST 2021 by Slot-o-Matic (c) Tridium, Inc. 2012 */

////////////////////////////////////////////////////////////////
// Type
////////////////////////////////////////////////////////////////

    @Override
    public Type getType() { return TYPE; }
    public static final Type TYPE = Sys.loadType(BMqttNumericWritable.class);

    /*+ ------------ END BAJA AUTO GENERATED CODE -------------- +*/
    public void setEchoSuppression(Boolean isSuppressed){
        m_SuppressUserPublishedEcho = isSuppressed;
    }
    public Boolean getEchoSuppression(){
        return m_SuppressUserPublishedEcho;
    }
    public void setMqttValue(Double value, Integer priorityLevel) {

        if(this.getEchoSuppression()){
            this.setEchoSuppression(false);
            return;
        }

        // Make sure the ProxyExt does not publish out the mqtt sub message you just received
        BMqttProxyExt mPE = (BMqttProxyExt) this.getProxyExt();
        mPE.setMqttPubAllowed(false);

        Integer mP =  priorityLevel;
        if(mP ==      BMqttPriorityType.IN_1EO){this.setIn1(new BStatusNumeric(value));}
        else if(mP == BMqttPriorityType.IN_2){this.setIn2(new BStatusNumeric(value));}
        else if(mP == BMqttPriorityType.IN_3){this.setIn3(new BStatusNumeric(value));}
        else if(mP == BMqttPriorityType.IN_4){this.setIn4(new BStatusNumeric(value));}
        else if(mP == BMqttPriorityType.IN_5){this.setIn5(new BStatusNumeric(value));}
        else if(mP == BMqttPriorityType.IN_6){this.setIn6(new BStatusNumeric(value));}
        else if(mP == BMqttPriorityType.IN_7){this.setIn7(new BStatusNumeric(value));}
        else if(mP == BMqttPriorityType.IN_8MO){
            // Dont just set the priority and let it latch, use the timer to clear as expected for In8
            BMqttSubDevice device =(BMqttSubDevice)((BMqttProxyExt)this.getProxyExt()).getDevice();
            if (device != null) {
                long mSecAdd = device.getPointIn8Duration().getMillis();        // Get the user delay
                BNumericOverride ovrRideVal = new BNumericOverride();           // Create the override instance
                ovrRideVal.set("duration", BRelTime.make(mSecAdd));             // Set the duration in milliseconds
                ovrRideVal.setValue(value);                                     // Set the value to be overrridden
                super.doOverride(ovrRideVal);                                    // Do override
                long mSecTarget = Clock.millis() + mSecAdd;                     // Add to the current time now
                this.setOverrideExpiration(BAbsTime.make(mSecTarget));          // Update the UI
            }
        }
        else if(mP == BMqttPriorityType.IN_9){this.setIn9(new BStatusNumeric(value));}
        else if(mP == BMqttPriorityType.IN_10){this.setIn10(new BStatusNumeric(value));}
        else if(mP == BMqttPriorityType.IN_11){this.setIn11(new BStatusNumeric(value));}
        else if(mP == BMqttPriorityType.IN_12){this.setIn12(new BStatusNumeric(value));}
        else if(mP == BMqttPriorityType.IN_13){this.setIn13(new BStatusNumeric(value));}
        else if(mP == BMqttPriorityType.IN_14){this.setIn14(new BStatusNumeric(value));}
        else if(mP == BMqttPriorityType.IN_15){this.setIn15(new BStatusNumeric(value));}
        else if(mP == BMqttPriorityType.IN_16){this.setIn16(new BStatusNumeric(value));}
        else if(mP == BMqttPriorityType.FALLBACK){this.setFallback(new BStatusNumeric(value));}
    }
    @Override
    public void doOverride(BNumericOverride v) {
        // Allow the ProxyExt to publish out an mmqtt message when use interacts with point
        // Since we are also subscribing then need to cancel an published echo messages
        BMqttProxyExt mPE = (BMqttProxyExt) this.getProxyExt();
        mPE.setMqttPubAllowed(true);
        this.setEchoSuppression(true);
        super.doOverride(v);
    }
    @Override
    public void doSet(BDouble v) {
        BMqttProxyExt mPE = (BMqttProxyExt) this.getProxyExt();
        mPE.setMqttPubAllowed(true);
        this.setEchoSuppression(true);
        super.doSet(v);
    }
    @Override
    public void doEmergencyOverride(BDouble v) {
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
