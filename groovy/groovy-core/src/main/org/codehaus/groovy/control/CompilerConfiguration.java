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

package org.codehaus.groovy.control;

import org.codehaus.groovy.control.io.NullWriter;
import org.codehaus.groovy.control.messages.WarningMessage;

import java.io.File;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;


/**
 * Compilation control flags and coordination stuff.
 *
 * @author <a href="mailto:cpoirier@dreaming.org">Chris Poirier</a>
 * @version $Id$
 */

public class CompilerConfiguration {
    public static final CompilerConfiguration DEFAULT = new CompilerConfiguration();

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
    private boolean useNewGroovy = true;

    private ParserPluginFactory pluginFactory;


    /**
     * Sets the Flags to defaults.
     */

    public CompilerConfiguration() {
        //
        // Set in safe defaults

        setWarningLevel(WarningMessage.LIKELY_ERRORS);
        setSourceEncoding("US-ASCII");
        setOutput(null);
        setTargetDirectory((File) null);
        setClasspath("");
        setVerbose(false);
        setDebug(false);
        setTolerance(10);
        setScriptBaseClass(null);


        //
        // Try for better defaults, ignore errors.

        try {
            setSourceEncoding(System.getProperty("file.encoding", "US-ASCII"));
        }
        catch (Exception e) {
        }
        try {
            setOutput(new PrintWriter(System.err));
        }
        catch (Exception e) {
        }
        try {
            setClasspath(System.getProperty("java.class.path"));
        }
        catch (Exception e) {
        }

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
        if (this.output == null) {
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
     * Sets the output writer.
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
}




