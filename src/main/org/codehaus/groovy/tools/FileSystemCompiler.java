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

package org.codehaus.groovy.tools;

import org.apache.commons.cli.*;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ConfigurationException;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Command-line compiler (aka. <tt>groovyc</tt>).
 *
 * @version $Id$
 */
public class FileSystemCompiler {
    private final CompilationUnit unit;

    public FileSystemCompiler(CompilerConfiguration configuration) throws ConfigurationException {
        this(configuration, null);
    }

    public FileSystemCompiler(CompilerConfiguration configuration, CompilationUnit cu) throws ConfigurationException {
        if (cu != null) {
            unit = cu;
        } else if (configuration.getJointCompilationOptions() != null) {
            this.unit = new JavaAwareCompilationUnit(configuration);
        } else {
            this.unit = new CompilationUnit(configuration);
        }
    }


    public void compile(String[] paths) throws Exception {
        unit.addSources(paths);
        unit.compile();
    }


    public void compile(File[] files) throws Exception {
        unit.addSources(files);
        unit.compile();
    }


    public static void displayHelp(final Options options) {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(80, "groovyc [options] <source-files>", "options:", options, "");
    }

    public static void displayVersion() {
        String version = InvokerHelper.getVersion();
        System.err.println("Groovy compiler version " + version);
        System.err.println("Copyright 2003-2008 The Codehaus. http://groovy.codehaus.org/");
        System.err.println("");
    }

    public static int checkFiles(String[] filenames) {
        int errors = 0;

        for (int i = 0; i < filenames.length; ++i) {
            File file = new File(filenames[i]);

            if (!file.exists()) {
                System.err.println("error: file not found: " + file);
                ++errors;
            } else if (!file.canRead()) {
                System.err.println("error: file not readable: " + file);
                ++errors;
            }
        }

        return errors;
    }

    public static boolean validateFiles(String[] filenames) {
        return checkFiles(filenames) == 0;
    }

    /**
     * Primary entry point for compiling from the command line
     * (using the groovyc script).
     */
    public static void main(String[] args) {
        boolean displayStackTraceOnError = false;

        try {
            Options options = createCompilationOptions();

            PosixParser cliParser = new PosixParser();

            CommandLine cli;
            cli = cliParser.parse(options, args);

            if (cli.hasOption('h')) {
                displayHelp(options);
                return;
            }

            if (cli.hasOption('v')) {
                displayVersion();
                return;
            }

            displayStackTraceOnError = cli.hasOption('e');

            CompilerConfiguration configuration = generateCompilerConfigurationFromOptions(cli);

            //
            // Load the file name list
            String[] filenames = generateFileNamesFromOptions(cli);
            boolean fileNameErrors = filenames == null;
            if (!fileNameErrors && (filenames.length == 0)) {
                displayHelp(options);
                return;
            }

            fileNameErrors = fileNameErrors && !validateFiles(filenames);

            if (!fileNameErrors) {
                doCompilation(configuration, null, filenames);
            }
        } catch( Throwable e ) {
            RuntimeException re = new RuntimeException();
            if (re.getStackTrace().length > 1) {
                if (e instanceof RuntimeException) {
                    re = (RuntimeException) e;
                } else {
                    re.initCause(e);
                }
            }
        }
    }

    public static void doCompilation(CompilerConfiguration configuration, CompilationUnit unit, String[] filenames) throws Exception {
        File tmpDir = null;
        // if there are any joint compilation options set stub dir if not set
        if ((configuration.getJointCompilationOptions() != null)
            && !configuration.getJointCompilationOptions().containsKey("stubDir"))  
        {
            tmpDir = createTempDir();
            configuration.getJointCompilationOptions().put("stubDir", tmpDir);
        }
        FileSystemCompiler compiler = new FileSystemCompiler(configuration, unit);
        compiler.compile(filenames);
        if (tmpDir != null) deleteRecursive(tmpDir);
    }

