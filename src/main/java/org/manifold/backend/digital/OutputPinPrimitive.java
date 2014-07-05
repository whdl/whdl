package org.manifold.backend.digital;

import org.manifold.backend.digital.Port.PortDirection;
import org.manifold.intermediate.Node;

public class OutputPinPrimitive extends Primitive {

  private Port in = new Port("in", PortDirection.INPUT);
  
  public OutputPinPrimitive(String name, Node node){
    super(name);
    
    addPort(in);
  }
  
}
