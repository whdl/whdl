package org.manifold.compiler.back.digital;

import java.util.Map;

import org.manifold.compiler.PortTypeValue;
import org.manifold.compiler.PortValue;
import org.manifold.compiler.UndeclaredIdentifierException;
import org.manifold.compiler.UndefinedBehaviourError;
import org.manifold.compiler.middle.Schematic;


public class NoMultipleDriversCheck extends Check {

  private Schematic schematic;
  private Netlist netlist;
  private PortTypeValue digitalOutType;

  public NoMultipleDriversCheck(Schematic schematic, Netlist netlist) {
    super("no multiple drivers");
    this.schematic = schematic;
    this.netlist = netlist;
    try {
      this.digitalOutType = schematic.getPortType("digitalOut");
    } catch (UndeclaredIdentifierException e) {
      throw new UndefinedBehaviourError(
          "schematic does not define digitalOut port type");
    }
  }

  @Override
  protected void verify() {
    System.err.println("expected digital out type = "
        + digitalOutType.toString());
    Map<String, Net> allNets = netlist.getNets();
    boolean noMultipleDrivers = true;
    for (Net net : allNets.values()) {
      int nDrivers = 0;
      // a driver is any Port of type `digitalOutType`
      for (PortValue port : net.getConnectedPorts()) {
        System.err.println("port type = "
            + port.getType().toString());
        if (port.getType() == digitalOutType) {
          nDrivers += 1;
        }
      }
      /*
       * after checking all ports on this net, if there are at least 2 drivers
       * then this net is multiply-driven and DRC fails
       */
      if (nDrivers >= 2) {
        noMultipleDrivers = false;
      }
      /*
       * TODO(murphy) If all we care about is the decision, we can break here.
       * However, we should keep processing in order to collect a list of which
       * nets are multiply driven, and the ports that are driving them. This
       * will be useful when showing the user the results of DRC so they can
       * correct the design.
       * This is described in Issue #131.
       */
    }
    this.result = noMultipleDrivers;
  }

}