    public static String[] generateFileNamesFromOptions(CommandLine cli) {
        String[] filenames = cli.getArgs();
        List fileList = new ArrayList(filenames.length);
        boolean errors = false;
        for (int i = 0; i < filenames.length; i++) {
            if (filenames[i].startsWith("@")) {
                try {
                    BufferedReader br = new BufferedReader(new FileReader(filenames[i].substring(1)));
                    String file;
                    while ((file = br.readLine()) != null) {
                        fileList.add(file);
                    }
                } catch (IOException ioe) {
                    System.err.println("error: file not readable: " + filenames[i].substring(1));
                    errors = true;
                }
            } else {
                fileList.addAll(Arrays.asList(filenames));
            }
        }
        if (errors) {
            return null;
        } else {
            return (String[]) fileList.toArray(new String[fileList.size()]);
        }
    }

    public static CompilerConfiguration generateCompilerConfigurationFromOptions(CommandLine cli) {
        //
        // Setup the configuration data

        CompilerConfiguration configuration = new CompilerConfiguration();

        if (cli.hasOption("classpath")) {
            configuration.setClasspath(cli.getOptionValue("classpath"));
        }

        if (cli.hasOption('d')) {
            configuration.setTargetDirectory(cli.getOptionValue('d'));
        }

        if (cli.hasOption("encoding")) {
            configuration.setSourceEncoding(cli.getOptionValue("encoding"));
        }

        // joint compilation parameters
        if (cli.hasOption('j')) {
            Map compilerOptions = new HashMap();

            String[] opts = cli.getOptionValues("J");
            compilerOptions.put("namedValues", opts);

            opts = cli.getOptionValues("F");
            compilerOptions.put("flags", opts);

            configuration.setJointCompilationOptions(compilerOptions);
        }
        return configuration;
    }

    @SuppressWarnings({"AccessStaticViaInstance"})
    public static Options createCompilationOptions() {
        //
        // Parse the command line

        Options options = new Options();

        options.addOption(OptionBuilder.withLongOpt("classpath").hasArg().withArgName("path").withDescription("Specify where to find the class files.").create());
        options.addOption(OptionBuilder.withLongOpt("sourcepath").hasArg().withArgName("path").withDescription("Specify where to find the source files.").create());
        options.addOption(OptionBuilder.withLongOpt("temp").hasArg().withArgName("temp").withDescription("").create());
        options.addOption(OptionBuilder.withLongOpt("encoding").hasArg().withArgName("encoding").withDescription("Specify the encoding of the user class files.").create());
        options.addOption(OptionBuilder.hasArg().withDescription("Specify where to place generated class files.").create('d'));
//            options.addOption(OptionBuilder.withLongOpt("strict").withDescription("Turn on strict type safety.").create('s'));
        options.addOption(OptionBuilder.withLongOpt("help").withDescription("Print a synopsis of standard options.").create('h'));
        options.addOption(OptionBuilder.withLongOpt("version").withDescription("Print the version.").create('v'));
        options.addOption(OptionBuilder.withLongOpt("exception").withDescription("Print stack trace on error.").create('e'));
        options.addOption(OptionBuilder.withLongOpt("jointCompilation").withDescription("Attach javac compiler to compile .java files.").create('j'));

        options.addOption(
                OptionBuilder.withArgName("property=value")
                        .withValueSeparator()
                        .hasArgs(2)
                        .withDescription("")
                        .create("J"));
        options.addOption(
                OptionBuilder.withArgName("flag")
                        .hasArg()
                        .withDescription("")
                        .create("F"));
        return options;
    }

    public static File createTempDir() throws IOException {
        File tempFile = File.createTempFile("groovy-generated-", "-java-source");
        tempFile.delete();
        tempFile.mkdirs();
        return tempFile;
    }

    public static void deleteRecursive(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isFile()) {
            file.delete();
            System.out.println("Deleting " + file.getPath());
        } else if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteRecursive(files[i]);
            }
            file.delete();
            System.out.println("Deleting as dir " + file.getPath());
        }
    }
}
