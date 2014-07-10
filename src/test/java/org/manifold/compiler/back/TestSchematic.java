package org.manifold.compiler.back;

import org.manifold.compiler.middle.SchematicException;
import org.manifold.compiler.middle.Schematic;
import org.manifold.compiler.StringTypeValue;
import org.manifold.compiler.ConnectionType;
import org.manifold.compiler.ConstraintType;
import org.manifold.compiler.UndeclaredIdentifierException;
import org.manifold.compiler.PortTypeValue;
import org.manifold.compiler.ConstraintValue;
import org.manifold.compiler.TypeValue;
import org.manifold.compiler.MultipleAssignmentException;
import org.manifold.compiler.NodeValue;
import org.manifold.compiler.PortValue;
import org.manifold.compiler.ConnectionValue;
import org.manifold.compiler.IntegerTypeValue;
import org.manifold.compiler.MultipleDefinitionException;
import org.manifold.compiler.NodeTypeValue;
import org.manifold.compiler.Value;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TestSchematic {
  
  private static final PortTypeValue defaultPortDefinition =
      new PortTypeValue(new HashMap<>());
  private static final String PORT_NAME1 = "P1";
  private static final String PORT_NAME2 = "P2";
  
  private NodeValue n;
  private PortValue p1, p2;
  
  @Before
  public void setup() throws SchematicException {
    Map<String, PortTypeValue> portMap = ImmutableMap.of(
        PORT_NAME1, defaultPortDefinition,
        PORT_NAME2, defaultPortDefinition);
    Map<String, Map<String, Value>> portAttrMap = ImmutableMap.of(
        PORT_NAME1, ImmutableMap.of(),
        PORT_NAME2, ImmutableMap.of());
    NodeTypeValue nType = new NodeTypeValue(new HashMap<>(), portMap);
    n = new NodeValue(nType, new HashMap<>(), portAttrMap);
    
    p1 = n.getPort(PORT_NAME1);
    p2 = n.getPort(PORT_NAME2);
  }

  @Test
  public void testAddTypeDef() throws MultipleDefinitionException {
    Schematic s = new Schematic("test");
    TypeValue t1 = IntegerTypeValue.getInstance();
    s.addUserDefinedType("foo", t1);
  }
  
  @Test(expected = MultipleDefinitionException.class)
  public void testAddTypeDef_multipleDefinitions()
      throws MultipleDefinitionException {
    
    // We should not be able to add two type definitions whose first argument
    // is the same string.
    Schematic s = new Schematic("test");
    try {
      TypeValue t1 = IntegerTypeValue.getInstance();
      s.addUserDefinedType("foo", t1);
    } catch (MultipleDefinitionException mde) {
      fail("exception thrown too early: " + mde.getMessage());
    }
    TypeValue t2 = IntegerTypeValue.getInstance();
    s.addUserDefinedType("foo", t2);
  }
  
  @Test(expected = MultipleDefinitionException.class)
  public void testAddTypeDef_maskDefaultType()
      throws MultipleDefinitionException {
    // Suppose we create a new Schematic and then try to redefine the meaning
    // of "Int". Since "Int" is a built-in type, this should result in a
    // MultipleDefinitionException being thrown.
    Schematic s = new Schematic("test");
    TypeValue td = StringTypeValue.getInstance();
    s.addUserDefinedType("Int", td);
  }
  
  @Test
  public void testGetTypeDef()
      throws UndeclaredIdentifierException, MultipleDefinitionException {
    Schematic s = new Schematic("test");
    TypeValue expected = IntegerTypeValue.getInstance();
    s.addUserDefinedType("foo", expected);
    TypeValue actual = s.getUserDefinedType("foo");
    assertEquals(expected, actual);
  }
  
  @Test(expected = UndeclaredIdentifierException.class)
  public void testGetTypeDef_notDeclared()
      throws UndeclaredIdentifierException {
    Schematic s = new Schematic("test");
    TypeValue bogus = s.getUserDefinedType("does-not-exist");
  }
  
  @Test
  public void testAddPortDef() throws MultipleDefinitionException {
    Schematic s = new Schematic("test");
    PortTypeValue e1 = new PortTypeValue(new HashMap<>());
    s.addPortType("n1", e1);
  }
  
  @Test(expected = MultipleDefinitionException.class)
  public void testAddPortDef_multipleDefinitions()
      throws MultipleDefinitionException {
    // We should not be able to add two port definitions whose first argument
    // is the same string.
    Schematic s = new Schematic("test");
    try {
      PortTypeValue n1 = new PortTypeValue(new HashMap<>());
      s.addPortType("foo", n1);
    } catch (MultipleDefinitionException mde) {
      fail("exception thrown too early: " + mde.getMessage());
    }
    PortTypeValue n2 = new PortTypeValue(new HashMap<>());
    s.addPortType("foo", n2);
  }
  
  @Test
  public void testGetPortDef()
      throws UndeclaredIdentifierException, MultipleDefinitionException{
    Schematic s = new Schematic("test");
    PortTypeValue expected = new PortTypeValue(new HashMap<>());
    s.addPortType("foo", expected);
    PortTypeValue actual = s.getPortType("foo");
    assertEquals(expected, actual);
  }
  
  @Test(expected = UndeclaredIdentifierException.class)
  public void testGetPortDef_notDeclared()
      throws UndeclaredIdentifierException {
    Schematic s = new Schematic("test");
    PortTypeValue bogus = s.getPortType("does-not-exist");
  }
  
  @Test
  public void testAddNodeDef() throws MultipleDefinitionException {
    Schematic s = new Schematic("test");
    NodeTypeValue n1 = new NodeTypeValue(new HashMap<>(), new HashMap<>());
    s.addNodeType("n1", n1);
  }
  
  @Test(expected = MultipleDefinitionException.class)
  public void testAddNodeDef_multipleDefinitions()
      throws MultipleDefinitionException {
    // We should not be able to add two node definitions whose first argument
    // is the same string.
    Schematic s = new Schematic("test");
    try {
      NodeTypeValue n1 = new NodeTypeValue(new HashMap<>(), new HashMap<>());
      s.addNodeType("foo", n1);
    } catch (MultipleDefinitionException mde) {
      fail("exception thrown too early: " + mde.getMessage());
    }
    NodeTypeValue n2 = new NodeTypeValue(new HashMap<>(), new HashMap<>());
    s.addNodeType("foo", n2);
  }
  
  @Test
  public void testGetNodeDef()
      throws UndeclaredIdentifierException, MultipleDefinitionException{
    Schematic s = new Schematic("test");
    NodeTypeValue expected = new NodeTypeValue(
        new HashMap<>(),
        new HashMap<>()
    );
    s.addNodeType("foo", expected);
    NodeTypeValue actual = s.getNodeType("foo");
    assertEquals(expected, actual);
  }
  
  @Test(expected = UndeclaredIdentifierException.class)
  public void testGetNodeDef_notDeclared()
      throws UndeclaredIdentifierException {
    Schematic s = new Schematic("test");
    NodeTypeValue bogus = s.getNodeType("does-not-exist");
  }
  
  @Test
  public void testAddConnectionDef() throws MultipleDefinitionException {
    Schematic s = new Schematic("test");
    ConnectionType c1 = new ConnectionType(new HashMap<>());
    s.addConnectionType("c1", c1);
  }
  
  @Test(expected = MultipleDefinitionException.class)
  public void testAddConnectionDef_multipleDefinitions()
      throws MultipleDefinitionException {
    // We should not be able to add two connection definitions whose first
    // argument is the same string.
    Schematic s = new Schematic("test");
    try {
      ConnectionType c1 = new ConnectionType(new HashMap<>());
      s.addConnectionType("foo", c1);
    } catch (MultipleDefinitionException mde) {
      fail("exception thrown too early: " + mde.getMessage());
    }
    ConnectionType c2 = new ConnectionType(new HashMap<>());
    s.addConnectionType("foo", c2);
  }
  
  @Test
  public void testGetConnectionDef()
      throws UndeclaredIdentifierException, MultipleDefinitionException {
    Schematic s = new Schematic("test");
    ConnectionType expected = new ConnectionType(new HashMap<>());
    s.addConnectionType("foo", expected);
    ConnectionType actual = s.getConnectionType("foo");
    assertEquals(expected, actual);
  }
  
  @Test(expected = UndeclaredIdentifierException.class)
  public void testGetConnectionDef_notDeclared()
      throws UndeclaredIdentifierException {
    Schematic s = new Schematic("test");
    ConnectionType bogus = s.getConnectionType("does-not-exist");
  }
  
  @Test
  public void testAddConstraintDef() throws MultipleDefinitionException {
    Schematic s = new Schematic("test");
    ConstraintType e1 = new ConstraintType(new HashMap<>());
    s.addConstraintType("e1", e1);
  }
  
  @Test(expected = MultipleDefinitionException.class)
  public void testAddConstraintDef_multipleDefinitions()
      throws MultipleDefinitionException {
    // We should not be able to add two constraint definitions whose first
    // argument is the same string.
    Schematic s = new Schematic("test");
    try {
      ConstraintType e1 = new ConstraintType(new HashMap<>());
      s.addConstraintType("foo", e1);
    } catch (MultipleDefinitionException mde) {
      fail("exception thrown too early: " + mde.getMessage());
    }
    ConstraintType e2 = new ConstraintType(new HashMap<>());
    s.addConstraintType("foo", e2);
  }

  @Test
  public void testGetConstraintDef()
      throws UndeclaredIdentifierException, MultipleDefinitionException{
    Schematic s = new Schematic("test");
    ConstraintType expected = new ConstraintType(new HashMap<>());
    s.addConstraintType("foo", expected);
    ConstraintType actual = s.getConstraintType("foo");
    assertEquals(expected, actual);
  }
  
  @Test(expected = UndeclaredIdentifierException.class)
  public void testGetConstraintDef_notDeclared()
      throws UndeclaredIdentifierException {
    Schematic s = new Schematic("test");
    ConstraintType bogus = s.getConstraintType("does-not-exist");
  }
  
  @Test
  public void testSeparationOfNamespaces_Definitions()
      throws MultipleDefinitionException{
    // We should be able to add one of each of a TypeDefinition,
    // ConstraintDefinition, ConnectionDefinition, NodeDefinition, and
    // PortDefinition with the same name without encountering a "multiple
    // definition" exception.
    Schematic s = new Schematic("test");
    
    TypeValue t1 = StringTypeValue.getInstance();
    ConstraintType ct1 = new ConstraintType(new HashMap<>());
    ConnectionType cn1 = new ConnectionType(new HashMap<>());
    NodeTypeValue n1 = new NodeTypeValue(new HashMap<>(), new HashMap<>());
    PortTypeValue e1 = new PortTypeValue(new HashMap<>());
    
    s.addUserDefinedType("foo", t1);
    s.addConstraintType("foo", ct1);
    s.addConnectionType("foo", cn1);
    s.addNodeType("foo", n1);
    s.addPortType("foo", e1);
  }
  
  @Test
  public void testAddNode() throws SchematicException {
    Schematic s = new Schematic("test");
    NodeTypeValue n1Type = new NodeTypeValue(new HashMap<>(), new HashMap<>());
    NodeValue n1 = new NodeValue(n1Type, new HashMap<>(), new HashMap<>());
    s.addNode("n1", n1);
  }
  
  @Test(expected = org.manifold.compiler.MultipleAssignmentException.class)
  public void testAddNode_multipleInstantiation() throws SchematicException {
    Schematic s = new Schematic("test");
    NodeTypeValue n1Type = new NodeTypeValue(new HashMap<>(), new HashMap<>());
    try {
      NodeValue n1 = new NodeValue(n1Type, new HashMap<>(), new HashMap<>());
      s.addNode("n1", n1);
    } catch (MultipleAssignmentException mie) {
      fail("exception thrown too early");
    }
    NodeValue n1Dup = new NodeValue(n1Type, new HashMap<>(), new HashMap<>());
    s.addNode("n1", n1Dup);
  }
  
  @Test
  public void testGetNode() throws SchematicException {
    Schematic s = new Schematic("test");
    NodeTypeValue n1Type = new NodeTypeValue(new HashMap<>(), new HashMap<>());
    NodeValue n1 = new NodeValue(n1Type, new HashMap<>(), new HashMap<>());
    s.addNode("n1", n1);
    
    NodeValue actual = s.getNode("n1");
    assertSame(n1, actual);
  }
  
  @Test(expected = UndeclaredIdentifierException.class)
  public void testGetNode_notInstantiated()
      throws UndeclaredIdentifierException {
    Schematic s = new Schematic("test");
    s.getNode("bogus");
  }
  
  @Test
  public void testAddConnection() throws SchematicException {
    Schematic s = new Schematic("test");
    ConnectionType c1Type = new ConnectionType(new HashMap<>());
    ConnectionValue c1 = new ConnectionValue(c1Type, p1, p2, new HashMap<>());
    s.addConnection("c1", c1);
  }
  
  @Test(expected = org.manifold.compiler.MultipleAssignmentException.class)
  public void testAddConnection_multipleInstantiation()
      throws SchematicException {
    Schematic s = new Schematic("test");
    ConnectionType c1Type = new ConnectionType(new HashMap<>());
    try {
      ConnectionValue c1 = new ConnectionValue(c1Type, p1, p2, new HashMap<>());
      s.addConnection("c1", c1);
    } catch (MultipleAssignmentException mie) {
      fail("exception thrown too early");
    }
    ConnectionValue c1Dup = new ConnectionValue(
        c1Type,
        p1,
        p2,
        new HashMap<>()
    );
    s.addConnection("c1", c1Dup);
  }
  
  @Test
  public void testGetConnection() throws SchematicException {
    Schematic s = new Schematic("test");
    ConnectionType c1Type = new ConnectionType(new HashMap<>());
    ConnectionValue c1 = new ConnectionValue(c1Type, p1, p2, new HashMap<>());
    s.addConnection("c1", c1);
    ConnectionValue actual = s.getConnection("c1");
    assertSame(c1, actual);
  }
  
  @Test(expected = UndeclaredIdentifierException.class)
  public void testGetConnection_notInstantiated()
      throws UndeclaredIdentifierException {
    Schematic s = new Schematic("test");
    s.getConnection("bogus");
  }
  
  @Test
  public void testAddConstraint() throws SchematicException {
    Schematic s = new Schematic("test");
    ConstraintType c1Type = new ConstraintType(new HashMap<>());
    ConstraintValue c1 = new ConstraintValue(c1Type, new HashMap<>());
    s.addConstraint("c1", c1);
  }
  
  @Test(expected = MultipleAssignmentException.class)
  public void testAddConstraint_multipleInstantiation()
      throws SchematicException {
    Schematic s = new Schematic("test");
    ConstraintType c1Type = new ConstraintType(new HashMap<>());
    
    try {
      ConstraintValue c1 = new ConstraintValue(c1Type, new HashMap<>());
      s.addConstraint("c1", c1);
    } catch (MultipleAssignmentException mie) {
      fail("exception thrown too early");
    }
    
    ConstraintValue c1Dup = new ConstraintValue(c1Type, new HashMap<>());
    s.addConstraint("c1", c1Dup);
  }
  
  @Test
  public void testGetConstraint() throws SchematicException {
    Schematic s = new Schematic("test");
    ConstraintType c1Type = new ConstraintType(new HashMap<>());
    ConstraintValue c1 = new ConstraintValue(c1Type, new HashMap<>());
    s.addConstraint("c1", c1);
    
    ConstraintValue actual = s.getConstraint("c1");
    assertSame(c1, actual);
  }
  
  @Test(expected = UndeclaredIdentifierException.class)
  public void testGetConstraint_notInstantiated()
      throws UndeclaredIdentifierException{
    Schematic s = new Schematic("test");
    s.getConstraint("bogus");
  }
  
}
