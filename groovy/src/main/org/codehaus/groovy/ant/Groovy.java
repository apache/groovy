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

package org.codehaus.groovy.ant;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import groovy.lang.MissingMethodException;
import groovy.lang.Script;
import groovy.util.AntBuilder;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.util.FileUtils;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.tools.ErrorReporter;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Vector;

/**
 * Executes a series of Groovy statements.
 *
 * <p>Statements can
 * either be read in from a text file using the <i>src</i> attribute or from
 * between the enclosing groovy tags.</p>
 *
 * @version $Id$
 */
public class Groovy extends Java {
    private static final String PREFIX = "embedded_script_in_";
    private static final String SUFFIX = "groovy_Ant_task";

    private final LoggingHelper log = new LoggingHelper(this);

    /**
     * files to load
     */
    private Vector filesets = new Vector();

    /**
     * input file
     */
    private File srcFile = null;

    /**
     * input command
     */
    private String command = "";

    /**
     * Results Output file.
     */
    private File output = null;

    /**
     * Append to an existing file or overwrite it?
     */
    private boolean append = false;

    private Path classpath;
    private boolean fork = false;
    private boolean includeAntRuntime = true;
    private boolean useGroovyShell = false;

    /**
     * Compiler configuration.
     *
     * Used to specify the debug output to print stacktraces in case something fails.
     * TODO: Could probably be reused to specify the encoding of the files to load or other properties.
     */
    private CompilerConfiguration configuration = new CompilerConfiguration();

    private Commandline cmdline = new Commandline();

    /**
     * Should the script be executed using a forked process. Defaults to false.
     *
     * @param fork true if the script should be executed in a forked process
     */
    public void setFork(boolean fork) {
        this.fork = fork;
    }


    /**
     * Should a new GroovyShell be used when forking. Special variables won't be available
     * but you don't need Ant in the classpath.
     *
     * @param useGroovyShell true if GroovyShell should be used to run the script directly
     */
    public void setUseGroovyShell(boolean useGroovyShell) {
        this.useGroovyShell = useGroovyShell;
    }

    /**
     * Should the system classpath be included on the classpath when forking. Defaults to true.
     *
     * @param includeAntRuntime true if the system classpath should be on the classpath
     */
    public void setIncludeAntRuntime(boolean includeAntRuntime) {
        this.includeAntRuntime = includeAntRuntime;
    }

    /**
     * Enable compiler to report stack trace information if a problem occurs
     * during compilation.
     * @param stacktrace set to true to enable stacktrace reporting
     */
    public void setStacktrace(boolean stacktrace) {
        configuration.setDebug(stacktrace);
    }


    /**
     * Set the name of the file to be run. The folder of the file is automatically added to the classpath.
     * Required unless statements are enclosed in the build file
     *
     * @param srcFile the file containing the groovy script to execute
     */
    public void setSrc(final File srcFile) {
        this.srcFile = srcFile;
    }

    /**
     * Set an inline command to execute.
     * NB: Properties are not expanded in this text.
     *
     * @param txt the inline groovy ommands to execute
     */
    public void addText(String txt) {
        log("addText('"+txt+"')", Project.MSG_VERBOSE);
        this.command += txt;
    }

    /**
     * Adds a set of files (nested fileset attribute).
     *
     * @param set the fileset representing source files
     */
    public void addFileset(FileSet set) {
        filesets.addElement(set);
    }

    /**
     * Set the output file;
     * optional, defaults to the Ant log.
     *
     * @param output the output file
     */
    public void setOutput(File output) {
        this.output = output;
    }

    /**
     * Whether output should be appended to or overwrite
     * an existing file.  Defaults to false.
     *
     * @param append set to true to append
     */
    public void setAppend(boolean append) {
        this.append = append;
    }


    /**
     * Sets the classpath for loading.
     * @param classpath The classpath to set
     */
    public void setClasspath(final Path classpath) {
        this.classpath = classpath;
    }

    /**
     * Returns a new path element that can be configured.
     * Gets called for instance by Ant when it encounters a nested &lt;classpath&gt; element.
     *
     * @return the resulting created path
     */
    public Path createClasspath() {
        if (this.classpath == null) {
            this.classpath = new Path(getProject());
        }
        return this.classpath.createPath();
    }

    /**
     * Set the classpath for loading
     * using the classpath reference.
     *
     * @param ref the refid to use
     */
    public void setClasspathRef(final Reference ref) {
        createClasspath().setRefid(ref);
    }

    /**
     * Gets the classpath.
     * @return Returns a Path
     */
    public Path getClasspath() {
        return classpath;
    }

