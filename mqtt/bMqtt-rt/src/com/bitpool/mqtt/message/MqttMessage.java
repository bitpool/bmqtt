/**
 * Copyright 2019 bitpool, All Rights Reserved.
 */
package  com.bitpool.mqtt.message;

import com.tridium.ndriver.comm.NMessage;
import com.tridium.ndriver.datatypes.BAddress;

/**
 *  MqttMessage is super class for all mqtt messages
 *
 *  @author   Admin
 *  @creation 25-Feb-19 
 */
public class MqttMessage
  extends NMessage
{

  
  public MqttMessage (BAddress address)
  {
    super(address);
  }

  
  // Override for outgoing messages
//  public boolean toOutputStream(OutputStream out) 
//    throws Exception
//  {
//    // Use typed stream for more readable code.   
//    TypedOutputStream to = new TypedOutputStream();
//  
//    to.toOutputStream(out);
//    return false;
//  }
    
  //   Override for incoming messages
//  public void fromInputStream(InputStream in) 
//    throws Exception
//  {
//    // Use typed stream for more readable code.
//    // Note: messageFactory must have created TypedInputStream
//    TypedInputStream ti = (TypedInputStream)in;
//  }
    
  //   Typical overrides  
//  public Object getTag() { return nullTag; }
//  public boolean isResponse() { return false; }
//  public boolean isFragmentable() { return false; }
//  public int getResponseTimeOut() { return 2500; }
    
//  public String toTraceString()
//  {
//    return "??";
//  }

}
