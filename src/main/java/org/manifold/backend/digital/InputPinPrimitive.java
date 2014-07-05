package org.manifold.backend.digital;

import org.manifold.backend.digital.Port.PortDirection;
import org.manifold.intermediate.Node;

public class InputPinPrimitive extends Primitive {
  
  private Port out = new Port("out", PortDirection.OUTPUT);

  public InputPinPrimitive(String name, Node node){
    super(name);
    addPort(out);
  }
  
}
