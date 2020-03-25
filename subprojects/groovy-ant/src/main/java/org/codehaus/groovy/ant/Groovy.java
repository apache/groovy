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
package org.codehaus.groovy.ant;

import groovy.ant.AntBuilder;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import groovy.lang.MissingMethodException;
import groovy.lang.Script;
import groovy.util.CharsetToolkit;
import org.apache.groovy.io.StringBuilderWriter;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.filters.util.ChainReaderHelper;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.FilterChain;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.util.FileUtils;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.reflection.ReflectionUtils;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.codehaus.groovy.tools.ErrorReporter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Vector;

/**
 * Executes a series of Groovy statements.
 * <p>
 * <p>Statements can either be read in from a text file using
 * the <i>src</i> attribute or from between the enclosing groovy tags.
 */
public class Groovy extends Java {
    private static final String PREFIX = "embedded_script_in_";
    private static final String SUFFIX = "groovy_Ant_task";
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /**
     * encoding; set to null or empty means 'default'
     */
    private String encoding = null;

    /**
     * output encoding; set to null or empty means 'default'
     */
    private String outputEncoding = null;

    private final LoggingHelper log = new LoggingHelper(this);

    /**
     * files to load
     */
    private final Vector<FileSet> filesets = new Vector<FileSet>();

    /**
     * The input resource
     */
    private Resource src = null;

    /**
     * input command
     */
    private String command = "";

    /**
     * Results Output file
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

    private boolean indy = false;
    private String scriptBaseClass;
    private String configscript;

    private final List<FilterChain> filterChains = new Vector<>();

    /**
     * Compiler configuration.
     * <p>
     * Used to specify the debug output to print stacktraces in case something fails.
     * TODO: Could probably be reused to specify the encoding of the files to load or other properties.
     */
    private final CompilerConfiguration configuration = new CompilerConfiguration();

    private final Commandline cmdline = new Commandline();
    private boolean contextClassLoader;

    /**
     * Should the script be executed using a forked process. Defaults to false.
     *
     * @param fork true if the script should be executed in a forked process
     */
    public void setFork(boolean fork) {
        this.fork = fork;
    }

    /**
     * Declare the encoding to use when outputting to a file;
     * Leave unspecified or use "" for the platform's default encoding.
     *
     * @param encoding the character encoding to use.
     * @since 3.0.3
     */
    public void setOutputEncoding(String encoding) {
        this.outputEncoding = encoding;
    }

    /**
     * Declare the encoding to use when inputting from a resource;
     * If not supplied or the empty encoding is supplied, a guess will be made for file resources,
     * otherwise the platform's default encoding will be used.
     *
     * @param encoding the character encoding to use.
     * @since 3.0.3
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
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
     *
     * @param stacktrace set to true to enable stacktrace reporting
     */
    public void setStacktrace(boolean stacktrace) {
        configuration.setDebug(stacktrace);
    }

    /**
     * Set the name of the file to be run. The folder of the file is automatically added to the classpath.
     * Required unless statements are enclosed in the build file or a nested resource is supplied.
     *
     * @param srcFile the file containing the groovy script to execute
     */
    public void setSrc(final File srcFile) {
        addConfigured(new FileResource(srcFile));
    }

    /**
     * Set an inline command to execute.
     * NB: Properties are not expanded in this text.
     *
     * @param txt the inline groovy commands to execute
     */
    public void addText(String txt) {
        log.verbose("addText('" + txt + "')");
        this.command += txt;
    }

    /**
     * Adds a fileset (nested fileset attribute) which should represent a single source file.
     *
     * @param set the fileset representing a source file
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
     *
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
     *
     * @return Returns a Path
     */
    public Path getClasspath() {
        return classpath;
    }

    /**
     * Sets the configuration script for the groovy compiler configuration.
     *
     * @param configscript path to the configuration script
     */
    public void setConfigscript(final String configscript) {
        this.configscript = configscript;
    }

    /**
     * Sets the indy flag to enable or disable invokedynamic
     *
     * @param indy true means invokedynamic support is active
     */
    public void setIndy(final boolean indy) {
        this.indy = indy;
    }

