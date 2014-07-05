package org.manifold.backend.digital;

import java.util.HashSet;
import java.util.Set;

public class Net {
  
  private String name;
  public String getName(){
    return name;
  }
  
  public Net(String name){
    this.name = name;
  }
  
  private Set<Port> ports = new HashSet<Port>();
  
  public Set<Port> getPorts(){
    return java.util.Collections.unmodifiableSet(ports);
  }
  
  public void addPort(Port p) throws NetlistConstructionException{
    ports.add(p);
    p.setNet(this);
  }
}
