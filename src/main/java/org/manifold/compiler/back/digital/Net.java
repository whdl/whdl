package org.manifold.compiler.back.digital;

import java.util.HashSet;
import java.util.Set;

import org.manifold.compiler.PortValue;
import org.manifold.compiler.UndefinedBehaviourError;

import com.google.common.collect.ImmutableSet;

// In digital design, a "net" is a wire that connects multiple ports together.
// Because the connections supported by the compiler are
// traditional graph edges, which connect only two ports at a time,
// Nets make it easier to work with instances where one port
// is connected to two or more other ports to supply them with signal.

public class Net {
  private String name;

  public String getName() {
    return name;
  }

  public Net(String name) {
    this.name = name;
  }

  private Set<PortValue> connectedPorts = new HashSet<>();

  public Set<PortValue> getConnectedPorts() {
    return ImmutableSet.copyOf(connectedPorts);
  }

  public void addPort(PortValue port) {
    if (connectedPorts.contains(port)) {
      throw new UndefinedBehaviourError(
          "port is connected to the same net twice");
    } else {
      connectedPorts.add(port);
    }
  }
}
