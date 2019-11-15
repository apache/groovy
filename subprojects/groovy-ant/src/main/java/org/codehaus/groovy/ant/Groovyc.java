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

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyResourceLoader;
import org.apache.groovy.io.StringBuilderWriter;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.RuntimeConfigurable;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.util.GlobPatternMapper;
import org.apache.tools.ant.util.SourceFileScanner;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceExtensionHandler;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.DefaultGroovyStaticMethods;
import org.codehaus.groovy.tools.ErrorReporter;
import org.codehaus.groovy.tools.FileSystemCompiler;
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit;
import org.objectweb.asm.ClassVisitor;
import picocli.CommandLine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Compiles Groovy source files using Ant.
 * <p>
 * Typically involves using Ant from the command-line and an Ant build file such as:
 * <pre>
 * &lt;?xml version="1.0"?&gt;
 * &lt;project name="MyGroovyBuild" default="compile"&gt;
 *   &lt;property name="groovy.home" location="/Path/To/Groovy"/&gt;
 *   &lt;property name="groovy.version" value="X.Y.Z"/&gt;
 *
 *   &lt;taskdef name="groovyc" classname="org.codehaus.groovy.ant.Groovyc"&gt;
 *     &lt;classpath&gt;
 *       &lt;fileset file="${groovy.home}/lib/groovy-${groovy.version}.jar"/&gt;
 *       &lt;fileset file="${groovy.home}/lib/groovy-ant-${groovy.version}.jar"/&gt;
 *     &lt;/classpath&gt;
 *   &lt;/taskdef&gt;
 *
 *   &lt;target name="compile" description="compile groovy sources"&gt;
 *     &lt;groovyc srcdir="src" destdir="bin" fork="true" listfiles="true" includeantruntime="false"/&gt;
 *       &lt;classpath&gt;
 *         &lt;fileset dir="${groovy.home}/lib" includes="groovy-*${groovy.version}.jar" excludes="groovy-ant-${groovy.version}.jar"/&gt;
 *       &lt;/classpath&gt;
 *     &lt;/groovyc&gt;
 *   &lt;/target&gt;
 * &lt;/project&gt;
 * </pre>
 * <p>
 * This task can take the following arguments:
 * <ul>
 * <li>srcdir</li>
 * <li>destdir</li>
 * <li>sourcepath</li>
 * <li>sourcepathRef</li>
 * <li>classpath</li>
 * <li>classpathRef</li>
 * <li>scriptExtension</li>
 * <li>targetBytecode</li>
 * <li>listfiles</li>
 * <li>failonerror</li>
 * <li>proceed</li>
 * <li>memoryInitialSize</li>
 * <li>memoryMaximumSize</li>
 * <li>encoding</li>
 * <li>verbose</li>
 * <li>includeantruntime</li>
 * <li>includejavaruntime</li>
 * <li>fork</li>
 * <li>javaHome</li>
 * <li>executable</li>
 * <li>updatedProperty</li>
 * <li>errorProperty</li>
 * <li>includeDestClasses</li>
 * <li>jointCompilationOptions</li>
 * <li>stacktrace</li>
 * <li>indy</li>
 * <li>scriptBaseClass</li>
 * <li>stubdir</li>
 * <li>keepStubs</li>
 * <li>forceLookupUnnamedFiles</li>
 * <li>configscript</li>
 * <li>parameters</li>
 * </ul>
 * And these nested tasks:
 * <ul>
 * <li>javac</li>
 * </ul>
 * Of these arguments, the <b>srcdir</b> and <b>destdir</b> are required.
 * <p>
 * When this task executes, it will recursively scan srcdir and destdir looking
 * for Groovy source files to compile. This task makes its compile decision based
 * on timestamp.
 * <p>
 * A more elaborate build file showing joint compilation:
 * <pre>
 * &lt;?xml version="1.0"?&gt;
 * &lt;project name="MyJointBuild" default="compile"&gt;
 *   &lt;property name="groovy.home" location="/Path/To/Groovy"/&gt;
 *   &lt;property name="groovy.version" value="X.Y.Z"/&gt;
 *
 *   &lt;path id="classpath.main"&gt;
 *     &lt;fileset dir="${groovy.home}/lib"&gt;
 *       &lt;include name="groovy-*${groovy.version}.jar"/&gt;
 *       &lt;exclude name="groovy-ant-${groovy.version}.jar"/&gt;
 *     &lt;/fileset&gt;
 *   &lt;/path&gt;
 *
 *   &lt;taskdef name="groovyc" classname="org.codehaus.groovy.ant.Groovyc"&gt;
 *     &lt;classpath&gt;
 *       &lt;fileset file="${groovy.home}/lib/groovy-${groovy.version}.jar"/&gt;
 *       &lt;fileset file="${groovy.home}/lib/groovy-ant-${groovy.version}.jar"/&gt;
 *     &lt;/classpath&gt;
 *   &lt;/taskdef&gt;
 *
 *   &lt;target name="clean"&gt;
 *     &lt;delete dir="bin" failonerror="false"/&gt;
 *   &lt;/target&gt;
 *
 *   &lt;target name="compile" depends="clean" description="compile java and groovy sources"&gt;
 *     &lt;mkdir dir="bin"/&gt;
 *
 *     &lt;groovyc srcdir="src" destdir="bin" stubdir="stubs" keepStubs="true"
 *      fork="true" includeantruntime="false" classpathref="classpath.main"&gt;
 *       &lt;javac debug="true" source="1.8" target="1.8"/&gt;
 *     &lt;/groovyc&gt;
 *   &lt;/target&gt;
 * &lt;/project&gt;
 * </pre>
 * <p>
 * Based on the implementation of the Javac task in Apache Ant.
 * <p>
 * Can also be used from {@link groovy.util.AntBuilder} to allow the build file to be scripted in Groovy.
 */
