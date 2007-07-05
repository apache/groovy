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
 * @version $Id$
 */

public class CompilerConfiguration {
    public static final CompilerConfiguration DEFAULT = new CompilerConfiguration();

    /** Whether to use the JSR parser or not if no property is explicitly stated */
    protected static final boolean DEFAULT_JSR_FLAG = true;

    private static final String JDK5_CLASSNAME_CHECK = "java.lang.annotation.Annotation";

    public static final String POST_JDK5 = "1.5";
    
    public static final String PRE_JDK5 = "1.4";

    private static boolean jsrGroovy;

    /**
     * See WarningMessage for levels
     */
    private int warningLevel;
    /**
     * Encoding for source files
     */
    private String sourceEncoding;
    /**
     * A PrintWriter for communicating with the user
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
    /**
     * should we use the New JSR Groovy parser or stay with the static one
     */
    private boolean useNewGroovy = getDefaultJsrFlag();

    private ParserPluginFactory pluginFactory;

    /**
     * extension used to find a groovy file
     */
    private String defaultScriptExtension = ".groovy";
    
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


        //
        // Source file encoding
        String encoding = null;
        try {
            encoding = System.getProperty("file.encoding", "US-ASCII");
        } catch (Exception e) {}
        try {
            encoding = System.getProperty("groovy.source.encoding", encoding);
        } catch (Exception e) {}
        setSourceEncoding(encoding);

        try {
            setOutput(new PrintWriter(System.err));
        }
        catch (Exception e) {
        }
        /*try {
            setClasspath(System.getProperty("java.class.path"));
        }
        catch (Exception e) {
        }*/

        try {
            String target = System.getProperty("groovy.target.directory");
            if (target != null) {
                setTargetDirectory(target);
            }
        }
        catch (Exception e) {
        }
    }


    /**
     * Sets the Flags to the specified configuration, with defaults
     * for those not supplied.
     */

    public CompilerConfiguration(Properties configuration) throws ConfigurationException {
        this();

        String text = null;
        int numeric = 0;


        //
        // Warning level

        numeric = getWarningLevel();
        try {
            text = configuration.getProperty("groovy.warnings", "likely errors");
            numeric = Integer.parseInt(text);
        }
        catch (NumberFormatException e) {
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

        text = configuration.getProperty("groovy.source.encoding");
        if (text != null) {
            setSourceEncoding(text);
        }


        //
        // Target directory for classes

        text = configuration.getProperty("groovy.target.directory");
        if (text != null) {
            setTargetDirectory(text);
        }

        //
        // Target bytecode
        setTargetBytecode(getVMVersion());
        
        text = configuration.getProperty("groovy.target.bytecode");
        if (text != null) {
            setTargetBytecode(text);
        }
        
        //
        // Classpath

        text = configuration.getProperty("groovy.classpath");
        if (text != null) {
            setClasspath(text);
        }


        //
        // Verbosity

        text = configuration.getProperty("groovy.output.verbose");
        if (text != null && text.equals("true")) {
            setVerbose(true);
        }


        //
        // Debugging

        text = configuration.getProperty("groovy.output.debug");
        if (text != null && text.equals("true")) {
            setDebug(true);
        }


        //
        // Tolerance

        numeric = 10;

        try {
            text = configuration.getProperty("groovy.errors.tolerance", "10");
            numeric = Integer.parseInt(text);
        }
        catch (NumberFormatException e) {
            throw new ConfigurationException(e);
        }

        setTolerance(numeric);


        //
        // Script Base Class

        text = configuration.getProperty("groovy.script.base");
        setScriptBaseClass(text);

        text = configuration.getProperty("groovy.jsr");
        if (text != null) {
            setUseNewGroovy(text.equalsIgnoreCase("true"));
        }
        
        
        //
        // recompilation options
        //
        text = configuration.getProperty("groovy.recompile");
        if (text != null) {
            setRecompileGroovySource(text.equalsIgnoreCase("true"));
        }
        
        numeric = 100;
        try {
            text = configuration.getProperty("groovy.recompile.minimumIntervall", ""+numeric);
            numeric = Integer.parseInt(text);
        }
        catch (NumberFormatException e) {
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
        }
        else {
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

    /**
     * Returns true if the new groovy (JSR) parser is enabled
     */
    public boolean isUseNewGroovy() {
        return useNewGroovy;
    }

    public void setUseNewGroovy(boolean useNewGroovy) {
        this.useNewGroovy = useNewGroovy;
    }

    public ParserPluginFactory getPluginFactory() {
        if (pluginFactory == null) {
            pluginFactory = ParserPluginFactory.newInstance(isUseNewGroovy());
        }
        return pluginFactory;
    }

    public void setPluginFactory(ParserPluginFactory pluginFactory) {
        this.pluginFactory = pluginFactory;
    }

    /**
     * Returns true if we are the JSR compatible Groovy language
     */
    public static boolean isJsrGroovy() {
        return jsrGroovy;
    }

    /**
     * Should only be called by the JSR parser
     */
    public static void setJsrGroovy(boolean value) {
        jsrGroovy = value;
    }

    protected static boolean getDefaultJsrFlag() {
        // TODO a temporary hack while we have 2 parsers
        String property = null;
        try {
             property = System.getProperty("groovy.jsr");
        }
        catch (Throwable e) {
            // ignore security warnings
        }
        if (property != null) {
            return "true".equalsIgnoreCase(property);
        }
        return DEFAULT_JSR_FLAG;
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
    
    private static final String getVMVersion() {
        try {
            Class.forName(JDK5_CLASSNAME_CHECK);
            return POST_JDK5;
        }
        catch(Exception _ex) {
        }
        
        return PRE_JDK5;
    }
    
    public Map getJointCompilationOptions() {
        return jointCompilationOptions;
    }
    
    public void setJointCompilationOptions(Map options) {
        jointCompilationOptions = options;
    }
}
