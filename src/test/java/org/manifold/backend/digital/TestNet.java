package org.manifold.backend.digital;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;
import org.manifold.backend.digital.Port.PortDirection;

public class TestNet {

  @Test
  public void testGetName() {
    String name = "n_test";
    Net n = new Net(name);
    assertEquals(name, n.getName());
  }
  
  @Test
  public void testAddPort() throws NetlistConstructionException {
    Port p = new Port("test", PortDirection.INPUT);
    Net n = new Net("n_test");
    n.addPort(p);
  }
  
  @Test
  public void testGetPorts_InitiallyEmpty(){
    Net n = new Net("n_test");
    Set<Port> ports = n.getPorts();
    assertTrue(ports.isEmpty());
  }
  
  @Test
  public void testGetPorts_ContainsAddedPort() throws NetlistConstructionException{
    Net n = new Net("n_test");
    Port p = new Port("test", PortDirection.INPUT);
    n.addPort(p);
    Set<Port> ports = n.getPorts();
    assertTrue(ports.contains(p));
  }

}