public class Groovyc extends MatchingTask {

    private static final File[] EMPTY_FILE_ARRAY = new File[0];
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private final LoggingHelper log = new LoggingHelper(this);

    private Path src;
    private File destDir;
    private Path compileClasspath;
    private Path compileSourcepath;
    private String encoding;
    private boolean stacktrace;
    private boolean verbose;
    private boolean includeAntRuntime = true;
    private boolean includeJavaRuntime;
    private boolean fork;
    private File forkJavaHome;
    private String forkedExecutable;
    private String memoryInitialSize;
    private String memoryMaximumSize;
    private String scriptExtension = "*.groovy";
    private String targetBytecode;

    protected boolean failOnError = true;
    protected boolean listFiles;
    protected File[] compileList = EMPTY_FILE_ARRAY;

    private String updatedProperty;
    private String errorProperty;
    private boolean taskSuccess = true;
    private boolean includeDestClasses = true;

    protected CompilerConfiguration configuration;
    private Javac javac;
    private boolean jointCompilation;

    private final List<File> temporaryFiles = new ArrayList<>(2);
    private File stubDir;
    private boolean keepStubs;
    private boolean forceLookupUnnamedFiles;
    private boolean useIndy;
    private String scriptBaseClass;
    private String configscript;

    private Set<String> scriptExtensions = new LinkedHashSet<>();

    /**
     * If true, generates metadata for reflection on method parameter names (jdk8+ only).  Defaults to false.
     */
    private boolean parameters;

    /**
     * If true, enable preview Java features (JEP 12) (jdk12+ only). Defaults to false.
     */
    private boolean previewFeatures;

    /**
     * Adds a path for source compilation.
     *
     * @return a nested src element.
     */
    public Path createSrc() {
        if (src == null) {
            src = new Path(getProject());
        }
        return src.createPath();
    }

    /**
     * Recreate src.
     *
     * @return a nested src element.
     */
    protected Path recreateSrc() {
        src = null;
        return createSrc();
    }

    /**
     * Set the source directories to find the source Java files.
     *
     * @param srcDir the source directories as a path
     */
    public void setSrcdir(Path srcDir) {
        if (src == null) {
            src = srcDir;
        } else {
            src.append(srcDir);
        }
    }

    /**
     * Gets the source dirs to find the source java files.
     *
     * @return the source directories as a path
     */
    public Path getSrcdir() {
        return src;
    }

    /**
     * Set the extension to use when searching for Groovy source files.
     * Accepts extensions in the form *.groovy, .groovy or groovy
     *
     * @param scriptExtension the extension of Groovy source files
     */
    public void setScriptExtension(String scriptExtension) {
        if (scriptExtension.startsWith("*.")) {
            this.scriptExtension = scriptExtension;
        } else if (scriptExtension.startsWith(".")) {
            this.scriptExtension = "*" + scriptExtension;
        } else {
            this.scriptExtension = "*." + scriptExtension;
        }
    }

    /**
     * Get the extension to use when searching for Groovy source files.
     *
     * @return the extension of Groovy source files
     */
    public String getScriptExtension() {
        return scriptExtension;
    }

    /**
     * Sets the bytecode compatibility level.
     * The parameter can take one of the values in {@link CompilerConfiguration#ALLOWED_JDKS}.
     *
     * @param version the bytecode compatibility level
     */
    public void setTargetBytecode(String version) {
        for (String allowedJdk : CompilerConfiguration.ALLOWED_JDKS) {
            if (allowedJdk.equals(version)) {
                this.targetBytecode = version;
                break;
            }
        }
    }

    /**
     * Retrieves the compiler bytecode compatibility level.
     *
     * @return bytecode compatibility level. Can be one of the values in {@link CompilerConfiguration#ALLOWED_JDKS}.
     */
    public String getTargetBytecode() {
        return this.targetBytecode;
    }

    /**
     * Set the destination directory into which the Java source
     * files should be compiled.
     *
     * @param destDir the destination director
     */
    public void setDestdir(File destDir) {
        this.destDir = destDir;
    }

    /**
     * Gets the destination directory into which the java source files
     * should be compiled.
     *
     * @return the destination directory
     */
    public File getDestdir() {
        return destDir;
    }

    /**
     * Set the sourcepath to be used for this compilation.
     *
     * @param sourcepath the source path
     */
    public void setSourcepath(Path sourcepath) {
        if (compileSourcepath == null) {
            compileSourcepath = sourcepath;
        } else {
            compileSourcepath.append(sourcepath);
        }
    }

    /**
     * Gets the sourcepath to be used for this compilation.
     *
     * @return the source path
     */
    public Path getSourcepath() {
        return compileSourcepath;
    }

    /**
     * Adds a path to sourcepath.
     *
     * @return a sourcepath to be configured
     */
    public Path createSourcepath() {
        if (compileSourcepath == null) {
            compileSourcepath = new Path(getProject());
        }
        return compileSourcepath.createPath();
    }

