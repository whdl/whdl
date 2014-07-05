package org.manifold.backend.digital;

import org.manifold.backend.digital.Port.PortDirection;
import org.manifold.intermediate.BooleanValue;
import org.manifold.intermediate.Node;
import org.manifold.intermediate.UndeclaredAttributeException;

public class RegisterPrimitive extends Primitive {
  private Port port_in = new Port("in", PortDirection.INPUT);
  private Port port_out = new Port("out", PortDirection.OUTPUT);
  private Port port_clock = new Port("clock", PortDirection.INPUT);
  private Port port_reset = new Port("reset", PortDirection.INPUT);
  
  private boolean initialValue;
  public boolean isInitialValueHigh(){
    return initialValue;
  }
  private boolean resetActiveHigh;
  public boolean isResetActiveHigh(){
    return resetActiveHigh;
  }
  private boolean resetAsynchronous;
  public boolean isResetAsynchronous(){
    return resetAsynchronous;
  }
  private boolean clockActiveHigh;
  public boolean isClockActiveHigh(){
    return clockActiveHigh;
  }
  
  public RegisterPrimitive(String name, Node node) throws PrimitiveConstructionException{
    super(name);
    
    try{
      initialValue = ((BooleanValue)node.getAttribute("initialValue")).getValue();
      resetActiveHigh = ((BooleanValue)node.getAttribute("resetActiveHigh")).getValue();
      resetAsynchronous = ((BooleanValue)node.getAttribute("resetAsynchronous")).getValue();
      clockActiveHigh = ((BooleanValue)node.getAttribute("clockActiveHigh")).getValue();
    }catch(UndeclaredAttributeException uae){
      throw new PrimitiveConstructionException("error while setting attributes of register '" + getName() + "'");
    }
    
    addPort(port_in);
    addPort(port_out);
    addPort(port_clock);
    addPort(port_reset);
  }
}
