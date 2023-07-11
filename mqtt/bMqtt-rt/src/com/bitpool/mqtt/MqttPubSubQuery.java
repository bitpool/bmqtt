package com.bitpool.mqtt;

import com.tridium.util.EscUtil;

import javax.baja.collection.BITable;
import javax.baja.collection.Column;
import javax.baja.collection.ColumnList;
import javax.baja.collection.TableCursor;
import javax.baja.hierarchy.BHierarchy;
import javax.baja.hierarchy.BHierarchyService;
import javax.baja.hierarchy.BLevelElem;
import javax.baja.hierarchy.HierarchyQuery;
import javax.baja.naming.BOrd;
import javax.baja.nav.BINavNode;
import javax.baja.sys.BComponent;
import javax.baja.sys.BRelation;
import javax.baja.sys.Sys;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class MqttPubSubQuery {
    private Map < String, String > m_Ords = null;
    private Map < String, String > m_OrdsType = null;
    private Map < String, String > m_OrdsPrev = null;
    private final Map < String, Integer > m_OrdsCount = new HashMap < > ();
    private Map < String, String > m_OrdsPollPrev = new HashMap < > ();
    private final Map < String, String > m_TopicsStationBroker = new HashMap < > ();
    private Map < String, String > m_Handles = null;
    private Map < String, String > m_Hierarchies = null;
    private Map < String, Set<String> > m_Relations = new HashMap < > ();

    public void prevOrdPollInitCache(){
        m_OrdsPollPrev = new HashMap < > ();
    }

    public void prevOrdPollAddToCache(String topic, String value){
        Integer indx = topic.indexOf("/points/") + 7;
        String keyStationOrdShort = unescapeOrd(topic.substring(indx));
        m_OrdsPollPrev.put(keyStationOrdShort, value);
        //System.out.println("ADD TO CACHE: " + keyStationOrdShort + " -> " + value);
    }
    public boolean prevOrdPollDoesTopicExist(String topic){
        String eTopic = EscUtil.ord.unescape(topic);
        return m_OrdsPollPrev.containsKey(eTopic);
    }
    public String prevOrdPollGetTopicValue(String topic){
        String eTopic = EscUtil.ord.unescape(topic);
        return m_OrdsPollPrev.get(eTopic);
    }
    public void showCache(){
        for (Map.Entry < String, String > entry: m_OrdsPollPrev.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            //System.out.println("CACHE: " + key + " -> " + value);
        }
    }

//    public void topicAddBrokerStationLookup(String brokerTopic, String stationTopic){
//        m_TopicsStationBroker.put(brokerTopic, stationTopic);
//    }
//    public boolean topicDoesStationTopicExist(String brokerTopic){
//        if (m_TopicsStationBroker.containsKey(brokerTopic)){
//            return true;
//        }
//        return false;
//    }
//    public String topicGetStationTopic(String brokerTopic){
//        return m_TopicsStationBroker.get(brokerTopic);
//    }

    private String unescapeOrd(String ord){
        return EscUtil.ord.unescape(ord);
    }
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
        m_Handles = new HashMap<> ();

        try {
            buildLookups(ordParent);
            return true;
        }catch (javax.baja.naming.UnresolvedException eU) {
            return false;
        }catch (Exception e) {
            System.out.println("Exception: Cannot build proxy extension lookup table");
            System.out.println("Failed ord: "+ordParent);
            e.printStackTrace();
            return false;
        }
    }

    public Boolean buildHierarchyLookup(BOrd hierarchyFile) {
       //System.out.println("Resolving Hierarchy: " + hierarchyFile);
        BHierarchyService hierarchyService = (BHierarchyService) Sys.getService(BHierarchyService.TYPE);
        m_Hierarchies = new HashMap< >();
        try {
            BHierarchy h = (BHierarchy)hierarchyFile.get();

            BLevelElem he = hierarchyService.resolveHierarchyLevelElem(new HierarchyQuery("/"+h.getNavName()));

            BINavNode[] firstLevelChildren = he.getNavChildren();
            for (int i=0; i<firstLevelChildren.length; i++) {
               // System.out.println("Initial: " + h.getNext());
                try {
                    printNavNode(firstLevelChildren[i]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
               // System.out.println("Hierarchy List: " + m_Hierarchies);
            } catch (Exception e) {
               // System.out.println("Exception while printing hierarchies: ");
                e.printStackTrace();
            }

            try {
              //  System.out.println("Handles List: " + m_Handles);
            } catch (Exception e) {
               // System.out.println("Exception while printing handles: ");
                e.printStackTrace();
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void printNavNode(BINavNode node) throws Exception {
        BComponent navComp = null;
        BLevelElem navElem = null;
        BComponent targetComponent = null;
        try {
            if(node instanceof BLevelElem) {
                Object obj = node.getNavOrd().get();
                if(obj instanceof BLevelElem) {
                    navElem = (BLevelElem) obj;
                    targetComponent = navElem.getTargetComponent();
                    if(targetComponent instanceof BComponent){
                       //System.out.println("Target Component: " + targetComponent);
                    }
                } else if(obj instanceof BComponent){
                    navComp = (BComponent) obj;
                    //System.out.println("Nav Object: " + navComp + " - OrdIn: "+ navComp.getOrdInSession() + " - " + navComp.getSlotPathOrd()) ;

                }
            } else if(node instanceof BComponent){
                navComp = (BComponent) node;
                //System.out.println("Nav Component: " + navComp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        String topic = EscUtil.ord.unescape(node.getNavOrd().toString());
        if(topic != null){
            if(containsMultipleStations(topic)){
                topic =  processStations(topic);
                //System.out.println("Changing topic: " + topic);
            }
        }
        if (targetComponent != null && targetComponent instanceof BComponent) {
            //System.out.println("Adding to Hierarchy: " + targetComponent.getHandleOrd().toString());
            //m_Hierarchies.put(targetComponent.getHandleOrd().toString(), EscUtil.ord.unescape(node.getNavOrd().toString()));
            m_Hierarchies.put(targetComponent.getHandleOrd().toString(), EscUtil.ord.unescape(topic));

        }
        if(navComp != null){
            //System.out.println("Adding to Hierarchy: Handle - " + navComp.getHandleOrd().toString() + ", Ord: - " + EscUtil.ord.unescape(node.getNavOrd().toString()));
            //m_Hierarchies.put(navComp.getHandleOrd().toString(), EscUtil.ord.unescape(node.getNavOrd().toString()));
            m_Hierarchies.put(navComp.getHandleOrd().toString(), EscUtil.ord.unescape(topic));
        }


        if (node.hasNavChildren()) {
            //System.out.println("Node has children.");
            BINavNode[] children = node.getNavChildren();
            for (int i = 0; i < children.length; i++) {
                try {
                    printNavNode(children[i]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            //System.out.println("Node has no children.");
        }
    }

    public static String processStations(String input) {
        String[] parts = input.split("/");

        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (part.startsWith("station:")) {
                // Process the station portion to resolve component name

                BOrd compName = BOrd.make(part);
                BComponent compComp = (BComponent)compName.get();
                String processedStation = compComp.getNavName();
                // Append the processed station portion to the result
                result.append(processedStation);
            } else {
                // Append other parts as is
                result.append(part);
            }
            result.append("/");
        }

        // Remove the trailing slash if necessary
        if (result.length() > 0 && result.charAt(result.length() - 1) == '/') {
            result.setLength(result.length() - 1);
        }

        return result.toString();
    }





    public static boolean containsMultipleStations(String input) {
        int count = 0;
        int index = input.indexOf("station:");

        while (index >= 0) {
            count++;
            index = input.indexOf("station:", index + 1);
        }

        return count > 1;
    }

    public String getNewTopic(String handle) {
        //System.out.println("Searching for "+ handle + " in m_Hierarchies");
        if (m_Hierarchies.containsKey(handle)) {
            //System.out.println("Found "+ handle + " in m_Hierarchies");

            return m_Hierarchies.get(handle);
        }
        return null;
    }
    public ArrayList<String> getRelations(String handle) {
        if (m_Relations.containsKey(handle)) {
            Set<String> relationSet = m_Relations.get(handle);
            return new ArrayList<>(relationSet); // Convert the Set to a List before returning
        }
        return null;
    }

    public String getExistingHandle(String topic) {
        //System.out.println("Searching for "+ topic + " in m_Handles");
        if (m_Handles.containsKey(topic)) {
            //System.out.println("Found "+ topic + " in m_Handles");

            return m_Handles.get(topic);
        }
        return null;
    }

    public Boolean doseOrdExist(String ord) {
        return m_Ords.containsKey(ord);
    }

    public String getOrdCurrentValue(String ord) {
        if (m_Ords.containsKey(ord)) {
            return m_Ords.get(ord);
        }
        return null;
    }
    public void setOrdCurrentValue(String ord, String value) {
        if (m_Ords.containsKey(ord)) {
            m_Ords.replace(ord, value);
        }
        return;
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
            String bqlStr = "bql:select navOrd, type, out.value, handleOrd from control:ControlPoint where slotExists('out')";
            String fullPath = ordParent + "|" + bqlStr;
            //System.out.println(bqlStr);
            BOrd ord = BOrd.make(fullPath);
            return (BITable) ord.resolve().get();
        });

        ColumnList columns = result.getColumns();
//        Column[] col = columns.list();
////        for (int i = 0; i < col.length; i++) {
////            System.out.println(col[i].getName());
////        }
        Column colNavOrd = columns.get("navOrd");
        Column colOutValue = columns.get("out.value");
        Column colType = columns.get("type");
        Column colHand= columns.get("handleOrd");

        TableCursor cursorTable = result.cursor();
        while (cursorTable.next()) {
            String keyOrd = cursorTable.cell(colNavOrd).toString();
            String keyValue = cursorTable.cell(colOutValue).toString();
            String keyType = cursorTable.cell(colType).toString();
            String keyHand = cursorTable.cell(colHand).toString();
            BOrd relOrd = (BOrd)cursorTable.cell(colNavOrd);
            BComponent relComp = (BComponent)relOrd.get() ;
            //System.out.println("Knobs: "+relComp.getRelationKnobCount());
            try {
                BRelation[] compRel = relComp.getComponentRelations();
                if (compRel != null) { // Check if the array is not null before using it
                    for (BRelation bRelation : compRel) {
                        if (bRelation != null && (bRelation.getType() == BRelation.TYPE) ) {
                            //System.out.println("Component relations: "+bRelation);
                            String relationStr = bRelation.toString();
                            //System.out.println("Relation Sting: "+relationStr);
                            String direction = getDirection(relationStr);
                            String reference = getReference(relationStr);
                            String endpoint = getEndpoint(relationStr);
                            String relationData = direction + "," + reference + "," + endpoint;
                            //System.out.println("Relation data: "+relationData);
                            Set<String> set;
                            if (m_Relations.containsKey(keyHand)) {
                                //System.out.println("Relation map contains key");
                                set = m_Relations.get(keyHand);
                            } else {
                                //System.out.println("Relation map does not contain key");
                                set = new HashSet<>();  // or new LinkedHashSet<>() if you want to preserve insertion order
                                m_Relations.put(keyHand, set);
                            }
                            //System.out.println("Adding relation data to list");
                            set.add(relationData);
                        } else {
                            //ystem.out.println("bRelation is null");
                        }
                    }
                } else {
                    //System.out.println("Component Relations Array is null");
                }
            } catch (Exception e) {
                //System.out.println("Exception caught: " + e);
            }


            //System.out.println(keyOrd + " - "+  keyHand);
            //System.out.println(keyOrd + " - "+ keyValue + " - " + keyType + " - " + keyHand);
            m_Ords.put(EscUtil.ord.unescape(keyOrd), keyValue);
            m_OrdsType.put(EscUtil.ord.unescape(keyOrd), keyType);
            m_Handles.put(EscUtil.ord.unescape(keyOrd), keyHand);
            //System.out.println("Relations map: "+ m_Relations);
        }
    }

    private String getDirection(String relation) {
        Pattern pattern = Pattern.compile("\\((.*?)\\)");
        Matcher matcher = pattern.matcher(relation);
        if (matcher.find()) {
            String direction = matcher.group(1); // Get the string inside the brackets
            //System.out.println("Direction extracted: " + direction);
            return direction;
        }
        //System.out.println("No direction found in: " + relation);
        return null; // No direction found
    }

    private String getReference(String relation) {
        Pattern pattern = Pattern.compile("<(.*?)>");
        Matcher matcher = pattern.matcher(relation);
        if (matcher.find()) {
            String reference = matcher.group(1); // Get the string inside the <>
            //System.out.println("Reference extracted: " + reference);
            return reference;
        }
        //System.out.println("No reference found in: " + relation);
        return null; // No reference found
    }

    private String getEndpoint(String relation) {
        Pattern pattern = Pattern.compile("h:(.*)");
        Matcher matcher = pattern.matcher(relation);
        if (matcher.find()) {
            String endpoint = "h:" + matcher.group(1).trim(); // Get the string after h:
            //System.out.println("Endpoint extracted: " + endpoint);
            return endpoint;
        }
        //System.out.println("No endpoint found in: " + relation);
        return null; // No endpoint found
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