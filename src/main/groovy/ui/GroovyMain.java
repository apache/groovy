/*
 * Copyright 2003-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.ui;

import groovy.lang.Binding;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovyShell;
import groovy.lang.GroovySystem;
import groovy.lang.MissingMethodException;
import groovy.lang.Script;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GroovyInternalPosixParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.codehaus.groovy.runtime.StackTraceUtils;

import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

/**
 * A Command line to execute groovy.
 *
 * @author Jeremy Rayner
 * @author Yuri Schimke
 * @author Roshan Dawrani
 * @version $Revision$
 */
public class GroovyMain {

    // arguments to the script
    private List args;

    // is this a file on disk
    private boolean isScriptFile;

    // filename or content of script
    private String script;

    // process args as input files
    private boolean processFiles;

    // edit input files in place
    private boolean editFiles;

    // automatically output the result of each script
    private boolean autoOutput;

    // automatically split each line using the splitpattern
    private boolean autoSplit;

    // The pattern used to split the current line
    private String splitPattern = " ";

    // process sockets
    private boolean processSockets;

    // port to listen on when processing sockets
    private int port;

    // backup input files with extension
    private String backupExtension;

    // do you want full stack traces in script exceptions?
    private boolean debug = false;

    // Compiler configuration, used to set the encodings of the scripts/classes
    private CompilerConfiguration conf = new CompilerConfiguration(System.getProperties());

    /**
     * Main CLI interface.
     *
     * @param args all command line args.
     */
    public static void main(String args[]) {
        processArgs(args, System.out);
    }

    // package-level visibility for testing purposes (just usage/errors at this stage)
    // TODO: should we have an 'err' printstream too for ParseException?
    static void processArgs(String[] args, final PrintStream out) {
        Options options = buildOptions();

        try {
            CommandLine cmd = parseCommandLine(options, args);

            if (cmd.hasOption('h')) {
                printHelp(out, options);
            } else if (cmd.hasOption('v')) {
                String version = GroovySystem.getVersion();
                out.println("Groovy Version: " + version + " JVM: " + System.getProperty("java.version") + 
                        " Vendor: " + System.getProperty("java.vm.vendor")  + " OS: " + System.getProperty("os.name"));
            } else {
                // If we fail, then exit with an error so scripting frameworks can catch it
                // TODO: pass printstream(s) down through process
                if (!process(cmd)) {
                    System.exit(1);
                }
            }
        } catch (ParseException pe) {
            out.println("error: " + pe.getMessage());
            printHelp(out, options);
        } catch (IOException ioe) {
            out.println("error: " + ioe.getMessage());
        }
    }

    private static void printHelp(PrintStream out, Options options) {
        HelpFormatter formatter = new HelpFormatter();
        PrintWriter pw = new PrintWriter(out);

        formatter.printHelp(
            pw,
            80,
            "groovy [options] [args]",
            "options:",
            options,
            2,
            4,
            null, // footer
            false);
       
        pw.flush();
    }

    /**
     * Parse the command line.
     *
     * @param options the options parser.
     * @param args    the command line args.
     * @return parsed command line.
     * @throws ParseException if there was a problem.
     */
    private static CommandLine parseCommandLine(Options options, String[] args) throws ParseException {
        CommandLineParser parser = new GroovyInternalPosixParser();
        return parser.parse(options, args, true);
    }

