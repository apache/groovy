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
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.codehaus.groovy.runtime.StackTraceUtils;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.Unmatched;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A Command line to execute groovy.
 */
public class GroovyMain {

    // arguments to the script
    private List<String> args;

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
    public static void main(String[] args) {
        processArgs(args, System.out, System.err);
    }

    // package-level visibility for testing purposes (just usage/errors at this stage)
    @Deprecated
    static void processArgs(String[] args, final PrintStream out) {
        processArgs(args, out, out);
    }

    // package-level visibility for testing purposes (just usage/errors at this stage)
    static void processArgs(String[] args, final PrintStream out, final PrintStream err) {
        GroovyCommand groovyCommand = new GroovyCommand();

        CommandLine parser = new CommandLine(groovyCommand)
                .setOut(new PrintWriter(out))
                .setErr(new PrintWriter(err))
                .setUnmatchedArgumentsAllowed(true)
                .setStopAtUnmatched(true);

        try {
            ParseResult result = parser.parseArgs(args);

            if (CommandLine.printHelpIfRequested(result)) {
                return;
            }

            // TODO: pass printstream(s) down through process
            if (!groovyCommand.process(parser)) {
                // If we fail, then exit with an error so scripting frameworks can catch it.
                System.exit(1);
            }

        } catch (ParameterException ex) { // command line arguments could not be parsed
            err.println(ex.getMessage());
            ex.getCommandLine().usage(err);
        } catch (IOException ioe) {
            err.println("error: " + ioe.getMessage());
        }
    }

    public static class VersionProvider implements IVersionProvider {
        @Override
        public String[] getVersion() {
            return new String[] {
                    "Groovy Version: " + GroovySystem.getVersion() + " JVM: " + System.getProperty("java.version") +
                    " Vendor: " + System.getProperty("java.vm.vendor")  + " OS: " + System.getProperty("os.name")
            };
        }
    }

    @Command(name = "groovy",
            customSynopsis = "groovy [options] [filename] [args]",
            description = "The Groovy command line processor.",
            sortOptions = false,
            versionProvider = VersionProvider.class)
    private static class GroovyCommand {

        // IMPLEMENTATION NOTE:
        // classpath must be the first argument, so that the `startGroovy(.bat)` script
        // can extract it and the JVM can be started with the classpath already correctly set.
        // This saves us from having to fork a new JVM process with the classpath set from the processed arguments.
        @Option(names = {"-cp", "-classpath", "--classpath"}, paramLabel = "<path>", description = "Specify where to find the class files - must be first argument")
        private String classpath;

        @Option(names = {"-D", "--define"}, paramLabel = "<property=value>", description = "Define a system property")
        private Map<String, String> systemProperties = new LinkedHashMap<String, String>();

        @Option(names = "--disableopt", paramLabel = "optlist", split = ",",
                description = {
                        "Disables one or all optimization elements; optlist can be a comma separated list with the elements: ",
                                "all (disables all optimizations), ",
                                "int (disable any int based optimizations)"})
        private List<String> disableopt = new ArrayList<String>();

        @Option(names = {"-d", "--debug"}, description = "Debug mode will print out full stack traces")
        private boolean debug;

        @Option(names = {"-c", "--encoding"}, paramLabel = "<charset>", description = "Specify the encoding of the files")
        private String encoding;

        @Option(names = {"-e"}, paramLabel = "<script>", description = "Specify a command line script")
        private String script;

        @Option(names = {"-i"}, arity = "0..1", paramLabel = "<extension>", description = "Modify files in place; create backup if extension is given (e.g. '.bak')")
        private String extension;

        @Option(names = {"-n"}, description = "Process files line by line using implicit 'line' variable")
        private boolean lineByLine;

        @Option(names = {"-p"}, description = "Process files line by line and print result (see also -n)")
        private boolean lineByLinePrint;

        @Option(names = {"-pa", "--parameters"}, description = "Generate metadata for reflection on method parameter names (jdk8+ only)")
        private boolean parameterMetadata;

        @Option(names = {"-pr", "--enable-preview"}, description = "Enable preview Java features (JEP 12) (jdk12+ only)")
        private boolean previewFeatures;

        @Option(names = "-l", arity = "0..1", paramLabel = "<port>", description = "Listen on a port and process inbound lines (default: 1960)")
        private String port;

        @Option(names = {"-a", "--autosplit"}, arity = "0..1", paramLabel = "<splitPattern>", description = "Split lines using splitPattern (default '\\s') using implicit 'split' variable")
        private String splitPattern;

        @Option(names = {"--configscript"}, paramLabel = "<script>", description = "A script for tweaking the configuration options")
        private String configscript;

        @Option(names = {"-b", "--basescript"}, paramLabel = "<class>", description = "Base class name for scripts (must derive from Script)")
        private String scriptBaseClass;

        @Option(names = {"-h", "--help"}, usageHelp = true, description = "Show this help message and exit")
        private boolean helpRequested;

        @Option(names = {"-v", "--version"}, versionHelp = true, description = "Print version information and exit")
        private boolean versionRequested;

        @Option(names = {"--compile-static"}, description = "Use CompileStatic")
        private boolean compileStatic;

