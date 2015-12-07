package net.i2cat.netconf.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;

import net.i2cat.netconf.rpc.RPCElement;
import net.i2cat.netconf.server.exceptions.ServerException;
import net.i2cat.netconf.server.netconf.NetconfSubsystemEx;
import net.i2cat.netconf.server.ssh.AlwaysTruePasswordAuthenticator;

public class ServerEx
  implements MessageStore, BehaviourContainer
{
  private static final Log log = LogFactory.getLog(Server.class);
  private SshServer sshd;
  private boolean storeMessages = false;
  private List<RPCElement> messages;
  private List<Behaviour> behaviours;

  public static ServerEx createServer(int listeningPort)
  {
    ServerEx server = new ServerEx();
    server.storeMessages = false;

    server.initializeServer("localhost", listeningPort);

    return server;
  }

  public static ServerEx createServer(String host, int listeningPort)
  {
    ServerEx server = new ServerEx();
    server.storeMessages = false;

    server.initializeServer(host, listeningPort);

    return server;
  }

  public static ServerEx createServerStoringMessages(int listeningPort)
  {
    ServerEx server = new ServerEx();
    server.messages = new ArrayList<RPCElement>();
    server.storeMessages = true;

    server.initializeServer("localhost", listeningPort);

    return server;
  }

  public static ServerEx createServerStoringMessages(String host, int listeiningPort)
  {
    ServerEx server = new ServerEx();
    server.messages = new ArrayList<RPCElement>();
    server.storeMessages = true;

    server.initializeServer(host, listeiningPort);

    return server;
  }

  private void initializeServer(String host, int listeningPort) {
    log.info("Configuring server...");
    this.sshd = SshServer.setUpDefaultServer();
    this.sshd.setHost(host);
    this.sshd.setPort(listeningPort);

    log.info("Host: '" + host + "', listenig port: " + listeningPort);

    this.sshd.setPasswordAuthenticator(new AlwaysTruePasswordAuthenticator());
    this.sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(""));

    List<NamedFactory<Command>> subsystemFactories = new ArrayList<NamedFactory<Command>>();
	subsystemFactories.add(NetconfSubsystemEx.Factory.createFactory(this, this));
	sshd.setSubsystemFactories(subsystemFactories);

    log.info("Server configured.");
  }

  public void defineBehaviour(Behaviour behaviour)
  {
    if (this.behaviours == null) {
      this.behaviours = new ArrayList<Behaviour>();
    }
    synchronized (this.behaviours) {
      this.behaviours.add(behaviour);
    }
  }

  public List<Behaviour> getBehaviours()
  {
    if (this.behaviours == null) {
      return null;
    }
    synchronized (this.behaviours) {
      return this.behaviours;
    }
  }

  public void startServer() throws ServerException {
    log.info("Starting server...");
    try {
      this.sshd.start();
    } catch (IOException e) {
      log.error("Error starting server!", e);
      throw new ServerException("Error starting server", e);
    }
    log.info("Server started.");
  }

  public void stopServer() {
    log.info("Stopping server...");
    try {
      this.sshd.stop();
    } catch (InterruptedException e) {
      log.error("Error stopping server!");
      throw new ServerException("Error stopping server", e);
    }
    log.info("Server stopped.");
  }

  public void storeMessage(RPCElement message)
  {
    if (this.messages != null)
      synchronized (this.messages) {
        log.info("Storing message");
        this.messages.add(message);
      }
  }

  public List<RPCElement> getStoredMessages()
  {
    if (this.storeMessages) {
      synchronized (this.messages) {
        return Collections.unmodifiableList(this.messages);
      }
    }
    throw new ServerException(new UnsupportedOperationException("Server is configured to not store messages!"));
  }

}