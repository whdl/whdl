package org.manifold.backend.digital;

public class Port {
  
  enum PortDirection {
    INPUT,
    OUTPUT,
  }
  
  private String name;
  public String getName(){
    return name;
  }
  private PortDirection direction;
  public PortDirection getDirection(){
    return direction;
  }
  private Net attachedNet = null;
  public Net getNet(){
    return attachedNet;
  }
  public void setNet(Net net) throws NetlistConstructionException{
    if(attachedNet != null){
      // FIXME subclass this
      throw new NetlistConstructionException("attempted to set net twice on port '" + name + "'");
    }
    attachedNet = net;
  }
  
  public Port(String name, PortDirection direction){
    this.name = name;
    this.direction = direction;
  }
}
