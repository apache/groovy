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

import groovy.lang.GroovyClassLoader;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.util.GlobPatternMapper;
import org.apache.tools.ant.util.SourceFileScanner;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.tools.ErrorReporter;
import org.codehaus.groovy.tools.FileSystemCompiler;
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Compiles Groovy source files. This task can take the following arguments:
 * <ul>
 * <li>srcdir</li>
 * <li>destdir</li>
 * <li>classpath</li>
 * <li>encoding</li>
 * <li>verbose</li>
 * <li>failonerror</li>
 * <li>includeantruntime</li>
 * <li>includejavaruntime</li>
 * <li>memoryInitialSize</li>
 * <li>memoryMaximumSize</li>
 * <li>fork</li>
 * <li>stacktrace</li>
 * <li>stubdir</li>
 * </ul>
 * Of these arguments, the <b>srcdir</b> and <b>destdir</b> are required.
 * <p/>
 * <p>When this task executes, it will recursively scan srcdir and destdir looking for Groovy source files
 * to compile. This task makes its compile decision based on timestamp.</p>
 * <p/>
 * <p>Based heavily on the Javac implementation in Ant.</p>
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Hein Meling
 * @author <a href="mailto:russel.winder@concertant.com">Russel Winder</a>
 * @author Danno Ferrin
 * @version $Revision$
 */
public class Groovyc extends MatchingTask {
    private final LoggingHelper log = new LoggingHelper(this);

    private Path src;
    private File destDir;
    private Path compileClasspath;
    private Path compileSourcepath;
    private String encoding;
    private boolean stacktrace = false;
    private boolean verbose = false;
    private boolean includeAntRuntime = true;
    private boolean includeJavaRuntime = false;
    private boolean fork = false;
    private File forkJDK;
    private String memoryInitialSize;
    private String memoryMaximumSize;

    protected boolean failOnError = true;
    protected boolean listFiles = false;
    protected File[] compileList = new File[0];

    private String updatedProperty;
    private String errorProperty;
    private boolean taskSuccess = true; // assume the best
    private boolean includeDestClasses = true;

    protected CompilerConfiguration configuration;
    private Javac javac;
    private boolean jointCompilation;

