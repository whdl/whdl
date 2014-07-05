package org.manifold.intermediate;

import java.util.HashMap;
import java.util.Map;

public class Node extends Value {

  private final Attributes attributes;
  private final Map<String, Port> ports;
  private final Map<Port, String> reversePorts;

  public Value getAttribute(String attrName)
      throws UndeclaredAttributeException {
    return attributes.get(attrName);
  }

  public Port getPort(String portName) throws UndeclaredIdentifierException{
    if (ports.containsKey(portName)){
      return ports.get(portName);
    } else {
      throw new UndeclaredIdentifierException(portName);
    }
  }

  public String getPortName(Port port) throws UndeclaredIdentifierException{
    if(reversePorts.containsKey(port)){
      return reversePorts.get(port);
    }else{
      // FIXME this is NOT the right exception to throw!
      throw new UndeclaredIdentifierException("no such port");
    }
  }

  public Node(NodeType type, Map<String, Value> attrs,
      Map<String, Map<String, Value>> portAttrMaps) throws SchematicException {
    super(type);
    this.attributes = new Attributes(type.getAttributes(), attrs);
    this.ports = new HashMap<>();
    this.reversePorts = new HashMap<>();

    final Map<String, PortType> portTypes = type.getPorts();
    if (portTypes != null) {
      for (String portName : portAttrMaps.keySet()) {
        if (!portTypes.containsKey(portName)) {
          throw new UndeclaredIdentifierException(portName);
        }
      }
      for (Map.Entry<String, PortType> portEntry : type.getPorts().entrySet()) {
        String portName = portEntry.getKey();
        PortType portType = portEntry.getValue();
        Map<String, Value> portAttrs = portAttrMaps.get(portName);
        if (portAttrs == null) {
          throw new InvalidIdentifierException(portName);
        }
        Port newPort = new Port(portType, this, portAttrs);
        this.ports.put(portName, newPort);
        this.reversePorts.put(newPort, portName);
      }
    }
  }
}
