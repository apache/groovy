/*
 $Id$

 Copyright 2005 (C) Jeremy Rayner. All Rights Reserved.

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

package org.codehaus.groovy.ant;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import groovy.util.AntBuilder;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.tools.ErrorReporter;

/**
 * Executes a series of Groovy statements.
 *
 * <p>Statements can
 * either be read in from a text file using the <i>src</i> attribute or from
 * between the enclosing groovy tags.</p>
 */
public class Groovy extends Task {
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

    /**
     * Compiler configuration.
     *
     * Used to specify the debug output to print stacktraces in case something fails.
     * TODO: Could probably be reused to specify the encoding of the files to load or other properties.
     */
    private CompilerConfiguration configuration = new CompilerConfiguration();

    /**
     * Enable compiler to report stack trace information if a problem occurs
     * during compilation.
     * @param stacktrace
     */
    public void setStacktrace(boolean stacktrace) {
        configuration.setDebug(stacktrace);
    }


    /**
     * Set the name of the file to be run. The folder of the file is automatically added to the classpath.
     * Required unless statements are enclosed in the build file
     */
    public void setSrc(final File srcFile) {
        this.srcFile = srcFile;
    }

    /**
     * Set an inline command to execute.
     * NB: Properties are not expanded in this text.
     */
    public void addText(String txt) {
        log("addText('"+txt+"')", Project.MSG_VERBOSE);
        this.command += txt;
    }

    /**
     * Adds a set of files (nested fileset attribute).
     */
    public void addFileset(FileSet set) {
        filesets.addElement(set);
    }

    /**
     * Set the output file;
     * optional, defaults to the Ant log.
     */
    public void setOutput(File output) {
        this.output = output;
    }

    /**
     * whether output should be appended to or overwrite
     * an existing file.  Defaults to false.
     *
     * @since Ant 1.5
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
     * Gets called for instance by Ant when it encounters a nested <classpath> element. 
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
     */
    public void setClasspathRef(final Reference r) {
        createClasspath().setRefid(r);
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
        log("execute()", Project.MSG_VERBOSE);

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
                    log("Opening PrintStream to output file " + output,
                        Project.MSG_VERBOSE);
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

        log("statements executed successfully", Project.MSG_VERBOSE);
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


    /**
     * read in lines and execute them
     */
    protected void runStatements(Reader reader, PrintStream out)
        throws IOException {
        log("runStatements()", Project.MSG_VERBOSE);

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
        if (!txt.equals("")) {
            execGroovy(txt.toString(), out);
        }
    }


    /**
     * Exec the statement.
     */
    protected void execGroovy(final String txt, final PrintStream out) {
        log("execGroovy()", Project.MSG_VERBOSE);

        // Check and ignore empty statements
        if ("".equals(txt.trim())) {
            return;
        }

        log("Groovy: " + txt, Project.MSG_VERBOSE);

        //log(getClasspath().toString(),Project.MSG_VERBOSE);
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

        final GroovyClassLoader classLoader = new GroovyClassLoader(baseClassLoader);
        addClassPathes(classLoader);
        
        final GroovyShell groovy = new GroovyShell(classLoader, new Binding(), configuration);
        try {
            final Script script = groovy.parse(txt);
            script.setProperty("ant", new AntBuilder(project, getOwningTarget()));
            script.setProperty("project", project);
            script.setProperty("properties", new AntProjectPropertiesDelegate(project));
            script.setProperty("target", getOwningTarget());
            script.setProperty("task", this);
            if (mavenPom != null) {
                script.setProperty("pom", mavenPom);
            }
            script.run();
        } catch (CompilationFailedException e) {
            StringWriter writer = new StringWriter();
            new ErrorReporter( e, false ).write( new PrintWriter(writer) );
            String message = writer.toString();
            throw new BuildException("Script Failed: "+ message, getLocation());
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
     */
    protected void printResults(PrintStream out) {
        log("printResults()", Project.MSG_VERBOSE);
        StringBuffer line = new StringBuffer();
        out.println(line);
        line = new StringBuffer();
        out.println();
    }
}
