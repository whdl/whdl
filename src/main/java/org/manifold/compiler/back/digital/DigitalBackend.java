package org.manifold.compiler.back.digital;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
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
import org.manifold.compiler.OptionError;
import org.manifold.compiler.UndefinedBehaviourError;
import org.manifold.compiler.middle.Schematic;
import org.manifold.compiler.middle.SchematicException;
import org.manifold.compiler.middle.serialization.SchematicDeserializer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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

  public enum TARGET_HDL {
    VHDL,
  };

  private TARGET_HDL targetHDL = null;
  public TARGET_HDL getTargetHDL() {
    return targetHDL;
  }

  private void createOptionTargetHDL() {
    Option hdl = new Option("h", "hdl", true, "target HDL type (vhdl)");
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
        throw new OptionError("target HDL '" + hdl + "' not recognized");
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
    if (outDir != null) {
      outputDirectory = outDir;
    }
  }

  boolean noChecks = false;

  @SuppressWarnings("static-access")
  private void createOptionNoChecks() {
    Option noChecks = OptionBuilder
        .withLongOpt("no-checks")
        .withDescription(
            "do not run design checks "
                + "(may result in incorrect code generation)").create();
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

  private List<String> inputs;

  public DigitalBackend(String[] args) throws ParseException {
    setupLogging();
    // set up options for command-line parsing
    createOptionDefinitions();
    // parse command line
    CommandLineParser parser = new org.apache.commons.cli.BasicParser();
    CommandLine cmd = parser.parse(options, args);
    // retrieve command-line options
    collectOptions(cmd);
    inputs = Arrays.asList(cmd.getArgs());
  }

  public void run() throws SchematicException {
    // TODO proper handling for multiple input files
    // TODO handling of input schematic from memory
    if (inputs.isEmpty()) {
      log.warn("no input files, nothing to do");
      return;
    }
    if (inputs.size() > 1) {
      throw new UndefinedBehaviourError("cannot compile from multiple inputs");
    }
    SchematicDeserializer deserializer = new SchematicDeserializer();
    FileReader inFile;
    try {
      inFile = new FileReader(inputs.get(0));
    } catch (FileNotFoundException e) {
      log.error("input file '" + inputs.get(0) + "' not found");
      return;
    }
    JsonObject inputJson = new JsonParser().parse(inFile).getAsJsonObject();
    Schematic schematic = deserializer.deserialize(inputJson);
    switch (targetHDL) {
        case VHDL: {
          VHDLCodeGenerator vhdlGen = new VHDLCodeGenerator(schematic);
          if (outputDirectory != null) {
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
      log.error("error while parsing command line: " + e.getMessage());
    } catch (OptionError e) {
      log.error(e.getMessage());
    } catch (SchematicException e) {
      log.error("error while building schematic: " + e.getMessage());
    } catch (CodeGenerationError e) {
      log.error("error while generating code: " + e.getMessage());
    }
  }

}
