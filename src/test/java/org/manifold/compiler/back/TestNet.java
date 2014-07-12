package org.manifold.compiler.back;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.manifold.compiler.NodeTypeValue;
import org.manifold.compiler.NodeValue;
import org.manifold.compiler.PortTypeValue;
import org.manifold.compiler.PortValue;
import org.manifold.compiler.TypeValue;
import org.manifold.compiler.Value;
import org.manifold.compiler.back.digital.Net;
import org.manifold.compiler.middle.SchematicException;

public class TestNet {

  @Test
  public void testGetName() {
    String name = "asdf";
    Net n = new Net(name);
    assertEquals(name, n.getName());
  }

  @Test
  public void testGetConnectedPorts_initiallyEmpty() {
    Net n = new Net("asdf");
    assertTrue(n.getConnectedPorts().isEmpty());
  }
  
  @Test
  public void testAddPort() throws SchematicException {
    Net n = new Net("asdf");
    PortTypeValue portType = new PortTypeValue(
        new HashMap<String, TypeValue>());
    NodeTypeValue nodeType = new NodeTypeValue(new HashMap<String, TypeValue>(),
        new HashMap<String, PortTypeValue>());
    Map<String, Map<String, Value>> portAttrMaps = 
        new HashMap<String, Map<String, Value>>();
    NodeValue parent = new NodeValue(nodeType, new HashMap<String, Value>(), 
        portAttrMaps);
    PortValue p = new PortValue(portType, parent, new HashMap<String, Value>());
    n.addPort(p);
    assertTrue(n.getConnectedPorts().contains(p));
  }
  
}
