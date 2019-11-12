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
package org.codehaus.groovy.control;

import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.codehaus.groovy.control.io.NullWriter;
import org.codehaus.groovy.control.messages.WarningMessage;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Compilation control flags and coordination stuff.
 */
public class CompilerConfiguration {

    private static final String JDK5_CLASSNAME_CHECK = "java.lang.annotation.Annotation";

    /** This (<code>"indy"</code>) is the Optimization Option value for enabling <code>invokedynamic</code> complilation. */
    public static final String INVOKEDYNAMIC = "indy";

    /** This (<code>"1.4"</code>) is the value for targetBytecode to compile for a JDK 1.4. **/
    public static final String JDK4 = "1.4";
    /** This (<code>"1.5"</code>) is the value for targetBytecode to compile for a JDK 1.5. **/
    public static final String JDK5 = "1.5";
    /** This (<code>"1.6"</code>) is the value for targetBytecode to compile for a JDK 1.6. **/
    public static final String JDK6 = "1.6";
    /** This (<code>"1.7"</code>) is the value for targetBytecode to compile for a JDK 1.7. **/
    public static final String JDK7 = "1.7";
    /** This (<code>"1.8"</code>) is the value for targetBytecode to compile for a JDK 1.8. **/
    public static final String JDK8 = "1.8";

    /** The valid targetBytecode values. */
    public static final String[] ALLOWED_JDKS = {JDK4, JDK5, JDK6, JDK7, JDK8};

    /** This (<code>"1.5"</code>) is the value for targetBytecode to compile for a JDK 1.5 or later JVM. **/
    public static final String POST_JDK5 = JDK5; // for backwards compatibility

    /** This (<code>"1.4"</code>) is the value for targetBytecode to compile for a JDK 1.4 JVM. **/
    public static final String PRE_JDK5 = JDK4;

    @Deprecated
    public static final String CURRENT_JVM_VERSION = getMinBytecodeVersion();

    /**
     *  A convenience for getting a default configuration.  Do not modify it!
     *  See {@link #CompilerConfiguration(Properties)} for an example on how to
     *  make a suitable copy to modify.  But if you're really starting from a
     *  default context, then you probably just want <code>new CompilerConfiguration()</code>. 
     */
    public static final CompilerConfiguration DEFAULT = new CompilerConfiguration();
    
    /**
     * See {@link WarningMessage} for levels.
     */
    private int warningLevel;

    /**
     * Encoding for source files
     */
    private String sourceEncoding;
    
    /**
      * The <code>PrintWriter</code> does nothing.
      */
     private PrintWriter output;

    /**
     * Directory into which to write classes
     */
    private File targetDirectory;

    /**
     * Classpath for use during compilation
     */
    private LinkedList<String> classpath;

    /**
     * If true, the compiler should produce action information
     */
    private boolean verbose;

    /**
     * If true, debugging code should be activated
     */
    private boolean debug;

    /**
     * The number of non-fatal errors to allow before bailing
     */
    private int tolerance;

    /**
     * Base class name for scripts (must derive from Script)
     */
    private String scriptBaseClass;

    private ParserPluginFactory pluginFactory;

    /**
     * extension used to find a groovy file
     */
    private String defaultScriptExtension;
    
    /**
     * extensions used to find a groovy files
     */
    private Set<String> scriptExtensions = new LinkedHashSet<String>();
    
    /**
     * if set to true recompilation is enabled
     */
    private boolean recompileGroovySource;
    
    /**
     * sets the minimum of time after a script can be recompiled.
     */
    private int minimumRecompilationInterval;

    /**
     * sets the bytecode version target
     */
    private String targetBytecode;

    /**
     * options for joint compilation (null by default == no joint compilation)
     */
    private Map<String, Object> jointCompilationOptions;
    
    /**
     * options for optimizations (empty map by default)
     */
    private Map<String, Boolean> optimizationOptions;

    private List<CompilationCustomizer> compilationCustomizers = new LinkedList<CompilationCustomizer>();

    /**
     * Sets a list of global AST transformations which should not be loaded even if they are
     * defined in META-INF/org.codehaus.groovy.transform.ASTTransformation files. By default,
     * none is disabled.
     */
    private Set<String> disabledGlobalASTTransformations;

    private BytecodeProcessor bytecodePostprocessor;

