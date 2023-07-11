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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MqttSubQuery {

    private Map<String, String> m_Ords = null;
    private Map<String, String> m_Topics = null;

    public boolean buildOrdLookup(String ordParent){

        if("".equals(ordParent)) {
            ordParent = "station:|slot:/Drivers";
        }
        m_Ords = new HashMap<>();
        m_Topics = new HashMap<>();

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
    public String getOrdIfExists(String ord){
        String ordRaw = EscUtil.ord.unescape(ord);
        if (m_Ords.containsKey(ordRaw)) {
            return m_Ords.get(ordRaw);
        }
        return null;
    }

    public ArrayList <String> getOrdsWithTopic(String findTopic) {
        ArrayList<String> ords = new ArrayList();
        for (Map.Entry < String, String > entry: m_Topics.entrySet()) {
            String pointOrd = "/" + entry.getKey();         // Only one unique ORD as this is a point
            String pointTopic = entry.getValue();           // Multiple topics of the same name
            if (findTopic.equals(pointTopic)) {
                // Only add the topic if it not the original
                if(pointOrd.equals(pointTopic) == false){
                    ords.add(entry.getKey());
                }
            }
        }
        return ords;
    }

    private void buildLookups(String ordParent) throws Exception {
        BITable result = AccessController.doPrivileged((PrivilegedAction<BITable>) () -> {
            String bqlStr = "bql:select navOrd, proxyExt.topic from control:ControlPoint where slotExists('out')";
            String fullPath = ordParent + "|" + bqlStr;
            BOrd ord = BOrd.make(fullPath);
            return (BITable) ord.resolve().get();
        });

        ColumnList columns = result.getColumns();
        Column colNavOrd = columns.get("navOrd");           //e.g. = '/Armin$27s$20Room/Group$201/Sensor$2055/Temperature'
        Column colPeTopic = columns.get("proxyExt.topic");

        TableCursor cursorTable = result.cursor();
        while (cursorTable.next()) {
            String pointOrd = cursorTable.cell(colNavOrd).toString();
            String valPeTopic = cursorTable.cell(colPeTopic).toString();

            // Remove prefix upto points folder
            // FROM local:|station:|slot:/Drivers/MqttNetwork/MqttSubDevice1/points/Drivers/SenseAgentNetwork....
            // TO   Drivers/SenseAgentNetwork....

            Integer indx = pointOrd.indexOf("/points/") + 8;
            String pointOrdShort = pointOrd.substring(indx);
            m_Ords.put(EscUtil.ord.unescape(pointOrdShort), pointOrd);

            // might need to be a pointOrd
            m_Topics.put(EscUtil.ord.unescape(pointOrdShort), valPeTopic);

        }
    }
}