    /**
     * Adds a reference to a source path defined elsewhere.
     *
     * @param r a reference to a source path
     */
    public void setSourcepathRef(Reference r) {
        createSourcepath().setRefid(r);
    }

    /**
     * Set the classpath to be used for this compilation.
     *
     * @param classpath an Ant Path object containing the compilation classpath.
     */
    public void setClasspath(Path classpath) {
        if (compileClasspath == null) {
            compileClasspath = classpath;
        } else {
            compileClasspath.append(classpath);
        }
    }

    /**
     * Gets the classpath to be used for this compilation.
     *
     * @return the class path
     */
    public Path getClasspath() {
        return compileClasspath;
    }

    /**
     * Adds a path to the classpath.
     *
     * @return a class path to be configured
     */
    public Path createClasspath() {
        if (compileClasspath == null) {
            compileClasspath = new Path(getProject());
        }
        return compileClasspath.createPath();
    }

    /**
     * Adds a reference to a classpath defined elsewhere.
     *
     * @param r a reference to a classpath
     */
    public void setClasspathRef(Reference r) {
        createClasspath().setRefid(r);
    }

    /**
     * If true, list the source files being handed off to the compiler.
     * Default is false.
     *
     * @param list if true list the source files
     */
    public void setListfiles(boolean list) {
        listFiles = list;
    }

    /**
     * Get the listfiles flag.
     *
     * @return the listfiles flag
     */
    public boolean getListfiles() {
        return listFiles;
    }

    /**
     * Indicates whether the build will continue
     * even if there are compilation errors; defaults to true.
     *
     * @param fail if true halt the build on failure
     */
    public void setFailonerror(boolean fail) {
        failOnError = fail;
    }

    /**
     * @param proceed inverse of failonerror
     */
    public void setProceed(boolean proceed) {
        failOnError = !proceed;
    }

    /**
     * Gets the failonerror flag.
     *
     * @return the failonerror flag
     */
    public boolean getFailonerror() {
        return failOnError;
    }

    /**
     * The initial size of the memory for the underlying VM
     * if javac is run externally; ignored otherwise.
     * Defaults to the standard VM memory setting.
     * (Examples: 83886080, 81920k, or 80m)
     *
     * @param memoryInitialSize string to pass to VM
     */
    public void setMemoryInitialSize(String memoryInitialSize) {
        this.memoryInitialSize = memoryInitialSize;
    }

    /**
     * Gets the memoryInitialSize flag.
     *
     * @return the memoryInitialSize flag
     */
    public String getMemoryInitialSize() {
        return memoryInitialSize;
    }

    /**
     * The maximum size of the memory for the underlying VM
     * if javac is run externally; ignored otherwise.
     * Defaults to the standard VM memory setting.
     * (Examples: 83886080, 81920k, or 80m)
     *
     * @param memoryMaximumSize string to pass to VM
     */
    public void setMemoryMaximumSize(String memoryMaximumSize) {
        this.memoryMaximumSize = memoryMaximumSize;
    }

    /**
     * Gets the memoryMaximumSize flag.
     *
     * @return the memoryMaximumSize flag
     */
    public String getMemoryMaximumSize() {
        return memoryMaximumSize;
    }

    /**
     * Sets the file encoding for generated files.
     *
     * @param encoding the file encoding to be used
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Returns the encoding to be used when creating files.
     *
     * @return the file encoding to use
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Enable verbose compiling which will display which files
     * are being compiled. Default is false.
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Gets the verbose flag.
     *
     * @return the verbose flag
     */
    public boolean getVerbose() {
        return verbose;
    }

    /**
     * If true, includes Ant's own classpath in the classpath. Default is true.
     * If setting to false and using groovyc in conjunction with AntBuilder
     * you might need to explicitly add the Groovy jar(s) to the groovyc
     * classpath using a nested classpath task.
     *
     * @param include if true, includes Ant's own classpath in the classpath
     */
    public void setIncludeantruntime(boolean include) {
        includeAntRuntime = include;
    }

    /**
     * Gets whether or not the ant classpath is to be included in the classpath.
     *
     * @return whether or not the ant classpath is to be included in the classpath
     */
    public boolean getIncludeantruntime() {
        return includeAntRuntime;
    }

    /**
     * If true, includes the Java runtime libraries in the classpath. Default is false.
     *
     * @param include if true, includes the Java runtime libraries in the classpath
     */
    public void setIncludejavaruntime(boolean include) {
        includeJavaRuntime = include;
    }

    /**
     * Gets whether or not the java runtime should be included in this
     * task's classpath.
     *
     * @return the includejavaruntime attribute
     */
    public boolean getIncludejavaruntime() {
        return includeJavaRuntime;
    }

    /**
     * If true forks the Groovy compiler. Default is false.
     *
     * @param f "true|false|on|off|yes|no"
     */
    public void setFork(boolean f) {
        fork = f;
    }

    /**
     * The JDK Home to use when forked.
     * Ignored if "executable" is specified.
     *
     * @param home the java.home value to use, default is the current JDK's home
     */
    public void setJavaHome(File home) {
        forkJavaHome = home;
    }

    /**
     * Sets the name of the java executable to use when
     * invoking the compiler in forked mode, ignored otherwise.
     *
     * @param forkExecPath the name of the executable
     * @since Groovy 1.8.7
     */
    public void setExecutable(String forkExecPath) {
        forkedExecutable = forkExecPath;
    }

