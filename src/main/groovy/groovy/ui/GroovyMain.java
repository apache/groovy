/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.ui;

import groovy.lang.Binding;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovyShell;
import groovy.lang.GroovySystem;
import groovy.lang.MissingMethodException;
import groovy.lang.Script;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.codehaus.groovy.runtime.StackTraceUtils;
import org.codehaus.groovy.runtime.StringGroovyMethods;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import static org.apache.commons.cli.Option.builder;

/**
 * A Command line to execute groovy.
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
                "groovy [options] [filename] [args]",
                "The Groovy command line processor.\nOptions:",
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
        CommandLineParser parser = new DefaultParser();
        return parser.parse(options, args, true);
    }

    /**
     * Build the options parser.
     *
     * @return an options parser.
     */
    private static Options buildOptions() {
        return new Options()
                .addOption(builder("classpath").hasArg().argName("path").desc("Specify where to find the class files - must be first argument").build())
                .addOption(builder("cp").longOpt("classpath").hasArg().argName("path").desc("Aliases for '-classpath'").build())
                .addOption(builder("D").longOpt("define").desc("Define a system property").numberOfArgs(2).valueSeparator().argName("name=value").build())
                .addOption(
                        builder().longOpt("disableopt")
                                .desc("Disables one or all optimization elements; " +
                                        "optlist can be a comma separated list with the elements: " +
                                        "all (disables all optimizations), " +
                                        "int (disable any int based optimizations)")
                                .hasArg().argName("optlist").build())
                .addOption(builder("h").hasArg(false).desc("Usage information").longOpt("help").build())
                .addOption(builder("d").hasArg(false).desc("Debug mode will print out full stack traces").longOpt("debug").build())
                .addOption(builder("v").hasArg(false).desc("Display the Groovy and JVM versions").longOpt("version").build())
                .addOption(builder("c").argName("charset").hasArg().desc("Specify the encoding of the files").longOpt("encoding").build())
                .addOption(builder("e").argName("script").hasArg().desc("Specify a command line script").build())
                .addOption(builder("i").argName("extension").optionalArg(true).desc("Modify files in place; create backup if extension is given (e.g. \'.bak\')").build())
                .addOption(builder("n").hasArg(false).desc("Process files line by line using implicit 'line' variable").build())
                .addOption(builder("p").hasArg(false).desc("Process files line by line and print result (see also -n)").build())
                .addOption(builder("pa").hasArg(false).desc("Generate metadata for reflection on method parameter names (jdk8+ only)").longOpt("parameters").build())
                .addOption(builder("l").argName("port").optionalArg(true).desc("Listen on a port and process inbound lines (default: 1960)").build())
                .addOption(builder("a").argName("splitPattern").optionalArg(true).desc("Split lines using splitPattern (default '\\s') using implicit 'split' variable").longOpt("autosplit").build())
                .addOption(builder().longOpt("indy").desc("Enables compilation using invokedynamic").build())
                .addOption(builder().longOpt("configscript").hasArg().desc("A script for tweaking the configuration options").build())
                .addOption(builder("b").longOpt("basescript").hasArg().argName("class").desc("Base class name for scripts (must derive from Script)").build());
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
            Properties optionProperties = line.getOptionProperties("D");
            Enumeration<String> propertyNames = (Enumeration<String>) optionProperties.propertyNames();
            while (propertyNames.hasMoreElements()) {
                String nextName = propertyNames.nextElement();
                System.setProperty(nextName, optionProperties.getProperty(nextName));
            }
        }

        GroovyMain main = new GroovyMain();

        // add the ability to parse scripts with a specified encoding
        main.conf.setSourceEncoding(line.getOptionValue('c',main.conf.getSourceEncoding()));

        main.isScriptFile = !line.hasOption('e');
        main.debug = line.hasOption('d');
        main.conf.setDebug(main.debug);
        main.conf.setParameters(line.hasOption("pa"));
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

        String configScripts = System.getProperty("groovy.starter.configscripts", null);
        if (line.hasOption("configscript") || (configScripts != null && !configScripts.isEmpty())) {
            List<String> scripts = new ArrayList<String>();
            if (line.hasOption("configscript")) {
                scripts.add(line.getOptionValue("configscript"));
            }
            if (configScripts != null) {
                scripts.addAll(StringGroovyMethods.tokenize((CharSequence) configScripts, ','));
            }
            processConfigScripts(scripts, main.conf);
        }

        main.args = args;
        return main.run();
    }

    public static void processConfigScripts(List<String> scripts, CompilerConfiguration conf) throws IOException {
        if (scripts.isEmpty()) return;
        Binding binding = new Binding();
        binding.setVariable("configuration", conf);
        CompilerConfiguration configuratorConfig = new CompilerConfiguration();
        ImportCustomizer customizer = new ImportCustomizer();
        customizer.addStaticStars("org.codehaus.groovy.control.customizers.builder.CompilerCustomizationBuilder");
        configuratorConfig.addCompilationCustomizers(customizer);
        GroovyShell shell = new GroovyShell(binding, configuratorConfig);
        for (String script : scripts) {
            shell.evaluate(new File(script));
        }
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
    private void processSockets() throws CompilationFailedException, IOException, URISyntaxException {
        GroovyShell groovy = new GroovyShell(conf);
        new GroovySocketServer(groovy, getScriptSource(isScriptFile, script), autoOutput, port);
    }

    /**
     * Get the text of the Groovy script at the given location.
     * If the location is a file path and it does not exist as given,
     * then {@link GroovyMain#huntForTheScriptFile(String)} is called to try
     * with some Groovy extensions appended.
     *
     * This method is not used to process scripts and is retained for backward
     * compatibility.  If you want to modify how GroovyMain processes scripts
     * then use {@link GroovyMain#getScriptSource(boolean, String)}.
     *
     * @param uriOrFilename
     * @return the text content at the location
     * @throws IOException
     * @deprecated
     */
    @Deprecated
    public String getText(String uriOrFilename) throws IOException {
        if (URI_PATTERN.matcher(uriOrFilename).matches()) {
            try {
                return ResourceGroovyMethods.getText(new URL(uriOrFilename));
            } catch (Exception e) {
                throw new GroovyRuntimeException("Unable to get script from URL: ", e);
            }
        }
        return ResourceGroovyMethods.getText(huntForTheScriptFile(uriOrFilename));
    }

    /**
     * Get a new GroovyCodeSource for a script which may be given as a location
     * (isScript is true) or as text (isScript is false).
     *
     * @param isScriptFile indicates whether the script parameter is a location or content
     * @param script the location or context of the script
     * @return a new GroovyCodeSource for the given script
     * @throws IOException
     * @throws URISyntaxException
     * @since 2.3.0
     */
    protected GroovyCodeSource getScriptSource(boolean isScriptFile, String script) throws IOException, URISyntaxException {
        //check the script is currently valid before starting a server against the script
        if (isScriptFile) {
            // search for the file and if it exists don't try to use URIs ...
            File scriptFile = huntForTheScriptFile(script);
            if (!scriptFile.exists() && URI_PATTERN.matcher(script).matches()) {
                return new GroovyCodeSource(new URI(script));
            }
            return new GroovyCodeSource( scriptFile );
        }
        return new GroovyCodeSource(script, "script_from_command_line", GroovyShell.DEFAULT_CODE_BASE);
    }

    // RFC2396
    // scheme        = alpha *( alpha | digit | "+" | "-" | "." )
    // match URIs but not Windows filenames, e.g.: http://cnn.com but not C:\xxx\file.ext
    private static final Pattern URI_PATTERN = Pattern.compile("\\p{Alpha}[-+.\\p{Alnum}]*:[^\\\\]*");

    /**
     * Search for the script file, doesn't bother if it is named precisely.
     *
     * Tries in this order:
     * - actual supplied name
     * - name.groovy
     * - name.gvy
     * - name.gy
     * - name.gsh
     *
     * @since 2.3.0
     */
    public static File searchForGroovyScriptFile(String input) {
        String scriptFileName = input.trim();
        File scriptFile = new File(scriptFileName);
        // TODO: Shouldn't these extensions be kept elsewhere?  What about CompilerConfiguration?
        // This method probably shouldn't be in GroovyMain either.
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
     * Hunt for the script file by calling searchForGroovyScriptFile(String).
     *
     * @see GroovyMain#searchForGroovyScriptFile(String)
     */
    public File huntForTheScriptFile(String input) {
        return GroovyMain.searchForGroovyScriptFile(input);
    }

    // GROOVY-6771
    private static void setupContextClassLoader(GroovyShell shell) {
        final Thread current = Thread.currentThread();
        class DoSetContext implements PrivilegedAction {
            ClassLoader classLoader;

            public DoSetContext(ClassLoader loader) {
                classLoader = loader;
            }

            public Object run() {
                current.setContextClassLoader(classLoader);
                return null;
            }
        }

        AccessController.doPrivileged(new DoSetContext(shell.getClassLoader()));
    }

    /**
     * Process the input files.
     */
    private void processFiles() throws CompilationFailedException, IOException, URISyntaxException {
        GroovyShell groovy = new GroovyShell(conf);
        setupContextClassLoader(groovy);

        Script s = groovy.parse(getScriptSource(isScriptFile, script));

        if (args.isEmpty()) {
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                PrintWriter writer = new PrintWriter(System.out)) {

                processReader(s, reader, writer);
            }
        } else {
            Iterator i = args.iterator();
            while (i.hasNext()) {
                String filename = (String) i.next();
                //TODO: These are the arguments for -p and -i.  Why are we searching using Groovy script extensions?
                // Where is this documented?
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

            try(BufferedReader reader = new BufferedReader(new FileReader(file));
                PrintWriter writer = new PrintWriter(System.out)) {

                processReader(s, reader, writer);
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


            try(BufferedReader reader = new BufferedReader(new FileReader(backup));
                PrintWriter writer = new PrintWriter(new FileWriter(file))) {

                processReader(s, reader, writer);
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
    private void processOnce() throws CompilationFailedException, IOException, URISyntaxException {
        GroovyShell groovy = new GroovyShell(conf);
        setupContextClassLoader(groovy);
        groovy.run(getScriptSource(isScriptFile, script), args);
    }
}
