/**
 * Copyright 2019 bitpool, All Rights Reserved.
 */
package com.bitpool.mqtt.message;

import com.tridium.ndriver.comm.*;


/**
 * MqttMessageFactory implementation of IMessageFactory.
 *
 * @author   Admin
 * @creation 25-Feb-19 
 */
public class MqttMessageFactory
  implements IMessageFactory
{
  
  public MqttMessageFactory() {}
  
  public NMessage makeMessage(LinkMessage lm) 
      throws Exception
  {
    //
    // TODO - convert linkMessage driver specific NMessage
    return null;
  }

}
