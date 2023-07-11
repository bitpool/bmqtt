package com.bitpool.mqtt;

import com.tridium.util.EscUtil;

import javax.baja.collection.BITable;
import javax.baja.collection.Column;
import javax.baja.collection.ColumnList;
import javax.baja.collection.TableCursor;
import javax.baja.naming.BOrd;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MqttPubQuery {

    private Map < String, String > m_Ords = null;
    private Map < String, String > m_OrdsType = null;
    private Map < String, String > m_OrdsPrev = null;
    private final Map < String, Integer > m_OrdsCount = new HashMap < > ();

    public boolean buildOrdLookup(String ordParent) {

        if ("".equals(ordParent)) {
            ordParent = "station:|slot:/Drivers";
        }

        if (m_Ords != null && m_Ords.size() > 0) {
            // Keep a copy only when there is data
            m_OrdsPrev = m_Ords;
        }
        m_Ords = new HashMap < > ();
        m_OrdsType = new HashMap < > ();

        try {
            buildLookups(ordParent);
            return true;
        }catch (javax.baja.naming.UnresolvedException eU) {
            return false;
        }catch (Exception e) {
            System.out.println("Exception: Cannot build proxy extension lookup table");
            e.printStackTrace();
            return false;
        }
    }
    public String getOrdCurrentValue(String ord) {
        if (m_Ords.containsKey(ord)) {
            return m_Ords.get(ord);
        }
        return null;
    }
    public String getOrdPreviousValue(String ord) {
        if (m_OrdsPrev.containsKey(ord)) {
            return m_OrdsPrev.get(ord);
        }
        return null;
    }
    public String getOrdType(String ord) {
        if (m_OrdsType.containsKey(ord)) {
            return m_OrdsType.get(ord);
        }
        return null;
    }
    private Boolean hasChangedValue(String ord) {
        // Compare the last two values of the each point, keeping a count of changes
        try {
            if (m_Ords != null && m_Ords.containsKey(ord)) {
                String currentValue = m_Ords.get(ord);
                if (m_OrdsPrev != null && m_OrdsPrev.containsKey(ord)) {
                    String previousValue = m_OrdsPrev.get(ord);
                    if (currentValue.equals(previousValue) == false) {

                        if (m_OrdsCount != null && m_OrdsCount.containsKey(ord) == false) {
                            m_OrdsCount.put(ord, 1);
                        } else {
                            Integer existingCount = m_OrdsCount.get(ord);
                            Integer newCount = existingCount + 1;
                            m_OrdsCount.put(ord, newCount);
                        }
                        return true;
                    }
                }
            }
        } catch (Exception e) {return false;}
        return false;
    }

    public Map < String, String > getChanged(boolean forceGetAllValues) {
        Map < String, String > topicsCov = new HashMap < > ();
        for (Map.Entry < String, String > entry: m_Ords.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (forceGetAllValues || hasChangedValue(key)) {
                topicsCov.put(key, value);
            }
        }
        return topicsCov;
    }
    public ArrayList < String > getOrdList() {
        return new ArrayList < > (m_Ords.keySet());
    }
    public ArrayList < String > getOrdTypeList() {
        return new ArrayList < > (m_OrdsType.keySet());
    }

    private void buildLookups(String ordParent) throws Exception {
        BITable result = AccessController.doPrivileged((PrivilegedAction < BITable > )() -> {
            String bqlStr = "bql:select navOrd, type, out.value from control:ControlPoint where slotExists('out')";
            String fullPath = ordParent + "|" + bqlStr;
            //System.out.println(fullPath);
            BOrd ord = BOrd.make(fullPath);
            return (BITable) ord.resolve().get();
        });

        ColumnList columns = result.getColumns();
        Column colNavOrd = columns.get("navOrd");
        Column colOutValue = columns.get("out.value");
        Column colType = columns.get("type");

        TableCursor cursorTable = result.cursor();
        while (cursorTable.next()) {
            String keyOrd = cursorTable.cell(colNavOrd).toString();
            String keyValue = cursorTable.cell(colOutValue).toString();
            String keyType = cursorTable.cell(colType).toString();
            //System.out.println(keyOrd + " - "+ keyValue + " - " + keyType);
            m_Ords.put(EscUtil.ord.unescape(keyOrd), keyValue);
            m_OrdsType.put(EscUtil.ord.unescape(keyOrd), keyType);
        }
    }
}