    /**
     * Set the script base class name
     *
     * @param scriptBaseClass the name of the base class for scripts
     */
    public void setScriptBaseClass(final String scriptBaseClass) {
        this.scriptBaseClass = scriptBaseClass;
    }

    /**
     * If true, generates metadata for reflection on method parameter names (jdk8+ only).  Defaults to false.
     *
     * @param parameters set to true to generate metadata.
     */
    public void setParameters(boolean parameters) {
        configuration.setParameters(parameters);
    }

    /**
     * Returns true if parameter metadata generation has been enabled.
     */
    public boolean getParameters() {
        return configuration.getParameters();
    }

    /**
     * Load the file and then execute it
     */
    public void execute() throws BuildException {
        log.debug("execute()");

        command = command.trim();

        // process filesets
        for (FileSet next : filesets) {
            for (Resource res : next) {
                if (src == null) {
                    src = res;
                } else {
                    throw new BuildException("A single source resource must be provided!", getLocation());
                }
            }
        }

        if (src == null && command.length() == 0) {
            throw new BuildException("Source does not exist!", getLocation());
        }

        if (src != null && !src.isExists()) {
            throw new BuildException("Source resource does not exist!", getLocation());
        }

        if (outputEncoding == null || outputEncoding.isEmpty()) {
            outputEncoding = Charset.defaultCharset().name();
        }
        try {
            PrintStream out = System.out;
            try {
                if (output != null) {
                    log.verbose("Opening PrintStream to output file " + output);
                    BufferedOutputStream bos = new BufferedOutputStream(
                            new FileOutputStream(output.getAbsolutePath(), append));
                    out = new PrintStream(bos, false, outputEncoding);
                }

                // if there are no groovy statements between the enclosing Groovy tags
                // then read groovy statements in from a resource using the src attribute
                if (command == null || command.trim().length() == 0) {
                    Reader reader;
                    if (src instanceof FileResource) {
                        File file = ((FileResource) src).getFile();
                        createClasspath().add(new Path(getProject(), file.getParentFile().getCanonicalPath()));
                        if (encoding != null && !encoding.isEmpty()) {
                            reader = new LineNumberReader(new InputStreamReader(new FileInputStream(file), encoding));
                        } else {
                            reader = new CharsetToolkit(file).getReader();
                        }
                    } else {
                        if (encoding != null && !encoding.isEmpty()) {
                            reader = new InputStreamReader(new BufferedInputStream(src.getInputStream()), encoding);
                        } else {
                            reader = new InputStreamReader(new BufferedInputStream(src.getInputStream()), Charset.defaultCharset());
                        }
                    }
                    try {
                        final long len = src.getSize();
                        log.debug("resource size = " + (len != Resource.UNKNOWN_SIZE ? String.valueOf(len) : "unknown"));
                        if (len == 0) {
                            log.info("Ignoring empty resource");
                            command = null;
                        } else {
                            try (ChainReaderHelper.ChainReader chainReader = new ChainReaderHelper(getProject(), reader, filterChains).with(crh -> {
                                if (len != Resource.UNKNOWN_SIZE && len <= Integer.MAX_VALUE) {
                                    crh.setBufferSize((int) len);
                                }
                            }).getAssembledReader()) {
                                command = chainReader.readFully();
                            }
                        }
                    } catch (final IOException ioe) {
                        throw new BuildException("Unable to load resource: ", ioe, getLocation());
                    }
                } else {
                    if (src != null) {
                        log.info("Ignoring supplied resource as direct script text found");
                    }
                }

                if (command != null) {
                    execGroovy(command, out);
                }

            } finally {
                if (out != null && out != System.out) {
                    out.close();
                }
            }
        } catch (IOException e) {
            throw new BuildException(e, getLocation());
        }

        log.verbose("Statements executed successfully");
    }

    public Commandline.Argument createArg() {
        return cmdline.createArgument();
    }

    /**
     * Add the FilterChain element.
     * @param filter the filter to add
     */
    public final void addFilterChain(FilterChain filter) {
        filterChains.add(filter);
    }