    /**
     * Sets the Flags to defaults.
     */
    public CompilerConfiguration() {
        //
        // Set in safe defaults

        setWarningLevel(WarningMessage.LIKELY_ERRORS);
        setOutput(null);
        setTargetDirectory((File) null);
        setClasspath("");
        setVerbose(false);
        setDebug(false);
        setTolerance(10);
        setScriptBaseClass(null);
        setRecompileGroovySource(false);
        setMinimumRecompilationInterval(100);
        // Target bytecode
        String targetByteCode = null;
        try {
            targetByteCode = System.getProperty("groovy.target.bytecode", targetByteCode);
        } catch (Exception e) {
            // IGNORE
        }
        if(targetByteCode != null) {
            setTargetBytecode(targetByteCode);
        } else {
            setTargetBytecode(getMinBytecodeVersion());
        }
        String tmpDefaultScriptExtension = null;
        try {
            tmpDefaultScriptExtension = System.getProperty("groovy.default.scriptExtension");
        } catch (Exception e) {
            // IGNORE
        }
        if(tmpDefaultScriptExtension != null) {
            setDefaultScriptExtension(tmpDefaultScriptExtension);
        } else {
            setDefaultScriptExtension(".groovy");
        }

        //
        // Source file encoding
        String encoding = null;
        try {
            encoding = System.getProperty("file.encoding", "US-ASCII");
        } catch (Exception e) {
            // IGNORE
        }
        try {
            encoding = System.getProperty("groovy.source.encoding", encoding);
        } catch (Exception e) {
            // IGNORE
        }
        setSourceEncoding(encoding);

        try {
            setOutput(new PrintWriter(System.err));
        } catch (Exception e) {
            // IGNORE
        }

        try {
            String target = System.getProperty("groovy.target.directory");
            if (target != null) {
                setTargetDirectory(target);
            }
        } catch (Exception e) {
            // IGNORE
        }

        boolean indy = false;
        try {
            indy = Boolean.getBoolean("groovy.target.indy");
        } catch (Exception e) {
            // IGNORE
        }
        if (DEFAULT!=null && Boolean.TRUE.equals(DEFAULT.getOptimizationOptions().get(INVOKEDYNAMIC))) {
            indy = true;
        }
        Map options = new HashMap<String,Boolean>(3);
        if (indy) {
            options.put(INVOKEDYNAMIC, Boolean.TRUE);
        }
        setOptimizationOptions(options);
    }

    /**
     * Copy constructor.  Use this if you have a mostly correct configuration
     * for your compilation but you want to make a some changes programatically.
     * An important reason to prefer this approach is that your code will most
     * likely be forward compatible with future changes to this configuration API.
     * <p>
     * An example of this copy constructor at work:
     * <pre>
     *    // In all likelihood there is already a configuration in your code's context
     *    // for you to copy, but for the sake of this example we'll use the global default.
     *    CompilerConfiguration myConfiguration = new CompilerConfiguration(CompilerConfiguration.DEFAULT);
     *    myConfiguration.setDebug(true);
     *</pre>
     *
     * @param configuration The configuration to copy.
     */
    public CompilerConfiguration(CompilerConfiguration configuration) {
        setWarningLevel(configuration.getWarningLevel());
        setOutput(configuration.getOutput());
        setTargetDirectory(configuration.getTargetDirectory());
        setClasspathList(new LinkedList<String>(configuration.getClasspath()));
        setVerbose(configuration.getVerbose());
        setDebug(configuration.getDebug());
        setTolerance(configuration.getTolerance());
        setScriptBaseClass(configuration.getScriptBaseClass());
        setRecompileGroovySource(configuration.getRecompileGroovySource());
        setMinimumRecompilationInterval(configuration.getMinimumRecompilationInterval());
        setTargetBytecode(configuration.getTargetBytecode());
        setDefaultScriptExtension(configuration.getDefaultScriptExtension());
        setSourceEncoding(configuration.getSourceEncoding());
        setTargetDirectory(configuration.getTargetDirectory());
        Map<String, Object> jointCompilationOptions = configuration.getJointCompilationOptions();
        if (jointCompilationOptions != null) {
            jointCompilationOptions = new HashMap<String, Object>(jointCompilationOptions);
        }
        setJointCompilationOptions(jointCompilationOptions);
        setPluginFactory(configuration.getPluginFactory());
        setScriptExtensions(configuration.getScriptExtensions());
        setOptimizationOptions(new HashMap<String, Boolean>(configuration.getOptimizationOptions()));
    }

