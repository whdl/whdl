package org.manifold.backend.digital;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class Primitive {
  private String name;
  public String getName(){
    return name;
  }
  
  private Map<String, Port> ports = new HashMap<String, Port>();
  
  public void addPort(Port p){
    ports.put(p.getName(), p);
  }
  
  public Port getPort(String portName) throws UndeclaredIdentifierException{
    if (ports.containsKey(portName)){
      return ports.get(portName);
    } else {
      throw new UndeclaredIdentifierException(portName);
    }
  }
  
  public Collection<Port> getPorts(){
    return java.util.Collections.unmodifiableCollection(ports.values());
  }

  public Primitive(String name){
    this.name = name;
  }
  
}
