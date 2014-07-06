package org.manifold.compiler;

public class UndefinedBehaviourError extends Error {

  private static final long serialVersionUID = 4302832267210981600L;

  private final String msg;
  
  public UndefinedBehaviourError(String msg){
    this.msg = msg;
  }
  
  @Override
  public String getMessage(){
    return "internal error: " + msg;
  }
  
}
