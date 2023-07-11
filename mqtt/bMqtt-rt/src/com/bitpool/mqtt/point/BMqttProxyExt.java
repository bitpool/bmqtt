/**
 * Copyright 2019 bitpool, All Rights Reserved.
 */
package com.bitpool.mqtt.point;

import com.bitpool.mqtt.BMqttNetwork;
import com.bitpool.mqtt.BMqttSubDevice;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.tridium.driver.util.DrUtil;
import com.tridium.json.JSONArray;
import com.tridium.json.JSONObject;
import com.tridium.ndriver.point.BNProxyExt;

import javax.baja.driver.point.BReadWriteMode;
import javax.baja.nre.annotations.Facet;
import javax.baja.nre.annotations.NiagaraProperty;
import javax.baja.nre.annotations.NiagaraType;
import javax.baja.status.BStatusBoolean;
import javax.baja.status.BStatusEnum;
import javax.baja.status.BStatusNumeric;
import javax.baja.status.BStatusString;
import javax.baja.sys.*;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * BMqttProxyExt
 *
 *  @author   Admin
 * @creation 25-Feb-19
 */
@NiagaraProperty(name = "topic", type = "BString", defaultValue ="", flags = Flags.SUMMARY, facets = {@Facet(name = "BFacets.FIELD_WIDTH", value = "100")})
@NiagaraProperty(name = "json", type = "BString", defaultValue = "", flags = Flags.READONLY | Flags.HIDDEN, facets = {@Facet(name = "BFacets.MULTI_LINE", value = "true"),@Facet(name = "BFacets.FIELD_WIDTH", value = "100")})
@NiagaraProperty(name = "jsonFault", type = "BString", defaultValue = "", flags = Flags.READONLY | Flags.HIDDEN, facets = {@Facet(name = "BFacets.FIELD_WIDTH", value = "100")})
@NiagaraProperty(name = "jsonFilter", type = "BString", defaultValue = "", flags = Flags.SUMMARY | Flags.HIDDEN, facets = {@Facet(name = "BFacets.FIELD_WIDTH", value = "100")})

@NiagaraProperty(name = "showHelpSection", type = "BBoolean", defaultValue = "BBoolean.make(\"false\")", flags = Flags.SUMMARY | Flags.HIDDEN)
@NiagaraProperty(name = "jsonExample", type = "BString", defaultValue = "",   flags = Flags.READONLY | Flags.HIDDEN, facets = {@Facet(name = "BFacets.MULTI_LINE", value = "true"),@Facet(name = "BFacets.FIELD_WIDTH", value = "125")})
@NiagaraProperty(name = "exampleA", type = "BString", defaultValue = "", flags = Flags.READONLY | Flags.HIDDEN, facets = {@Facet(name = "BFacets.FIELD_WIDTH", value = "125")})
@NiagaraProperty(name = "exampleB", type = "BString", defaultValue = "", flags = Flags.READONLY | Flags.HIDDEN, facets = {@Facet(name = "BFacets.FIELD_WIDTH", value = "125")})
@NiagaraProperty(name = "exampleC", type = "BString", defaultValue = "", flags = Flags.READONLY | Flags.HIDDEN, facets = {@Facet(name = "BFacets.FIELD_WIDTH", value = "125")})
@NiagaraProperty(name = "exampleD", type = "BString", defaultValue = "", flags = Flags.READONLY | Flags.HIDDEN, facets = {@Facet(name = "BFacets.FIELD_WIDTH", value = "125")})
@NiagaraProperty(name = "exampleE", type = "BString", defaultValue = "", flags = Flags.READONLY | Flags.HIDDEN, facets = {@Facet(name = "BFacets.FIELD_WIDTH", value = "125")})
@NiagaraProperty(name = "exampleF", type = "BString", defaultValue = "", flags = Flags.READONLY | Flags.HIDDEN, facets = {@Facet(name = "BFacets.FIELD_WIDTH", value = "125")})
@NiagaraProperty(name = "exampleG", type = "BString", defaultValue = "", flags = Flags.READONLY | Flags.HIDDEN, facets = {@Facet(name = "BFacets.FIELD_WIDTH", value = "125")})
@NiagaraProperty(name = "helpReference", type = "BString", defaultValue = "", flags = Flags.READONLY | Flags.HIDDEN, facets = {@Facet(name = "BFacets.FIELD_WIDTH", value = "125")})

