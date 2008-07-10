/*
 * Copyright 2003-2007 the original author or authors.
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
import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.tools.shell.util.MessageSource;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.tools.ErrorReporter;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.HelpFormatter;

import jline.ConsoleReader;
import jline.SimpleCompletor;

//
// TODO: See if there is any reason why this class is implemented in Java instead of Groovy, and if there
//       is none, then port it over ;-)
//

//
// NOTE: After GShell becomes a little more mature, this shell could be easily implemented as a set of GShell
//       commands, and would inherit a lot of functionality and could be extended easily to allow groovysh
//       to become very, very powerful
//

/**
 * A simple interactive shell for evaluating groovy expressions on the command line (aka. groovysh).
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author <a href="mailto:cpoirier@dreaming.org"   >Chris Poirier</a>
 * @author Yuri Schimke
 * @author Brian McCallistair
 * @author Guillaume Laforge
 * @author Dierk Koenig, include the inspect command, June 2005
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 *
 * @version $Revision$
 */
public class InteractiveShell
    implements Runnable
{
    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final MessageSource MESSAGES = new MessageSource(InteractiveShell.class);

    private final GroovyShell shell;
    private final InputStream in; // FIXME: This doesn't really need to be a field, but hold on to it for now
    private final PrintStream out;
    private final PrintStream err;
    private final ConsoleReader reader;

    private Object lastResult;
    private Closure beforeExecution;
    private Closure afterExecution;

    /**
     * Entry point when called directly.
     */
    public static void main(final String args[]) {
        try {
            processCommandLineArguments(args);

            final InteractiveShell groovy = new InteractiveShell();
            groovy.run();
        }
        catch (Exception e) {
            System.err.println("FATAL: " + e);
            e.printStackTrace();
            System.exit(1);
        }

        System.exit(0);
    }

    /**
     * Process cli args when the shell is invoked via main().
     *
     * @noinspection AccessStaticViaInstance
     */
    private static void processCommandLineArguments(final String[] args) throws Exception {
        assert args != null;

        //
        // TODO: Let this take a single, optional argument which is a file or URL to run?
        //
        
        Options options = new Options();

        options.addOption(OptionBuilder.withLongOpt("help")
            .withDescription(MESSAGES.getMessage("cli.option.help.description"))
            .create('h'));

        options.addOption(OptionBuilder.withLongOpt("version")
            .withDescription(MESSAGES.getMessage("cli.option.version.description"))
            .create('V'));

        //
        // TODO: Add more options, maybe even add an option to prime the buffer from a URL or File?
        //
        
        //
        // FIXME: This does not currently barf on unsupported options short options, though it does for long ones.
        //        Same problem with commons-cli 1.0 and 1.1
        //
        
        CommandLineParser parser = new PosixParser();
        CommandLine line = parser.parse(options, args, true);
        String[] lineargs = line.getArgs();

        // Puke if there were arguments, we don't support any right now
        if (lineargs.length != 0) {
            System.err.println(MESSAGES.format("cli.info.unexpected_args", new Object[] { DefaultGroovyMethods.join(lineargs, " ") }));
            System.exit(1);
        }

        PrintWriter writer = new PrintWriter(System.out);

        if (line.hasOption('h')) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(
                writer,
                80, // width
                "groovysh [options]",
                "",
                options,
                4, // left pad
                4, // desc pad
                "",
                false); // auto usage

            writer.flush();
            System.exit(0);
        }

        if (line.hasOption('V')) {
            writer.println(MESSAGES.format("cli.info.version", new Object[] { InvokerHelper.getVersion() }));
            writer.flush();
            System.exit(0);
        }
    }

    /**
     * Default constructor, initializes uses new binding and system streams.
     */
    public InteractiveShell() throws IOException {
        this(System.in, System.out, System.err);
    }

    /**
     * Constructs a new InteractiveShell instance
     *
     * @param in The input stream to use
     * @param out The output stream to use
     * @param err The error stream to use
     */
    public InteractiveShell(final InputStream in, final PrintStream out, final PrintStream err) throws IOException {
        this(null, new Binding(), in, out, err);
    }

    /**
     * Constructs a new InteractiveShell instance
     * 
     * @param binding The binding instance
     * @param in The input stream to use
     * @param out The output stream to use
     * @param err The error stream to use
     */    
    public InteractiveShell(final Binding binding, final InputStream in, final PrintStream out, final PrintStream err) throws IOException {
    	this(null, binding, in, out, err);
    }
    
    /**
     * Constructs a new InteractiveShell instance
     * 
     * @param parent The parent ClassLoader
     * @param binding The binding instance
     * @param in The input stream to use
     * @param out The output stream to use
     * @param err The error stream to use
     */
    public InteractiveShell(final ClassLoader parent, final Binding binding, final InputStream in, final PrintStream out, final PrintStream err) throws IOException {
        assert binding != null;
        assert in != null;
        assert out != null;
        assert err != null;

        this.in = in;
        this.out = out;
        this.err = err;

        // Initialize the JLine console input reader
        Writer writer = new OutputStreamWriter(out);
        reader = new ConsoleReader(in, writer);
        reader.setDefaultPrompt("groovy> ");

        // Add some completors to fancy things up
        reader.addCompletor(new CommandNameCompletor());

        if (parent != null) {
            shell = new GroovyShell(parent, binding);
        }
        else {
            shell = new GroovyShell(binding);
        }        

        // Add some default variables to the shell
        Map map = shell.getContext().getVariables();

        //
        // FIXME: Um, is this right?  Only set the "shell" var in the context if its set already?
        //
        
        if (map.get("shell") != null) {
            map.put("shell", shell);
        }
    }    

    //---------------------------------------------------------------------------
    // COMMAND LINE PROCESSING LOOP

    //
    // TODO: Add a general error display handler, and probably add a "ERROR: " prefix to the result for clarity ?
    //       Maybe add one for WARNING's too?
    //
    
    /**
     * Reads commands and statements from input stream and processes them.
     */
    public void run() {
        // Display the startup banner
        out.println(MESSAGES.format("startup_banner.0", new Object[] { InvokerHelper.getVersion(), System.getProperty("java.version") }));
        out.println(MESSAGES.getMessage("startup_banner.1"));

        while (true) {
            // Read a code block to evaluate; this will deal with basic error handling
            final String code = read();

            // If we got a null, then quit
            if (code == null) {
                break;
            }

            reset();

            // Evaluate the code block if it was parsed
            if (code.length() > 0) {
                try {
                    if (beforeExecution != null) {
                        beforeExecution.call();
                    }

                    lastResult = shell.evaluate(code);
                    
                    if (afterExecution != null) {
                        afterExecution.call();
                    }

                    // Shows the result of the evaluated code
                    out.print("===> ");
                    out.println(lastResult);
                }
                catch (CompilationFailedException e) {
                    err.println(e);
                }
                catch (Throwable e) {
                    // Unroll invoker exceptions
                    if (e instanceof InvokerInvocationException) {
                        e = e.getCause();
                    }
                    
                    filterAndPrintStackTrace(e);
                }
            }
        }
    }

    /**
     * A closure that is executed before the exection of a given script
     *
     * @param beforeExecution The closure to execute
     */
    public void setBeforeExecution(final Closure beforeExecution) {
        this.beforeExecution = beforeExecution;
    }

    /**
     * A closure that is executed after the execution of the last script. The result of the
     * execution is passed as the first argument to the closure (the value of 'it')
     *
     * @param afterExecution The closure to execute
     */
    public void setAfterExecution(final Closure afterExecution) {
        this.afterExecution = afterExecution;
    }

    /**
     * Filter stacktraces to show only relevant lines of the exception thrown.
     *
     * @param cause the throwable whose stacktrace needs to be filtered
     */
    private void filterAndPrintStackTrace(final Throwable cause) {
        assert cause != null;

        //
        // TODO: Use message...
        //
        
        err.print("ERROR: ");
        err.println(cause);

        cause.printStackTrace(err);

        //
        // FIXME: What is the point of this?  AFAICT, this just produces crappy/corrupt traces and is completely useless
        //
        
//        StackTraceElement[] stackTrace = e.getStackTrace();
//
//        for (int i = 0; i < stackTrace.length; i++) {
//            StackTraceElement element = stackTrace[i];
//            String fileName = element.getFileName();
//
//            if ((fileName==null || (!fileName.endsWith(".java")) && (!element.getClassName().startsWith("gjdk")))) {
//                err.print("\tat ");
//                err.println(element);
//            }
//        }
    }

    //---------------------------------------------------------------------------
    // COMMAND LINE PROCESSING MACHINERY

    /** The statement text accepted to date */
    private StringBuffer accepted = new StringBuffer();

    /** A line of statement text not yet accepted */
    private String pending;

    //
    // FIXME: Doesn't look like 'line' is really used/needed anywhere... could drop it, or perhaps
    //        could use it to update the prompt er something to show the buffer size?
    //

    /** The current line number */
    private int line;
    
    /** Set to force clear of accepted */
    private boolean stale = false;

    /** A SourceUnit used to check the statement */
    private SourceUnit parser;

    /** Any actual syntax error caught during parsing */
    private Exception error;

    /**
     * Resets the command-line processing machinery after use.
     */
    protected void reset() {
        stale = true;
        pending = null;
        line = 1;
        parser = null;
        error = null;
    }

    //
    // FIXME: This Javadoc is not correct... read() will return the full code block read until "go"
    //
    
    /**
     * Reads a single statement from the command line.  Also identifies
     * and processes command shell commands.  Returns the command text
     * on success, or null when command processing is complete.
     * 
     * NOTE: Changed, for now, to read until 'execute' is issued.  At
     * 'execute', the statement must be complete.
     */
    protected String read() {
        reset();
        
        boolean complete = false;
        boolean done = false;
        
        while (/* !complete && */ !done) {
            // Read a line.  If IOException or null, or command "exit", terminate processing.
            try {
                pending = reader.readLine();
            }
            catch (IOException e) {
                //
                // FIXME: Shouldn't really eat this exception, may be something we need to see... ?
                //
            }

            // If result is null then we are shutting down
            if (pending == null) {
                return null;
            }

            // First up, try to process the line as a command and proceed accordingly
            // Trim what we have for use in command bits, so things like "help " actually show the help screen
            String command = pending.trim();

            if (COMMAND_MAPPINGS.containsKey(command)) {
                int code = ((Integer)COMMAND_MAPPINGS.get(command)).intValue();

                switch (code) {
                    case COMMAND_ID_EXIT:
                        return null;
                    
                    case COMMAND_ID_HELP:
                        displayHelp();
                        break;

                    case COMMAND_ID_DISCARD:
                        reset();
                        done = true;
                        break;

                    case COMMAND_ID_DISPLAY:
                        displayStatement();
                        break;

                    case COMMAND_ID_EXPLAIN:
                        explainStatement();
                        break;

                    case COMMAND_ID_BINDING:
                        displayBinding();
                        break;

                    case COMMAND_ID_EXECUTE:
                        if (complete) {
                            done = true;
                        }
                        else {
                            err.println(MESSAGES.getMessage("command.execute.not_complete"));
                        }
                        break;

                    case COMMAND_ID_DISCARD_LOADED_CLASSES:
                        resetLoadedClasses();
                        break;

                    case COMMAND_ID_INSPECT:
                        inspect();
                        break;

                    default:
                        throw new Error("BUG: Unknown command for code: " + code);
                }

                // Finished processing command bits, continue reading, don't need to process code
                continue;
            }

            // Otherwise, it's part of a statement.  If it's just whitespace,
            // we'll just accept it and move on.  Otherwise, parsing is attempted
            // on the cumulated statement text, and errors are reported.  The
            // pending input is accepted or rejected based on that parsing.

            freshen();

            if (pending.trim().length() == 0) {
                accept();
                continue;
            }

            // Try to parse the current code buffer
            final String code = current();
            
            if (parse(code)) {
                // Code parsed fine
                accept();
                complete = true;
            }
            else if (error == null) {
                // Um... ???
                accept();
            }
            else {
                // Parse failed, spit out something to the user
                report();
            }
        }

        // Get and return the statement.
        return accepted(complete);
    }

    private void inspect() {
        if (lastResult == null){
            err.println(MESSAGES.getMessage("command.inspect.no_result"));
            return;
        }

        //
        // FIXME: Update this once we have joint compile happy in the core build?
        //
        // this should read: groovy.inspect.swingui.ObjectBrowser.inspect(lastResult)
        // but this doesnt compile since ObjectBrowser.groovy is compiled after this class.
        //

        //
        // FIXME: When launching this, if the user tries to "exit" and the window is still opened, the shell will
        //        hang... not really nice user experence IMO.  Should try to fix this if we can.
        //
        
        try {
            Class type = Class.forName("groovy.inspect.swingui.ObjectBrowser");
            Method method = type.getMethod("inspect", new Class[]{ Object.class });
            method.invoke(type, new Object[]{ lastResult });
        }
        catch (Exception e) {
            err.println("Cannot invoke ObjectBrowser");
            e.printStackTrace();
        }
    }

    /**
     * Returns the accepted statement as a string.  If not complete, returns empty string.
     */
    private String accepted(final boolean complete) {
        if (complete) {
            return accepted.toString();
        }
        return "";
    }

    /**
     * Returns the current statement, including pending text.
     */
    private String current() {
        return accepted.toString() + pending + NEW_LINE;
    }

    /**
     * Accepts the pending text into the statement.
     */
    private void accept() {
        accepted.append(pending).append(NEW_LINE);
        line += 1;
    }

    /**
     * Clears accepted if stale.
     */
    private void freshen() {
        if (stale) {
            accepted.setLength(0);
            stale = false;
        }
    }

    //---------------------------------------------------------------------------
    // SUPPORT ROUTINES

    /**
     * Attempts to parse the specified code with the specified tolerance.
     * Updates the <code>parser</code> and <code>error</code> members
     * appropriately.  Returns true if the text parsed, false otherwise.
     * The attempts to identify and suppress errors resulting from the
     * unfinished source text.
     */
    private boolean parse(final String code, final int tolerance) {
        assert code != null;

        boolean parsed = false;
        parser = null;
        error = null;

        // Create the parser and attempt to parse the text as a top-level statement.
        try {
            parser = SourceUnit.create("groovysh-script", code, tolerance);
            parser.parse();
            parsed = true;
        }

        // We report errors other than unexpected EOF to the user.
        catch (CompilationFailedException e) {
            if (parser.getErrorCollector().getErrorCount() > 1 || !parser.failedWithUnexpectedEOF()) {
                error = e;
            }
        }
        catch (Exception e) {
            error = e;
        }

        return parsed;
    }

    private boolean parse(final String code) {
        return parse(code, 1);
    }
    
    /**
     * Reports the last parsing error to the user.
     */
    private void report() {
        err.println("Discarding invalid text:"); // TODO: i18n
        new ErrorReporter(error, false).write(err);
    }

    //-----------------------------------------------------------------------
    // COMMANDS

    //
    // TODO: Add a simple command to read in a File/URL into the buffer for execution, but need better command
    //       support first (aka GShell) so we can allow commands to take args, etc.
    //

    private static final int COMMAND_ID_EXIT = 0;
    private static final int COMMAND_ID_HELP = 1;
    private static final int COMMAND_ID_DISCARD = 2;
    private static final int COMMAND_ID_DISPLAY = 3;
    private static final int COMMAND_ID_EXPLAIN = 4;
    private static final int COMMAND_ID_EXECUTE = 5;
    private static final int COMMAND_ID_BINDING = 6;
    private static final int COMMAND_ID_DISCARD_LOADED_CLASSES = 7;
    private static final int COMMAND_ID_INSPECT = 8;
    private static final int LAST_COMMAND_ID = 8;

    private static final String[] COMMANDS = {
        "exit",
        "help",
        "discard",
        "display",
        "explain",
        "execute",
        "binding",
        "discardclasses",
        "inspect"
    };

    private static final Map COMMAND_MAPPINGS = new HashMap();

    static {
        for (int i = 0; i <= LAST_COMMAND_ID; i++) {
            COMMAND_MAPPINGS.put(COMMANDS[i], Integer.valueOf(i));
        }

        // A few synonyms
        COMMAND_MAPPINGS.put("quit", Integer.valueOf(COMMAND_ID_EXIT));
        COMMAND_MAPPINGS.put("go", Integer.valueOf(COMMAND_ID_EXECUTE));
    }

    private static final Map COMMAND_HELP = new HashMap();

    static {
        COMMAND_HELP.put(COMMANDS[COMMAND_ID_EXIT],    "exit/quit         - " + MESSAGES.getMessage("command.exit.descripion"));
        COMMAND_HELP.put(COMMANDS[COMMAND_ID_HELP],    "help              - " + MESSAGES.getMessage("command.help.descripion"));
        COMMAND_HELP.put(COMMANDS[COMMAND_ID_DISCARD], "discard           - " + MESSAGES.getMessage("command.discard.descripion"));
        COMMAND_HELP.put(COMMANDS[COMMAND_ID_DISPLAY], "display           - " + MESSAGES.getMessage("command.display.descripion"));

        //
        // FIXME: If this is disabled, then er comment it out, so it doesn't confuse the user
        //
        
        COMMAND_HELP.put(COMMANDS[COMMAND_ID_EXPLAIN], "explain           - " + MESSAGES.getMessage("command.explain.descripion"));
        COMMAND_HELP.put(COMMANDS[COMMAND_ID_EXECUTE], "execute/go        - " + MESSAGES.getMessage("command.execute.descripion"));
        COMMAND_HELP.put(COMMANDS[COMMAND_ID_BINDING], "binding           - " + MESSAGES.getMessage("command.binding.descripion"));
        COMMAND_HELP.put(COMMANDS[COMMAND_ID_DISCARD_LOADED_CLASSES],
                                                       "discardclasses    - " + MESSAGES.getMessage("command.discardclasses.descripion"));
        COMMAND_HELP.put(COMMANDS[COMMAND_ID_INSPECT], "inspect           - " + MESSAGES.getMessage("command.inspect.descripion"));
    }

    /**
     * Displays help text about available commands.
     */
    private void displayHelp() {
        out.println(MESSAGES.getMessage("command.help.available_commands"));

        for (int i = 0; i <= LAST_COMMAND_ID; i++) {
            out.print("    ");
            out.println(COMMAND_HELP.get(COMMANDS[i]));
        }
    }

    /**
     * Displays the accepted statement.
     */
    private void displayStatement() {
        final String[] lines = accepted.toString().split(NEW_LINE);

        if (lines.length == 1 && lines[0].trim().equals("")) {
            out.println(MESSAGES.getMessage("command.display.buffer_empty"));
        }
        else {
            // Eh, try to pick a decent pad size... but don't try to hard
            int padsize = 2;
            if (lines.length >= 10) padsize++;
            if (lines.length >= 100) padsize++;
            if (lines.length >= 1000) padsize++;

            // Dump the current buffer with a line number prefix
            for (int i = 0; i < lines.length; i++) {
                // Normalize the field size of the line number
                String lineno = DefaultGroovyMethods.padLeft(String.valueOf(i + 1), Integer.valueOf(padsize), " ");
                
                out.print(lineno);
                out.print("> ");
                out.println(lines[i]);
            }
        }
    }

    /**
     * Displays the current binding used when instanciating the shell.
     */
    private void displayBinding() {
        Binding context = shell.getContext();
        Map variables = context.getVariables();
        Set set = variables.keySet();

        if (set.isEmpty()) {
            out.println(MESSAGES.getMessage("command.binding.binding_empty"));
        }
        else {
            out.println(MESSAGES.getMessage("command.binding.available_variables"));

            Iterator iter = set.iterator();
            while (iter.hasNext()) {
                Object key = iter.next();

                out.print("    ");
                out.print(key);
                out.print(" = ");
                out.println(variables.get(key));
            }
        }
    }

    /**
     * Attempts to parse the accepted statement and display the parse tree for it.
     */
    private void explainStatement() {
        if (parse(accepted(true), 10) || error == null) {
            out.println(MESSAGES.getMessage("command.explain.tree_header"));
            //out.println(tree);
        }
        else {
            out.println(MESSAGES.getMessage("command.explain.unparsable"));
        }
    }

    private void resetLoadedClasses() {
        shell.resetLoadedClasses();
        
        out.println(MESSAGES.getMessage("command.discardclasses.classdefs_discarded"));
    }

    //
    // Custom JLine Completors to fancy up the user experence more.
    //

    private class CommandNameCompletor
        extends SimpleCompletor
    {
        public CommandNameCompletor() {
            super(new String[0]);

            // Add each command name/alias as a candidate
            Iterator iter = COMMAND_MAPPINGS.keySet().iterator();

            while (iter.hasNext()) {
                addCandidateString((String)iter.next());
            }
        }
    }

    //
    // TODO: Add local variable completion?
    //

    //
    // TODO: Add shell method complention?
    //

    /*
    private void findShellMethods(String complete) {
        List methods = shell.getMetaClass().getMetaMethods();
        for (Iterator i = methods.iterator(); i.hasNext();) {
            MetaMethod method = (MetaMethod) i.next();
            if (method.getName().startsWith(complete)) {
                if (method.getParameterTypes().length > 0) {
                    completions.add(method.getName() + "(");
                }
                else {
                    completions.add(method.getName() + "()");
                }
            }
        }
    }

    private void findLocalVariables(String complete) {
        Set names = shell.getContext().getVariables().keySet();

        for (Iterator i = names.iterator(); i.hasNext();) {
            String name = (String) i.next();
            if (name.startsWith(complete)) {
                completions.add(name);
            }
        }
    }
    */
}
