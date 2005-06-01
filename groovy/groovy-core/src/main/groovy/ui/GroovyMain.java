/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
 statements and notices.  Redistributions must also contain a
 copy of this document.

 2. Redistributions in binary form must reproduce the
 above copyright notice, this list of conditions and the
 following disclaimer in the documentation and/or other
 materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
 products derived from this Software without prior written
 permission of The Codehaus.  For written permission,
 please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
 nor may "groovy" appear in their names without prior written
 permission of The Codehaus. "groovy" is a registered
 trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
 http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package groovy.ui;

import groovy.lang.GroovyShell;
import groovy.lang.MetaClass;
import groovy.lang.Script;
import org.apache.commons.cli.*;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.codehaus.groovy.tools.ErrorReporter;

import java.io.*;
import java.util.Iterator;
import java.util.List;

/**
 * A Command line to execute groovy.
 *
 * @author Jeremy Rayner
 * @author Yuri Schimke
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

    // process sockets
    private boolean processSockets;

    // port to listen on when processing sockets
    private int port;

    // backup input files with extension
    private String backupExtension;

    // do you want full stack traces in script exceptions?
    private boolean debug = false;

    // Compiler configuration, used to set the encodings of the scripts/classes
    private CompilerConfiguration conf = new CompilerConfiguration();

    /**
     * Main CLI interface.
     *
     * @param args all command line args.
     */
    public static void main(String args[]) {
        MetaClass.setUseReflection(true);

        Options options = buildOptions();

        try {
            CommandLine cmd = parseCommandLine(options, args);

            if (cmd.hasOption('h')) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("groovy", options);
            } else if (cmd.hasOption('v')) {
                String version = InvokerHelper.getVersion();
                System.out.println("Groovy Version: " + version + " JVM: " + System.getProperty("java.vm.version"));
            } else {
                // If we fail, then exit with an error so scripting frameworks can catch it
                if (!process(cmd)) {
                    System.exit(1);
                }
            }
        } catch (ParseException pe) {
            System.out.println("error: " + pe.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("groovy", options);
        }
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
        CommandLineParser parser = new PosixParser();
        CommandLine cmd = parser.parse(options, args, true);
        return cmd;
    }

    /**
     * Build the options parser.  Has to be synchronized because of the way Options are constructed.
     *
     * @return an options parser.
     */
    private static synchronized Options buildOptions() {
        Options options = new Options();

        options.addOption(OptionBuilder.hasArg(false).withDescription("usage information").withLongOpt("help").create('h'));

        options.addOption(OptionBuilder.hasArg(false).withDescription("debug mode will print out full stack traces").withLongOpt("debug").create('d'));

        options.addOption(OptionBuilder.hasArg(false).withDescription("display the Groovy and JVM versions").withLongOpt("version").create('v'));

        options.addOption(OptionBuilder.withArgName("charset").hasArg().withDescription("specify the encoding of the files").withLongOpt("encoding").create('c'));

        options.addOption(OptionBuilder.withArgName("script").hasArg().withDescription("specify a command line script").create('e'));

        options.addOption(OptionBuilder.withArgName("extension").hasOptionalArg().withDescription("modify files in place").create('i'));

        options.addOption(OptionBuilder.hasArg(false).withDescription("process files line by line").create('n'));

        options.addOption(OptionBuilder.hasArg(false).withDescription("process files line by line and print result").create('p'));

        options.addOption(OptionBuilder.withArgName("port").hasOptionalArg().withDescription("listen on a port and process inbound lines").create('l'));
        return options;
    }

    /**
     * Process the users request.
     *
     * @param line the parsed command line.
     * @throws ParseException if invalid options are chosen
     */
    private static boolean process(CommandLine line) throws ParseException {
        GroovyMain main = new GroovyMain();

        List args = line.getArgList();

        // add the ability to parse scripts with a specified encoding
        if (line.hasOption('c')) {
            main.conf.setSourceEncoding(line.getOptionValue("encoding"));
        }

        main.isScriptFile = !line.hasOption('e');
        main.debug = line.hasOption('d');
        main.processFiles = line.hasOption('p') || line.hasOption('n');
        main.autoOutput = line.hasOption('p');
        main.editFiles = line.hasOption('i');
        if (main.editFiles) {
            main.backupExtension = line.getOptionValue('i');
        }

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
            main.port = new Integer(p).intValue();
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
            new ErrorReporter( e, debug ).write( System.err );
            return false;
        } catch (Throwable e) {
            if (e instanceof InvokerInvocationException) {
                InvokerInvocationException iie = (InvokerInvocationException) e;
                e = iie.getCause();
            }
            System.err.println("Caught: " + e);
            if (debug) {
                e.printStackTrace();
            } else {
                StackTraceElement[] stackTrace = e.getStackTrace();
                for (int i = 0; i < stackTrace.length; i++) {
                    StackTraceElement element = stackTrace[i];
                    if (!element.getFileName().endsWith(".java")) {
                        System.err.println("\tat " + element);
                    }
                }
            }
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
            groovy.parse(new FileInputStream(huntForTheScriptFile(script)));
        } else {
            groovy.parse(script);
        }
        new GroovySocketServer(groovy, isScriptFile, script, autoOutput, port);
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
    public File huntForTheScriptFile(String scriptFileName) {
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

        Script s = null;

        if (isScriptFile) {
            s = groovy.parse(huntForTheScriptFile(script));
        } else {
            s = groovy.parse(script, "main");
        }

        if (args.isEmpty()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter writer = new PrintWriter(System.out);

            processReader(s, reader, writer);
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
            File backup = null;
            if (backupExtension == null) {
                backup = File.createTempFile("groovy_", ".tmp");
                backup.deleteOnExit();
            } else {
                backup = new File(file.getPath() + backupExtension);
                backup.delete();
            }
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
        String line = null;
        s.setProperty("out", pw);
        while ((line = reader.readLine()) != null) {
            s.setProperty("line", line);
            Object o = s.run();

            if (autoOutput) {
                pw.println(o);
            }
        }
    }

    /**
     * Process the standard, single script with args.
     */
    private void processOnce() throws CompilationFailedException, IOException {
        GroovyShell groovy = new GroovyShell(conf);

        if (isScriptFile)
            groovy.run(huntForTheScriptFile(script), args);
        else
            groovy.run(script, "script_from_command_line", args);
    }
}