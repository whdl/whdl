package org.manifold.backend.digital;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.manifold.intermediate.BooleanType;
import org.manifold.intermediate.BooleanValue;
import org.manifold.intermediate.Connection;
import org.manifold.intermediate.ConnectionType;
import org.manifold.intermediate.MultipleAssignmentException;
import org.manifold.intermediate.MultipleDefinitionException;
import org.manifold.intermediate.Node;
import org.manifold.intermediate.NodeType;
import org.manifold.intermediate.PortType;
import org.manifold.intermediate.Schematic;
import org.manifold.intermediate.Type;
import org.manifold.intermediate.UndeclaredIdentifierException;

@RunWith(value = Parameterized.class)
public class TestNetlist {
  
  private static PortType digitalInPortType;
  private static PortType digitalOutPortType;
  
  private static final Map<String, Type> noAttributes = new HashMap<String,Type>();
  
  private static Map<String, Type> registerTypeAttributes = new HashMap<String, Type>();
  private static Map<String, PortType> registerTypePorts = new HashMap<String, PortType>();
  private static NodeType registerType;
  
  private static Map<String, PortType> inputPinTypePorts = new HashMap<String, PortType>();
  private static NodeType inputPinType;
  
  private static Map<String, PortType> outputPinTypePorts = new HashMap<String, PortType>();
  private static NodeType outputPinType;
  
  private static ConnectionType digitalWireType;
  
  
  @BeforeClass
  public static void setupIntermediateTypes(){
    
    digitalInPortType = new PortType(noAttributes);
    digitalOutPortType = new PortType(noAttributes);
    
    registerTypeAttributes.put("initialValue", BooleanType.getInstance());
    registerTypeAttributes.put("resetActiveHigh", BooleanType.getInstance());
    registerTypeAttributes.put("resetAsynchronous", BooleanType.getInstance());
    registerTypeAttributes.put("clockActiveHigh", BooleanType.getInstance());
    registerTypePorts.put("in", digitalInPortType);
    registerTypePorts.put("out", digitalOutPortType);
    registerTypePorts.put("clock", digitalInPortType);
    registerTypePorts.put("reset", digitalInPortType);
    registerType = new NodeType(registerTypeAttributes, registerTypePorts);
    
    inputPinTypePorts.put("out", digitalOutPortType);
    inputPinType = new NodeType(noAttributes, inputPinTypePorts);
    
    outputPinTypePorts.put("in", digitalInPortType);
    outputPinType = new NodeType(noAttributes, outputPinTypePorts);
    
    digitalWireType = new ConnectionType(noAttributes);
  }
  
  public static Schematic instantiateSchematic(String name) throws MultipleDefinitionException{
    Schematic s = new Schematic(name);
    s.addPortType("digitalIn", digitalInPortType);
    s.addPortType("digitalOut", digitalOutPortType);
    
    s.addNodeType("register", registerType);
    s.addNodeType("inputPin", inputPinType);
    s.addNodeType("outputPin", outputPinType);
    
    s.addConnectionType("digitalWire", digitalWireType);
    return s;
  }
  
  public static Node instantiateRegister(boolean initialValue, boolean resetActiveHigh, boolean resetAsynchronous, boolean clockActiveHigh){
    Node register = new Node(registerType);
    
    register.setAttribute("initialValue", new BooleanValue(BooleanType.getInstance(), initialValue));
    register.setAttribute("resetActiveHigh", new BooleanValue(BooleanType.getInstance(), resetActiveHigh));
    register.setAttribute("resetAsynchronous", new BooleanValue(BooleanType.getInstance(), resetAsynchronous));
    register.setAttribute("clockActiveHigh", new BooleanValue(BooleanType.getInstance(), clockActiveHigh));
    
    return register;
  }
  
  public static Node instantiateInputPin(){
    Node inputPin = new Node(inputPinType);
    return inputPin;
  }
  
  public static Node instantiateOutputPin(){
    Node outputPin = new Node(outputPinType);
    return outputPin;
  }
  
  public static Connection instantiateWire(org.manifold.intermediate.Port from, org.manifold.intermediate.Port to){
    Connection wire = new Connection(digitalWireType, from, to);
    return wire;
  }

  @Parameters
  public static Collection<Object[]> testSchematics() throws MultipleDefinitionException, UndeclaredIdentifierException, MultipleAssignmentException {
    List<Object[]> data = new LinkedList<Object[]>();
    
    // BEGIN CASE 0
    // [digitalIn] -> [digitalOut]
    {
      Schematic case0 = instantiateSchematic("case0");
      Node in = instantiateInputPin();
      Node out = instantiateOutputPin();
      Connection in_to_out = instantiateWire(in.getPort("out"), out.getPort("in"));
      case0.addNode("in", in);
      case0.addNode("out", out);
      case0.addConnection("in_to_out", in_to_out);
      
      Object[] params = { case0, };
      data.add(params);
    }
    // END CASE 1
    
    return data;
  }
  
  // per-test input variables
  private Schematic _schematic;
  
  // single-run test output from the constructor
  private Netlist _netlist;
  
  // parameterized test constructor
  public TestNetlist(Schematic _schematic) throws NetlistConstructionException{
    this._schematic = _schematic;
    
    _netlist = new Netlist(_schematic);
  }
  
  /**
   * Verify that every netlist primitive was created from a schematic node.
   */
  
  @Test
  public void testPrimitiveTranslation_IsOnto(){
    Map<String, Boolean> checklist = new HashMap<String, Boolean>();
    for(Primitive prim : _netlist.getPrimitives()){
      checklist.put(prim.getName(), false);
    }
    
    for(String nodeName : _schematic.getNodes().keySet()){
      if(checklist.containsKey(nodeName)){
        checklist.put(nodeName, true);
      }
    }
    
    for(Map.Entry<String, Boolean> entry : checklist.entrySet()){
      if(entry.getValue() == false){
        fail("no corresponding node found for primitive '" + entry.getKey() + "'");
      }
    }
  }
  
  /**
   * Verify that every schematic node created a corresponding netlist primitive.
   */
  
  @Test
  public void testPrimitiveTranslation_IsOneToOne(){
    Map<String, Boolean> checklist = new HashMap<String, Boolean>();
    
    for(Map.Entry<String, Node> nodeEntry : _schematic.getNodes().entrySet()){
      String nodeName = nodeEntry.getKey();
      Node node = nodeEntry.getValue();
      checklist.put(nodeName, false);
    }
    
    for(Primitive prim : _netlist.getPrimitives()){
      if(checklist.containsKey(prim.getName())){
        checklist.put(prim.getName(), true);
      }
    }
    
    for(Map.Entry<String, Boolean> entry : checklist.entrySet()){
      if(entry.getValue() == false){
        fail("no corresponding primitive found for node '" + entry.getKey() + "'");
      }
    }
  }

}
