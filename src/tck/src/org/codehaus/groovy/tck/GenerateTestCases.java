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
package org.codehaus.groovy.tck;

import java.io.*;
import java.nio.charset.Charset;

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.util.*;

/**
 * Generates test files. This task can take the following
 * arguments:
 * <ul>
 * <li>sourcedir
 * <li>destdir
 * </ul>
 * Both are required.
 * <p>
 * When this task executes, it will recursively scan the sourcedir
 * looking for source files to expand into testcases. This task makes its
 * generation decision based on timestamp.
 *
 * Based heavily on the Javac implementation in Ant
 */
public class GenerateTestCases extends MatchingTask {

    private BatchGenerate batchGenerate = new BatchGenerate();
    private Path src;
    private File destDir;
    private Path compileClasspath;
    private Path compileSourcepath;
    private String encoding;

    protected boolean failOnError = true;
    protected boolean listFiles = false;
    protected File[] compileList = new File[0];

    public GenerateTestCases() {
    }

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
     * @param srcDir the source directories as a path
     */
    public void setSrcdir(Path srcDir) {
        if (src == null) {
            src = srcDir;
        }
        else {
            src.append(srcDir);
        }
        batchGenerate.setSrcdirPath(src.toString());
    }

    /**
     * Gets the source dirs to find the source java files.
     * @return the source directorys as a path
     */
    public Path getSrcdir() {
        return src;
    }

    /**
     * Set the destination directory into which the Java source
     * files should be compiled.
     * @param destDir the destination director
     */
    public void setDestdir(File destDir) {
        this.destDir = destDir;
    }

    /**
     * Enable verbose compiling which will display which files
     * are being compiled
     * @param verbose
     */
    public void setVerbose(boolean verbose) {
        batchGenerate.setVerbose( verbose );
    }

    /**
     * Gets the destination directory into which the java source files
     * should be compiled.
     * @return the destination directory
     */
    public File getDestdir() {
        return destDir;
    }

    /**
     * Set the sourcepath to be used for this compilation.
     * @param sourcepath the source path
     */
    public void setSourcepath(Path sourcepath) {
        if (compileSourcepath == null) {
            compileSourcepath = sourcepath;
        }
        else {
            compileSourcepath.append(sourcepath);
        }
    }

    /**
     * Gets the sourcepath to be used for this compilation.
     * @return the source path
     */
    public Path getSourcepath() {
        return compileSourcepath;
    }

    /**
     * Adds a path to sourcepath.
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
        }
        else {
            compileClasspath.append(classpath);
        }
    }

    /**
     * Gets the classpath to be used for this compilation.
     * @return the class path
     */
    public Path getClasspath() {
        return compileClasspath;
    }

    /**
     * Adds a path to the classpath.
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
     * @param r a reference to a classpath
     */
    public void setClasspathRef(Reference r) {
        createClasspath().setRefid(r);
    }

    public String createEncoding() {
        if (encoding == null) {
            encoding = System.getProperty("file.encoding");
        }
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getEncoding() {
        return encoding;
    }

    /**
     * If true, list the source files being handed off to the compiler.
     * @param list if true list the source files
     */
    public void setListfiles(boolean list) {
        listFiles = list;
    }

    /**
     * Get the listfiles flag.
     * @return the listfiles flag
     */
    public boolean getListfiles() {
        return listFiles;
    }

    /**
     * Indicates whether the build will continue
     * even if there are compilation errors; defaults to true.
     * @param fail if true halt the build on failure
     */
    public void setFailonerror(boolean fail) {
        failOnError = fail;
    }

    /**
     * @param proceed inverse of failoferror
     */
    public void setProceed(boolean proceed) {
        failOnError = !proceed;
    }

    /**
     * Gets the failonerror flag.
     * @return the failonerror flag
     */
    public boolean getFailonerror() {
        return failOnError;
    }

    /**
     * Executes the task.
     * @exception BuildException if an error occurs
     */
    public void execute() throws BuildException {
        checkParameters();
        resetFileLists();

        // scan source directories and dest directory to build up
        // compile lists
        String[] list = src.list();
        for (int i = 0; i < list.length; i++) {
            File srcDir = getProject().resolveFile(list[i]);
            if (!srcDir.exists()) {
                throw new BuildException("srcdir \"" + srcDir.getPath() + "\" does not exist!", getLocation());
            }

            DirectoryScanner ds = this.getDirectoryScanner(srcDir);
            String[] files = ds.getIncludedFiles();

            scanDir(srcDir, destDir != null ? destDir : srcDir, files);
        }

        compile();
    }

    /**
     * Clear the list of files to be compiled and copied..
     */
    protected void resetFileLists() {
        compileList = new File[0];
    }

    /**
     * Scans the directory looking for source files to be compiled.
     * The results are returned in the class variable compileList
     *
     * @param srcDir   The source directory
     * @param destDir  The destination directory
     * @param files    An array of filenames
     */
    protected void scanDir(File srcDir, File destDir, String[] files) {
        GlobPatternMapper m = new GlobPatternMapper();
        m.setFrom("*");
        m.setTo("*.html");
        SourceFileScanner sfs = new SourceFileScanner(this);
        File[] newFiles = sfs.restrictAsFiles(files, srcDir, destDir, m);

        if (newFiles.length > 0) {
            File[] newCompileList = new File[compileList.length + newFiles.length];
            System.arraycopy(compileList, 0, newCompileList, 0, compileList.length);
            System.arraycopy(newFiles, 0, newCompileList, compileList.length, newFiles.length);
            compileList = newCompileList;
        }
    }

    /**
     * Gets the list of files to be compiled.
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
            throw new BuildException(
                "destination directory \"" + destDir + "\" does not exist " + "or is not a directory",
                getLocation());
        }

        if (encoding != null && !Charset.isSupported(encoding)) {
            throw new BuildException("encoding \"\" not supported");
        }
    }

    protected void compile() {
        if (compileList.length > 0) {
            log(
                "Generating Tests "
                    + compileList.length
                    + " source file"
                    + (compileList.length == 1 ? "" : "s")
                    + (destDir != null ? " to " + destDir : ""));

            if (listFiles) {
                for (int i = 0; i < compileList.length; i++) {
                    String filename = compileList[i].getAbsolutePath();
                    log(filename);
                }
            }

            try {
                Path classpath = getClasspath();
                if (classpath != null) {
                    //@todo - is this useful?
                    //batchOfBiscuits.setClasspath(classpath.toString());
                }
                batchGenerate.setTargetDirectory(destDir);

                if (encoding != null) {
                    batchGenerate.setSourceEncoding(encoding);
                }

                batchGenerate.addSources( compileList );
                batchGenerate.compile( );
            }
            catch (Exception e) {

                StringWriter writer = new StringWriter();
                //@todo --
                e.printStackTrace();
                //new ErrorReporter( e, false ).write( new PrintWriter(writer) );
                String message = writer.toString();

                if (failOnError) {
                    throw new BuildException(message, e, getLocation());
                }
                else {
                    log(message, Project.MSG_ERR);
                }

            }
        }
    }
}
