package org.manifold.backend.digital;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.BiFunction;

import org.manifold.intermediate.Connection;
import org.manifold.intermediate.ConnectionType;
import org.manifold.intermediate.Node;
import org.manifold.intermediate.PortType;
import org.manifold.intermediate.Schematic;
import org.manifold.intermediate.NodeType;
import org.manifold.intermediate.UndeclaredIdentifierException;

/**
 * Representation of a digital circuit as a collection of
 * Primitives (equivalent to IR Nodes) and Nets (built from IR Connections).
 */
public class Netlist {
  
  @FunctionalInterface
  interface BiFunctionConstructor<T, U, R> {
    R apply(T t, U u) throws PrimitiveConstructionException;
  }
  
  /**
   * Associates intermediate node types with functions that can be used to construct named primitives of that kind of node.
   */
  private Map<NodeType, BiFunctionConstructor<String, Node,Primitive> > primitiveConstructor = new HashMap<NodeType, BiFunctionConstructor<String, Node,Primitive> >();
  
  private Map<Node, Primitive> translatedPrimitives = new HashMap<Node, Primitive>();
  public Set<Primitive> getPrimitives(){
    return java.util.Collections.unmodifiableSet(new HashSet<Primitive>(translatedPrimitives.values()));
  }
  
  private Set<Net> nets = new HashSet<Net>();
  public Set<Net> getNets(){
    return java.util.Collections.unmodifiableSet(nets);
  }
  
  private ConnectionType wireConnectionType;
  
  private PortType inPortType;
  private PortType outPortType;
  
  public Netlist(Schematic schematic) throws NetlistConstructionException{
    // start by examining the Schematic's definitions for certain node types we are interested in seeing
    try{
      primitiveConstructor.put(schematic.getNodeType("register"), (name, node) -> {
        return new RegisterPrimitive(name, node);
      });
      primitiveConstructor.put(schematic.getNodeType("inputPin"), (name, node) -> {
        return new InputPinPrimitive(name, node);
      });
      primitiveConstructor.put(schematic.getNodeType("outputPin"), (name, node) -> {
        return new OutputPinPrimitive(name, node);
      });
    }catch(UndeclaredIdentifierException uie){
      throw new NetlistConstructionException(uie.getMessage() + " while searching for node types");
    }
    
    // now look for connection types
    try{
      wireConnectionType = schematic.getConnectionType("digitalWire");
    }catch(UndeclaredIdentifierException uie){
      throw new NetlistConstructionException(uie.getMessage() + " while searching for connection types");
    }
    
    // now look for port types
    try{
      inPortType = schematic.getPortType("digitalIn");
      outPortType = schematic.getPortType("digitalOut");
    }catch(UndeclaredIdentifierException uie){
      throw new NetlistConstructionException(uie.getMessage() + " while searching for port types");
    }
    
    // now, assuming that went well, we can build a lookup table from Node -> Primitive
    for(Map.Entry<String, Node> e : schematic.getNodes().entrySet()){
      String nodeName = e.getKey();
      Node node = e.getValue();
      NodeType nodeType = (NodeType)node.getType();
      if(primitiveConstructor.containsKey(nodeType)){
        Primitive prim = primitiveConstructor.get(nodeType).apply(nodeName, node);
        translatedPrimitives.put(node, prim);
      }else{
        throw new NetlistConstructionException("node '" + nodeName + "' has unknown type");
      }
    }
    
    // Every digitalWire in the schematic is a statement that two ports are connected to the same net.
    // So, we can iterate over connections (that are digitalWires) and build nets by following these steps:
    // * Translate each schematic Port to a netlist Port corresponding to an existing primitive.
    // * CASE 1: If both ports are part of a net, nothing to do.
    // * CASE 2: If neither port is part of a net, create a new net and attach both ports to it.
    // * CASE 3: If one port is part of a net and the other is not, attach the unconnected port to the same net as the other port.
    for(Map.Entry<String, Connection> e : schematic.getConnections().entrySet()){
      String connectionName = e.getKey();
      Connection connection = e.getValue();
      // check type
      if(!connection.getType().equals(wireConnectionType)){
        throw new NetlistConstructionException("connection '" + connectionName + "' has unknown type");
      }
      
      org.manifold.intermediate.Port schematicPortFrom = connection.getFrom();
      if(! (schematicPortFrom.getType().equals(inPortType) || schematicPortFrom.getType().equals(outPortType))){
        throw new NetlistConstructionException("'from' port on connection '" + connectionName + "' has unknown type");
      }
      org.manifold.intermediate.Port schematicPortTo = connection.getTo();
      if(! (schematicPortTo.getType().equals(inPortType) || schematicPortTo.getType().equals(outPortType))){
        throw new NetlistConstructionException("'to' port on connection '" + connectionName + "' has unknown type");
      }
      
      Port netlistPortFrom = translatePort(schematicPortFrom);
      Port netlistPortTo = translatePort(schematicPortTo);
      
      Net netFrom = netlistPortFrom.getNet();
      Net netTo = netlistPortTo.getNet();
      
      if(netFrom == null){
        // portFrom is not connected to a net
        if(netTo == null){
          // portTo is not connected to a net
          // CASE 2: neither port is part of a net
          Net n = new Net(connectionName);
          n.addPort(netlistPortFrom);
          n.addPort(netlistPortTo);
        }else{
          // portTo is connected to a net
          // CASE 3: one port is connected and the other is not
          netlistPortTo.getNet().addPort(netlistPortFrom);
        }
      }else{
        // portFrom is connected to a net
        if(netTo == null){
          // portTo is not connected to a net
          // CASE 3: one port is connected and the other is not
          netlistPortFrom.getNet().addPort(netlistPortTo);
        }else{
          // portTo is connected to a net
          // CASE 1: both ports are connected
          // do nothing (this may be an error in later DRC)
        }
      }
      
    }
  }
  
  /**
   * Translate a schematic port to a netlist port.
   * Each Port knows its parent Node; we have a map from Node to Primitive; and if we can get the name of
   * the Port from its Node, we can use that name to find the corresponding Port in the Primitive.
   * @throws NetlistConstructionException 
   */
  private Port translatePort(org.manifold.intermediate.Port schPort) throws NetlistConstructionException{
    Node schParentNode = schPort.getParent();
    Primitive primitive = translatedPrimitives.get(schParentNode);
    String portName;
    try{
      portName = schParentNode.getPortName(schPort);
    }catch(NoSuchElementException nsee){
      throw new NetlistConstructionException("internal error: unable to look up name of port belonging to node");
    }
    Port primitivePort = primitive.getPort(portName);
    return primitivePort;
  }
  
}
