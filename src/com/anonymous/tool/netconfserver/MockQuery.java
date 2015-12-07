package com.anonymous.tool.netconfserver;

import net.i2cat.netconf.rpc.Query;

@SuppressWarnings("serial")
public class MockQuery extends Query
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