    /**
     * The value of the executable attribute, if any.
     *
     * @return the name of the java executable
     * @since Groovy 1.8.7
     */
    public String getExecutable() {
        return forkedExecutable;
    }

    /**
     * The property to set on compilation success.
     * This property will not be set if the compilation
     * fails, or if there are no files to compile.
     *
     * @param updatedProperty the property name to use.
     */
    public void setUpdatedProperty(String updatedProperty) {
        this.updatedProperty = updatedProperty;
    }

    /**
     * The property to set on compilation failure.
     * This property will be set if the compilation
     * fails.
     *
     * @param errorProperty the property name to use.
     */
    public void setErrorProperty(String errorProperty) {
        this.errorProperty = errorProperty;
    }

    /**
     * This property controls whether to include the
     * destination classes directory in the classpath
     * given to the compiler.
     * The default value is "true".
     *
     * @param includeDestClasses the value to use.
     */
    public void setIncludeDestClasses(boolean includeDestClasses) {
        this.includeDestClasses = includeDestClasses;
    }

    /**
     * Get the value of the includeDestClasses property.
     *
     * @return the value.
     */
    public boolean isIncludeDestClasses() {
        return includeDestClasses;
    }

    /**
     * Get the result of the groovyc task (success or failure).
     *
     * @return true if compilation succeeded, or
     * was not necessary, false if the compilation failed.
     */
    public boolean getTaskSuccess() {
        return taskSuccess;
    }

    /**
     * Add the configured nested javac task if present to initiate joint compilation.
     */
    public void addConfiguredJavac(final Javac javac) {
        this.javac = javac;
        jointCompilation = true;
    }

    /**
     * Enable compiler to report stack trace information if a problem occurs
     * during compilation. Default is false.
     */
    public void setStacktrace(boolean stacktrace) {
        this.stacktrace = stacktrace;
    }

    /**
     * Set the indy flag.
     *
     * @param useIndy the indy flag
     */
    public void setIndy(boolean useIndy) {
        this.useIndy = useIndy;
    }

    /**
     * Get the value of the indy flag.
     *
     * @return if to use indy
     */
    public boolean getIndy() {
        return this.useIndy;
    }

    /**
     * Set the base script class name for the scripts (must derive from Script)
     *
     * @param scriptBaseClass Base class name for scripts (must derive from Script)
     */
    public void setScriptBaseClass(String scriptBaseClass) {
        this.scriptBaseClass = scriptBaseClass;
    }

    /**
     * Get the base script class name for the scripts (must derive from Script)
     *
     * @return Base class name for scripts (must derive from Script)
     */
    public String getScriptBaseClass() {
        return this.scriptBaseClass;
    }

    /**
     * Get the configuration file used to customize the compilation configuration.
     *
     * @return a path to a configuration script
     */
    public String getConfigscript() {
        return configscript;
    }

    /**
     * Set the configuration file used to customize the compilation configuration.
     *
     * @param configscript a path to a configuration script
     */
    public void setConfigscript(final String configscript) {
        this.configscript = configscript;
    }

    /**
     * Set the stub directory into which the Java source stub
     * files should be generated. The directory need not exist
     * and will not be deleted automatically - though its contents
     * will be cleared unless 'keepStubs' is true. Ignored when forked.
     *
     * @param stubDir the stub directory
     */
    public void setStubdir(File stubDir) {
        jointCompilation = true;
        this.stubDir = stubDir;
    }

    /**
     * Gets the stub directory into which the Java source stub
     * files should be generated
     *
     * @return the stub directory
     */
    public File getStubdir() {
        return stubDir;
    }

    /**
     * Set the keepStubs flag. Defaults to false. Set to true for debugging.
     * Ignored when forked.
     *
     * @param keepStubs should stubs be retained
     */
    public void setKeepStubs(boolean keepStubs) {
        this.keepStubs = keepStubs;
    }

    /**
     * Gets the keepStubs flag.
     *
     * @return the keepStubs flag
     */
    public boolean getKeepStubs() {
        return keepStubs;
    }

    /**
     * Set the forceLookupUnnamedFiles flag. Defaults to false.
     * <p>
     * The Groovyc Ant task is frequently used in the context of a build system
     * that knows the complete list of source files to be compiled. In such a
     * context, it is wasteful for the Groovy compiler to go searching the
     * classpath when looking for source files and hence by default the
     * Groovyc Ant task calls the compiler in a special mode with such searching
     * turned off. If you wish the compiler to search for source files then
     * you need to set this flag to {@code true}.
     *
     * @param forceLookupUnnamedFiles should unnamed source files be searched for on the classpath
     */
    public void setForceLookupUnnamedFiles(boolean forceLookupUnnamedFiles) {
        this.forceLookupUnnamedFiles = forceLookupUnnamedFiles;
    }

    /**
     * Gets the forceLookupUnnamedFiles flag.
     *
     * @return the forceLookupUnnamedFiles flag
     */
    public boolean getForceLookupUnnamedFiles() {
        return forceLookupUnnamedFiles;
    }

    /**
     * If true, generates metadata for reflection on method parameter names (jdk8+ only).  Defaults to false.
     *
     * @param parameters set to true to generate metadata.
     */
    public void setParameters(boolean parameters) {
        this.parameters = parameters;
    }

    /**
     * Returns true if parameter metadata generation has been enabled.
     */
    public boolean getParameters() {
        return this.parameters;
    }

