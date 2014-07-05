package org.manifold.backend.digital;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.manifold.intermediate.BooleanType;
import org.manifold.intermediate.BooleanValue;
import org.manifold.intermediate.Node;
import org.manifold.intermediate.NodeType;
import org.manifold.intermediate.PortType;
import org.manifold.intermediate.SchematicException;
import org.manifold.intermediate.Type;

public class TestRegisterPrimitive {
  
  @BeforeClass
  public static void setup(){
    TestNetlist.setupIntermediateTypes();
  }
  
  @Test
  public void testConstruction() throws NetlistConstructionException, SchematicException {
    Node register = TestNetlist.instantiateRegister(true, true, true, true);
    RegisterPrimitive regPrim = new RegisterPrimitive("reg", register);
  }
  
  @Test
  public void testIsInitialValueHigh() throws NetlistConstructionException, SchematicException {
    Node register = TestNetlist.instantiateRegister(false, true, true, true);
    RegisterPrimitive regPrim = new RegisterPrimitive("reg", register);
    assertFalse(regPrim.isInitialValueHigh());
  }
  
  @Test
  public void testIsResetActiveHigh() throws NetlistConstructionException, SchematicException {
    Node register = TestNetlist.instantiateRegister(true, false, true, true);
    RegisterPrimitive regPrim = new RegisterPrimitive("reg", register);
    assertFalse(regPrim.isResetActiveHigh());
  }
  
  @Test
  public void testIsResetAsynchronous() throws NetlistConstructionException, SchematicException {
    Node register = TestNetlist.instantiateRegister(true, true, false, true);
    RegisterPrimitive regPrim = new RegisterPrimitive("reg", register);
    assertFalse(regPrim.isResetAsynchronous());
  }
  
  @Test
  public void testIsClockActiveHigh() throws NetlistConstructionException, SchematicException {
    Node register = TestNetlist.instantiateRegister(true, true, true, false);
    RegisterPrimitive regPrim = new RegisterPrimitive("reg", register);
    assertFalse(regPrim.isClockActiveHigh());
  }

}
