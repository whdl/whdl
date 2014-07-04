package org.manifold.backend.digital;

public class SchematicConstructionException extends Exception {
  private static final long serialVersionUID = 8069548761921986148L;

  private String reason;
  public SchematicConstructionException(String reason){
    this.reason = reason;
  }
  
  @Override
  public String getMessage(){
    return "could not construct schematic: " + reason;
  }
  
}