    /**
     * If true, enable preview Java features (JEP 12) (jdk12+ only).
     *
     * @param previewFeatures set to true to enable preview features
     */
    public void setPreviewFeatures(boolean previewFeatures) {
        this.previewFeatures = previewFeatures;
    }

    /**
     * Returns true if preview features has been enabled.
     */
    public boolean getPreviewFeatures() {
        return previewFeatures;
    }

    /**
     * Executes the task.
     *
     * @throws BuildException if an error occurs
     */
    public void execute() throws BuildException {
        checkParameters();
        resetFileLists();
        loadRegisteredScriptExtensions();

        if (javac != null) jointCompilation = true;

        // scan source directories and dest directory to build up
        // compile lists
        String[] list = src.list();
        for (String filename : list) {
            File file = getProject().resolveFile(filename);
            if (!file.exists()) {
                throw new BuildException("srcdir \"" + file.getPath() + "\" does not exist!", getLocation());
            }
            DirectoryScanner ds = this.getDirectoryScanner(file);
            String[] files = ds.getIncludedFiles();
            scanDir(file, destDir != null ? destDir : file, files);
        }

        compile();
        if (updatedProperty != null
                && taskSuccess
                && compileList.length != 0) {
            getProject().setNewProperty(updatedProperty, "true");
        }
    }

    /**
     * Clear the list of files to be compiled and copied.
     */
    protected void resetFileLists() {
        compileList = EMPTY_FILE_ARRAY;
        scriptExtensions = new LinkedHashSet<>();
    }

    /**
     * Scans the directory looking for source files to be compiled.
     * The results are returned in the class variable compileList
     *
     * @param srcDir  The source directory
     * @param destDir The destination directory
     * @param files   An array of filenames
     */
    protected void scanDir(File srcDir, File destDir, String[] files) {
        GlobPatternMapper m = new GlobPatternMapper();
        SourceFileScanner sfs = new SourceFileScanner(this);
        File[] newFiles;
        for (String extension : getScriptExtensions()) {
            m.setFrom("*." + extension);
            m.setTo("*.class");
            newFiles = sfs.restrictAsFiles(files, srcDir, destDir, m);
            addToCompileList(newFiles);
        }

        if (jointCompilation) {
            m.setFrom("*.java");
            m.setTo("*.class");
            newFiles = sfs.restrictAsFiles(files, srcDir, destDir, m);
            addToCompileList(newFiles);
        }
    }

    protected void addToCompileList(File[] newFiles) {
        if (newFiles.length > 0) {
            File[] newCompileList = new File[compileList.length + newFiles.length];
            System.arraycopy(compileList, 0, newCompileList, 0, compileList.length);
            System.arraycopy(newFiles, 0, newCompileList, compileList.length, newFiles.length);
            compileList = newCompileList;
        }
    }

    /**
     * Gets the list of files to be compiled.
     *
     * @return the list of files as an array
     */
    public File[] getFileList() {
        return compileList;
    }

    protected void checkParameters() throws BuildException {
        if (src == null) {
            throw new BuildException("srcdir attribute must be set!", getLocation());
        }
        if (src.size() == 0) {
            throw new BuildException("srcdir attribute must be set!", getLocation());
        }

        if (destDir != null && !destDir.isDirectory()) {
            throw new BuildException("destination directory \""
                    + destDir
                    + "\" does not exist or is not a directory",
                    getLocation());
        }

        if (encoding != null && !Charset.isSupported(encoding)) {
            throw new BuildException("encoding \"" + encoding + "\" not supported.");
        }
    }

    private void listFiles() {
        if (listFiles) {
            for (File srcFile : compileList) {
                log.info(srcFile.getAbsolutePath());
            }
        }
    }