    /**
     * Sets the Flags to the specified configuration, with defaults
     * for those not supplied.
     * Note that those "defaults" here do <em>not</em> include checking the
     * settings in {@link System#getProperties()} in general, only file.encoding, 
     * groovy.target.directory and groovy.source.encoding are.
     * <p>
     * If you want to set a few flags but keep Groovy's default
     * configuration behavior then be sure to make your settings in
     * a Properties that is backed by <code>System.getProperties()</code> (which
     * is done using this constructor). That might be done like this:
     * <pre>
     * Properties myProperties = new Properties(System.getProperties());
     * myProperties.setProperty("groovy.output.debug", "true");
     * myConfiguration = new CompilerConfiguration(myProperties);
     * </pre>
     * And you also have to contend with a possible SecurityException when
     * getting the system properties (See {@link java.lang.System#getProperties()}).
     * A safer approach would be to copy a default
     * CompilerConfiguration and make your changes there using the setter:
     * <pre>
     * // In all likelihood there is already a configuration for you to copy,
     * // but for the sake of this example we'll use the global default.
     * CompilerConfiguration myConfiguration = new CompilerConfiguration(CompilerConfiguration.DEFAULT);
     * myConfiguration.setDebug(true);
     * </pre>
     * <p>
     * <table summary="Groovy Compiler Configuration Properties">
     *   <tr>
     *      <th>Property Key</th><th>Get/Set Property Name</th>
     *   </tr>
     *      <tr>
     *      <td><code>"groovy.warnings"</code></td><td>{@link #getWarningLevel}</td></tr>
     *      <tr><td><code>"groovy.source.encoding"</code></td><td>{@link #getSourceEncoding}</td></tr>
     *      <tr><td><code>"groovy.target.directory"</code></td><td>{@link #getTargetDirectory}</td></tr>
     *      <tr><td><code>"groovy.target.bytecode"</code></td><td>{@link #getTargetBytecode}</td></tr>
     *      <tr><td><code>"groovy.classpath"</code></td><td>{@link #getClasspath}</td></tr>
     *      <tr><td><code>"groovy.output.verbose"</code></td><td>{@link #getVerbose}</td></tr>
     *      <tr><td><code>"groovy.output.debug"</code></td><td>{@link #getDebug}</td></tr>
     *      <tr><td><code>"groovy.errors.tolerance"</code></td><td>{@link #getTolerance}</td></tr>
     *      <tr><td><code>"groovy.script.extension"</code></td><td>{@link #getDefaultScriptExtension}</td></tr>
     *      <tr><td><code>"groovy.script.base"</code></td><td>{@link #getScriptBaseClass}</td></tr>
     *      <tr><td><code>"groovy.recompile"</code></td><td>{@link #getRecompileGroovySource}</td></tr>
     *      <tr><td><code>"groovy.recompile.minimumInterval"</code></td><td>{@link #getMinimumRecompilationInterval}</td></tr>
     *      <tr><td>
     *   </tr>
     * </table>
     *
     * @param configuration The properties to get flag values from.
     */
    public CompilerConfiguration(Properties configuration) throws ConfigurationException {
        this();
        configure(configuration);
    }

    /**
     * Checks if the specified bytecode version string represents a JDK 1.5+ compatible
     * bytecode version.
     * @param bytecodeVersion the bytecode version string (1.4, 1.5, 1.6, 1.7 or 1.8)
     * @return true if the bytecode version is JDK 1.5+
     */
    public static boolean isPostJDK5(String bytecodeVersion) {
        return JDK5.equals(bytecodeVersion)
            || JDK6.equals(bytecodeVersion)
            || JDK7.equals(bytecodeVersion)
            || JDK8.equals(bytecodeVersion);
    }

    /**
     * Checks if the specified bytecode version string represents a JDK 1.7+ compatible
     * bytecode version.
     * @param bytecodeVersion the bytecode version string (1.4, 1.5, 1.6, 1.7 or 1.8)
     * @return true if the bytecode version is JDK 1.7+
     */
    public static boolean isPostJDK7(String bytecodeVersion) {
        return JDK7.equals(bytecodeVersion)
            || JDK8.equals(bytecodeVersion);
    }

