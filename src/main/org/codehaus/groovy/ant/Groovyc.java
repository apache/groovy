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
import org.apache.commons.cli.*;
import org.apache.tools.ant.*;
import org.apache.tools.ant.listener.AnsiColorLogger;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.util.GlobPatternMapper;
import org.apache.tools.ant.util.SourceFileScanner;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.tools.ErrorReporter;
import org.codehaus.groovy.tools.StringHelper;
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Compiles Groovy source files. This task can take the following
 * arguments:
 * <ul>
 * <li>sourcedir
 * <li>destdir
 * <li>classpath
 * <li>stacktrace
 * <li>jointCompilation
 * </ul>
 * Of these arguments, the <b>sourcedir</b> and <b>destdir</b> are required.
 * <p>
 * When this task executes, it will recursively scan the sourcedir and
 * destdir looking for Groovy source files to compile. This task makes its
 * compile decision based on timestamp.
 * 
 * Based heavily on the Javac implementation in Ant
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Hein Meling
 * @version $Revision$ 
 */
public class Groovyc extends MatchingTask
{
    private final LoggingHelper log = new LoggingHelper(this);


    private Path compileClasspath;
    private Path compileSourcepath;
    private String encoding;

    protected File destDir;
    protected Path src;    
    protected CompilerConfiguration configuration = new CompilerConfiguration();
    protected boolean failOnError = true;
    protected boolean listFiles = false;
    protected File[] compileList = new File[0];
    
    private boolean jointCompilation;

    public static void main(String[] args) {

        Project project = new Project();
        DefaultLogger logger = new AnsiColorLogger();
        logger.setMessageOutputLevel(Project.MSG_INFO);
        project.addBuildListener(logger);
        // project.init();
        
        Groovyc compiler = new Groovyc();
        compiler.setProject(project);

        args = compiler.evalCompilerFlags(args);
        
        String dest = ".";
        String src = ".";
        boolean listFiles = false;
        if (args.length > 0) {
            dest = args[0];
        }
        if (args.length > 1) {
            src = args[1];
        }
        if (args.length > 2) {
            String flag = args[2];
            if (flag.equalsIgnoreCase("true")) {
                listFiles = true;
            }
        }
        
        compiler.setSrcdir(new Path(project, src));
        compiler.setDestdir(project.resolveFile(dest));
        compiler.setListfiles(listFiles);
        
        compiler.execute();
    }

    private String[] evalCompilerFlags(String[] args) {
        //
        // Parse the command line
        
        Options options = new Options();
        options.addOption(
                OptionBuilder.withArgName( "property=value" )
                .withValueSeparator('=')
                .hasArgs(2)
                .create( "J" ));
        options.addOption(
                OptionBuilder.withArgName( "property=value" )
                .hasArg()
                .create( "F" ));
        options.addOption(OptionBuilder.withLongOpt("jointCompilation").create('j'));
        
        PosixParser cliParser = new PosixParser();

        CommandLine cli;
        try {
            cli = cliParser.parse(options, args);
        } catch (ParseException e) {
            throw new BuildException(e);
        }
        
        jointCompilation = cli.hasOption('j');
        if (jointCompilation) {
            Map compilerOptions =  new HashMap();
            
            String[] opts = cli.getOptionValues("J");
            compilerOptions.put("namedValues", opts);
            
            opts = cli.getOptionValues("F");
            compilerOptions.put("flags", opts);
            
            compilerOptions.put("stubDir", createTempDir());    
            configuration.setJointCompilationOptions(compilerOptions);
        }            
        
        return cli.getArgs();
    }

    public Groovyc() {
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
     */
    public void setVerbose(boolean verbose) {
        configuration.setVerbose( verbose );
    }

    /**
     * Enable compiler to report stack trace information if a problem occurs
     * during compilation.
     */
    public void setStacktrace(boolean stacktrace) {
        configuration.setDebug(stacktrace);
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
    
    private void addToCompileList(File[] newFiles) {
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
        if (compileList.length == 0) {
            log.info("No sources to compile");
            return;
        }

        log.info("Compiling " + compileList.length +
                " source file" + (compileList.length == 1 ? "" : "s") +
                " to " + destDir);

        if (listFiles) {
            for (int i = 0; i < compileList.length; i++) {
                String filename = compileList[i].getAbsolutePath();
                log.info("    " + filename);
            }
        }

        try {
            Path classpath = getClasspath();
            if (classpath != null) {
                configuration.setClasspath(classpath.toString());
            }
            configuration.setTargetDirectory(destDir);

            if (encoding != null) {
                configuration.setSourceEncoding(encoding);
            }

            CompilationUnit unit;
            if (jointCompilation) {
                unit = new JavaAwareCompilationUnit(configuration, buildClassLoaderFor());
            } else {
                unit = new CompilationUnit(configuration, null, buildClassLoaderFor());
            }

            unit.addSources(compileList);
            unit.compile();
        }
        catch (Exception e) {

            StringWriter writer = new StringWriter();
            new ErrorReporter( e, false ).write( new PrintWriter(writer) );
            String message = writer.toString();

            if (failOnError) {
                throw new BuildException(message, e, getLocation());
            }
            else {
                log(message, Project.MSG_ERR);
            }

        }
    }
    
    protected File createTempDir()  {
        File tempFile;
        try {
            tempFile = File.createTempFile("generated-", "java-source");
            tempFile.delete();
            tempFile.mkdirs();
        } catch (IOException e) {
            throw new BuildException(e);
        }
        return tempFile;
    }

    private GroovyClassLoader buildClassLoaderFor() {
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
                if (!found)
                    antLoader.addPathElement(cpEntry);
            }
        }
        return new GroovyClassLoader(parent, configuration);
    }
    
    public void setJointCompilationOptions(String options) {
        String[] args = StringHelper.tokenizeUnquoted(options);
        evalCompilerFlags(args);
    }    
}
