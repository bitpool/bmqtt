package com.bitpool.mqtt.point;

import com.bitpool.mqtt.BMqttPriorityType;
import com.bitpool.mqtt.BMqttSubDevice;

import javax.baja.control.BStringWritable;
import javax.baja.control.util.BStringOverride;
import javax.baja.nre.annotations.NiagaraType;
import javax.baja.status.BStatusString;
import javax.baja.sys.BAbsTime;
import javax.baja.sys.BRelTime;
import javax.baja.sys.BString;
import javax.baja.sys.Clock;
import javax.baja.sys.Sys;
import javax.baja.sys.Type;

@NiagaraType
public class BMqttStringWritable  extends BStringWritable {
    private boolean m_SuppressUserPublishedEcho = false;
/*+ ------------ BEGIN BAJA AUTO GENERATED CODE ------------ +*/
/*@ $com.bitpool.mqtt.point.BMqttStringWritable(2979906276)1.0$ @*/
/* Generated Tue May 14 12:15:31 AEST 2019 by Slot-o-Matic (c) Tridium, Inc. 2012 */

////////////////////////////////////////////////////////////////
// Type
////////////////////////////////////////////////////////////////
  
  @Override
  public Type getType() { return TYPE; }
  public static final Type TYPE = Sys.loadType(BMqttStringWritable.class);

/*+ ------------ END BAJA AUTO GENERATED CODE -------------- +*/
    public void setEchoSuppression(Boolean isSuppressed){
        m_SuppressUserPublishedEcho = isSuppressed;
    }
    public Boolean getEchoSuppression(){
        return m_SuppressUserPublishedEcho;
    }
    public void setMqttValue(String value, Integer priorityLevel) {

        if(this.getEchoSuppression()){
            this.setEchoSuppression(false);
            return;
        }

        // Make sure the ProxyExt does not publish out the mqtt sub message you just received
        BMqttProxyExt mPE = (BMqttProxyExt) this.getProxyExt();
        mPE.setMqttPubAllowed(false);

        Integer mP =  priorityLevel;
        if(mP ==      BMqttPriorityType.IN_1EO){this.setIn1(new BStatusString(value));}
        else if(mP == BMqttPriorityType.IN_2){this.setIn2(new BStatusString(value));}
        else if(mP == BMqttPriorityType.IN_3){this.setIn3(new BStatusString(value));}
        else if(mP == BMqttPriorityType.IN_4){this.setIn4(new BStatusString(value));}
        else if(mP == BMqttPriorityType.IN_5){this.setIn5(new BStatusString(value));}
        else if(mP == BMqttPriorityType.IN_6){this.setIn6(new BStatusString(value));}
        else if(mP == BMqttPriorityType.IN_7){this.setIn7(new BStatusString(value));}
        else if(mP == BMqttPriorityType.IN_8MO){
            // Dont just set the priority and let it latch, use the timer to clear as expected for In8
            BMqttSubDevice device =(BMqttSubDevice)((BMqttProxyExt)this.getProxyExt()).getDevice();
            if (device != null) {
                long mSecAdd = device.getPointIn8Duration().getMillis();        // Get the user delay
                BStringOverride ovrRideVal = new BStringOverride();             // Create the override instance
                ovrRideVal.set("duration", BRelTime.make(mSecAdd));             // Set the duration in milliseconds
                ovrRideVal.setValue(value);                                     // Set the value to be overrridden
                super.doOverride(ovrRideVal);                                   // Do override
                long mSecTarget = Clock.millis() + mSecAdd;                     // Add to the current time now
                this.setOverrideExpiration(BAbsTime.make(mSecTarget));          // Update the UI
            }
        }
        else if(mP == BMqttPriorityType.IN_9){this.setIn9(new BStatusString(value));}
        else if(mP == BMqttPriorityType.IN_10){this.setIn10(new BStatusString(value));}
        else if(mP == BMqttPriorityType.IN_11){this.setIn11(new BStatusString(value));}
        else if(mP == BMqttPriorityType.IN_12){this.setIn12(new BStatusString(value));}
        else if(mP == BMqttPriorityType.IN_13){this.setIn13(new BStatusString(value));}
        else if(mP == BMqttPriorityType.IN_14){this.setIn14(new BStatusString(value));}
        else if(mP == BMqttPriorityType.IN_15){this.setIn15(new BStatusString(value));}
        else if(mP == BMqttPriorityType.IN_16){this.setIn16(new BStatusString(value));}
        else if(mP == BMqttPriorityType.FALLBACK){this.setFallback(new BStatusString(value));}
    }
    @Override
    public void doOverride(BStringOverride v) {
        // Allow the ProxyExt to publish out an mmqtt message when use interacts with point
        // Since we are also subscribing then need to cancel an published echo messages
        BMqttProxyExt mPE = (BMqttProxyExt) this.getProxyExt();
        mPE.setMqttPubAllowed(true);
        this.setEchoSuppression(true);
        super.doOverride(v);
    }
    @Override
    public void doSet(BString v) {
        BMqttProxyExt mPE = (BMqttProxyExt) this.getProxyExt();
        mPE.setMqttPubAllowed(true);
        this.setEchoSuppression(true);
        super.doSet(v);
    }
    @Override
    public void doEmergencyOverride(BString v) {
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