    /**
     * If {@code groovyc} task includes a nested {@code javac} task, check for
     * shareable configuration.  {@code FileSystemCompiler} supports several
     * command-line arguments for configuring joint compilation:
     * <ul>
     * <li><tt>-j</tt> enables joint compile
     * <li><tt>-F</tt> is used to pass flags
     * <li><tt>-J</tt> is used to pass name=value pairs
     * </ul>
     * Joint compilation options are transferred from {@link FileSystemCompiler}
     * to {@link CompilerConfiguration}'s jointCompileOptions property.  Flags
     * are saved to key "flags" (with the inclusion of "parameters" if enabled
     * on groovyc), pairs are saved to key "namedValues" and the key "memStub"
     * may also be set to {@link Boolean#TRUE} to influence joint compilation.
     *
     * @see org.codehaus.groovy.tools.javac.JavacJavaCompiler
     * @see javax.tools.JavaCompiler
     */
    private List<String> extractJointOptions(Path classpath) {
        List<String> jointOptions = new ArrayList<>();
        if (!jointCompilation) return jointOptions;

        // map "debug" and "debuglevel" to "-Fg"
        if (javac.getDebug()) {
            String level = javac.getDebugLevel();
            jointOptions.add("-Fg" + (level == null ? "" : ":" + level));
        } else {
            jointOptions.add("-Fg:none");
        }

        // map "deprecation" to "-Fdeprecation"
        if (javac.getDeprecation()) {
            jointOptions.add("-Fdeprecation");
        }

        // map "nowarn" to "-Fnowarn"
        if (javac.getNowarn()) {
            jointOptions.add("-Fnowarn");
        }

        // map "verbose" to "-Fverbose"
        if (javac.getVerbose()) {
            jointOptions.add("-Fverbose");
        }

        RuntimeConfigurable rc = javac.getRuntimeConfigurableWrapper();

        for (Map.Entry<String, Object> e : rc.getAttributeMap().entrySet()) {
            String key = e.getKey();
            if (key.equals("depend")
                    || key.equals("encoding")
                    || key.equals("extdirs")
                    || key.equals("nativeheaderdir")
                    || key.equals("release")
                    || key.equals("source")
                    || key.equals("target")) {
                switch (key) {
                case "nativeheaderdir":
                    key = "h"; break;
                case "release":
                    key = "-" + key; // to get "--" when passed to javac
                }
                // map "depend", "encoding", etc. to "-Jkey=val"
                jointOptions.add("-J" + key + "=" + getProject().replaceProperties(e.getValue().toString()));

            } else if (key.contains("classpath")) {
                if (key.startsWith("boot")) {
                    // map "bootclasspath" or "bootclasspathref" to "-Jbootclasspath="
                    jointOptions.add("-Jbootclasspath=" + javac.getBootclasspath());
                } else {
                    // map "classpath" or "classpathref" to "--classpath"
                    classpath.add(javac.getClasspath());
                }
            } else if (key.contains("module") && key.contains("path")) {
                if (key.startsWith("upgrade")) {
                    // map "upgrademodulepath" or "upgrademodulepathref" to "-J-upgrade-module-path="
                    jointOptions.add("-J-upgrade-module-path=" + javac.getUpgrademodulepath());
                } else if (key.contains("source")) {
                    // map "modulesourcepath" or "modulesourcepathref" to "-J-module-source-path="
                    jointOptions.add("-J-module-source-path=" + javac.getModulesourcepath());
                } else {
                    // map "modulepath" or "modulepathref" to "-J-module-path="
                    jointOptions.add("-J-module-path=" + javac.getModulepath());
                }
            } else if (!key.contains("debug") && !key.equals("deprecation") && !key.equals("nowarn") && !key.equals("verbose")) {
                log.warn("The option " + key + " cannot be set on the contained <javac> element. The option will be ignored.");
            }
            // TODO: defaultexcludes, excludes(file)?, includes(file)?, includeDestClasses, tempdir
        }

        // Ant's <javac> supports nested <compilerarg value=""> elements (there
        // can be multiple of them) for additional options to be passed to javac.
        for (RuntimeConfigurable childrc : Collections.list(rc.getChildren())) {
            if (childrc.getElementTag().equals("compilerarg")) {
                for (Map.Entry<String, Object> e : childrc.getAttributeMap().entrySet()) {
                    String key = e.getKey();
                    if (key.equals("value")) {
                        String value = getProject().replaceProperties(e.getValue().toString());
                        StringTokenizer st = new StringTokenizer(value, " ");
                        while (st.hasMoreTokens()) {
                            String option = st.nextToken();
                            // GROOVY-5063: map "-Werror", etc. to "-FWerror"
                            jointOptions.add(option.replaceFirst("^-(W|X|proc:)", "-F$1"));
                        }
                    }
                }
            }
        }

        return jointOptions;
    }

    private void doForkCommandLineList(List<String> commandLineList, Path classpath, String separator) {
        if (forkedExecutable != null && !forkedExecutable.isEmpty()) {
            commandLineList.add(forkedExecutable);
        } else {
            String javaHome;
            if (forkJavaHome != null) {
                javaHome = forkJavaHome.getPath();
            } else {
                javaHome = System.getProperty("java.home");
            }
            commandLineList.add(javaHome + separator + "bin" + separator + "java");
        }

        String[] bootstrapClasspath;
        ClassLoader loader = getClass().getClassLoader();
        if (loader instanceof AntClassLoader) {
            bootstrapClasspath = ((AntClassLoader) loader).getClasspath().split(File.pathSeparator);
        } else {
            Class<?>[] bootstrapClasses = {
                FileSystemCompilerFacade.class,
                FileSystemCompiler.class,
                antlr.Parser.class,
                ClassVisitor.class,
                CommandLine.class,
            };
            Set<String> locations = new LinkedHashSet<>();
            for (Class<?> clazz : bootstrapClasses) {
                locations.add(new File(getLocation(clazz)).getAbsolutePath());
            }
            bootstrapClasspath = locations.toArray(new String[locations.size()]);
        }
        if (bootstrapClasspath.length > 0) {
            commandLineList.add("-classpath");
            commandLineList.add(getClasspathRelative(bootstrapClasspath));
        }

        if (memoryInitialSize != null && !memoryInitialSize.isEmpty()) {
            commandLineList.add("-Xms" + memoryInitialSize);
        }
        if (memoryMaximumSize != null && !memoryMaximumSize.isEmpty()) {
            commandLineList.add("-Xmx" + memoryMaximumSize);
        }
        if (targetBytecode != null) {
            commandLineList.add("-Dgroovy.target.bytecode=" + targetBytecode);
        }
        if (!"*.groovy".equals(getScriptExtension())) {
            String tmpExtension = getScriptExtension();
            if (tmpExtension.startsWith("*."))
                tmpExtension = tmpExtension.substring(1);
            commandLineList.add("-Dgroovy.default.scriptExtension=" + tmpExtension);
        }

        commandLineList.add(FileSystemCompilerFacade.class.getName());
        commandLineList.add("--classpath");
        if (includeAntRuntime) {
            classpath.addExisting(new Path(getProject()).concatSystemClasspath("last"));
        }
        if (includeJavaRuntime) {
            classpath.addJavaRuntime();
        }
        commandLineList.add(getClasspathRelative(classpath.list()));
        if (forceLookupUnnamedFiles) {
            commandLineList.add("--forceLookupUnnamedFiles");
        }
    }

