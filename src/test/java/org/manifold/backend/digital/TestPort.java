package org.manifold.backend.digital;

import static org.junit.Assert.*;

import org.junit.Test;
import org.manifold.backend.digital.Port.PortDirection;

public class TestPort {

  @Test
  public void testGetName() {
    String name = "test";
    Port port = new Port(name, PortDirection.INPUT);
    assertEquals(name, port.getName());
  }
  
  @Test
  public void testGetDirection(){
    PortDirection dir = PortDirection.INPUT;
    Port port = new Port("test", dir);
    assertEquals(dir, port.getDirection());
  }
  
  @Test
  public void testGetNet_NewPort_HasNullNet(){
    Port port = new Port("test", PortDirection.INPUT);
    assertNull(port.getNet());
  }
  
  @Test
  public void testSetNet() throws NetlistConstructionException{
    Port port = new Port("test", PortDirection.INPUT);
    Net n = new Net("n_test");
    port.setNet(n);
    assertEquals(n, port.getNet());
  }
  
  @Test(expected = NetlistConstructionException.class)
  public void testSetNet_AlreadySet_ThrowsException() 
      throws NetlistConstructionException{
    Port port = new Port("test", PortDirection.INPUT);
    Net n1 = new Net("n1_test");
    try {
      port.setNet(n1);
    } catch (NetlistConstructionException sce) {
      fail("exception thrown too early: " + sce.getMessage());
    }
    Net n2 = new Net("n2_test");
    port.setNet(n2);
  }

}