/*
    NEQL examples
    local:|foxs:|station:|slot:/|neql:n:point
    local:|foxs:|station:|slot:/|neql:n:device
    local:|foxs:|station:|slot:/|neql:n:point|bql:select toDisplayPathString, enabled
    local:|foxs:|station:|slot:/|neql:n:point|bql:select toDisplayPathString, out, enabled

    BQL examples
    local:|foxs:|station:/|bql:select displayName, MAX(out.value), gatewayId, sensorId, sensorFeature from control:NumericPoint
    local:|foxs:|station:/|bql:select navOrd, MAX(out.value), gatewayId, sensorId, sensorFeature from control:NumericPoint where sensorFeature = 'Power'
    station:/|bql:select navOrd, out.value from control:NumericPoint where sensorFeature = 'Power' and sensorId = '62' and gatewayId = '7c8c7df22fd5'

    // Found these examples
    // https://gist.github.com/mrupperman/8a0761bbb416b8ef1ca4f51c228f63bf

    Alarm Queries
    local:|fox:|station:|slot:/|bql:select name as 'Point Name',out as 'Point Status' from control:ControlPoint where status.alarm = 'true'
    local:|fox:|station:|slot:/|bql:select * from alarm:AlarmSourceExt where alarmClass = 'defaultAlarmClass' and status.alarm = true
    alarm:|bql:select timestamp,alarmData.sourceName,sourceState,ackState,ackRequired,alarmData.msgText,alarmClass where alarmData.sourceName like 'B1J*' and alarmClass like '*1*' order by timestamp desc
    alarm:|bql:select timestamp,alarmData.sourceName,sourceState,ackState,ackRequired,alarmData.msgText,alarmClass where alarmData.sourceName like 'B1J*' and alarmClass like '*2*' order by timestamp desc
    alarm:|bql:select *

    History Queries
    local:|fox:|history:/MyStation/ZoneTemp|bql:select timestamp, value where timestamp in bqltime.today
    history:|bql:select *

    Component Queries
    local:|fox:|station:|slot:/VAV_Bldg_1|bql:select slotPath as 'Path', displayName as 'Display', out.value as 'Output Value' from control:NumericPoint where displayName like '*ZN_T*'
    local:|fox:|station:|slot:/VAV_Bldg_1|bql:select slotPath as 'Path', displayName as 'Display', out.value as 'Output Value' from control:NumericPoint where displayName like '*ZN_T*' or displayName like '*STPT*'
    local:|fox:|station:|slot:/Exercises|bql:select * where slotPath like '*TrafficLight1'

    Http Queries - Must be using Basic Hx Profile
    http://192.168.7.194/ord?station:|alarm:/|bql:select%20*
    http://192.168.7.194/ord?station:|alarm:/|bql:select%20timestamp,%20source%20where%20source%20like%20%27*Temperature*%27
    http://192.168.7.194/ord?station:|history:/SqlDemo|bql:select%20*
    http://192.168.7.194/ord?station:|history:/SqlDemo/ZoneTemperature|bql:select%20*
    http://192.168.7.194/ord?station:|history:/SqlDemo/ZoneTemperature|bql:select%20status,%20value%20where%20timestamp%20in%20bqltime.lastweek     (for this you can also use bqltime.lastmonth, bqltime.last7days, etc)
    http://192.168.7.194/ord?station:|history:/SqlDemo/ZoneTemperature?period=timeRange;start=2016-04-05T00:00:00.000-00:00;end=2016-04-17T00:00:00.000-00:00|bql:select%20status,%20value,%20timestamp
    http://192.168.7.194/ord?station:|history:/SqlDemo/AuditHistory|bql:select%20*
    http://192.168.7.194/ord?station:|slot:/|bql:select%20name,out.value%20as%20%27Absolute%20Ord%27%20from%20control:NumericPoint
 */