    /**
     * Load the file and then execute it
     */
    public void execute() throws BuildException {
        log.debug("execute()");

        command = command.trim();

        if (srcFile == null && command.length() == 0
            && filesets.isEmpty()) {
            throw new BuildException("Source file does not exist!", getLocation());
        }

        if (srcFile != null && !srcFile.exists()) {
            throw new BuildException("Source file does not exist!", getLocation());
        }

        // deal with the filesets
        for (int i = 0; i < filesets.size(); i++) {
            FileSet fs = (FileSet) filesets.elementAt(i);
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            File srcDir = fs.getDir(getProject());
            String[] srcFiles = ds.getIncludedFiles();
        }

        try {
            PrintStream out = System.out;
            try {
                if (output != null) {
                    log.verbose("Opening PrintStream to output file " + output);
                    
                    out = new PrintStream(
                              new BufferedOutputStream(
                                  new FileOutputStream(output
                                                       .getAbsolutePath(),
                                                       append)));
                }

                // if there are no groovy statements between the enclosing Groovy tags
                // then read groovy statements in from a text file using the src attribute
                if (command == null || command.trim().length() == 0) {
                	createClasspath().add(new Path(getProject(), srcFile.getParentFile().getCanonicalPath()));
                    command = getText(new BufferedReader(new FileReader(srcFile)));
                }

                if (command != null) {
                    execGroovy(command,out);
                } else {
                    throw new BuildException("Source file does not exist!", getLocation());
                }

            } finally {
                if (out != null && out != System.out) {
                    out.close();
                }
            }
        } catch (IOException e) {
            throw new BuildException(e, getLocation());
        }

        log.verbose("statements executed successfully");
    }


    private static String getText(BufferedReader reader) throws IOException {
        StringBuffer answer = new StringBuffer();
        // reading the content of the file within a char buffer allow to keep the correct line endings
        char[] charBuffer = new char[4096];
        int nbCharRead = 0;
        while ((nbCharRead = reader.read(charBuffer)) != -1) {
            // appends buffer
            answer.append(charBuffer, 0, nbCharRead);
        }
        reader.close();
        return answer.toString();
    }

    public Commandline.Argument createArg() {
        return cmdline.createArgument();
    }

    /**
     * Read in lines and execute them.
     *
     * @param reader the reader from which to get the groovy source to exec
     * @param out    the outputstream to use
     * @throws java.io.IOException if something goes wrong
     */
    protected void runStatements(Reader reader, PrintStream out)
        throws IOException {
        log.debug("runStatements()");
        StringBuffer txt = new StringBuffer();
        String line = "";
        BufferedReader in = new BufferedReader(reader);

        while ((line = in.readLine()) != null) {
            line = getProject().replaceProperties(line);
            if (line.indexOf("--") >= 0) {
                txt.append("\n");
            }
        }
        // Catch any statements not followed by ;
        if (!txt.toString().equals("")) {
            execGroovy(txt.toString(), out);
        }
    }


    /**
     * Exec the statement.
     *
     * @param txt the groovy source to exec
     * @param out not used?
     */
    protected void execGroovy(final String txt, final PrintStream out) {
        log.debug("execGroovy()");

        // Check and ignore empty statements
        if ("".equals(txt.trim())) {
            return;
        }

        log.verbose("Script: " + txt);
        if (classpath != null) {
            log.debug("Explicit Classpath: " + classpath.toString());
        }

        if (fork) {
            log.debug("Using fork mode");
            try {
                createClasspathParts();
                createNewArgs(txt);
                super.setFork(fork);
                super.setClassname(useGroovyShell ? "groovy.lang.GroovyShell" : "org.codehaus.groovy.ant.Groovy");
                super.execute();
            } catch (Exception e) {
                StringWriter writer = new StringWriter();
                new ErrorReporter(e, false).write(new PrintWriter(writer));
                String message = writer.toString();
                throw new BuildException("Script Failed: " + message, e, getLocation());
            }
            return;
        }

        Object mavenPom = null;
        final Project project = getProject();
        final ClassLoader baseClassLoader;
        // treat the case Ant is run through Maven, and
        if ("org.apache.commons.grant.GrantProject".equals(project.getClass().getName())) {
            try {
                final Object propsHandler = project.getClass().getMethod("getPropsHandler", new Class[0]).invoke(project, new Object[0]);
                final Field contextField = propsHandler.getClass().getDeclaredField("context");
                contextField.setAccessible(true);
                final Object context = contextField.get(propsHandler);
                mavenPom = InvokerHelper.invokeMethod(context, "getProject", new Object[0]);
            }
            catch (Exception e) {
                throw new BuildException("Impossible to retrieve Maven's Ant project: " + e.getMessage(), getLocation());
            }
            // let ASM lookup "root" classloader
            Thread.currentThread().setContextClassLoader(GroovyShell.class.getClassLoader());
            // load groovy into "root.maven" classloader instead of "root" so that
            // groovy script can access Maven classes
            baseClassLoader = mavenPom.getClass().getClassLoader();
        } else {
            baseClassLoader = GroovyShell.class.getClassLoader();
        }

        final String scriptName = computeScriptName();
        final GroovyClassLoader classLoader = new GroovyClassLoader(baseClassLoader);
        addClassPathes(classLoader);
        
        final GroovyShell groovy = new GroovyShell(classLoader, new Binding(), configuration);
        parseAndRunScript(groovy, txt, mavenPom, scriptName, null, new AntBuilder(this));
    }

