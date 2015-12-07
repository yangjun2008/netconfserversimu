package com.anonymous.tool.netconfserver;

import net.i2cat.netconf.rpc.Reply;

@SuppressWarnings("serial")
public class MockReply extends Reply
{
  private String rawXml;

  public void setRawXML(String rawXml)
  {
    this.rawXml = rawXml;
  }

  public String toXML()
  {
    return this.rawXml;
  }
}