    /**
     * Method to configure a CompilerConfiguration by using Properties.
     * For a list of available properties look at {@link #CompilerConfiguration(Properties)}.
     * @param configuration The properties to get flag values from.
     */
    public void configure(Properties configuration) throws ConfigurationException {
        String text = null;
        int numeric = 0;

        //
        // Warning level

        numeric = getWarningLevel();
        try {
            text = configuration.getProperty("groovy.warnings", "likely errors");
            numeric = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            text = text.toLowerCase();
            if (text.equals("none")) {
                numeric = WarningMessage.NONE;
            }
            else if (text.startsWith("likely")) {
                numeric = WarningMessage.LIKELY_ERRORS;
            }
            else if (text.startsWith("possible")) {
                numeric = WarningMessage.POSSIBLE_ERRORS;
            }
            else if (text.startsWith("paranoia")) {
                numeric = WarningMessage.PARANOIA;
            }
            else {
                throw new ConfigurationException("unrecognized groovy.warnings: " + text);
            }
        }
        setWarningLevel(numeric);

        // 
        // Source file encoding 
        // 
        text = configuration.getProperty("groovy.source.encoding");
        if (text == null) {
            text = configuration.getProperty("file.encoding", "US-ASCII");
        }
        setSourceEncoding(text);

        //
        // Target directory for classes
        //
        text = configuration.getProperty("groovy.target.directory");
        if (text != null) setTargetDirectory(text);

        text = configuration.getProperty("groovy.target.bytecode");
        if (text != null) setTargetBytecode(text);

        //
        // Classpath
        //
        text = configuration.getProperty("groovy.classpath");
        if (text != null) setClasspath(text);

        //
        // Verbosity
        //
        text = configuration.getProperty("groovy.output.verbose");
        if (text != null && text.equalsIgnoreCase("true")) setVerbose(true);

        //
        // Debugging
        //
        text = configuration.getProperty("groovy.output.debug");
        if (text != null && text.equalsIgnoreCase("true")) setDebug(true);

        //
        // Tolerance
        // 
        numeric = 10;
        try {
            text = configuration.getProperty("groovy.errors.tolerance", "10");
            numeric = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new ConfigurationException(e);
        }
        setTolerance(numeric);

        //
        // Script Base Class
        //
        text = configuration.getProperty("groovy.script.base");
        if (text!=null) setScriptBaseClass(text);
        
        //
        // recompilation options
        //
        text = configuration.getProperty("groovy.recompile");
        if (text != null) {
            setRecompileGroovySource(text.equalsIgnoreCase("true"));
        }
        
        numeric = 100;
        try {
            text = configuration.getProperty("groovy.recompile.minimumIntervall");
            if (text==null) text = configuration.getProperty("groovy.recompile.minimumInterval");
            if (text!=null) {
                numeric = Integer.parseInt(text);
            } else {
                numeric = 100;
            }
        } catch (NumberFormatException e) {
            throw new ConfigurationException(e);
        }
        setMinimumRecompilationInterval(numeric);

        // disabled global AST transformations
        text = configuration.getProperty("groovy.disabled.global.ast.transformations");
        if (text!=null) {
            String[] classNames = text.split(",\\s*}");
            Set<String> blacklist = new HashSet<String>(Arrays.asList(classNames));
            setDisabledGlobalASTTransformations(blacklist);
        }
    }

    /**
     * Gets the currently configured warning level. See {@link WarningMessage}
     * for level details.
     */
    public int getWarningLevel() {
        return this.warningLevel;
    }

    /**
     * Sets the warning level. See {@link WarningMessage} for level details.
     */
    public void setWarningLevel(int level) {
        if (level < WarningMessage.NONE || level > WarningMessage.PARANOIA) {
            this.warningLevel = WarningMessage.LIKELY_ERRORS;
        }
        else {
            this.warningLevel = level;
        }
    }

    /**
     * Gets the currently configured source file encoding.
     */
    public String getSourceEncoding() {
        return this.sourceEncoding;
    }

    /**
     * Sets the encoding to be used when reading source files.
     */
    public void setSourceEncoding(String encoding) {
        if (encoding == null) encoding = "US-ASCII";
        this.sourceEncoding = encoding;
    }

