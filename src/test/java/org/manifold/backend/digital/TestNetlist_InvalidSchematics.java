package org.manifold.backend.digital;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.manifold.intermediate.Connection;
import org.manifold.intermediate.ConnectionType;
import org.manifold.intermediate.MultipleDefinitionException;
import org.manifold.intermediate.Node;
import org.manifold.intermediate.NodeType;
import org.manifold.intermediate.PortType;
import org.manifold.intermediate.Schematic;
import org.manifold.intermediate.SchematicException;
import org.manifold.intermediate.Type;
import org.manifold.intermediate.Value;

/**
 * Test the behaviour of Netlist when given an invalid schematic to work with.
 */

public class TestNetlist_InvalidSchematics {

  @BeforeClass
  public static void setup(){
    TestNetlist.setupIntermediateTypes();
  }
  
  @Test
  public void testInvalidSchematic_MissingPortTypes() 
      throws MultipleDefinitionException {
    Schematic s = TestNetlist.instantiateSchematic("bogus", false, true, true);
    try {
      Netlist netlist = new Netlist(s);
      fail("schematic constructed with missing port types");
    } catch (NetlistConstructionException ex) {
      assertTrue("unexpected message: " + ex.getMessage(), 
          ex.getMessage().contains("port types"));
    }
  }
  
  @Test
  public void testInvalidSchematic_MissingNodeTypes() 
      throws MultipleDefinitionException {
    Schematic s = TestNetlist.instantiateSchematic("bogus", true, false, true);
    try {
      Netlist netlist = new Netlist(s);
      fail("schematic constructed with missing node types");
    } catch (NetlistConstructionException ex) {
      assertTrue("unexpected message: " + ex.getMessage(), 
          ex.getMessage().contains("node types"));
    }
  }

  @Test
  public void testInvalidSchematic_MissingConnectionTypes() 
      throws MultipleDefinitionException {
    Schematic s = TestNetlist.instantiateSchematic("bogus", true, true, false);
    try {
      Netlist netlist = new Netlist(s);
      fail("schematic constructed with missing connection types");
    } catch (NetlistConstructionException ex) {
      assertTrue("unexpected message: " + ex.getMessage(), 
          ex.getMessage().contains("connection types"));
    }
  }
  
  @Test
  public void testInvalidSchematic_UnknownNodeType() throws SchematicException {
    Schematic s = TestNetlist.instantiateSchematic("bogus");
    // now we add an extra node type
    Map<String, Type> noTypeAttributes = new HashMap<String, Type>();
    Map<String, PortType> noTypePorts = new HashMap<String, PortType>();
    NodeType bogusNodeType = new NodeType(noTypeAttributes, noTypePorts);
    s.addNodeType("bogus_t", bogusNodeType);
    // add a node of our extra type
    Map<String, Value> noAttributes = new HashMap<String, Value>();
    Map<String, Map<String, Value>> noPorts = new HashMap<>();
    Node bogusNode = new Node(bogusNodeType, noAttributes, noPorts);
    s.addNode("bogus", bogusNode);
    
    try {
      Netlist netlist = new Netlist(s);
      fail("schematic constructed with unknown node type");
    } catch (NetlistConstructionException ex) {
      assertTrue("unexpected message: " + ex.getMessage(), 
          ex.getMessage().contains("node"));
      assertTrue("unexpected message: " + ex.getMessage(), 
          ex.getMessage().contains("unknown type"));
    }
  }
  
  @Test
  public void testInvalidSchematic_UnknownConnectionType() 
      throws SchematicException {
    Schematic s = TestNetlist.instantiateSchematic("bogus");
    // now we add an extra connection type
    Map<String, Type> noTypeAttributes = new HashMap<String, Type>();
    ConnectionType bogusConnectionType = new ConnectionType(noTypeAttributes);
    s.addConnectionType("bogus_t", bogusConnectionType);
    // add two simple nodes and connect them with our extra type
    Node in = TestNetlist.instantiateInputPin();
    Node out = TestNetlist.instantiateOutputPin();
    Map<String, Value> noAttributes = new HashMap<String, Value>();
    Connection bogus_in_to_out = new Connection(bogusConnectionType, 
        in.getPort("out"), out.getPort("in"), noAttributes);
    s.addNode("in", in);
    s.addNode("out", out);
    s.addConnection("in_to_out", bogus_in_to_out);
    
    try {
      Netlist netlist = new Netlist(s);
      fail("schematic constructed with unknown connection type");
    } catch (NetlistConstructionException ex) {
      assertTrue("unexpected message: " + ex.getMessage(), 
          ex.getMessage().contains("connection"));
      assertTrue("unexpected message: " + ex.getMessage(), 
          ex.getMessage().contains("unknown type"));
    }
  }
  
}
