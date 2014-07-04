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

  public void setAttribute(String attrName, Value attrValue) {
    attributes.put(attrName, attrValue);
  }


  public Port getPort(String portName) throws UndeclaredIdentifierException{
    if (ports.containsKey(portName)){
      return ports.get(portName);
    } else {
      throw new UndeclaredIdentifierException(
        "no port named '" + portName + "'"
      );
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
  
  public void setPortAttributes(
      String portName,
      String attrName,
      Value attrValue
  ) throws UndeclaredIdentifierException {

    if (ports.containsKey(portName)) {
      ports.get(portName).setAttribute(attrName, attrValue);
    } else {
      throw new UndeclaredIdentifierException(
        "no port named '" + portName + "'"
      );
    }
  }

  public Node(NodeType type){
    super(type);
    this.attributes = new Attributes();
    this.ports = new HashMap<>();
    this.reversePorts = new HashMap<>();

    if (type.getPorts() != null) {
      for (Map.Entry<String, PortType> portEntry : type.getPorts().entrySet()) {
        Port p = new Port(portEntry.getValue(), this); 
        this.ports.put(
          portEntry.getKey(),
          p
        );
        this.reversePorts.put(p, portEntry.getKey());
      }
    }
  }

}