    private String getClasspathRelative(String[] classpath) {
        String baseDir = getProject().getBaseDir().getAbsolutePath();
        StringBuilder sb = new StringBuilder();
        for (String next : classpath) {
            if (sb.length() > 0) {
                sb.append(File.pathSeparatorChar);
            }
            if (next.startsWith(baseDir)) {
                sb.append(".").append(next.substring(baseDir.length()));
            } else {
                sb.append(next);
            }
        }
        return sb.toString();
    }

    private static URI getLocation(Class<?> clazz) {
        try {
            return clazz.getProtectionDomain().getCodeSource().getLocation().toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Add "groovyc" parameters to the commandLineList, based on the ant configuration.
     *
     * @param commandLineList
     * @param jointOptions
     * @param classpath
     */
    private void doNormalCommandLineList(List<String> commandLineList, List<String> jointOptions, Path classpath) {
        if (!fork) {
            commandLineList.add("--classpath");
            commandLineList.add(classpath.toString());
        }
        if (jointCompilation) {
            commandLineList.add("-j");
            commandLineList.addAll(jointOptions);
        }
        if (destDir != null) {
            commandLineList.add("-d");
            commandLineList.add(destDir.getPath());
        }
        if (encoding != null) {
            commandLineList.add("--encoding");
            commandLineList.add(encoding);
        }
        if (stacktrace) {
            commandLineList.add("-e");
        }
        if (parameters) {
            commandLineList.add("--parameters");
        }
        if (previewFeatures) {
            commandLineList.add("--enable-preview");
        }
        if (useIndy) {
            commandLineList.add("--indy");
        }
        if (scriptBaseClass != null) {
            commandLineList.add("-b");
            commandLineList.add(scriptBaseClass);
        }
        if (configscript != null) {
            commandLineList.add("--configscript");
            commandLineList.add(configscript);
        }
    }

    private void addSourceFiles(List<String> commandLineList) {
        // check to see if an external file is needed
        int count = 0;
        if (fork) {
            for (File srcFile : compileList) {
                count += srcFile.getPath().length();
            }
            for (Object commandLineArg : commandLineList) {
                count += commandLineArg.toString().length();
            }
            count += compileList.length;
            count += commandLineList.size();
        }
        // 32767 is the command line length limit on Windows
        if (fork && (count > 32767)) {
            try {
                File tempFile = File.createTempFile("groovyc-files-", ".txt");
                temporaryFiles.add(tempFile);
                PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
                for (File srcFile : compileList) {
                    pw.println(srcFile.getPath());
                }
                pw.close();
                commandLineList.add("@" + tempFile.getPath());
            } catch (IOException e) {
                log.error("Error creating file list", e);
            }
        } else {
            for (File srcFile : compileList) {
                commandLineList.add(srcFile.getPath());
            }
        }
    }

    private String[] makeCommandLine(List<String> commandLineList) {
        log.verbose("Compilation arguments:\n" + DefaultGroovyMethods.join((Iterable<String>) commandLineList, "\n"));
        return commandLineList.toArray(EMPTY_STRING_ARRAY);
    }

    private void runForked(String[] commandLine) {
        Execute executor = new Execute();
        executor.setAntRun(getProject());
        executor.setWorkingDirectory(getProject().getBaseDir());
        executor.setCommandline(commandLine);
        try {
            executor.execute();
        } catch (final IOException ioe) {
            throw new BuildException("Error running forked groovyc.", ioe);
        }
        int returnCode = executor.getExitValue();
        if (returnCode != 0) {
            taskSuccess = false;
            if (errorProperty != null) {
                getProject().setNewProperty(errorProperty, "true");
            }
            if (failOnError) {
                throw new BuildException("Forked groovyc returned error code: " + returnCode);
            } else {
                log.error("Forked groovyc returned error code: " + returnCode);
            }
        }
    }

    private void runCompiler(String[] commandLine) {
        // hand crank it so we can add our own compiler configuration
        try {
            FileSystemCompiler.CompilationOptions options = new FileSystemCompiler.CompilationOptions();
            CommandLine parser = FileSystemCompiler.configureParser(options);
            parser.parseArgs(commandLine);
            configuration = options.toCompilerConfiguration();
            configuration.setScriptExtensions(getScriptExtensions());
            String tmpExtension = getScriptExtension();
            if (tmpExtension.startsWith("*."))
                tmpExtension = tmpExtension.substring(1);
            configuration.setDefaultScriptExtension(tmpExtension);
            if (targetBytecode != null) {
                configuration.setTargetBytecode(targetBytecode);
            }

            // Load the file name list
            String[] fileNames = options.generateFileNames();
            boolean fileNameErrors = (fileNames == null || !FileSystemCompiler.validateFiles(fileNames));
            if (!fileNameErrors) {
                try (GroovyClassLoader loader = buildClassLoaderFor()) {
                    FileSystemCompiler.doCompilation(configuration, makeCompileUnit(loader), fileNames, forceLookupUnnamedFiles);
                }
            }
        } catch (Exception e) {
            Throwable t = e;
            if (e.getClass() == RuntimeException.class && e.getCause() != null) {
                // unwrap to the real exception
                t = e.getCause();
            }
            Writer writer = new StringBuilderWriter();
            new ErrorReporter(t, false).write(new PrintWriter(writer));
            String message = writer.toString();

            taskSuccess = false;
            if (errorProperty != null) {
                getProject().setNewProperty(errorProperty, "true");
            }

            if (failOnError) {
                log.error(message);
                throw new BuildException("Compilation Failed", t, getLocation());
            } else {
                log.error(message);
            }
        }
    }

    protected void compile() {
        if (compileList.length == 0) return;

        try {
            log.info("Compiling " + compileList.length + " source file"
                    + (compileList.length == 1 ? "" : "s")
                    + (destDir != null ? " to " + destDir : ""));

            listFiles();

            Path classpath = getClasspath();
            if (classpath == null)
                classpath = new Path(getProject());
            List<String> jointOptions = extractJointOptions(classpath);
            List<String> commandLineList = new ArrayList<>();

            if (fork) doForkCommandLineList(commandLineList, classpath, File.separator);
            doNormalCommandLineList(commandLineList, jointOptions, classpath);
            addSourceFiles(commandLineList);

            String[] commandLine = makeCommandLine(commandLineList);

            if (fork) {
                runForked(commandLine);
            } else {
                runCompiler(commandLine);
            }
        } finally {
            for (File temporaryFile : temporaryFiles) {
                try {
                    FileSystemCompiler.deleteRecursive(temporaryFile);
                } catch (Throwable t) {
                    System.err.println("error: could not delete temp files - " + temporaryFile.getPath());
                }
            }
        }
    }

    /**
     * @deprecated This method is not in use anymore. Use {@link Groovyc#makeCompileUnit(GroovyClassLoader)} instead.
     */
    @Deprecated
    protected CompilationUnit makeCompileUnit() {
        return makeCompileUnit(buildClassLoaderFor());
    }

    protected CompilationUnit makeCompileUnit(GroovyClassLoader loader) {
        Map<String, Object> options = configuration.getJointCompilationOptions();
        if (options != null) {
            if (keepStubs) {
                options.put("keepStubs", Boolean.TRUE);
            }
            if (stubDir != null) {
                options.put("stubDir", stubDir);
            } else {
                try {
                    File tempStubDir = DefaultGroovyStaticMethods.createTempDir(null, "groovy-generated-", "-java-source");
                    temporaryFiles.add(tempStubDir);
                    options.put("stubDir", tempStubDir);
                } catch (IOException ioe) {
                    throw new BuildException(ioe);
                }
            }
            return new JavaAwareCompilationUnit(configuration, loader);
        } else {
            return new CompilationUnit(configuration, null, loader);
        }
    }

    protected GroovyClassLoader buildClassLoaderFor() {
        if (fork) {
            throw new GroovyBugError("Cannot use Groovyc#buildClassLoaderFor() for forked compilation");
        }
        // GROOVY-5044
        if (!getIncludeantruntime()) {
            throw new IllegalArgumentException("The includeAntRuntime=false option is not compatible with fork=false");
        }

        final ClassLoader loader = getClass().getClassLoader();
        if (loader instanceof AntClassLoader) {
            AntClassLoader antLoader = (AntClassLoader) loader;
            String[] pathElm = antLoader.getClasspath().split(File.pathSeparator);
            List<String> classpath = configuration.getClasspath();
            /*
             * Iterate over the classpath provided to groovyc, and add any missing path
             * entries to the AntClassLoader.  This is a workaround, since for some reason
             * 'directory' classpath entries were not added to the AntClassLoader' classpath.
             */
            for (String cpEntry : classpath) {
                boolean found = false;
                for (String path : pathElm) {
                    if (cpEntry.equals(path)) {
                        found = true;
                        break;
                    }
                }
                /*
                 * fix for GROOVY-2284
                 * seems like AntClassLoader doesn't check if the file
                 * may not exist in the classpath yet
                 */
                if (!found && new File(cpEntry).exists()) {
                    try {
                        antLoader.addPathElement(cpEntry);
                    } catch (BuildException e) {
                        log.warn("The classpath entry " + cpEntry + " is not a valid Java resource");
                    }
                }
            }
        }

        GroovyClassLoader groovyLoader = AccessController.doPrivileged(new PrivilegedAction<GroovyClassLoader>() {
            @Override
            public GroovyClassLoader run() {
                return new GroovyClassLoader(loader, configuration);
            }
        });
        if (!forceLookupUnnamedFiles) {
            // in normal case we don't need to do script lookups
            groovyLoader.setResourceLoader(new GroovyResourceLoader() {
                @Override
                public URL loadGroovySource(String filename) {
                    return null;
                }
            });
        }
        return groovyLoader;
    }

    private Set<String> getScriptExtensions() {
        return scriptExtensions;
    }

    private void loadRegisteredScriptExtensions() {
        if (scriptExtensions.isEmpty()) {
            scriptExtensions.add(getScriptExtension().substring(2)); // first extension will be the one set explicitly on <groovyc>

            Path classpath = getClasspath();
            if (classpath == null)
                classpath = new Path(getProject());
            try (GroovyClassLoader loader = new GroovyClassLoader(getClass().getClassLoader())) {
                for (String element : classpath.list()) {
                    loader.addClasspath(element);
                }
                scriptExtensions.addAll(SourceExtensionHandler.getRegisteredExtensions(loader));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
