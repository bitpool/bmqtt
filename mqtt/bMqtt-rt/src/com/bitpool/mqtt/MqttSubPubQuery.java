package com.bitpool.mqtt;

import com.tridium.util.EscUtil;

import javax.baja.collection.BITable;
import javax.baja.collection.Column;
import javax.baja.collection.ColumnList;
import javax.baja.collection.TableCursor;
import javax.baja.naming.BOrd;
import javax.baja.status.BStatusBoolean;
import javax.baja.status.BStatusNumeric;
import javax.baja.sys.BBoolean;
import javax.baja.sys.BDouble;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MqttSubPubQuery {
    public Map < String, String >  m_Ords = null;                // Short ORD vs long ORD
    private Map< String, String >   m_Topics = null;              // Short ORD vs Control Point topic property
    private Map < String, String >  m_Values = null;              // Short ORD vs current value
//    private Map < String, String >  m_ValuesPrevious = null;      // Short ORD vs previous value


    public boolean buildOrdLookup(String ordParent){

        if("".equals(ordParent)) {  // Set a default if input empty
            ordParent = "station:|slot:/Drivers";
        }
//        if (m_Values != null && m_Values.size() > 0) { // Keep a copy only when there is data
//            clearDownPreviousVals();
//            m_ValuesPrevious = m_Values;
//        }

        m_Ords = new HashMap<>();
        m_Topics = new HashMap<>();
        m_Values = new HashMap<>();

        try {
            buildLookups(ordParent);
            return true;
        }catch (javax.baja.naming.UnresolvedException eU) {
            return false;
        }catch (Exception e){
            System.out.println("Exception: Cannot build proxy extension lookup table");
            e.printStackTrace();
            return false;
        }
    }

    // Get a list of Topics that have changed value since the last poll
//    public Map < String, String > getChangedTopicValues() {
//        Map < String, String > changedTopicValues = new HashMap < > ();
//        for (Map.Entry < String, String > entry: m_Ords.entrySet()) {
//            String keyStationOrdShort = entry.getKey();
//            if (topicValueHasChanged(keyStationOrdShort)) {
//                changedTopicValues.put(keyStationOrdShort, m_Values.get(keyStationOrdShort));
//            }
//        }
//        return changedTopicValues;
//    }

    // Compare the last two values of the each point
//    private Boolean topicValueHasChanged(String brokerTopic) {
//        try {
//            if (m_Values != null && m_Values.containsKey(brokerTopic)) {
//                String currentValue = m_Values.get(brokerTopic);
//                if (m_ValuesPrevious != null && m_ValuesPrevious.containsKey(brokerTopic)) {
//                    String previousValue = m_ValuesPrevious.get(brokerTopic);
//                    if (currentValue.equals(previousValue) == false) {
//                        return true;
//                    }
//                }
//            }
//        } catch (Exception e) {
//            return false;
//        }
//        return false;
//    }

    // Compare Broker topic numeric value to Station point numeric value
    public Boolean hasStringValueChanged(String brokerTopic, String brokerValue) {
        try {
            if (m_Values != null && m_Values.containsKey(brokerTopic)) {
                String pointValue = m_Values.get(brokerTopic);
                if (pointValue.equals(brokerValue) == false) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
    // Compare Broker topic numeric value to Station point numeric value
    public Boolean hasNumericValueChanged(String brokerTopic, String value) {
        try {
            if (m_Values != null && m_Values.containsKey(brokerTopic)) {
                BStatusNumeric dCompare = new BStatusNumeric(BDouble.make(value).getDouble());
                String pointValue = m_Values.get(brokerTopic);
                String brokerValue = dCompare.getValueValue().toString();

                if (pointValue.equals(brokerValue) == false) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
    // Compare Broker topic boolean value to Station point boolean value
    public Boolean hasBooleanValueChanged(String brokerTopic, String value) {
        try {
            if (m_Values != null && m_Values.containsKey(brokerTopic)) {
                BStatusBoolean bCompare = new BStatusBoolean(BBoolean.make(value).getBoolean());
                String pointValue = m_Values.get(brokerTopic);
                String brokerValue = bCompare.getValueValue().toString();

                if (pointValue.equals(brokerValue) == false) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
    // The Broker 'Topic' property (read/write) on the Proxy Extension is set when the control point created.
    // Multiple control points can therefore use the same 'Topic' and be updated together using the same broker value.
    // This is useful when the payload is JSON as the Control Point's 'out' can be further filtered with a nested JSON value.
    //
    // Use this call to get all the ORD's that have which have the same Proxy Extension property 'Topic'
    //  e.g.
    //      Topic = '/aaaa/bbbbb/cc/ddd/eeee/fff/g/h' or 'aaaa/bbbbb/cc/ddd/eeee/fff/g/h'

    public ArrayList<String> getOrdsWithProxyExtTopic(String brokerTopic) {
        ArrayList<String> ords = new ArrayList();
        for (Map.Entry < String, String > entry: m_Topics.entrySet()) {
            String keyStationOrdShort = entry.getKey();           // Only one unique ORD as this is a point
            String valProxyExtTopic  = entry.getValue();          // Multiple topics of the same topic

            // Add all the short Station ORDs to the array where the broker Topic matches the Proxy Ext Topic
            if (brokerTopic.equals(valProxyExtTopic)) {
                if(keyStationOrdShort.equals(valProxyExtTopic) == false){   // Only add the Topic if not the original broker topic
                    ords.add(keyStationOrdShort);
                }
            }
        }
        return ords;
    }
    
    // Use the proxy extension topic as a ket to find the station ord
    //  e.g.
    //      Topic = '/aaaa/bbbbb/cc/ddd/eeee/fff/g/h' or 'aaaa/bbbbb/cc/ddd/eeee/fff/g/h'
    // given there are multiple 
    public String getStationOrdIfTopicExists(String brokerTopic){
        String topic = unescapeOrd(brokerTopic);
        if (m_Ords.containsKey(topic)) {
            return m_Ords.get(topic);
        }
        return null;
    }

    private void buildLookups(String ordParent) throws Exception {
        BITable result = AccessController.doPrivileged((PrivilegedAction<BITable>) () -> {
            String bqlStr = "bql:select navOrd, proxyExt.topic, out.value from control:ControlPoint where slotExists('out')";
            String fullPath = ordParent + "|" + bqlStr;
            //System.out.println(fullPath);
            BOrd ord = BOrd.make(fullPath);
            return (BITable) ord.resolve().get();
        });

        ColumnList columns = result.getColumns();
        Column colNavOrd = columns.get("navOrd");
        Column colOutValue = columns.get("out.value");
        Column colPeTopic = columns.get("proxyExt.topic");

        TableCursor cursorTable = result.cursor();
        while (cursorTable.next()) {
            String keyStationOrdLong = cursorTable.cell(colNavOrd).toString();          // Is a UNIQUE Station ORD
            String valControlPointOut = cursorTable.cell(colOutValue).toString();   // Is the out value of Control Point
            String valProxyExtTopic = cursorTable.cell(colPeTopic).toString();      // Is a NON-UNIQUE Topic property value

            // We mapped the broker topic path to the points folder using the configuration topic property in the device
            // e.g.
            //
            // Device Setting
            //      Topic = 'aaaa/bbbbb/cc' (subscribe to all topics below this one)
            //
            // MQTT Broker
            //      aaaa/bbbbb/cc/ddd/eeee/fff/g/h
            //      aaaa/bbbbb/cc/ddd/eeee/fff/g/hh
            //      aaaa/bbbbb/cc/ddd/eeee/fff/g/hhh
            //
            // Station ORD containing driver, network, device and topic
            //      local:|station:|slot:/Drivers/MqttNetwork/MqttSubDevice/points/ddd/eeee/fff/g/h
            //      local:|station:|slot:/Drivers/MqttNetwork/MqttSubDevice/points/ddd/eeee/fff/g/hh
            //      local:|station:|slot:/Drivers/MqttNetwork/MqttSubDevice/points/ddd/eeee/fff/g/hhh
            //
            // Shorten the ORD relative to the points folder (this is a UNIQUE point folder ORD)
            //      FROM:   local:|station:|slot:/Drivers/MqttNetwork/MqttSubDevice/points/ddd/eeee/fff/g/h
            //      TO:     ddd/eeee/fff/g/h
            //

            Integer indx = keyStationOrdLong.indexOf("/points/") + 8;   // dont include the leading slash
            
            // This relativized ORD is is not the 'Topic'. The 'Topic' is a property of the proxy extension
            String keyStationOrdShort = unescapeOrd(keyStationOrdLong.substring(indx));

            // Store 'Short ORD' => 'Full ORD'
            // ['ddd/eeee/fff/g/h'] = 'local:|station:|slot:/Drivers/MqttNetwork/MqttSubDevice/points/ddd/eeee/fff/g/h'
            m_Ords.put(keyStationOrdShort, unescapeOrd(keyStationOrdLong));

            // Store 'Short ORD'' => 'Proxy Extension Topic'
            // ['ddd/eeee/fff/g/h'] = 'ddd/eeee/fff/g/h'
            m_Topics.put(keyStationOrdShort, valProxyExtTopic);

            // Store 'Short ORD'' => value.out
            // ['ddd/eeee/fff/g/h'] = x
            m_Values.put(keyStationOrdShort, valControlPointOut);

        }
    }
    private String unescapeOrd(String ord){
        return EscUtil.ord.unescape(ord);
    }
}
