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
import org.manifold.intermediate.InvalidAttributeException;
import org.manifold.intermediate.MultipleAssignmentException;
import org.manifold.intermediate.MultipleDefinitionException;
import org.manifold.intermediate.Node;
import org.manifold.intermediate.NodeType;
import org.manifold.intermediate.PortType;
import org.manifold.intermediate.Schematic;
import org.manifold.intermediate.SchematicException;
import org.manifold.intermediate.Type;
import org.manifold.intermediate.UndeclaredAttributeException;
import org.manifold.intermediate.UndeclaredIdentifierException;
import org.manifold.intermediate.Value;

@RunWith(value = Parameterized.class)
public class TestNetlist {
  
  private static PortType digitalInPortType;
  private static PortType digitalOutPortType;
  
  private static final Map<String, Type> noTypeAttributes = new HashMap<String,Type>();
  private static final Map<String, Value> noAttributes = new HashMap<>();
  
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
    
    digitalInPortType = new PortType(noTypeAttributes);
    digitalOutPortType = new PortType(noTypeAttributes);
    
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
    inputPinType = new NodeType(noTypeAttributes, inputPinTypePorts);
    
    outputPinTypePorts.put("in", digitalInPortType);
    outputPinType = new NodeType(noTypeAttributes, outputPinTypePorts);
    
    digitalWireType = new ConnectionType(noTypeAttributes);
  }
  
  /**
   * Instantiate a Schematic, but with varying degrees of "completeness" in terms of
   * what object types are included. If types are missing, it will not be possible
   * to construct a Netlist.
   * @throws MultipleDefinitionException 
   */
  public static Schematic instantiateSchematic(String name, boolean includePortTypes, boolean includeNodeTypes, boolean includeConnectionTypes) throws MultipleDefinitionException{
    Schematic s = new Schematic(name);
    if(includePortTypes){
      s.addPortType("digitalIn", digitalInPortType);
      s.addPortType("digitalOut", digitalOutPortType);
    }
    
    if(includeNodeTypes){
      s.addNodeType("register", registerType);
      s.addNodeType("inputPin", inputPinType);
      s.addNodeType("outputPin", outputPinType);
    }
    
    if(includeConnectionTypes){
      s.addConnectionType("digitalWire", digitalWireType);
    }
    
    return s;
  }
  
  /**
   * Instantiate a Schematic and include all object types.
   */
  public static Schematic instantiateSchematic(String name) throws MultipleDefinitionException{
    return instantiateSchematic(name, true, true, true);
  }
  
  public static Node instantiateRegister(boolean initialValue, boolean resetActiveHigh, boolean resetAsynchronous, boolean clockActiveHigh) throws SchematicException{
    Map<String, Value> registerAttrs = new HashMap<>();
    registerAttrs.put("initialValue", new BooleanValue(BooleanType.getInstance(), initialValue));
    registerAttrs.put("resetActiveHigh", new BooleanValue(BooleanType.getInstance(), resetActiveHigh));
    registerAttrs.put("resetAsynchronous", new BooleanValue(BooleanType.getInstance(), resetAsynchronous));
    registerAttrs.put("clockActiveHigh", new BooleanValue(BooleanType.getInstance(), clockActiveHigh));
    Map<String, Map<String, Value>> registerPortAttrs = new HashMap<>();
    registerPortAttrs.put("in", noAttributes);
    registerPortAttrs.put("out", noAttributes);
    registerPortAttrs.put("clock", noAttributes);
    registerPortAttrs.put("reset", noAttributes);
    Node register = new Node(registerType, registerAttrs, registerPortAttrs);
    return register;
  }
  
  public static Node instantiateInputPin() throws SchematicException{
    Map<String, Map<String, Value>> inputPinPortAttrs = new HashMap<>();
    inputPinPortAttrs.put("out", noAttributes);
    Node inputPin = new Node(inputPinType, noAttributes, inputPinPortAttrs);
    return inputPin;
  }
  
  public static Node instantiateOutputPin() throws SchematicException{
    Map<String, Map<String, Value>> outputPinPortAttrs = new HashMap<>();
    outputPinPortAttrs.put("in", noAttributes);
    Node outputPin = new Node(outputPinType, noAttributes, outputPinPortAttrs);
    return outputPin;
  }
  
  public static Connection instantiateWire(org.manifold.intermediate.Port from, org.manifold.intermediate.Port to) throws UndeclaredAttributeException, InvalidAttributeException{
    Connection wire = new Connection(digitalWireType, from, to, noAttributes);
    return wire;
  }

  // parameterized test cases
  
  @Parameters
  public static Collection<Object[]> testSchematics() throws SchematicException {
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
