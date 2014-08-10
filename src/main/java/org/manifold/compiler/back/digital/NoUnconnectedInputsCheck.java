package org.manifold.compiler.back.digital;

import java.util.Map;
import java.util.Map.Entry;

import org.manifold.compiler.NodeValue;
import org.manifold.compiler.PortTypeValue;
import org.manifold.compiler.PortValue;
import org.manifold.compiler.UndeclaredIdentifierException;
import org.manifold.compiler.UndefinedBehaviourError;
import org.manifold.compiler.middle.Schematic;

public class NoUnconnectedInputsCheck extends Check {

  private Schematic schematic;
  private Netlist netlist;
  private PortTypeValue digitalInType;
  private PortTypeValue digitalOutType;

  public NoUnconnectedInputsCheck(Schematic schematic, Netlist netlist) {
    super("no unconnected inputs");
    this.schematic = schematic;
    this.netlist = netlist;
    try {
      this.digitalInType = schematic.getPortType("digitalIn");
    } catch (UndeclaredIdentifierException e) {
      throw new UndefinedBehaviourError(
          "schematic does not define digitalIn port type");
    }
    try {
      this.digitalOutType = schematic.getPortType("digitalOut");
    } catch (UndeclaredIdentifierException e) {
      throw new UndefinedBehaviourError(
          "schematic does not define digitalOut port type");
    }
  }

  @Override
  protected void verify() {
    Map<String, Net> allNets = netlist.getNets();
    boolean noUnconnectedInputs = true;
    for (Entry<String, NodeValue> nodeEntry : schematic.getNodes().entrySet()) {
      String nodeName = nodeEntry.getKey();
      NodeValue node = nodeEntry.getValue();
      for (Entry<String, PortValue> portEntry : node.getPorts().entrySet()) {
        String portName = portEntry.getKey();
        PortValue port = portEntry.getValue();
        if (port.getType() == digitalInType) {
          // check netlist for presence of connection
          try {
            Net connectedNet = netlist.getConnectedNet(port);
          } catch (IllegalArgumentException iae) {
            // not connected
            noUnconnectedInputs = false;
            // TODO(murphy) keep processing and record which port(s) on which
            // node(s) are unconnected, to help the user debug
          }
        }
      }
    }
    this.result = noUnconnectedInputs;
  }

}