    /**
     * Build the options parser.  Has to be synchronized because of the way Options are constructed.
     *
     * @return an options parser.
     */
    @SuppressWarnings("static-access")
    private static synchronized Options buildOptions() {
        Options options = new Options();
        options.addOption(OptionBuilder.hasArg().withArgName("path").withDescription("Specify where to find the class files - must be first argument").create("classpath"));
        options.addOption(OptionBuilder.withLongOpt("classpath").hasArg().withArgName("path").withDescription("Aliases for '-classpath'").create("cp"));

        options.addOption(
            OptionBuilder.withLongOpt("define").
            withDescription("define a system property").
            hasArg(true).
            withArgName("name=value").
            create('D'));
        options.addOption(
            OptionBuilder.withLongOpt("disableopt").
            withDescription("disables one or all optimization elements. " +
                            "optlist can be a comma separated list with the elements: " +
                            "all (disables all optimizations), " +
                            "int (disable any int based optimizations)").
            hasArg(true).
            withArgName("optlist").
            create());
        options.addOption(
            OptionBuilder.hasArg(false)
            .withDescription("usage information")
            .withLongOpt("help")
            .create('h'));
        options.addOption(
            OptionBuilder.hasArg(false)
            .withDescription("debug mode will print out full stack traces")
            .withLongOpt("debug")
            .create('d'));
        options.addOption(
            OptionBuilder.hasArg(false)
            .withDescription("display the Groovy and JVM versions")
            .withLongOpt("version")
            .create('v'));
        options.addOption(
            OptionBuilder.withArgName("charset")
            .hasArg()
            .withDescription("specify the encoding of the files")
            .withLongOpt("encoding")
            .create('c'));
        options.addOption(
            OptionBuilder.withArgName("script")
            .hasArg()
            .withDescription("specify a command line script")
            .create('e'));
        options.addOption(
            OptionBuilder.withArgName("extension")
            .hasOptionalArg()
            .withDescription("modify files in place; create backup if extension is given (e.g. \'.bak\')")
            .create('i'));
        options.addOption(
            OptionBuilder.hasArg(false)
            .withDescription("process files line by line using implicit 'line' variable")
            .create('n'));
        options.addOption(
            OptionBuilder.hasArg(false)
            .withDescription("process files line by line and print result (see also -n)")
            .create('p'));
        options.addOption(
            OptionBuilder.withArgName("port")
            .hasOptionalArg()
            .withDescription("listen on a port and process inbound lines (default: 1960)")
            .create('l'));
        options.addOption(
            OptionBuilder.withArgName("splitPattern")
            .hasOptionalArg()
            .withDescription("split lines using splitPattern (default '\\s') using implicit 'split' variable")
            .withLongOpt("autosplit")
            .create('a'));
        options.addOption(
            OptionBuilder.withLongOpt("indy")
            .withDescription("enables compilation using invokedynamic")
            .create());
        options.addOption(
            OptionBuilder.withLongOpt("configscript")
            .hasArg().withDescription("A script for tweaking the configuration options")
            .create());
        options.addOption(
                OptionBuilder.withLongOpt("basescript")
                .hasArg().withArgName("class").withDescription("Base class name for scripts (must derive from Script)")
                .create('b'));
        return options;

    }

    private static void setSystemPropertyFrom(final String nameValue) {
        if(nameValue==null) throw new IllegalArgumentException("argument should not be null");

        String name, value;
        int i = nameValue.indexOf("=");

        if (i == -1) {
            name = nameValue;
            value = Boolean.TRUE.toString();
        }
        else {
            name = nameValue.substring(0, i);
            value = nameValue.substring(i + 1, nameValue.length());
        }
        name = name.trim();

        System.setProperty(name, value);
    }