    /**
     * Set the source resource.
     * @param a the resource to load as a single element Resource collection.
     */
    public void addConfigured(ResourceCollection a) {
        if (a.size() != 1) {
            throw new BuildException("Only single argument resource collections are supported");
        }
        src = a.iterator().next();
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
        StringBuilder txt = new StringBuilder();
        String line = "";
        BufferedReader in = new BufferedReader(reader);

        while ((line = in.readLine()) != null) {
            line = getProject().replaceProperties(line);
            if (line.contains("--")) {
                txt.append("\n");
            }
        }
        // Catch any statements not followed by ;
        if (!txt.toString().isEmpty()) {
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
        if (txt.trim().isEmpty()) {
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
                configureCompiler();
                super.execute();
            } catch (Exception e) {
                Writer writer = new StringBuilderWriter();
                new ErrorReporter(e, false).write(new PrintWriter(writer));
                String message = writer.toString();
                throw new BuildException("Script Failed: " + message, e, getLocation());
            }
            return;
        }

        Object mavenPom = null;
        final Project project = getProject();
        final ClassLoader baseClassLoader;
        ClassLoader savedLoader = null;
        final Thread thread = Thread.currentThread();
        boolean maven = "org.apache.commons.grant.GrantProject".equals(project.getClass().getName());
        // treat the case Ant is run through Maven, and
        if (maven) {
            if (contextClassLoader) {
                throw new BuildException("Using setContextClassLoader not permitted when using Maven.", getLocation());
            }
            try {
                final Object propsHandler = project.getClass().getMethod("getPropsHandler").invoke(project);
                final Field contextField = propsHandler.getClass().getDeclaredField("context");
                ReflectionUtils.trySetAccessible(contextField);
                final Object context = contextField.get(propsHandler);
                mavenPom = InvokerHelper.invokeMethod(context, "getProject", EMPTY_OBJECT_ARRAY);
            } catch (Exception e) {
                throw new BuildException("Impossible to retrieve Maven's Ant project: " + e.getMessage(), getLocation());
            }
            // load groovy into "root.maven" classloader instead of "root" so that
            // groovy script can access Maven classes
            baseClassLoader = mavenPom.getClass().getClassLoader();
        } else {
            baseClassLoader = GroovyShell.class.getClassLoader();
        }
        if (contextClassLoader || maven) {
            savedLoader = thread.getContextClassLoader();
            thread.setContextClassLoader(GroovyShell.class.getClassLoader());
        }

        final String scriptName = computeScriptName();
        final GroovyClassLoader classLoader =
                AccessController.doPrivileged(
                        (PrivilegedAction<GroovyClassLoader>) () -> new GroovyClassLoader(baseClassLoader));
        addClassPathes(classLoader);
        configureCompiler();
        final GroovyShell groovy = new GroovyShell(classLoader, new Binding(), configuration);
        try {
            parseAndRunScript(groovy, txt, mavenPom, scriptName, null, new AntBuilder(this));
        } finally {
            groovy.resetLoadedClasses();
            groovy.getClassLoader().clearCache();
            if (contextClassLoader || maven) thread.setContextClassLoader(savedLoader);
        }
    }

    private void configureCompiler() {
        if (scriptBaseClass != null) {
            configuration.setScriptBaseClass(scriptBaseClass);
        }
        if (indy) {
            configuration.getOptimizationOptions().put("indy", Boolean.TRUE);
            configuration.getOptimizationOptions().put("int", Boolean.FALSE);
        }
        if (configscript != null) {
            Binding binding = new Binding();
            binding.setVariable("configuration", configuration);

            CompilerConfiguration configuratorConfig = new CompilerConfiguration();
            ImportCustomizer customizer = new ImportCustomizer();
            customizer.addStaticStars("org.codehaus.groovy.control.customizers.builder.CompilerCustomizationBuilder");
            configuratorConfig.addCompilationCustomizers(customizer);

            GroovyShell shell = new GroovyShell(binding, configuratorConfig);
            File confSrc = new File(configscript);
            try {
                shell.evaluate(confSrc);
            } catch (IOException e) {
                throw new BuildException("Unable to configure compiler using configuration file: " + confSrc, e);
            }
        }
    }

