package com.anonymous.tool.netconfserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.dom4j.DocumentException;

import net.i2cat.netconf.rpc.RPCElement;
import net.i2cat.netconf.server.Behaviour;
import net.i2cat.netconf.server.ServerEx;

public class NetconfServer
{
  public static void main(String[] args)
    throws IOException, DocumentException
  {
    Locale.setDefault(new Locale("en", "US"));

    if (args.length < 2) {
      System.out.println("Error: must have IP address and port.");
      System.exit(0);
    }

    ServerEx server = ServerEx.createServerStoringMessages(args[0], Integer.parseInt(args[1]));

    List<MocDataItem> allItems = loadMocData(new File("mockdata"));
    Behaviour behaviour;
    for (MocDataItem item : allItems) {
      behaviour = new Behaviour(item.query, item.reply);
      server.defineBehaviour(behaviour);
    }

    server.startServer();

    BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));

    while (true) {
		if (buffer.readLine().equalsIgnoreCase("EXIT")) {
			break;
		}

		System.out.println("Messages received(" + server.getStoredMessages().size() + "):");
		for (RPCElement rpcElement : server.getStoredMessages()) {
			System.out.println("#####  BEGIN message #####\n" +
					rpcElement.toXML() + '\n' +
					"#####   END message  #####");
		}
	}

    System.out.println("Exiting");
	System.exit(0);
  }

  private static List<MocDataItem> loadMocData(File file) throws IOException, DocumentException {
	List<MocDataItem> allItems = new ArrayList<MocDataItem>();
    if (file.isDirectory()) {
      File[] subFiles = file.listFiles();

      for (File childFile : subFiles) {
        allItems.addAll(loadMocData(childFile));
      }
    }
    else if (file.getName().endsWith(".xml"))
    {
      allItems.addAll(MocDataParser.parse(file));
    }
    return allItems;
  }
}