    /**
     * Process the users request.
     *
     * @param line the parsed command line.
     * @throws ParseException if invalid options are chosen
     */
     private static boolean process(CommandLine line) throws ParseException, IOException {
        List args = line.getArgList();
        
        if (line.hasOption('D')) {
            String[] values = line.getOptionValues('D');

            for (int i=0; i<values.length; i++) {
                setSystemPropertyFrom(values[i]);
            }
        }

        GroovyMain main = new GroovyMain();
        
        // add the ability to parse scripts with a specified encoding
        main.conf.setSourceEncoding(line.getOptionValue('c',main.conf.getSourceEncoding()));

        main.isScriptFile = !line.hasOption('e');
        main.debug = line.hasOption('d');
        main.conf.setDebug(main.debug);
        main.processFiles = line.hasOption('p') || line.hasOption('n');
        main.autoOutput = line.hasOption('p');
        main.editFiles = line.hasOption('i');
        if (main.editFiles) {
            main.backupExtension = line.getOptionValue('i');
        }
        main.autoSplit = line.hasOption('a');
        String sp = line.getOptionValue('a');
        if (sp != null)
            main.splitPattern = sp;

        if (main.isScriptFile) {
            if (args.isEmpty())
                throw new ParseException("neither -e or filename provided");

            main.script = (String) args.remove(0);
            if (main.script.endsWith(".java"))
                throw new ParseException("error: cannot compile file with .java extension: " + main.script);
        } else {
            main.script = line.getOptionValue('e');
        }

        main.processSockets = line.hasOption('l');
        if (main.processSockets) {
            String p = line.getOptionValue('l', "1960"); // default port to listen to
            main.port = Integer.parseInt(p);
        }
        
        // we use "," as default, because then split will create
        // an empty array if no option is set
        String disabled = line.getOptionValue("disableopt", ",");
        String[] deopts = disabled.split(",");
        for (String deopt_i : deopts) {
            main.conf.getOptimizationOptions().put(deopt_i,false);
        }
        
        if (line.hasOption("indy")) {
            CompilerConfiguration.DEFAULT.getOptimizationOptions().put("indy", true);
            main.conf.getOptimizationOptions().put("indy", true);
        }

         if (line.hasOption("basescript")) {
             main.conf.setScriptBaseClass(line.getOptionValue("basescript"));
         }

         if (line.hasOption("configscript")) {
             String path = line.getOptionValue("configscript");
             File groovyConfigurator = new File(path);
             Binding binding = new Binding();
             binding.setVariable("configuration", main.conf);

             CompilerConfiguration configuratorConfig = new CompilerConfiguration();
             ImportCustomizer customizer = new ImportCustomizer();
             customizer.addStaticStars("org.codehaus.groovy.control.customizers.builder.CompilerCustomizationBuilder");
             configuratorConfig.addCompilationCustomizers(customizer);

             GroovyShell shell = new GroovyShell(binding, configuratorConfig);
             shell.evaluate(groovyConfigurator);
         }

         main.args = args;

        return main.run();
    }