    private void parseAndRunScript(GroovyShell shell, String txt, Object mavenPom, String scriptName, File scriptFile, AntBuilder builder) {
        try {
            final Script script;
            if (scriptFile != null) {
                script = shell.parse(scriptFile);
            } else {
                script = shell.parse(txt, scriptName);
            }
            final Project project = getProject();
            script.setProperty("ant", builder);
            script.setProperty("project", project);
            script.setProperty("properties", new AntProjectPropertiesDelegate(project));
            script.setProperty("target", getOwningTarget());
            script.setProperty("task", this);
            script.setProperty("args", cmdline.getCommandline());
            if (mavenPom != null) {
                script.setProperty("pom", mavenPom);
            }
            script.run();
        } catch (final MissingMethodException mme) {
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
        } catch (final CompilationFailedException | IOException e) {
            processError(e);
        }
    }

    private void processError(Exception e) {
        Writer writer = new StringBuilderWriter();
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
        if (strings != null) {
            for (String prop : strings) {
                if (prop.startsWith("-Dgroovy.home=")) {
                    groovyHome = prop.substring("-Dgroovy.home=".length());
                }
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
        File jarDir = new File(groovyHome, "lib");
        if (!jarDir.exists()) {
            throw new IllegalStateException("GROOVY_HOME incorrectly defined. No lib directory found in: " + groovyHome);
        }
        final File[] files = jarDir.listFiles();
        if (files != null) {
            for (File file : files) {
                try {
                    log.debug("Adding jar to classpath: " + file.getCanonicalPath());
                } catch (IOException e) {
                    // ignore
                }
                path = super.createClasspath();
                path.setLocation(file);
            }
        }
    }

    private void createNewArgs(String txt) throws IOException {
        final String[] args = cmdline.getCommandline();
        // Temporary file - delete on exit, create (assured unique name).
        final File tempFile = FileUtils.getFileUtils().createTempFile(PREFIX, SUFFIX, null, true, true);
        final String[] commandline = new String[args.length + 1];
        ResourceGroovyMethods.write(tempFile, txt);
        commandline[0] = tempFile.getCanonicalPath();
        System.arraycopy(args, 0, commandline, 1, args.length);
        super.clearArgs();
        for (String arg : commandline) {
            final Commandline.Argument argument = super.createArg();
            argument.setValue(arg);
        }
    }

    /**
     * Try to build a script name for the script of the groovy task to have an helpful value in stack traces in case of exception
     *
     * @return the name to use when compiling the script
     */
    private String computeScriptName() {
        if (src instanceof FileResource) {
            FileResource fr = (FileResource) src;
            return fr.getFile().getAbsolutePath();
        } else {
            String name = PREFIX;
            if (getLocation().getFileName().length() > 0)
                name += getLocation().getFileName().replaceAll("[^\\w_\\.]", "_").replaceAll("[\\.]", "_dot_");
            else
                name += SUFFIX;

            return name;
        }
    }

    /**
     * Adds the class paths (if any)
     *
     * @param classLoader the classloader to configure
     */
    protected void addClassPathes(final GroovyClassLoader classLoader) {
        if (classpath != null) {
            for (int i = 0; i < classpath.list().length; i++) {
                classLoader.addClasspath(classpath.list()[i]);
            }
        }
    }

    /**
     * print any results in the statement.
     *
     * @param out the output PrintStream to print to
     */
    protected void printResults(PrintStream out) {
        log.debug("printResults()");
//        StringBuilder line = new StringBuilder();
//        out.println(line);
        out.println();
    }

    /**
     * Setting to true will cause the contextClassLoader to be set with
     * the classLoader of the shell used to run the script. Not used if
     * fork is true. Not allowed when running from Maven but in that
     * case the context classLoader is set appropriately for Maven.
     *
     * @param contextClassLoader set to true to set the context classloader
     */
    public void setContextClassLoader(boolean contextClassLoader) {
        this.contextClassLoader = contextClassLoader;
    }
}
