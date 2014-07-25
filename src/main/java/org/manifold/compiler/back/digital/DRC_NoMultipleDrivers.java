package org.manifold.compiler.back.digital;

import java.util.Map;

import org.manifold.compiler.PortTypeValue;
import org.manifold.compiler.PortValue;


public class DRC_NoMultipleDrivers extends DesignRuleCheck {

  private Netlist netlist;
  private PortTypeValue digitalInType;
  private PortTypeValue digitalOutType;

  public DRC_NoMultipleDrivers(Netlist netlist) {
    this.netlist = netlist;
    this.digitalInType = netlist.getDigitalInType();
    this.digitalOutType = netlist.getDigitalOutType();
  }

  @Override
  public void check() {
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
       */
    }
    this.result = noMultipleDrivers;
  }

}
