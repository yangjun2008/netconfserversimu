package com.anonymous.tool.netconfserver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

public class MocDataParser
{
  private static final Pattern rpcPattern = Pattern.compile("<rpc[\\s\\S]*</rpc>");
  private static final Pattern rpcMessageIdPattern = Pattern.compile("message-id=\".*\"");
  private static final Pattern numberPattern = Pattern.compile("(\\d+)");
  private static final Pattern rpcReplyPattern = Pattern.compile("<rpc-reply[\\s\\S]*</rpc-reply>");

  public static List<MocDataItem> parse(File file) throws DocumentException {
    List<MocDataItem> ret = new ArrayList<MocDataItem>();

    SAXReader reader = new SAXReader();
    Document doc = reader.read(file);

    List<Node> nodes = doc.selectNodes("/mockdata/item");

    for (Node node : nodes)
    {
      String nodeContent = node.asXML();
      MocDataItem item = new MocDataItem();
      item.query = new MockQuery();
      item.reply = new MockReply();
      Matcher queryMatcher = rpcPattern.matcher(nodeContent);
      if (queryMatcher.find())
      {
        item.query.setRawXML(queryMatcher.group());
        Matcher msgIdMatcher = rpcMessageIdPattern.matcher(nodeContent);
        if (msgIdMatcher.find()) {
          Matcher msgIdNumberMatcher = numberPattern.matcher(msgIdMatcher.group());
          if (msgIdNumberMatcher.find()) {
            item.query.setMessageId(msgIdNumberMatcher.group());
          }
        }

      }

      Matcher replyMatcher = rpcReplyPattern.matcher(nodeContent);
      if (replyMatcher.find()) {
        item.reply.setRawXML(replyMatcher.group());
      }
      ret.add(item);
    }

    return ret;
  }

  public static void main(String[] args) throws DocumentException {
    File file = new File("mockdata/example_1.xml");

    parse(file);
  }
}