        @Option(names = {"--type-checked"}, description = "Use TypeChecked")
        private boolean typeChecked;

        @Unmatched
        List<String> arguments = new ArrayList<>();

        /**
         * Process the users request.
         *
         * @param parser the parsed command line. Used when the user input was invalid.
         * @throws ParameterException if the user input was invalid
         */
        boolean process(CommandLine parser) throws ParameterException, IOException {
            for (Map.Entry<String, String> entry : systemProperties.entrySet()) {
                System.setProperty(entry.getKey(), entry.getValue());
            }

            final GroovyMain main = new GroovyMain();

            // add the ability to parse scripts with a specified encoding
            main.conf.setSourceEncoding(encoding);

            main.debug = debug;
            main.conf.setDebug(main.debug);
            main.conf.setParameters(parameterMetadata);
            main.conf.setPreviewFeatures(previewFeatures);
            main.processFiles = lineByLine || lineByLinePrint;
            main.autoOutput = lineByLinePrint;
            main.editFiles = extension != null;
            if (main.editFiles) {
                main.backupExtension = extension;
            }

            main.autoSplit = splitPattern != null;
            if (main.autoSplit) {
                main.splitPattern = splitPattern;
            }

            main.isScriptFile = script == null;
            if (main.isScriptFile) {
                if (arguments.isEmpty()) {
                    throw new ParameterException(parser, "error: neither -e or filename provided");
                }
                main.script = arguments.remove(0);
                if (main.script.endsWith(".java")) {
                    throw new ParameterException(parser, "error: cannot compile file with .java extension: " + main.script);
                }
            } else {
                main.script = script;
            }

            main.processSockets = port != null;
            if (main.processSockets) {
                String p = port.trim().length() > 0 ? port : "1960"; // default port to listen to
                main.port = Integer.parseInt(p);
            }

            for (String optimization : disableopt) {
                main.conf.getOptimizationOptions().put(optimization, false);
            }

            if (scriptBaseClass != null) {
                main.conf.setScriptBaseClass(scriptBaseClass);
            }

            final List<String> transformations = new ArrayList<>();
            if (compileStatic) {
                transformations.add("ast(groovy.transform.CompileStatic)");
            }
            if (typeChecked) {
                transformations.add("ast(groovy.transform.TypeChecked)");
            }
            if (!transformations.isEmpty()) {
                processConfigScriptText(buildConfigScriptText(transformations), main.conf);
            }

            processConfigScripts(getConfigScripts(), main.conf);

            main.args = arguments;

            return main.run();
        }

        private List<String> getConfigScripts() {
            List<String> scripts = new ArrayList<String>();

            if (this.configscript != null) {
                scripts.add(this.configscript);
            }

            String configScripts = System.getProperty("groovy.starter.configscripts", null);

            if (configScripts != null && !configScripts.isEmpty()) {
                scripts.addAll(StringGroovyMethods.tokenize(configScripts, ','));
            }

            return scripts;
        }
    }

    public static void processConfigScripts(List<String> scripts, CompilerConfiguration conf) throws IOException {
        if (scripts.isEmpty()) return;

        GroovyShell shell = createConfigScriptsShell(conf);

        for (String script : scripts) {
            shell.evaluate(new File(script));
        }
    }

    public static void processConfigScriptText(String scriptText, CompilerConfiguration conf) {
        if (scriptText.trim().isEmpty()) return;

        GroovyShell shell = createConfigScriptsShell(conf);

        shell.evaluate(scriptText);
    }

    public static String buildConfigScriptText(List<String> transforms) {
        StringBuilder script = new StringBuilder();
        script.append("withConfig(configuration) {").append("\n");

        for (String t : transforms) {
            script.append(t).append(";\n");
        }

        script.append("}");

        return script.toString();
    }

    private static GroovyShell createConfigScriptsShell(CompilerConfiguration conf) {
        Binding binding = new Binding();
        binding.setVariable("configuration", conf);

        CompilerConfiguration configuratorConfig = new CompilerConfiguration();
        ImportCustomizer customizer = new ImportCustomizer();

        customizer.addStaticStars("org.codehaus.groovy.control.customizers.builder.CompilerCustomizationBuilder");

        configuratorConfig.addCompilationCustomizers(customizer);

        return new GroovyShell(binding, configuratorConfig);
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
     * @param script       the location or context of the script
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
            return new GroovyCodeSource(scriptFile);
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
        class DoSetContext implements PrivilegedAction<Object> {
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
        GroovyShell groovy = new GroovyShell(Thread.currentThread().getContextClassLoader(), conf);
        setupContextClassLoader(groovy);

        Script s = groovy.parse(getScriptSource(isScriptFile, script));

        if (args.isEmpty()) {
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                PrintWriter writer = new PrintWriter(System.out);
                processReader(s, reader, writer);
                writer.flush();
            }
        } else {
            for (String filename : args) {
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

            try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
                PrintWriter writer = new PrintWriter(System.out);
                processReader(s, reader, writer);
                writer.flush();
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
        GroovyShell groovy = new GroovyShell(Thread.currentThread().getContextClassLoader(), conf);
        setupContextClassLoader(groovy);
        groovy.run(getScriptSource(isScriptFile, script), args);
    }
}
