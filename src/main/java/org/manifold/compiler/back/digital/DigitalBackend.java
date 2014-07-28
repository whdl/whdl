package org.manifold.compiler.back.digital;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.manifold.compiler.ConnectionValue;
import org.manifold.compiler.NodeValue;
import org.manifold.compiler.OptionError;
import org.manifold.compiler.back.UtilSchematicConstruction;
import org.manifold.compiler.middle.Schematic;
import org.manifold.compiler.middle.SchematicException;

public class DigitalBackend {
    
    private static Logger log = LogManager.getLogger("DigitalBackend");
    private void setupLogging() {
        // TODO option to change log level
        LogManager.getRootLogger().setLevel(Level.ALL);
        PatternLayout layout = new PatternLayout(
            "%d{ISO8601} [%t] %-5p %c %x - %m%n");
        LogManager.getRootLogger().addAppender(
            new ConsoleAppender(layout, ConsoleAppender.SYSTEM_ERR));
    }
    
    private Options options;

    private enum TARGET_HDL {
      VHDL,
    };
    private TARGET_HDL targetHDL;
    private void createOptionTargetHDL() {
        Option hdl = new Option("h", "hdl", true, 
                "target HDL type (vhdl)");
        options.addOption(hdl);
    }
    private void collectOptionTargetHDL(CommandLine cmd) {
        String hdl = cmd.getOptionValue("hdl");
        if (hdl == null) {
            log.warn("no target HDL specified, assuming VHDL");
            targetHDL = TARGET_HDL.VHDL;
        } else {
            hdl = hdl.toLowerCase();
            if (hdl.equals("vhdl")) {
                targetHDL = TARGET_HDL.VHDL;
            } else {
                throw new OptionError("target HDL '" + hdl 
                        + "' not recognized");
            }
        }
    }
    
    String outputDirectory = null;
    private void createOptionOutputDirectory() {
        Option outDir = new Option("o", "output", true,
                "directory for output products");
        options.addOption(outDir);
    }
    private void collectOptionOutputDirectory(CommandLine cmd) {
        String outDir = cmd.getOptionValue("output");
        if (outDir != null){
            outputDirectory = outDir;
        }
    }
    
    boolean noChecks = false;
    @SuppressWarnings("static-access")
    private void createOptionNoChecks() {
      Option noChecks = OptionBuilder.withLongOpt("no-checks")
          .withDescription(
              "do not run design checks "
              + "(may result in incorrect code generation)"
              ).create();
      options.addOption(noChecks);
    }
    private void collectOptionNoChecks(CommandLine cmd) {
      if (cmd.hasOption("no-checks")) {
        noChecks = true;
      }
    }
    
    private void createOptionDefinitions() {
        options = new Options();
        createOptionTargetHDL();
        createOptionOutputDirectory();
        createOptionNoChecks();
    }
    
    private void collectOptions(CommandLine cmd) {
        collectOptionTargetHDL(cmd);
        collectOptionOutputDirectory(cmd);
        collectOptionNoChecks(cmd);
    }
    
    public DigitalBackend(String[] args) throws ParseException {
        setupLogging();
        // set up options for command-line parsing
        createOptionDefinitions();
        // parse command line
        CommandLineParser parser = new org.apache.commons.cli.BasicParser();
        CommandLine cmd = parser.parse(options, args);
        // retrieve command-line options
        collectOptions(cmd);
    }
    
    private Schematic createDemoSchematic() throws SchematicException {
        // TODO remove this once schematic import from JSON is ready
        Schematic schematic 
            = UtilSchematicConstruction.instantiateSchematic("demo");
        
        NodeValue clk = UtilSchematicConstruction.instantiateInputPin();
        schematic.addNode("clk", clk);
        NodeValue rst = UtilSchematicConstruction.instantiateInputPin();
        schematic.addNode("rst", rst);
        NodeValue in0 = UtilSchematicConstruction.instantiateInputPin();
        schematic.addNode("in0", in0);
        NodeValue reg0 = UtilSchematicConstruction.instantiateRegister(
                false, true, false, true);
        schematic.addNode("reg0", reg0);
        NodeValue out0 = UtilSchematicConstruction.instantiateOutputPin();
        schematic.addNode("out0", out0);
        ConnectionValue in0_to_reg0 = UtilSchematicConstruction.instantiateWire(
            in0.getPort("out"), reg0.getPort("in"));
        schematic.addConnection("in0_to_reg0", in0_to_reg0);
        ConnectionValue clk_to_reg0 = UtilSchematicConstruction.instantiateWire(
                clk.getPort("out"), reg0.getPort("clock"));
        schematic.addConnection("clk_to_reg0", clk_to_reg0);
        ConnectionValue rst_to_reg0 = UtilSchematicConstruction.instantiateWire(
                rst.getPort("out"), reg0.getPort("reset"));
        schematic.addConnection("rst_to_reg0", rst_to_reg0);
        ConnectionValue reg0_to_out0 = UtilSchematicConstruction.instantiateWire(
                reg0.getPort("out"), out0.getPort("in"));
        schematic.addConnection("reg0_to_out0", reg0_to_out0);
        
        return schematic;
    }
    
    public void run() throws SchematicException {
        // TODO read schematic from JSON
        Schematic schematic = createDemoSchematic();
        switch (targetHDL) {
        case VHDL: {
            VHDLCodeGenerator vhdlGen = new VHDLCodeGenerator(schematic);
            if (outputDirectory != null){
                vhdlGen.setOutputDirectory(outputDirectory);
            }
            if (noChecks) {
              vhdlGen.setRunChecks(false);
            }
            vhdlGen.generateOutputProducts();
        } // end case VHDL
        }
    }
    
    public static void main(String[] args) {
        try {
            DigitalBackend backend = new DigitalBackend(args);
            backend.run();
        } catch (ParseException e) {
            log.error("error while parsing command line: "
                    + e.getMessage());
        } catch (OptionError e) {
            log.error(e.getMessage());
        } catch (SchematicException e) {
            log.error("error while building schematic: "
                    + e.getMessage());
        } catch (CodeGenerationError e) {
            log.error("error while generating code: "
                    + e.getMessage());
        }
    }

}