    /**
     * Run the script.
     */
    private boolean run() {
        try {
            if (processSockets) {
                processSockets();
            } else if (processFiles) {
                processFiles();
            } else {
                processOnce();
            }
            return true;
        } catch (CompilationFailedException e) {
            System.err.println(e);
            return false;
        } catch (Throwable e) {
            if (e instanceof InvokerInvocationException) {
                InvokerInvocationException iie = (InvokerInvocationException) e;
                e = iie.getCause();
            }
            System.err.println("Caught: " + e);
            if (!debug) {
                StackTraceUtils.deepSanitize(e);
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Process Sockets.
     */
    private void processSockets() throws CompilationFailedException, IOException {
        GroovyShell groovy = new GroovyShell(conf);
        //check the script is currently valid before starting a server against the script
        if (isScriptFile) {
            groovy.parse(getText(script));
        } else {
            groovy.parse(script);
        }
        new GroovySocketServer(groovy, isScriptFile, script, autoOutput, port);
    }

    public String getText(String urlOrFilename) throws IOException {
        if (isScriptUrl(urlOrFilename)) {
            try {
                return ResourceGroovyMethods.getText(new URL(urlOrFilename));
            } catch (Exception e) {
                throw new GroovyRuntimeException("Unable to get script from URL: ", e);
            }
        }
        return ResourceGroovyMethods.getText(huntForTheScriptFile(urlOrFilename));
    }

    private boolean isScriptUrl(String urlOrFilename) {
        return urlOrFilename.startsWith("http://") || urlOrFilename.startsWith("https://") || urlOrFilename.startsWith("file:") || urlOrFilename.startsWith("jar:");
    }

    /**
     * Hunt for the script file, doesn't bother if it is named precisely.
     *
     * Tries in this order:
     * - actual supplied name
     * - name.groovy
     * - name.gvy
     * - name.gy
     * - name.gsh
     */
    public File huntForTheScriptFile(String input) {
        String scriptFileName = input.trim();
        File scriptFile = new File(scriptFileName);
        String[] standardExtensions = {".groovy",".gvy",".gy",".gsh"};
        int i = 0;
        while (i < standardExtensions.length && !scriptFile.exists()) {
            scriptFile = new File(scriptFileName + standardExtensions[i]);
            i++;
        }
        // if we still haven't found the file, point back to the originally specified filename
        if (!scriptFile.exists()) {
            scriptFile = new File(scriptFileName);
        }
        return scriptFile;
    }

    /**
     * Process the input files.
     */
    private void processFiles() throws CompilationFailedException, IOException {
        GroovyShell groovy = new GroovyShell(conf);

        Script s;

        if (isScriptFile) {
            if (isScriptUrl(script)) {
                s = groovy.parse(getText(script), script.substring(script.lastIndexOf("/") + 1));
            } else {
                s = groovy.parse(huntForTheScriptFile(script));
            }
        } else {
            s = groovy.parse(script, "main");
        }

        if (args.isEmpty()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter writer = new PrintWriter(System.out);

            try {
                processReader(s, reader, writer);
            } finally {
                reader.close();
                writer.close();
            }

        } else {
            Iterator i = args.iterator();
            while (i.hasNext()) {
                String filename = (String) i.next();
                File file = huntForTheScriptFile(filename);
                processFile(s, file);
            }
        }
    }

    /**
     * Process a single input file.
     *
     * @param s    the script to execute.
     * @param file the input file.
     */
    private void processFile(Script s, File file) throws IOException {
        if (!file.exists())
            throw new FileNotFoundException(file.getName());

        if (!editFiles) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            try {
                PrintWriter writer = new PrintWriter(System.out);
                processReader(s, reader, writer);
                writer.flush();
            } finally {
                reader.close();
            }
        } else {
            File backup;
            if (backupExtension == null) {
                backup = File.createTempFile("groovy_", ".tmp");
                backup.deleteOnExit();
            } else {
                backup = new File(file.getPath() + backupExtension);
            }
            backup.delete();
            if (!file.renameTo(backup))
                throw new IOException("unable to rename " + file + " to " + backup);

            BufferedReader reader = new BufferedReader(new FileReader(backup));
            try {
                PrintWriter writer = new PrintWriter(new FileWriter(file));
                try {
                    processReader(s, reader, writer);
                } finally {
                    writer.close();
                }
            } finally {
                reader.close();
            }
        }
    }

    /**
     * Process a script against a single input file.
     *
     * @param s      script to execute.
     * @param reader input file.
     * @param pw     output sink.
     */
    private void processReader(Script s, BufferedReader reader, PrintWriter pw) throws IOException {
        String line;
        String lineCountName = "count";
        s.setProperty(lineCountName, BigInteger.ZERO);
        String autoSplitName = "split";
        s.setProperty("out", pw);

        try {
            InvokerHelper.invokeMethod(s, "begin", null);
        } catch (MissingMethodException mme) {
            // ignore the missing method exception
            // as it means no begin() method is present
        }

        while ((line = reader.readLine()) != null) {
            s.setProperty("line", line);
            s.setProperty(lineCountName, ((BigInteger)s.getProperty(lineCountName)).add(BigInteger.ONE));

            if(autoSplit) {
                s.setProperty(autoSplitName, line.split(splitPattern));
            }

            Object o = s.run();

            if (autoOutput && o != null) {
                pw.println(o);
            }
        }

        try {
            InvokerHelper.invokeMethod(s, "end", null);
        } catch (MissingMethodException mme) {
            // ignore the missing method exception
            // as it means no end() method is present
        }
    }

    /**
     * Process the standard, single script with args.
     */
    private void processOnce() throws CompilationFailedException, IOException {
        GroovyShell groovy = new GroovyShell(conf);

        if (isScriptFile) {
            if (isScriptUrl(script)) {
                groovy.run(getText(script), script.substring(script.lastIndexOf("/") + 1), args);
            } else {
                groovy.run(huntForTheScriptFile(script), args);
            }
        } else {
            groovy.run(script, "script_from_command_line", args);
        }
    }
}
