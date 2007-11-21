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

package org.codehaus.groovy.control;

import org.codehaus.groovy.control.io.NullWriter;
import org.codehaus.groovy.control.messages.WarningMessage;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;


/**
 * Compilation control flags and coordination stuff.
 *
 * @author <a href="mailto:cpoirier@dreaming.org">Chris Poirier</a>
 * @author <a href="mailto:blackdrag@gmx.org">Jochen Theodorou</a>
 * @author <a href="mailto:jim@pagesmiths.com">Jim White</a>
 * @version $Id$
 */

public class CompilerConfiguration {

    private static final String JDK5_CLASSNAME_CHECK = "java.lang.annotation.Annotation";

    /** This (<code>"1.5"</code>) is the value for targetBytecode to compile for a JDK 1.5 or later JVM. **/
    public static final String POST_JDK5 = "1.5";
    
    /** This (<code>"1.4"<code/>) is the value for targetBytecode to compile for a JDK 1.4 JVM. **/
    public static final String PRE_JDK5 = "1.4";

    // Just call getVMVersion() once.
    public static final String currentJVMVersion = getVMVersion();

    // Static initializers are executed in text order,
    // therefore we must do this one last!
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
     * A <code>PrintWriter</code> for communicating with the user
     */
    private PrintWriter output;
    /**
     * Directory into which to write classes
     */
    private File targetDirectory;
    /**
     * Classpath for use during compilation
     */
    private LinkedList classpath;
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
    private Map jointCompilationOptions;
    
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
        setTargetBytecode(getVMVersion());
        setDefaultScriptExtension(".groovy");

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
    }
    
    /**
     * Copy constructor.  Use this if you have a mostly correct configuration
     * for your compilation but you want to make a some changes programmatically.  
     * An important reason to prefer this approach is that your code will most
     * likely be forward compatible with future changes to this configuration API.<br/>
     * An example of this copy constructor at work:<br/>
     * <pre>
     *    // In all likelihood there is already a configuration in your code's context
     *    // for you to copy, but for the sake of this example we'll use the global default.
     *    CompilerConfiguration myConfiguration = new CompilerConfiguration(CompilerConfiguration.DEFAULT);
     *    myConfiguration.setDebug(true);
     *</pre>
     * @param configuration The configuration to copy.
     */
    public CompilerConfiguration(CompilerConfiguration configuration) {
        setWarningLevel(configuration.getWarningLevel());
        setOutput(configuration.getOutput());
        setTargetDirectory(configuration.getTargetDirectory());
        setClasspathList(new LinkedList(configuration.getClasspath()));
        setVerbose(configuration.getVerbose());
        setDebug(configuration.getDebug());
        setTolerance(configuration.getTolerance());
        setScriptBaseClass(configuration.getScriptBaseClass());
        setRecompileGroovySource(configuration.getRecompileGroovySource());
        setMinimumRecompilationInterval(configuration.getMinimumRecompilationInterval());
        setTargetBytecode(configuration.getTargetBytecode());
        setDefaultScriptExtension(configuration.getDefaultScriptExtension());
        setSourceEncoding(configuration.getSourceEncoding());
        setOutput(configuration.getOutput());
        setTargetDirectory(configuration.getTargetDirectory());
        Map jointCompilationOptions = configuration.getJointCompilationOptions();
        if (jointCompilationOptions!=null) {
            jointCompilationOptions = new HashMap(jointCompilationOptions);
        }
        setJointCompilationOptions(jointCompilationOptions);
        setPluginFactory(configuration.getPluginFactory());
    }


    /**
     * Sets the Flags to the specified configuration, with defaults
     * for those not supplied.
     * Note that those "defaults" here do <em>not</em> include checking the
     * settings in {@link System#getProperties()} in general, only file.encoding, 
     * groovy.target.directory and groovy.source.encoding are.<br/>
     * If you want to set a few flags but keep Groovy's default
     * configuration behavior then be sure to make your settings in
     * a Properties that is backed by <code>System.getProperties()</code> (which
     * is done using the {@link #CompilerConfiguration(Properties)} constructor).<br/>
     *   That might be done like this:<br/>
     * <pre>
     *    Properties myProperties = new Properties(System.getProperties());
     *    myProperties.setProperty("groovy.output.debug", "true");
     *    myConfiguration = new CompilerConfiguration(myProperties);
     * </pre>
     * And you also have to contend with a possible SecurityException when
     * getting the system properties (See {@link java.lang.System#getProperties()}).<br/> 
     * An safer method would be to copy a default
     * CompilerConfiguration and make your changes there using the
     * setter.<br/>
     * <pre>
     *    // In all likelihood there is already a configuration for you to copy,
     *    // but for the sake of this example we'll use the global default.
     *    CompilerConfiguration myConfiguration = new CompilerConfiguration(CompilerConfiguration.DEFAULT);
     *    myConfiguration.setDebug(true);
     * </pre>
     * Another reason to use the copy constructor rather than this one is that you
     * must call {@link #setOutput}.  Calling <code>setOutput(null)</code> is valid and will
     * set up a <code>PrintWriter</code> to a bit bucket.  The copy constructor will of course set
     * the same one as the original.
     *
     *<table summary="Groovy Compiler Configuration Properties">
         <tr>
            <th>Property Key</th><th>Get/Set Property Name</th>
         </tr>
            <tr>
            <td><code>"groovy.warnings"</code></td><td>{@link #getWarningLevel}</td></tr>
            <tr><td><code>"groovy.source.encoding"</code></td><td>{@link #getSourceEncoding}</td></tr>
            <tr><td><code>"groovy.target.directory"</code></td><td>{@link #getTargetDirectory}</td></tr>
            <tr><td><code>"groovy.target.bytecode"</code></td><td>{@link #getTargetBytecode}</td></tr>
            <tr><td><code>"groovy.classpath"</code></td><td>{@link #getClasspath}</td></tr>
            <tr><td><code>"groovy.output.verbose"</code></td><td>{@link #getVerbose}</td></tr>
            <tr><td><code>"groovy.output.debug"</code></td><td>{@link #getDebug}</td></tr>
            <tr><td><code>"groovy.errors.tolerance"</code></td><td>{@link #getTolerance}</td></tr>
            <tr><td><code>"groovy.script.extension"</code></td><td>{@link #getDefaultScriptExtension}</td></tr>
            <tr><td><code>"groovy.script.base"</code></td><td>{@link #getScriptBaseClass}</td></tr>
            <tr><td><code>"groovy.recompile"</code></td><td>{@link #getRecompileGroovySource}</td></tr>
            <tr><td><code>"groovy.recompile.minimumInterval"</code></td><td>{@link #getMinimumRecompilationInterval}</td></tr>
            <tr><td>
         </tr>
     </table>
     <br/>
     * @param configuration The properties to get flag values from.
     */
    public CompilerConfiguration(Properties configuration) throws ConfigurationException {
        this();
        configure(configuration);
    }
    
    /**
     * Method to configure a this CompilerConfiguration by using Properties.
     * For a list of available properties look at {link {@link #CompilerConfiguration(Properties)}.
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
                throw new ConfigurationException("unrecogized groovy.warnings: " + text);
            }
        }
        setWarningLevel(numeric);

        // 
        // Source file encoding 
        // 
        text = configuration.getProperty("groovy.source.encoding");
        if (text != null) setSourceEncoding(text);


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
        
        
    }


    /**
     * Gets the currently configured warning level.  See WarningMessage
     * for level details.
     */
    public int getWarningLevel() {
        return this.warningLevel;
    }


    /**
     * Sets the warning level.  See WarningMessage for level details.
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
     */
    public PrintWriter getOutput() {
        return this.output;
    }


    /**
     * Sets the output writer.
     */
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
     * Gets the classpath.
     */
    public List getClasspath() {
        return this.classpath;
    }


    /**
     * Sets the classpath.
     */
    public void setClasspath(String classpath) {
        this.classpath = new LinkedList();

        StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator);
        while (tokenizer.hasMoreTokens()) {
            this.classpath.add(tokenizer.nextToken());
        }
    }
    
    /**
     * sets the classpath using a list of Strings
     * @param l list of strings containg the classpathparts
     */
    public void setClasspathList(List l) {
        this.classpath = new LinkedList(l);
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
            pluginFactory = ParserPluginFactory.newInstance(true);
        }
        return pluginFactory;
    }

    public void setPluginFactory(ParserPluginFactory pluginFactory) {
        this.pluginFactory = pluginFactory;
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
     * Allow setting the bytecode compatibility. The parameter can take
     * one of the values <tt>1.5</tt> or <tt>1.4</tt>. If wrong parameter
     * then the value will default to VM determined version.
     * 
     * @param version the bytecode compatibility mode
     */
    public void setTargetBytecode(String version) {
        if(PRE_JDK5.equals(version) || POST_JDK5.equals(version)) {
            this.targetBytecode = version;
        }
    }

    /**
     * Retrieves the compiler bytecode compatibility mode.
     * 
     * @return bytecode compatibity mode. Can be either <tt>1.5</tt> or <tt>1.4</tt>.
     */
    public String getTargetBytecode() {
        return this.targetBytecode;
    }
    
    private static String getVMVersion() {
        try {
            Class.forName(JDK5_CLASSNAME_CHECK);
            return POST_JDK5;
        } catch(Exception ex) {
            // IGNORE
        }
        
        return PRE_JDK5;
    }
    
    /**
     * Gets the joint compilation options for this configuration.
     * @return the options
     */
    public Map getJointCompilationOptions() {
        return jointCompilationOptions;
    }
    
    /**
     * Sets the joint compilation options for this configuration. 
     * Using null will disable joint compilation.
     * @param options the options
     */
    public void setJointCompilationOptions(Map options) {
        jointCompilationOptions = options;
    }
}
