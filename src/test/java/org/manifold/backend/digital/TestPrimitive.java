package org.manifold.backend.digital;

import static org.junit.Assert.*;

import org.junit.Test;
import org.manifold.backend.digital.Port.PortDirection;

public class TestPrimitive {

  class MockPrimitive extends Primitive {

    public MockPrimitive(String name) {
      super(name);
    }
    
  }
  
  @Test
  public void testGetName() {
    String name = "test";
    Primitive p = new MockPrimitive(name);
    assertEquals(name, p.getName());
  }
  
  @Test
  public void testGetPorts_InitiallyEmpty(){
    Primitive p = new MockPrimitive("test");
    assertTrue(p.getPorts().isEmpty());
  }
  
  @Test
  public void testAddPort(){
    Primitive prim = new MockPrimitive("test");
    Port port = new Port("a", PortDirection.INPUT);
    prim.addPort(port);
  }
  
  @Test
  public void testGetPorts_ContainsAddedPort(){
    Primitive prim = new MockPrimitive("test");
    Port port = new Port("a", PortDirection.INPUT);
    prim.addPort(port);
    assertTrue(prim.getPorts().contains(port));
  }
  
  @Test
  public void testGetPort() throws UndeclaredIdentifierException{
    Primitive prim = new MockPrimitive("test");
    Port port = new Port("a", PortDirection.INPUT);
    prim.addPort(port);
    assertEquals(port, prim.getPort("a"));
  }
  
  @Test(expected = UndeclaredIdentifierException.class)
  public void testGetPort_nonexistent_throwsException() 
      throws UndeclaredIdentifierException{
    Primitive prim = new MockPrimitive("test");
    Port bogus = prim.getPort("bogus");
  }

}