    private void parseAndRunScript(GroovyShell shell, String txt, Object mavenPom, String scriptName, File scriptFile, AntBuilder builder) {
        try {
            final Script script;
            if (scriptFile != null) {
                script = shell.parse(scriptFile);
            } else {
                script = shell.parse(txt, scriptName);
            }
            script.setProperty("ant", builder);
            script.setProperty("project", getProject());
            script.setProperty("properties", new AntProjectPropertiesDelegate(project));
            script.setProperty("target", getOwningTarget());
            script.setProperty("task", this);
            script.setProperty("args", cmdline.getCommandline());
            if (mavenPom != null) {
                script.setProperty("pom", mavenPom);
            }
            script.run();
        }
        catch (final MissingMethodException mme) {
            // not a script, try running through run method but properties will not be available
            if (scriptFile != null) {
                try {
                    shell.run(scriptFile, cmdline.getCommandline());
                } catch (IOException e) {
                    processError(e);
                }
            } else {
                shell.run(txt, scriptName, cmdline.getCommandline());
            }
        }
        catch (final CompilationFailedException e) {
            processError(e);
        } catch (IOException e) {
            processError(e);
        }
    }

    private void processError(Exception e) {
        StringWriter writer = new StringWriter();
        new ErrorReporter(e, false).write(new PrintWriter(writer));
        String message = writer.toString();
        throw new BuildException("Script Failed: " + message, e, getLocation());
    }

    public static void main(String[] args) {
        final GroovyShell shell = new GroovyShell(new Binding());
        final Groovy groovy = new Groovy();
        for (int i = 1; i < args.length; i++) {
            final Commandline.Argument argument = groovy.createArg();
            argument.setValue(args[i]);
        }
        final AntBuilder builder = new AntBuilder();
        groovy.setProject(builder.getProject());
        groovy.parseAndRunScript(shell, null, null, null, new File(args[0]), builder);
    }

    private void createClasspathParts() {
        Path path;
        if (classpath != null) {
            path = super.createClasspath();
            path.setPath(classpath.toString());
        }

        if (includeAntRuntime) {
            path = super.createClasspath();
            path.setPath(System.getProperty("java.class.path"));
        }
        String groovyHome = null;
        final String[] strings = getSysProperties().getVariables();
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            if (string.startsWith("-Dgroovy.home=")) {
                groovyHome = string.substring("-Dgroovy.home=".length());
            }
        }
        if (groovyHome == null) {
            groovyHome = System.getProperty("groovy.home");
        }
        if (groovyHome == null) {
            groovyHome = System.getenv("GROOVY_HOME");
        }
        if (groovyHome == null) {
            throw new IllegalStateException("Neither ${groovy.home} nor GROOVY_HOME defined.");
        }
        File jarDir = new File(groovyHome, "embeddable");
        if (!jarDir.exists()) {
            throw new IllegalStateException("GROOVY_HOME incorrectly defined. No embeddable directory found in: " + groovyHome);
        }
        final File[] files = jarDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            try {
                log.debug("Adding jar to classpath: " + file.getCanonicalPath());
            } catch (IOException e) {
                // ignore
            }
            path = super.createClasspath();
            path.setLocation(file);
        }
    }

    private void createNewArgs(String txt) throws IOException {
        final String[] args = cmdline.getCommandline();
        final File tempFile = FileUtils.getFileUtils().createTempFile(PREFIX, SUFFIX, null, true);
        final String[] commandline = new String[args.length + 1];
        DefaultGroovyMethods.write(tempFile, txt);
        commandline[0] = tempFile.getCanonicalPath();
        System.arraycopy(args, 0, commandline, 1, args.length);
        super.clearArgs();
        for (int i = 0; i < commandline.length; i++) {
            final Commandline.Argument argument = super.createArg();
            argument.setValue(commandline[i]);
        }
    }

    /**
     * Try to build a script name for the script of the groovy task to have an helpful value in stack traces in case of exception
     * @return the name to use when compiling the script
     */
    private String computeScriptName() {
        if (srcFile != null) {
            return srcFile.getAbsolutePath();
        } else {
            String name = PREFIX;
            if (getLocation().getFileName().length() > 0)
                name += getLocation().getFileName().replaceAll("[^\\w_\\.]", "_");
            else
                name += SUFFIX;
			return name;
		}
	}


	/**
	 * Adds the class pathes (if any)
	 * @param classLoader the classloader to configure
	 */
	protected void addClassPathes(final GroovyClassLoader classLoader)
	{
		if (classpath != null)
		{
			for (int i = 0; i < classpath.list().length; i++)
			{
	            classLoader.addClasspath(classpath.list()[i]);
			}
		}
	}

    /**
     * print any results in the statement.
     * @param out the output PrintStream to print to
     */
    protected void printResults(PrintStream out) {
        log.debug("printResults()");
        StringBuffer line = new StringBuffer();
        out.println(line);
        line = new StringBuffer();
        out.println();
    }
}
