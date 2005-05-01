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

import org.apache.tools.ant.types.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;

import org.apache.tools.ant.*;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.runtime.InvokerHelper;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import groovy.util.AntBuilder;

/**
 * Executes a series of Groovy statements.
 *
 * <p>Statements can
 * either be read in from a text file using the <i>src</i> attribute or from
 * between the enclosing groovy tags.</p>
 *
 *
 * Based heavily on SQLExec.java which is part of apache-ant
 * http://cvs.apache.org/viewcvs.cgi/ant/src/main/org/apache/tools/ant/taskdefs/SQLExec.java?rev=MAIN
 *
 * Copyright  2000-2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
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
     * Print results.
     */
    private boolean print = false;

    /**
     * Results Output file.
     */
    private File output = null;

    /**
     * Append to an existing file or overwrite it?
     */
    private boolean append = false;

    /**
     * Used for caching loaders / driver. This is to avoid
     * getting an OutOfMemoryError when calling this task
     * multiple times in a row.
     */
    private static Hashtable loaderMap = new Hashtable(3);

    private Path classpath;

    /**
     * User name.
     */
    private String userId = null;

    /**
     * Groovy Version needed for this collection of statements.
     **/
    private String version = null;


    /**
     * Set the name of the file to be run.
     * Required unless statements are enclosed in the build file
     */
    public void setSrc(File srcFile) {
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
     * Print results from the statements;
     * optional, default false
     */
    public void setPrint(boolean print) {
        this.print = print;
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
    public void setClasspath(Path classpath) {
        this.classpath = classpath;
    }

    /**
     * Add a path to the classpath for loading.
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
    public void setClasspathRef(Reference r) {
        createClasspath().setRefid(r);
    }

    /**
     * Sets the version string, execute task only if
     * groovy version match; optional.
     * @param version The version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }


    protected static Hashtable getLoaderMap() {
        return loaderMap;
    }




    /**
     * Gets the classpath.
     * @return Returns a Path
     */
    public Path getClasspath() {
        return classpath;
    }

    /**
     * Gets the userId.
     * @return Returns a String
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Set the user name for the connection; required.
     * @param userId The userId to set
     */
    public void setUserid(String userId) {
        this.userId = userId;
    }

    /**
     * Gets the version.
     * @return Returns a String
     */
    public String getVersion() {
        return version;
    }

    /**
     * Load the file and then execute it
     */
    public void execute() throws BuildException {
        log("execute()", Project.MSG_VERBOSE);

        command = command.trim();

        try {
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

            log("statements executed successfully");
        } finally{}
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
    protected void execGroovy(String txt, PrintStream out) {
        log("execGroovy()", Project.MSG_VERBOSE);

        // Check and ignore empty statements
        if ("".equals(txt.trim())) {
            return;
        }

            log("Groovy: " + txt, Project.MSG_VERBOSE);

            //log(getClasspath().toString(),Project.MSG_VERBOSE);
            GroovyShell groovy = new GroovyShell(GroovyShell.class.getClassLoader());

            try {
                Script script = groovy.parse(txt);
                Project project = getProject();
                script.setProperty("ant",new AntBuilder(project));
                script.setProperty("project",project);
                script.setProperty("properties",project.getProperties());
                script.setProperty("target",getOwningTarget());
                script.setProperty("task",this);

                // treat the case Ant is run through Maven, and
                if ("org.apache.commons.grant.GrantProject".equals(project.getClass().getName())) {
                    try {
                        Object propsHandler = project.getClass().getMethod("getPropsHandler", new Class[0]).invoke(project, new Object[0]);
                        Field contextField = propsHandler.getClass().getDeclaredField("context");
                        contextField.setAccessible(true);
                        Object context = contextField.get(propsHandler);
                        Object mavenPom = InvokerHelper.invokeMethod(context, "getProject", new Object[0]);
                        script.setProperty("pom", mavenPom);
                    } catch (Exception e) {
                        throw new BuildException("Impossible to retrieve Maven's Ant project: " + e.getMessage(), getLocation());
                    }
                }

                script.run();
            } catch (CompilationFailedException e) {
                throw new BuildException("Script Failed: "+ e.getMessage(), getLocation());
            }

            if (print) {
                StringBuffer line = new StringBuffer();
                line.append( " foo bar");
                out.println(line);
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
