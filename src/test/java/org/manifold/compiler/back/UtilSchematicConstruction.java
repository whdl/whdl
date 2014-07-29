package org.manifold.compiler.back;

import java.util.HashMap;
import java.util.Map;

import org.manifold.compiler.BooleanTypeValue;
import org.manifold.compiler.BooleanValue;
import org.manifold.compiler.ConnectionType;
import org.manifold.compiler.ConnectionValue;
import org.manifold.compiler.InvalidAttributeException;
import org.manifold.compiler.MultipleDefinitionException;
import org.manifold.compiler.NodeTypeValue;
import org.manifold.compiler.NodeValue;
import org.manifold.compiler.PortTypeValue;
import org.manifold.compiler.PortValue;
import org.manifold.compiler.TypeValue;
import org.manifold.compiler.UndeclaredAttributeException;
import org.manifold.compiler.Value;
import org.manifold.compiler.middle.Schematic;
import org.manifold.compiler.middle.SchematicException;

// Utility class for quickly setting up Schematics in test cases.
public class UtilSchematicConstruction {

  private static boolean setUp = false;
  
  public static PortTypeValue digitalInPortType;
  public static PortTypeValue digitalOutPortType;

  private static final Map<String, TypeValue> noTypeAttributes 
    = new HashMap<>();
  private static final Map<String, Value> noAttributes = new HashMap<>();

  private static Map<String, TypeValue> registerTypeAttributes 
    = new HashMap<>();
  private static Map<String, PortTypeValue> registerTypePorts = new HashMap<>();
  private static NodeTypeValue registerType;

  private static Map<String, PortTypeValue> andTypePorts = new HashMap<>();
  private static NodeTypeValue andType;

  private static Map<String, PortTypeValue> orTypePorts = new HashMap<>();
  private static NodeTypeValue orType;

  private static Map<String, PortTypeValue> notTypePorts = new HashMap<>();
  private static NodeTypeValue notType;
  
  private static Map<String, PortTypeValue> inputPinTypePorts = new HashMap<>();
  private static NodeTypeValue inputPinType;

  private static Map<String, PortTypeValue> outputPinTypePorts 
    = new HashMap<>();
  private static NodeTypeValue outputPinType;

  private static ConnectionType digitalWireType;

  public static void setupIntermediateTypes() {

    digitalInPortType = new PortTypeValue(noTypeAttributes);
    digitalOutPortType = new PortTypeValue(noTypeAttributes);

    registerTypeAttributes.put("initialValue", BooleanTypeValue.getInstance());
    registerTypeAttributes.put("resetActiveHigh",
        BooleanTypeValue.getInstance());
    registerTypeAttributes.put("resetAsynchronous",
        BooleanTypeValue.getInstance());
    registerTypeAttributes.put("clockActiveHigh",
        BooleanTypeValue.getInstance());
    registerTypePorts.put("in", digitalInPortType);
    registerTypePorts.put("out", digitalOutPortType);
    registerTypePorts.put("clock", digitalInPortType);
    registerTypePorts.put("reset", digitalInPortType);
    registerType = new NodeTypeValue(registerTypeAttributes, registerTypePorts);

    andTypePorts.put("in0", digitalInPortType);
    andTypePorts.put("in1", digitalInPortType);
    andTypePorts.put("out", digitalOutPortType);
    andType = new NodeTypeValue(noTypeAttributes, andTypePorts);
    
    orTypePorts.put("in0", digitalInPortType);
    orTypePorts.put("in1", digitalInPortType);
    orTypePorts.put("out", digitalOutPortType);
    orType = new NodeTypeValue(noTypeAttributes, orTypePorts);
    
    notTypePorts.put("in", digitalInPortType);
    notTypePorts.put("out", digitalOutPortType);
    notType = new NodeTypeValue(noTypeAttributes, notTypePorts);
    
    inputPinTypePorts.put("out", digitalOutPortType);
    inputPinType = new NodeTypeValue(noTypeAttributes, inputPinTypePorts);

    outputPinTypePorts.put("in", digitalInPortType);
    outputPinType = new NodeTypeValue(noTypeAttributes, outputPinTypePorts);

    digitalWireType = new ConnectionType(noTypeAttributes);
    
    setUp = true;
  }

