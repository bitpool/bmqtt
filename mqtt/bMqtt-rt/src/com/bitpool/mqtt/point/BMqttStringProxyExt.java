package com.bitpool.mqtt.point;

import javax.baja.driver.point.BReadWriteMode;
import javax.baja.nre.annotations.Facet;
import javax.baja.nre.annotations.NiagaraProperty;
import javax.baja.nre.annotations.NiagaraType;
import javax.baja.status.BStatusBoolean;
import javax.baja.status.BStatusEnum;
import javax.baja.status.BStatusNumeric;
import javax.baja.status.BStatusString;
import javax.baja.sys.BFacets;
import javax.baja.sys.Context;
import javax.baja.sys.Flags;
import javax.baja.sys.Property;
import javax.baja.sys.Sys;
import javax.baja.sys.Type;

@NiagaraProperty(name = "topic", type = "BString", defaultValue = "", flags = Flags.READONLY, facets = {@Facet(name = "BFacets.FIELD_WIDTH", value = "100")})
@NiagaraProperty(name = "json", type = "BString", defaultValue = "", flags = Flags.READONLY, facets = {@Facet(name = "BFacets.MULTI_LINE", value = "true"),@Facet(name = "BFacets.FIELD_WIDTH", value = "100")})


@NiagaraType
public class BMqttStringProxyExt extends BMqttProxyExt {
/*+ ------------ BEGIN BAJA AUTO GENERATED CODE ------------ +*/
/*@ $com.bitpool.mqtt.point.BMqttStringProxyExt(4037028157)1.0$ @*/
/* Generated Mon Apr 15 16:00:47 AEST 2019 by Slot-o-Matic (c) Tridium, Inc. 2012 */

////////////////////////////////////////////////////////////////
// Property "topic"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code topic} property.
   * @see #getTopic
   * @see #setTopic
   */
  public static final Property topic = newProperty(Flags.READONLY, "", BFacets.make(BFacets.FIELD_WIDTH, 100));
  
  /**
   * Get the {@code topic} property.
   * @see #topic
   */
  public String getTopic() { return getString(topic); }
  
  /**
   * Set the {@code topic} property.
   * @see #topic
   */
  public void setTopic(String v) { setString(topic, v, null); }

////////////////////////////////////////////////////////////////
// Property "json"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code json} property.
   * @see #getJson
   * @see #setJson
   */
  public static final Property json = newProperty(Flags.READONLY, "", BFacets.make(BFacets.make(BFacets.MULTI_LINE, true), BFacets.make(BFacets.FIELD_WIDTH, 100)));
  
  /**
   * Get the {@code json} property.
   * @see #json
   */
  public String getJson() { return getString(json); }
  
  /**
   * Set the {@code json} property.
   * @see #json
   */
  public void setJson(String v) { setString(json, v, null); }

////////////////////////////////////////////////////////////////
// Type
////////////////////////////////////////////////////////////////
  
  @Override
  public Type getType() { return TYPE; }

    @Override
    public Type getDeviceExtType() {return BMqttStringProxyExt.TYPE; }

    @Override
    public void readSubscribed(Context context) throws Exception { }

    @Override
    public void readUnsubscribed(Context context) throws Exception { }

    @Override
    public boolean write(Context context) throws Exception { return false; }

    public static final Type TYPE = Sys.loadType(BMqttStringProxyExt.class);

/*+ ------------ END BAJA AUTO GENERATED CODE -------------- +*/

    public void hideJsonProperty(){
        this.setFlags(getSlot("json"), Flags.HIDDEN);
    }

    public BReadWriteMode getMode(){
        return BReadWriteMode.readWrite;
    }

    public boolean isBoolean()
    {
        return getParentPoint().getOutStatusValue() instanceof BStatusBoolean;
    }
    public boolean isNumeric()
    {
        return getParentPoint().getOutStatusValue() instanceof BStatusNumeric;
    }
    public boolean isString()
    {
        return getParentPoint().getOutStatusValue() instanceof BStatusString;
    }
    public boolean isEnum()
    {
        return getParentPoint().getOutStatusValue() instanceof BStatusEnum;
    }
}