    /**
     * Gets the currently configured output writer.
     * @deprecated not used anymore
     */
    @Deprecated 
    public PrintWriter getOutput() {
        return this.output;
    }

    /**
     * Sets the output writer.
     * @deprecated not used anymore, has no effect
     */
    @Deprecated
    public void setOutput(PrintWriter output) {
        if (output == null) {
            this.output = new PrintWriter(NullWriter.DEFAULT);
        }
        else {
            this.output = output;
        }
    }

    /**
     * Gets the target directory for writing classes.
     */
    public File getTargetDirectory() {
        return this.targetDirectory;
    }

    /**
     * Sets the target directory.
     */
    public void setTargetDirectory(String directory) {
        if (directory != null && directory.length() > 0) {
            this.targetDirectory = new File(directory);
        } else {
            this.targetDirectory = null;
        }
    }

    /**
     * Sets the target directory.
     */
    public void setTargetDirectory(File directory) {
        this.targetDirectory = directory;
    }

    /**
     * @return the classpath
     */
    public List<String> getClasspath() {
        return this.classpath;
    }

    /**
     * Sets the classpath.
     */
    public void setClasspath(String classpath) {
        this.classpath = new LinkedList<String>();
        StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator);
        while (tokenizer.hasMoreTokens()) {
            this.classpath.add(tokenizer.nextToken());
        }
    }
    
    /**
     * sets the classpath using a list of Strings
     * @param parts list of strings containing the classpath parts
     */
    public void setClasspathList(List<String> parts) {
        this.classpath = new LinkedList<String>(parts);
    }

    /**
     * Returns true if verbose operation has been requested.
     */
    public boolean getVerbose() {
        return this.verbose;
    }

    /**
     * Turns verbose operation on or off.
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Returns true if debugging operation has been requested.
     */
    public boolean getDebug() {
        return this.debug;
    }

    /**
     * Turns debugging operation on or off.
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * Returns the requested error tolerance.
     */
    public int getTolerance() {
        return this.tolerance;
    }

    /**
     * Sets the error tolerance, which is the number of
     * non-fatal errors (per unit) that should be tolerated before
     * compilation is aborted.
     */
    public void setTolerance(int tolerance) {
        this.tolerance = tolerance;
    }

    /**
     * Gets the name of the base class for scripts.  It must be a subclass
     * of Script.
     */
    public String getScriptBaseClass() {
        return this.scriptBaseClass;
    }

    /**
     * Sets the name of the base class for scripts.  It must be a subclass
     * of Script.
     */
    public void setScriptBaseClass(String scriptBaseClass) {
        this.scriptBaseClass = scriptBaseClass;
    }

    public ParserPluginFactory getPluginFactory() {
        if (pluginFactory == null) {
            pluginFactory = ParserPluginFactory.newInstance();
        }
        return pluginFactory;
    }

    public void setPluginFactory(ParserPluginFactory pluginFactory) {
        this.pluginFactory = pluginFactory;
    }

    public void setScriptExtensions(Set<String> scriptExtensions) {
        if(scriptExtensions == null) scriptExtensions = new LinkedHashSet<String>();
        this.scriptExtensions = scriptExtensions;
    }
    
    public Set<String> getScriptExtensions() {
        if(scriptExtensions == null || scriptExtensions.isEmpty()) {
            /*
             *  this happens 
             *  *    when groovyc calls FileSystemCompiler in forked mode, or
             *  *    when FileSystemCompiler is run from the command line directly, or
             *  *    when groovy was not started using groovyc or FileSystemCompiler either
             */
            scriptExtensions = SourceExtensionHandler.getRegisteredExtensions(
                    this.getClass().getClassLoader());
        }
        return scriptExtensions;
    }
    
    public String getDefaultScriptExtension() {
        return defaultScriptExtension;
    }


    public void setDefaultScriptExtension(String defaultScriptExtension) {
        this.defaultScriptExtension = defaultScriptExtension;
    }
    
    public void setRecompileGroovySource(boolean recompile) {
        recompileGroovySource = recompile;
    }
    
    public boolean getRecompileGroovySource(){
        return recompileGroovySource;
    }
    
    public void setMinimumRecompilationInterval(int time) {
        minimumRecompilationInterval = Math.max(0,time);
    }
    
    public int getMinimumRecompilationInterval() {
        return minimumRecompilationInterval;
    }

    /**
     * Allow setting the bytecode compatibility level. The parameter can take
     * one of the values in {@link #ALLOWED_JDKS}.
     *
     * @param version the bytecode compatibility level
     */
    public void setTargetBytecode(String version) {
        for (String allowedJdk : ALLOWED_JDKS) {
            if (allowedJdk.equals(version)) {
                this.targetBytecode = version;
            }
        }
    }

    /**
     * Retrieves the compiler bytecode compatibility level.
     * Defaults to the minimum officially supported bytecode
     * version for any particular Groovy version.
     *
     * @return bytecode compatibility level
     */
    public String getTargetBytecode() {
        return this.targetBytecode;
    }

    private static String getMinBytecodeVersion() {
        try {
            Class.forName(JDK5_CLASSNAME_CHECK);
            return POST_JDK5;
        } catch(Exception ignore) {
        }
        return PRE_JDK5;
    }

    /**
     * Gets the joint compilation options for this configuration.
     * @return the options
     */
    public Map<String, Object> getJointCompilationOptions() {
        return jointCompilationOptions;
    }
    
    /**
     * Sets the joint compilation options for this configuration. 
     * Using null will disable joint compilation.
     * @param options the options
     */
    public void setJointCompilationOptions(Map<String, Object> options) {
        jointCompilationOptions = options;
    }

    /**
     * Gets the optimization options for this configuration.
     * @return the options (always not null)
     */
    public Map<String, Boolean> getOptimizationOptions() {
        return optimizationOptions;
    }
    
    /**
     * Sets the optimization options for this configuration. 
     * No entry or a true for that entry means to enable that optimization, 
     * a false means the optimization is disabled. 
     * Valid keys are "all" and "int".
     * @param options the options.
     * @throws IllegalArgumentException if the options are null
     */
    public void setOptimizationOptions(Map<String, Boolean> options) {
        if (options==null) throw new IllegalArgumentException("provided option map must not be null");
        optimizationOptions = options;
    }

    /**
     * Adds compilation customizers to the compilation process. A compilation customizer is a class node
     * operation which performs various operations going from adding imports to access control.
     * @param customizers the list of customizers to be added
     * @return this configuration instance
     */
    public CompilerConfiguration addCompilationCustomizers(CompilationCustomizer... customizers) {
        if (customizers==null) throw new IllegalArgumentException("provided customizers list must not be null");
        compilationCustomizers.addAll(Arrays.asList(customizers));
        return this;
    }

    /**
     * Returns the list of compilation customizers.
     * @return the customizers (always not null)
     */
    public List<CompilationCustomizer> getCompilationCustomizers() {
        return compilationCustomizers;
    }

    /**
     * Returns the list of disabled global AST transformation class names.
     * @return a list of global AST transformation fully qualified class names
     */
    public Set<String> getDisabledGlobalASTTransformations() {
        return disabledGlobalASTTransformations;
    }

    /**
     * Disables global AST transformations. In order to avoid class loading side effects, it is not recommended
     * to use MyASTTransformation.class.getName() by directly use the class name as a string. Disabled AST transformations
     * only apply to automatically loaded global AST transformations, that is to say transformations defined in a
     * META-INF/org.codehaus.groovy.transform.ASTTransformation file. If you explicitly add a global AST transformation
     * in your compilation process, for example using the {@link org.codehaus.groovy.control.customizers.ASTTransformationCustomizer} or
     * using a {@link org.codehaus.groovy.control.CompilationUnit.PrimaryClassNodeOperation}, then nothing will prevent
     * the transformation from being loaded.
     * @param disabledGlobalASTTransformations a set of fully qualified class names of global AST transformations
     * which should not be loaded.
     */
    public void setDisabledGlobalASTTransformations(final Set<String> disabledGlobalASTTransformations) {
        this.disabledGlobalASTTransformations = disabledGlobalASTTransformations;
    }

    public BytecodeProcessor getBytecodePostprocessor() {
        return bytecodePostprocessor;
    }

    public void setBytecodePostprocessor(final BytecodeProcessor bytecodePostprocessor) {
        this.bytecodePostprocessor = bytecodePostprocessor;
    }
}
