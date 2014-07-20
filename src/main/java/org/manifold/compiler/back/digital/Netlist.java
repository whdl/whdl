package org.manifold.compiler.back.digital;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.manifold.compiler.ConnectionType;
import org.manifold.compiler.ConnectionValue;
import org.manifold.compiler.PortTypeValue;
import org.manifold.compiler.PortValue;
import org.manifold.compiler.TypeMismatchException;
import org.manifold.compiler.UndeclaredIdentifierException;
import org.manifold.compiler.middle.Schematic;

import com.google.common.collect.ImmutableMap;

public class Netlist {
  
  private ConnectionType digitalWireType;
  public ConnectionType getDigitalWireType() {
    return digitalWireType;
  }
  private PortTypeValue digitalInType;
  public PortTypeValue getDigitalInType() {
    return digitalInType;
  }
  private PortTypeValue digitalOutType;
  public PortTypeValue getDigitalOutType() {
    return digitalOutType;
  }
  
  private Map<String, Net> nets = new HashMap<>();
  public Map<String, Net> getNets() {
    return ImmutableMap.copyOf(nets);
  }
  private Map<PortValue, Net> connectedNet = new HashMap<>();
  public Net getConnectedNet(PortValue port) {
    if (connectedNet.containsKey(port)) {
      return connectedNet.get(port);
    } else {
      // TODO(murphy) throw an exception?
      return null;
    }
  }
  
  public Netlist(Schematic schematic) 
      throws UndeclaredIdentifierException, TypeMismatchException {
    digitalWireType = schematic.getConnectionType("digitalWire");
    digitalInType = schematic.getPortType("digitalIn");
    digitalOutType = schematic.getPortType("digitalOut");
    
    // iterate over connections in the schematic and build nets
    for (Entry<String, ConnectionValue> connEntry 
        : schematic.getConnections().entrySet()) {
      String connectionName = connEntry.getKey();
      ConnectionValue connection = connEntry.getValue();
      
      verify_ConnectionIsDigitalWire(connection);
      
      // get both ports
      PortValue portFrom = connection.getFrom();
      PortValue portTo = connection.getTo();
      
      verify_PortIsDigitalIO(portFrom);
      verify_PortIsDigitalIO(portTo);
      
      // now we can take both ports and attach them to a net
      if (connectedNet.containsKey(portFrom)) {
        Net existingNet = connectedNet.get(portFrom);
        if (connectedNet.containsKey(portTo)) {
          // both ports already connected to a net
        } else {
          connectToNet(portTo, existingNet);
        }
      } else {
        // portFrom not connected to a net
        if (connectedNet.containsKey(portTo)) {
          Net existingNet = connectedNet.get(portTo);
          connectToNet(portFrom, existingNet);
        } else {
          // neither port is connected to a net, so create a new one
          String netName = "n_" + connectionName;
          Net newNet = new Net(netName);
          nets.put(netName, newNet);
          connectToNet(portFrom, newNet);
          connectToNet(portTo, newNet);
        }
      }
    }
  }
  
  private void verify_ConnectionIsDigitalWire(ConnectionValue connection) 
      throws TypeMismatchException {
    if (!connection.getType().equals(digitalWireType)) {
      throw new TypeMismatchException(digitalWireType, connection.getType());
    }
  }
  
  private void verify_PortIsDigitalIO(PortValue port) 
      throws TypeMismatchException {
    if (!(port.getType().equals(digitalInType) 
        || port.getType().equals(digitalOutType))) {
      // TODO this exception is inaccurate because there are two valid types
      throw new TypeMismatchException(digitalInType, port.getType()); 
    }
  }
  
  private void connectToNet(PortValue port, Net net){
    net.addPort(port);
    connectedNet.put(port, net);
  }
}