@NiagaraType
public class BMqttProxyExt
  extends BNProxyExt
{
    private boolean m_AllowedToPublish = false;

/*+ ------------ BEGIN BAJA AUTO GENERATED CODE ------------ +*/
/*@ $com.bitpool.mqtt.point.BMqttProxyExt(4280141264)1.0$ @*/
/* Generated Wed May 29 15:29:18 AEST 2019 by Slot-o-Matic (c) Tridium, Inc. 2012 */

////////////////////////////////////////////////////////////////
// Property "topic"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code topic} property.
   * @see #getTopic
   * @see #setTopic
   */
  public static final Property topic = newProperty(Flags.SUMMARY, "", BFacets.make(BFacets.FIELD_WIDTH, 100));
  
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
  public static final Property json = newProperty(Flags.READONLY | Flags.HIDDEN, "", BFacets.make(BFacets.make(BFacets.MULTI_LINE, true), BFacets.make(BFacets.FIELD_WIDTH, 100)));
  
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
// Property "jsonFault"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code jsonFault} property.
   * @see #getJsonFault
   * @see #setJsonFault
   */
  public static final Property jsonFault = newProperty(Flags.READONLY | Flags.HIDDEN, "", BFacets.make(BFacets.FIELD_WIDTH, 100));
  
  /**
   * Get the {@code jsonFault} property.
   * @see #jsonFault
   */
  public String getJsonFault() { return getString(jsonFault); }
  
  /**
   * Set the {@code jsonFault} property.
   * @see #jsonFault
   */
  public void setJsonFault(String v) { setString(jsonFault, v, null); }

////////////////////////////////////////////////////////////////
// Property "jsonFilter"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code jsonFilter} property.
   * @see #getJsonFilter
   * @see #setJsonFilter
   */
  public static final Property jsonFilter = newProperty(Flags.SUMMARY | Flags.HIDDEN, "", BFacets.make(BFacets.FIELD_WIDTH, 100));
  
  /**
   * Get the {@code jsonFilter} property.
   * @see #jsonFilter
   */
  public String getJsonFilter() { return getString(jsonFilter); }
  
  /**
   * Set the {@code jsonFilter} property.
   * @see #jsonFilter
   */
  public void setJsonFilter(String v) { setString(jsonFilter, v, null); }

////////////////////////////////////////////////////////////////
// Property "showHelpSection"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code showHelpSection} property.
   * @see #getShowHelpSection
   * @see #setShowHelpSection
   */
  public static final Property showHelpSection = newProperty(Flags.SUMMARY | Flags.HIDDEN, ((BBoolean.make("false"))).getBoolean(), null);
  
  /**
   * Get the {@code showHelpSection} property.
   * @see #showHelpSection
   */
  public boolean getShowHelpSection() { return getBoolean(showHelpSection); }
  
  /**
   * Set the {@code showHelpSection} property.
   * @see #showHelpSection
   */
  public void setShowHelpSection(boolean v) { setBoolean(showHelpSection, v, null); }

////////////////////////////////////////////////////////////////
// Property "jsonExample"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code jsonExample} property.
   * @see #getJsonExample
   * @see #setJsonExample
   */
  public static final Property jsonExample = newProperty(Flags.READONLY | Flags.HIDDEN, "", BFacets.make(BFacets.make(BFacets.MULTI_LINE, true), BFacets.make(BFacets.FIELD_WIDTH, 125)));
  
  /**
   * Get the {@code jsonExample} property.
   * @see #jsonExample
   */
  public String getJsonExample() { return getString(jsonExample); }
  
  /**
   * Set the {@code jsonExample} property.
   * @see #jsonExample
   */
  public void setJsonExample(String v) { setString(jsonExample, v, null); }

////////////////////////////////////////////////////////////////
// Property "exampleA"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code exampleA} property.
   * @see #getExampleA
   * @see #setExampleA
   */
  public static final Property exampleA = newProperty(Flags.READONLY | Flags.HIDDEN, "", BFacets.make(BFacets.FIELD_WIDTH, 125));
  
  /**
   * Get the {@code exampleA} property.
   * @see #exampleA
   */
  public String getExampleA() { return getString(exampleA); }
  
  /**
   * Set the {@code exampleA} property.
   * @see #exampleA
   */
  public void setExampleA(String v) { setString(exampleA, v, null); }

////////////////////////////////////////////////////////////////
// Property "exampleB"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code exampleB} property.
   * @see #getExampleB
   * @see #setExampleB
   */
  public static final Property exampleB = newProperty(Flags.READONLY | Flags.HIDDEN, "", BFacets.make(BFacets.FIELD_WIDTH, 125));
  
  /**
   * Get the {@code exampleB} property.
   * @see #exampleB
   */
  public String getExampleB() { return getString(exampleB); }
  
  /**
   * Set the {@code exampleB} property.
   * @see #exampleB
   */
  public void setExampleB(String v) { setString(exampleB, v, null); }

////////////////////////////////////////////////////////////////
// Property "exampleC"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code exampleC} property.
   * @see #getExampleC
   * @see #setExampleC
   */
  public static final Property exampleC = newProperty(Flags.READONLY | Flags.HIDDEN, "", BFacets.make(BFacets.FIELD_WIDTH, 125));
  
  /**
   * Get the {@code exampleC} property.
   * @see #exampleC
   */
  public String getExampleC() { return getString(exampleC); }
  
  /**
   * Set the {@code exampleC} property.
   * @see #exampleC
   */
  public void setExampleC(String v) { setString(exampleC, v, null); }

////////////////////////////////////////////////////////////////
// Property "exampleD"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code exampleD} property.
   * @see #getExampleD
   * @see #setExampleD
   */
  public static final Property exampleD = newProperty(Flags.READONLY | Flags.HIDDEN, "", BFacets.make(BFacets.FIELD_WIDTH, 125));
  
  /**
   * Get the {@code exampleD} property.
   * @see #exampleD
   */
  public String getExampleD() { return getString(exampleD); }
  
  /**
   * Set the {@code exampleD} property.
   * @see #exampleD
   */
  public void setExampleD(String v) { setString(exampleD, v, null); }

////////////////////////////////////////////////////////////////
// Property "exampleE"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code exampleE} property.
   * @see #getExampleE
   * @see #setExampleE
   */
  public static final Property exampleE = newProperty(Flags.READONLY | Flags.HIDDEN, "", BFacets.make(BFacets.FIELD_WIDTH, 125));
  
  /**
   * Get the {@code exampleE} property.
   * @see #exampleE
   */
  public String getExampleE() { return getString(exampleE); }
  
  /**
   * Set the {@code exampleE} property.
   * @see #exampleE
   */
  public void setExampleE(String v) { setString(exampleE, v, null); }

////////////////////////////////////////////////////////////////
// Property "exampleF"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code exampleF} property.
   * @see #getExampleF
   * @see #setExampleF
   */
  public static final Property exampleF = newProperty(Flags.READONLY | Flags.HIDDEN, "", BFacets.make(BFacets.FIELD_WIDTH, 125));
  
  /**
   * Get the {@code exampleF} property.
   * @see #exampleF
   */
  public String getExampleF() { return getString(exampleF); }
  
  /**
   * Set the {@code exampleF} property.
   * @see #exampleF
   */
  public void setExampleF(String v) { setString(exampleF, v, null); }

////////////////////////////////////////////////////////////////
// Property "exampleG"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code exampleG} property.
   * @see #getExampleG
   * @see #setExampleG
   */
  public static final Property exampleG = newProperty(Flags.READONLY | Flags.HIDDEN, "", BFacets.make(BFacets.FIELD_WIDTH, 125));
  
  /**
   * Get the {@code exampleG} property.
   * @see #exampleG
   */
  public String getExampleG() { return getString(exampleG); }
  
  /**
   * Set the {@code exampleG} property.
   * @see #exampleG
   */
  public void setExampleG(String v) { setString(exampleG, v, null); }

////////////////////////////////////////////////////////////////
// Property "helpReference"
////////////////////////////////////////////////////////////////
  
  /**
   * Slot for the {@code helpReference} property.
   * @see #getHelpReference
   * @see #setHelpReference
   */
  public static final Property helpReference = newProperty(Flags.READONLY | Flags.HIDDEN, "", BFacets.make(BFacets.FIELD_WIDTH, 125));
  
  /**
   * Get the {@code helpReference} property.
   * @see #helpReference
   */
  public String getHelpReference() { return getString(helpReference); }
  
  /**
   * Set the {@code helpReference} property.
   * @see #helpReference
   */
  public void setHelpReference(String v) { setString(helpReference, v, null); }

////////////////////////////////////////////////////////////////
// Type
////////////////////////////////////////////////////////////////
  
  @Override
  public Type getType() { return TYPE; }
  public static final Type TYPE = Sys.loadType(BMqttProxyExt.class);

/*+ ------------ END BAJA AUTO GENERATED CODE -------------- +*/



////////////////////////////////////////////////////////////////
// Access
////////////////////////////////////////////////////////////////

  /**
   * Get the network cast to a BMqttNetwork.
   */
  public final BMqttNetwork getMqttNetwork()
  {
    return (BMqttNetwork)getNetwork();
  }

  /**
   * Get the device cast to a BMqttDevice.
   */
  public final BMqttSubDevice getBMqttDevice()
  {
    return (BMqttSubDevice)DrUtil.getParent(this, BMqttSubDevice.TYPE);
  }
  /**
   * Get the point device ext cast to a BMqttPointDeviceExt.
   */
  public final BMqttPointDeviceExt getMqttPointDeviceExt()
  {
    return (BMqttPointDeviceExt)getDeviceExt();
  }

////////////////////////////////////////////////////////////////
// ProxyExt
////////////////////////////////////////////////////////////////
    // The ProxyExt component contains two properties used for managing read and write values.

    // The readValue property indicates the last value read from the device.
    // For writeonly points this is the last value successfully written.
    // This value is used to feed the parent point's extensions and out property. If numeric, it is in device units.
    // The writeValue property stores the value currently desired to be written to the device. If numeric, it is in device units.

  public void readSubscribed(Context cx) throws Exception{
    // This callback is made when the point enters the subscribed state.
    // This is an indication to the driver that something is now interested in this point.
    // Drivers should begin polling or register for changes.
  }

  public void readUnsubscribed(Context cx) throws Exception {
      // This callback is made when the point enters the unsubscribed state.
      // This is an indication to the driver that no one is interested in the point's current value anymore.
      // Drivers should cease polling or unregister for changes.
  }
    private String convertOrdToTopic(String ord) {
        Integer indx = ord.indexOf("/points/") + 8;              // Find the start of topic after the 'points' folder
        ord = ord.replace("/proxyExt", "");     // Remove the trailing proxy extension
        ord = ord.substring(indx);
        ord = ord.replaceAll("^/+", "");        // Remany any/all leading slashes
        return ord;
    }
    public void changed(Property p, Context cx) {
        super.changed(p, cx);
        if (p == jsonFilter) {
            translateUsingJsonFilter();
        }
        if (p == showHelpSection) {
            showHelpSection(getShowHelpSection());
        }

    }
    private void translateUsingJsonFilter(){
        String jFilterStr = getJsonFilter();
        JSONObject jO =  new JSONObject(getJson());
        if(jO != null) {

            try {
                DocumentContext jsonContext = JsonPath.parse(jO.toString());
                String keyValue = "";
                Object keyValueRead = jsonContext.read(jFilterStr);

                if (keyValueRead.getClass() == java.lang.String.class) {
                    keyValue = keyValueRead.toString();

                } else if (keyValueRead.getClass() == java.util.LinkedHashMap.class) {
                    Map lhm = new LinkedHashMap((Map) keyValueRead);
                    keyValue = String.valueOf(lhm.toString());

                } else if (keyValueRead.getClass() == net.minidev.json.JSONArray.class) {
                    String jStr = ((net.minidev.json.JSONArray) keyValueRead).toJSONString();
                    JSONArray jAval = new JSONArray(jStr);
                    if (jAval.length() > 0) {
                        if (jAval.length() == 1) {
                            keyValue = jAval.get(0).toString();
                        } else {
                            keyValue = jAval.toString();
                        }
                    }
                } else {
                    // Just try and get the string of the object. Could be Int, Float, Double....
                    keyValue = String.valueOf(keyValueRead);
                }

                if ("".equals(keyValue) == false) {
                    if (getEnabled()) {
                        readOk(new BStatusString(keyValue).getStatusValue());
                        writeOk(new BStatusString(keyValue).getStatusValue());
                    }
                    setJsonFault("");
                } else {
                    setJsonFault("Cannot evaluate JSON path expression (CLASS=" + keyValueRead.getClass() + ")");
                }

            } catch (Exception e) {
                setJsonFault(e.toString());
            }

        }
    }
    public void setMqttPubAllowed(Boolean isAllowed){
        m_AllowedToPublish = isAllowed;
    }
    public Boolean getMqttPubAllowed(){
        return m_AllowedToPublish;
    }
    public boolean write(Context cx)  throws Exception{

         try {
            // This callback is made when the framework determines that a point should be written.
            // The tuning policy is used to manage write scheduling.

            // Force setting the topic do to a bug in previous version
            // This will remove the leading forward slash in the topic
            // From V1.38.1615
            setTopic(convertOrdToTopic(getNavOrd().toString()));

            BMqttSubDevice device = (BMqttSubDevice) this.getDevice();
            if (device != null) {
                if (isNumeric()) {
                    BStatusNumeric sVal = (BStatusNumeric) this.getWriteValue();
                    BMqttNumericWritable pW = (BMqttNumericWritable)this.getParent();
                    if (sVal.getStatus().isNull() == false) {
                        Double dVal = sVal.getNumeric();
                        writeOk(new BStatusNumeric(dVal));
                        readOk(new BStatusNumeric(dVal));
                        if(this.getMqttPubAllowed()) {
                            device.mqttPublishMessage(getTopic(), dVal.toString());
                            pW.setEchoSuppression(true);
                        }

                    }else{
                        pW.setEchoSuppression(false);
                    }

                } else if (isBoolean()) {
                    BStatusBoolean sVal = (BStatusBoolean) this.getWriteValue();
                    BMqttBooleanWritable pW = (BMqttBooleanWritable)this.getParent();
                    if (sVal.getStatus().isNull() == false) {
                        Boolean bVal = sVal.getBoolean();
                        readOk(sVal.getStatusValue());
                        if(this.getMqttPubAllowed()){
                            device.mqttPublishMessage(getTopic(), bVal.toString());
                            pW.setEchoSuppression(true);
                        }

                    }else{
                        pW.setEchoSuppression(false);
                    }

                } else if (isString()) {
                    BStatusString sVal = (BStatusString) this.getWriteValue();
                    BMqttStringWritable pW = (BMqttStringWritable)this.getParent();
                    if (sVal.getStatus().isNull() == false) {
                        String strVal = sVal.getValue();
                        writeOk(new BStatusString(strVal).getStatusValue());
                        readOk(new BStatusString(strVal).getStatusValue());
                        if(this.getMqttPubAllowed()) {
                            device.mqttPublishMessage(getTopic(), strVal);
                            pW.setEchoSuppression(true);
                        }

                    }else{
                        pW.setEchoSuppression(false);
                    }
                } else if (isEnum()) {
                    BStatusEnum eVal = (BStatusEnum) this.getWriteValue();
                    BMqttEnumWritable pW = (BMqttEnumWritable)this.getParent();
                    if (eVal.getStatus().isNull() == false) {
                        BEnum enumVal = eVal.getEnum();
                        writeOk(new BStatusEnum(enumVal).getStatusValue());
                        readOk(new BStatusEnum(enumVal).getStatusValue());
                        if(this.getMqttPubAllowed()) {
                            device.mqttPublishMessage(getTopic(), new BStatusEnum(enumVal).getValueValue().toString());
                            pW.setEchoSuppression(true);
                        }

                    }else{
                        pW.setEchoSuppression(false);
                    }
                }
            }
        }catch (Exception e){
             e.printStackTrace();
            return false;
        }
        return false;
    }

  /**
   * Return the device type.
   */
  public Type getDeviceExtType() {
    return BMqttPointDeviceExt.TYPE;
  }

  /**
   * Return the read/write mode of this proxy.
   */
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

    public void showJsonProperty() {
        try {
            showSlot("json", true);
            showSlot("jsonFault", true);
            showSlot("jsonFilter", true);
            showSlot("showHelpSection", true);

            // Load the JSON example properties
            setJsonExample(JSON_EXAMPLE);
            setExampleA(JSON_EXAMPLE_A);
            setExampleB(JSON_EXAMPLE_B);
            setExampleC(JSON_EXAMPLE_C);
            setExampleD(JSON_EXAMPLE_D);
            setExampleE(JSON_EXAMPLE_E);
            setExampleF(JSON_EXAMPLE_F);
            setExampleG(JSON_EXAMPLE_G);
            setHelpReference(JSON_EXAMPLE_REFER);

        } catch (Exception e) {
        }

    }

    private void showSlot(String slotName, Boolean showSlot){
        Slot objSlot =  getSlot(slotName);
        Integer slotFlags = getFlags(objSlot);

        if(((slotFlags & Flags.HIDDEN) != 0)){ // Is hidden
          if(showSlot){
              setFlags(objSlot, slotFlags & ~Flags.HIDDEN); // Show
          }
        }else{
          if(!showSlot){
              setFlags(objSlot, slotFlags | Flags.HIDDEN); // Hide
          }
        }
    }

    public void showHelpSection(Boolean showSection){

        try {
            showSlot("jsonExample", showSection);
            showSlot("exampleA", showSection);
            showSlot("exampleB", showSection);
            showSlot("exampleC", showSection);
            showSlot("exampleD", showSection);
            showSlot("exampleE", showSection);
            showSlot("exampleF", showSection);
            showSlot("exampleG", showSection);
            showSlot("helpReference", showSection);

        }catch (Exception e){ }

    }

private static final String JSON_EXAMPLE_A =    "[ATTRIB] date  -->  Friday, Feb 08, 2019 09:49:48 PM GMT+10:00";
private static final String JSON_EXAMPLE_B =    "[OBJECT] properties  -->  {id={description=Unique identifier, type=integer}, name={description=Name of product, type=string}}";
private static final String JSON_EXAMPLE_C =    "[WHERE ] rankings.[?(@.position == 1)]  -->  {buy:true, usd:3358.39, name:Bitcoin , position:1}";
private static final String JSON_EXAMPLE_D =    "[WHERE ] rankings.[?(@.usd > 100)].name  -->  [Bitcoin, Ethereum]";
private static final String JSON_EXAMPLE_E =    "[REGEX ] rankings.[?(@.name =~ /eth.*/i)] -->  {buy:true, usd:104.70, name:Ethereum, position:2}";
private static final String JSON_EXAMPLE_F =    "[INDEX ] symbols.[2]  -->  XRP";
private static final String JSON_EXAMPLE_G =    "[RANGE ] symbols.[0:2]  -->  [BTC, ETH]";
private static final String JSON_EXAMPLE_REFER ="https://github.com/json-path/JsonPath";
private static final String JSON_EXAMPLE =
"{\n" +
"    \"date\": \"Friday, Feb 08, 2019 09:49:48 PM GMT+10:00\",\n" +
"    \"properties\": {\n" +
"        \"id\": {\n" +
"            \"description\": \"Unique identifier\",\n" +
"            \"type\": \"integer\"\n" +
"        },\n" +
"        \"name\": {\n" +
"            \"description\": \"Name of product\",\n" +
"            \"type\": \"string\"\n" +
"        }\n" +
"    },\n" +
"    \"rankings\": [\n" +
"        {\n" +
"            \"buy\": true,\n" +
"            \"name\": \"Bitcoin\",\n" +
"            \"position\": 1,\n" +
"            \"usd\": 3358.39\n" +
"        },\n" +
"        {\n" +
"            \"buy\": true,\n" +
"            \"name\": \"Ethereum\",\n" +
"            \"position\": 2,\n" +
"            \"usd\": 104.70\n" +
"        },\n" +
"        {\n" +
"            \"buy\": false,\n" +
"            \"name\": \"XRP\",\n" +
"            \"position\": 3,\n" +
"            \"usd\": 0.377\n" +
"        }\n" +
"    ],\n" +
"    \"symbols\": [\n" +
"        \"BTC\",\n" +
"        \"ETH\",\n" +
"        \"XRP\"\n" +
"    ],\n" +
"    \"time\": 1549626588\n" +
"}";

}
