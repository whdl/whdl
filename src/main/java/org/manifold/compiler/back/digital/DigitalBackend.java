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
        
        NodeValue a = UtilSchematicConstruction.instantiateInputPin();
        NodeValue b = UtilSchematicConstruction.instantiateInputPin();
        NodeValue c = UtilSchematicConstruction.instantiateOutputPin();
        
        NodeValue notA = UtilSchematicConstruction.instantiateNot();
        NodeValue notB = UtilSchematicConstruction.instantiateNot();
        NodeValue notAandB = UtilSchematicConstruction.instantiateAnd();
        NodeValue AandnotB = UtilSchematicConstruction.instantiateAnd();
        NodeValue xorOr = UtilSchematicConstruction.instantiateOr();
        
        schematic.addNode("a", a);
        schematic.addNode("b", b);
        schematic.addNode("c", c);
        schematic.addNode("notA", notA);
        schematic.addNode("notB", notB);
        schematic.addNode("notAandB", notAandB);
        schematic.addNode("AandnotB", AandnotB);
        schematic.addNode("xorOr", xorOr);
        
        ConnectionValue aToNotA = UtilSchematicConstruction.instantiateWire(
            a.getPort("out"),
            notA.getPort("in")
            );
        ConnectionValue bToNotB = UtilSchematicConstruction.instantiateWire(
            b.getPort("out"),
            notB.getPort("in")
            );
        ConnectionValue aToaAndNotb = UtilSchematicConstruction.instantiateWire(
            a.getPort("out"),
            AandnotB.getPort("in0")
            );
        ConnectionValue bToNotaAndb= UtilSchematicConstruction.instantiateWire(
            b.getPort("out"),
            notAandB.getPort("in1")
            );
        ConnectionValue notAtoNotaAndb = UtilSchematicConstruction.instantiateWire(
            notA.getPort("out"),
            notAandB.getPort("in0")
            );
        ConnectionValue notBtoaAndNotb = UtilSchematicConstruction.instantiateWire(
            notB.getPort("out"),
            AandnotB.getPort("in1")
            );
        ConnectionValue notaAndbToOr = UtilSchematicConstruction.instantiateWire(
            notAandB.getPort("out"),
            xorOr.getPort("in0")
            );
        ConnectionValue aAndNotbToOr = UtilSchematicConstruction.instantiateWire(
            AandnotB.getPort("out"),
            xorOr.getPort("in1")
            );
        ConnectionValue orToC = UtilSchematicConstruction.instantiateWire(
            xorOr.getPort("out"),
            c.getPort("in")
            );
        schematic.addConnection("a1", aToNotA);
        schematic.addConnection("b1", bToNotB);
        schematic.addConnection("a2", aToaAndNotb);
        schematic.addConnection("b2", bToNotaAndb);
        schematic.addConnection("notA1", notAtoNotaAndb);
        schematic.addConnection("notB1", notBtoaAndNotb);
        schematic.addConnection("notA2", notaAndbToOr);
        schematic.addConnection("notB2", aAndNotbToOr);
        schematic.addConnection("c1", orToC);
        
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