  /**
   * Instantiate a Schematic, but with varying degrees of "completeness" in
   * terms of what object types are included. If types are missing, it will not
   * be possible to construct a Netlist.
   * 
   * @throws MultipleDefinitionException
   */
  public static Schematic instantiateSchematic(String name,
      boolean includePortTypes, boolean includeNodeTypes,
      boolean includeConnectionTypes) throws MultipleDefinitionException {
    if (!setUp) {
      setupIntermediateTypes();
    }
    Schematic s = new Schematic(name);
    if (includePortTypes) {
      s.addPortType("digitalIn", digitalInPortType);
      s.addPortType("digitalOut", digitalOutPortType);
    }

    if (includeNodeTypes) {
      s.addNodeType("register", registerType);
      s.addNodeType("and", andType);
      s.addNodeType("or", orType);
      s.addNodeType("not", notType);
      s.addNodeType("inputPin", inputPinType);
      s.addNodeType("outputPin", outputPinType);
    }

    if (includeConnectionTypes) {
      s.addConnectionType("digitalWire", digitalWireType);
    }

    return s;
  }

  /**
   * Instantiate a Schematic and include all object types.
   */
  public static Schematic instantiateSchematic(String name)
      throws MultipleDefinitionException {
    return instantiateSchematic(name, true, true, true);
  }

  public static NodeValue instantiateRegister(boolean initialValue,
      boolean resetActiveHigh, boolean resetAsynchronous,
      boolean clockActiveHigh) throws SchematicException {
    Map<String, Value> registerAttrs = new HashMap<>();
    registerAttrs.put("initialValue", BooleanValue.getInstance(initialValue));
    registerAttrs.put("resetActiveHigh",
        BooleanValue.getInstance(resetActiveHigh));
    registerAttrs.put("resetAsynchronous",
        BooleanValue.getInstance(resetAsynchronous));
    registerAttrs.put("clockActiveHigh",
        BooleanValue.getInstance(clockActiveHigh));
    Map<String, Map<String, Value>> registerPortAttrs = new HashMap<>();
    registerPortAttrs.put("in", noAttributes);
    registerPortAttrs.put("out", noAttributes);
    registerPortAttrs.put("clock", noAttributes);
    registerPortAttrs.put("reset", noAttributes);
    NodeValue register = new NodeValue(registerType, registerAttrs,
        registerPortAttrs);
    return register;
  }
  
  public static NodeValue instantiateAnd() throws SchematicException {
    Map<String, Map<String, Value>> andPortAttrs = new HashMap<>();
    andPortAttrs.put("in0", noAttributes);
    andPortAttrs.put("in1", noAttributes);
    andPortAttrs.put("out", noAttributes);
    NodeValue andGate = new NodeValue(andType, noAttributes, andPortAttrs);
    return andGate;
  }
  
  public static NodeValue instantiateOr() throws SchematicException {
    Map<String, Map<String, Value>> orPortAttrs = new HashMap<>();
    orPortAttrs.put("in0", noAttributes);
    orPortAttrs.put("in1", noAttributes);
    orPortAttrs.put("out", noAttributes);
    NodeValue orGate = new NodeValue(orType, noAttributes, orPortAttrs);
    return orGate;
  }
  
  public static NodeValue instantiateNot() throws SchematicException {
    Map<String, Map<String, Value>> notPortAttrs = new HashMap<>();
    notPortAttrs.put("in", noAttributes);
    notPortAttrs.put("out", noAttributes);
    NodeValue notGate = new NodeValue(notType, noAttributes, notPortAttrs);
    return notGate;
  }

  public static NodeValue instantiateInputPin() throws SchematicException {
    Map<String, Map<String, Value>> inputPinPortAttrs = new HashMap<>();
    inputPinPortAttrs.put("out", noAttributes);
    NodeValue inputPin = new NodeValue(inputPinType, noAttributes,
        inputPinPortAttrs);
    return inputPin;
  }

  public static NodeValue instantiateOutputPin() throws SchematicException {
    Map<String, Map<String, Value>> outputPinPortAttrs = new HashMap<>();
    outputPinPortAttrs.put("in", noAttributes);
    NodeValue outputPin = new NodeValue(outputPinType, noAttributes,
        outputPinPortAttrs);
    return outputPin;
  }

  public static ConnectionValue instantiateWire(PortValue from, PortValue to)
      throws UndeclaredAttributeException, InvalidAttributeException {
    ConnectionValue wire = new ConnectionValue(digitalWireType, from, to,
        noAttributes);
    return wire;
  }
}