    private List<File> temporaryFiles = new ArrayList(2);


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
     * @return the source directorys as a path
     */
    public Path getSrcdir() {
        return src;
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
     * are being compiled
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
     * If true, includes Ant's own classpath in the classpath.
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
     * If true, includes the Java runtime libraries in the classpath.
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
     * If true forks the Groovy compiler.
     *
     * @param f "true|false|on|off|yes|no"
     */
    public void setFork(boolean f) {
        fork = f;
    }

    /**
     * The JDK Home to use when forked.
     *
     * @param home the java.home value to use, default is the current JDK's home
     */
    public void setJavaHome(File home) {
        forkJDK = home;
    }

    /**
     * The property to set on compliation success.
     * This property will not be set if the compilation
     * fails, or if there are no files to compile.
     *
     * @param updatedProperty the property name to use.
     */
    public void setUpdatedProperty(String updatedProperty) {
        this.updatedProperty = updatedProperty;
    }

    /**
     * The property to set on compliation failure.
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
     *         was not neccessary, false if the compilation failed.
     */
    public boolean getTaskSuccess() {
        return taskSuccess;
    }

    /*
      public void setJointCompilationOptions(String options) {
          String[] args = StringHelper.tokenizeUnquoted(options);
          evalCompilerFlags(args);
      }
    */

    /**
     * Add the configured nested javac task if present to initiate joint compilation.
     */
    public void addConfiguredJavac(final Javac javac) {
        this.javac = javac;
        jointCompilation = true;
    }

    /**
     * Enable compiler to report stack trace information if a problem occurs
     * during compilation.
     */
    public void setStacktrace(boolean stacktrace) {
        this.stacktrace = stacktrace;
    }

    /**
     * Executes the task.
     *
     * @throws BuildException if an error occurs
     */
    public void execute() throws BuildException {
        checkParameters();
        resetFileLists();

        if (javac != null) jointCompilation = true;

        // scan source directories and dest directory to build up
        // compile lists
        String[] list = src.list();
        for (int i = 0; i < list.length; i++) {
            File file = getProject().resolveFile(list[i]);
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
     * Clear the list of files to be compiled and copied..
     */
    protected void resetFileLists() {
        compileList = new File[0];
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
        m.setFrom("*.groovy");
        m.setTo("*.class");
        SourceFileScanner sfs = new SourceFileScanner(this);
        File[] newFiles = sfs.restrictAsFiles(files, srcDir, destDir, m);
        addToCompileList(newFiles);

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

    protected void compile() {
        try {
            if (compileList.length > 0) {
                log("Compiling " + compileList.length + " source file"
                        + (compileList.length == 1 ? "" : "s")
                        + (destDir != null ? " to " + destDir : ""));

                if (listFiles) {
                    for (int i = 0; i < compileList.length; ++i) {
                        String filename = compileList[i].getAbsolutePath();
                        log(filename);
                    }
                }

                Path classpath = getClasspath() != null ? getClasspath() : new Path(getProject());
                // extract joint options, some get pushed up...
                List jointOptions = new ArrayList();
                if (jointCompilation) {
                    for (Iterator i = javac.getRuntimeConfigurableWrapper().getAttributeMap().entrySet().iterator(); i.hasNext();) {
                        final Map.Entry e = (Map.Entry) i.next();
                        final String key = e.getKey().toString();
                        final String value = e.getValue().toString();
                        if (key.indexOf("debug") != -1) {
                            String level = "";
                            if (javac.getDebugLevel() != null) {
                                level = ":" + javac.getDebugLevel();
                            }
                            jointOptions.add("-Fg" + level);
                        } else if (key.indexOf("debugLevel") != -1) {
                            // ignore, taken care of in debug
                        } else if (((key.indexOf("nowarn") != -1)
                                || (key.indexOf("verbose") != -1)
                                || (key.indexOf("deprecation") != -1)
                        ) && ("on".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase("value"))
                                ) {
                            jointOptions.add("-F" + key);
                        } else if (key.indexOf("classpath") != -1) {
                            classpath.add(javac.getClasspath());
                        } else if ((key.indexOf("depend") != -1)
                                || (key.indexOf("extdirs") != -1)
                                || (key.indexOf("encoding") != -1)
                                || (key.indexOf("source") != -1)
                                || (key.indexOf("target") != -1)
                                || (key.indexOf("verbose") != -1)
                                || (key.indexOf("depend") != -1)) {
                            jointOptions.add("-J" + key + "=" + value);
                        } else {
                            log("The option " + key + " cannot be set on the contained <javac> element. The option will be ignored", Project.MSG_WARN);
                        }
                        // includes? excludes?
                    }
                }


                String separator = System.getProperty("file.separator");
                ArrayList commandLineList = new ArrayList();

                if (fork) {
                    String javaHome;
                    if (forkJDK != null) {
                        javaHome = forkJDK.getPath();
                    } else {
                        javaHome = System.getProperty("java.home");
                    }
                    if (includeAntRuntime) {
                        classpath.addExisting((new Path(getProject())).concatSystemClasspath("last"));
                    }
                    if (includeJavaRuntime) {
                        classpath.addJavaRuntime();
                    }

                    commandLineList.add(javaHome + separator + "bin" + separator + "java");
                    commandLineList.add("-classpath");
                    commandLineList.add(classpath.toString());
                    if ((memoryInitialSize != null) && !memoryInitialSize.equals("")) {
                        commandLineList.add("-Xms" + memoryInitialSize);
                    }
                    if ((memoryMaximumSize != null) && !memoryMaximumSize.equals("")) {
                        commandLineList.add("-Xmx" + memoryMaximumSize);
                    }
                    commandLineList.add("org.codehaus.groovy.tools.FileSystemCompiler");
                }
                commandLineList.add("--classpath");
                commandLineList.add(classpath.toString());
                if (jointCompilation) {
                    commandLineList.add("-j");
                    commandLineList.addAll(jointOptions);
                }
                commandLineList.add("-d");
                commandLineList.add(destDir.getPath());
                if (encoding != null) {
                    commandLineList.add("--encoding");
                    commandLineList.add(encoding);
                }
                if (stacktrace) {
                    commandLineList.add("-e");
                }

                // check to see if an external file is needed
                int count = 0;
                if (fork) {

                    for (int i = 0; i < compileList.length; i++) {
                        count += compileList[i].getPath().length();
                    }
                    for (Iterator iter = commandLineList.iterator(); iter.hasNext();) {
                        count += iter.next().toString().length();
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
                        for (int i = 0; i < compileList.length; i++) {
                            pw.println(compileList[i].getPath());
                        }
                        pw.close();
                        commandLineList.add("@" + tempFile.getPath());
                    } catch (IOException e) {
                        log("Error createing file list", e, Project.MSG_ERR);
                    }
                } else {
                    for (int i = 0; i < compileList.length; i++) {
                        commandLineList.add(compileList[i].getPath());
                    }
                }
                final String[] commandLine = new String[commandLineList.size()];
                for (int i = 0; i < commandLine.length; ++i) {
                    commandLine[i] = (String) commandLineList.get(i);
                }
                if (fork) {
                    // use the main method in FileSystemCompiler
                    final Execute executor = new Execute(); // new LogStreamHandler ( attributes , Project.MSG_INFO , Project.MSG_WARN ) ) ;
                    executor.setAntRun(getProject());
                    executor.setWorkingDirectory(getProject().getBaseDir());
                    executor.setCommandline(commandLine);
                    try {
                        executor.execute();
                    }
                    catch (final IOException ioe) {
                        throw new BuildException("Error running forked groovyc.", ioe);
                    }
                    final int returnCode = executor.getExitValue();
                    if (returnCode != 0) {

                        if (failOnError) {
                            throw new BuildException("Forked groovyc returned error code: " + returnCode);
                        } else {
                            log("Forked groovyc returned error code: " + returnCode, Project.MSG_ERR);
                        }
                    }
                } else {
                    // hand crank it so we can add our own compiler configuration
                    try {
                        Options options = FileSystemCompiler.createCompilationOptions();

                        PosixParser cliParser = new PosixParser();

                        CommandLine cli;
                        cli = cliParser.parse(options, commandLine);

                        configuration = FileSystemCompiler.generateCompilerConfigurationFromOptions(cli);

                        //
                        // Load the file name list
                        String[] filenames = FileSystemCompiler.generateFileNamesFromOptions(cli);
                        boolean fileNameErrors = filenames == null;

                        fileNameErrors = fileNameErrors && !FileSystemCompiler.validateFiles(filenames);

                        if (!fileNameErrors) {
                            FileSystemCompiler.doCompilation(configuration, makeCompileUnit(), filenames);
                        }

                    } catch (Exception re) {
                        Throwable t = re;
                        if ((re.getClass() == RuntimeException.class) && (re.getCause() != null)) {
                            // unwrap to the real exception
                            t = re.getCause();
                        }
                        StringWriter writer = new StringWriter();
                        new ErrorReporter(t, false).write(new PrintWriter(writer));
                        String message = writer.toString();

                        if (failOnError) {
                            log(message, Project.MSG_INFO);
                            throw new BuildException("Compilation Failed", t, getLocation());
                        } else {
                            log(message, Project.MSG_ERR);
                        }
                    }
                }
            }
        } finally {
            Iterator<File> files = temporaryFiles.iterator();
            while (files.hasNext()) {
                File tmpFile = files.next();
                try {
                    FileSystemCompiler.deleteRecursive(tmpFile);
                } catch (Throwable t) {
                    System.err.println("error: could not delete temp files - " + tmpFile.getPath());
                }
            }
        }
    }

    protected CompilationUnit makeCompileUnit() {
        if (configuration.getJointCompilationOptions() != null) {
            if (!configuration.getJointCompilationOptions().containsKey("stubDir")) {
                try {
                    File tempStubDir = FileSystemCompiler.createTempDir();
                    temporaryFiles.add(tempStubDir);
                    configuration.getJointCompilationOptions().put("stubDir", tempStubDir);
                } catch (IOException ioe) {
                    throw new BuildException(ioe);
                }
            }
            return new JavaAwareCompilationUnit(configuration, buildClassLoaderFor());
        } else {
            return new CompilationUnit(configuration, null, buildClassLoaderFor());
        }
    }


    protected GroovyClassLoader buildClassLoaderFor() {
        ClassLoader parent = this.getClass().getClassLoader();
        if (parent instanceof AntClassLoader) {
            AntClassLoader antLoader = (AntClassLoader) parent;
            String[] pathElm = antLoader.getClasspath().split(File.pathSeparator);
            List classpath = configuration.getClasspath();
            /*
             * Iterate over the classpath provided to groovyc, and add any missing path
             * entries to the AntClassLoader.  This is a workaround, since for some reason
             * 'directory' classpath entries were not added to the AntClassLoader' classpath.
             */
            for (Iterator iter = classpath.iterator(); iter.hasNext();) {
                String cpEntry = (String) iter.next();
                boolean found = false;
                for (int i = 0; i < pathElm.length; i++) {
                    if (cpEntry.equals(pathElm[i])) {
                        found = true;
                        break;
                    }
                }
                /*
                 * fix for GROOVY-2284
                 * seems like AntClassLoader doesn't check if the file
                 * may not exist in the classpath yet
                 */
                if (!found && new File(cpEntry).exists())
                    antLoader.addPathElement(cpEntry);
            }
        }
        return new GroovyClassLoader(parent, configuration);
    }

    /**
     * Set the stub directory into which the Java source stub
     * files should be generated. The directory should exist 
     * will not be deleted automatically.
     *
     * @param stubDir the stub directory
     */
    public void setStubdir(File stubDir) {
        jointCompilation = true;
        configuration.getJointCompilationOptions().put("stubDir", stubDir);
    }

    /**
     * Gets the stub directory into which the Java source stub
     * files should be generated
     *
     * @return the stub directory
     */
    public File getStubdir() {
        return (File) configuration.getJointCompilationOptions().get("stubDir");
    }
}
