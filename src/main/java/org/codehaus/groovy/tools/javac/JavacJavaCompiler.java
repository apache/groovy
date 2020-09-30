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
package org.codehaus.groovy.tools.javac;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.apache.groovy.io.StringBuilderWriter;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.messages.ExceptionMessage;
import org.codehaus.groovy.control.messages.SimpleMessage;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class JavacJavaCompiler implements JavaCompiler {

    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
    private final CompilerConfiguration config;
    private final Charset charset;

    public JavacJavaCompiler(CompilerConfiguration config) {
        this.config = config;
        this.charset = Charset.forName(config.getSourceEncoding());
    }

    @Override
    public void compile(List<String> files, CompilationUnit cu) {
        List<String> javacParameters = makeParameters(cu.getClassLoader());
        StringBuilderWriter javacOutput = new StringBuilderWriter();
        int javacReturnValue = 0;
        try {
            try {
                boolean successful = doCompileWithSystemJavaCompiler(cu, files, javacParameters, javacOutput);
                if (!successful) {
                    javacReturnValue = 1;
                }
            } catch (IllegalArgumentException e) {
                javacReturnValue = 2; // any of the options are invalid
                cu.getErrorCollector().addFatalError(new ExceptionMessage(e, true, cu));
            } catch (IOException e) {
                javacReturnValue = 1;
                cu.getErrorCollector().addFatalError(new ExceptionMessage(e, true, cu));
            }
        } catch (Exception e) {
            cu.getErrorCollector().addFatalError(new ExceptionMessage(e, true, cu));
        }

        if (javacReturnValue != 0) {
            switch (javacReturnValue) {
                case 1: addJavacError("Compile error during compilation with javac.", cu, javacOutput); break;
                case 2: addJavacError("Invalid commandline usage for javac.", cu, javacOutput); break;
                default: addJavacError("unexpected return value by javac.", cu, javacOutput); break;
            }
        } else {
            // print warnings if any
            System.out.print(javacOutput);
        }
    }

    private boolean doCompileWithSystemJavaCompiler(CompilationUnit cu, List<String> files, List<String> javacParameters, StringBuilderWriter javacOutput) throws IOException {
        javax.tools.JavaCompiler compiler = javax.tools.ToolProvider.getSystemJavaCompiler();
        try (javax.tools.StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, DEFAULT_LOCALE, charset)) {
            Set<javax.tools.JavaFileObject> compilationUnitSet = cu.getJavaCompilationUnitSet(); // java stubs already added

            Map<String, Object> options = this.config.getJointCompilationOptions();
            if (!Boolean.parseBoolean(options.get(CompilerConfiguration.MEM_STUB).toString())) {
                // clear the java stubs in the source set of Java compilation
                compilationUnitSet = new HashSet<>();

                // use sourcepath to specify the root directory of java stubs
                javacParameters.add("-sourcepath");
                final File stubDir = (File) options.get("stubDir");
                if (null == stubDir) {
                    throw new GroovyBugError("stubDir is not specified");
                }
                javacParameters.add(stubDir.getAbsolutePath());
            }

            // add java source files to compile
            fileManager.getJavaFileObjectsFromFiles(
                    files.stream().map(File::new).collect(Collectors.toList())
            ).forEach(compilationUnitSet::add);

            javax.tools.JavaCompiler.CompilationTask compilationTask = compiler.getTask(
                    javacOutput,
                    fileManager,
                    null,
                    javacParameters,
                    Collections.emptyList(),
                    compilationUnitSet
            );
            compilationTask.setLocale(DEFAULT_LOCALE);

            return compilationTask.call();
        }
    }

    private static void addJavacError(String header, CompilationUnit cu, StringBuilderWriter msg) {
        if (msg != null) {
            header = header + "\n" + msg.getBuilder().toString();
        } else {
            header = header +
                    "\nThis javac version does not support compile(String[],PrintWriter), " +
                    "so no further details of the error are available. The message error text " +
                    "should be found on System.err.\n";
        }
        cu.getErrorCollector().addFatalError(new SimpleMessage(header, cu));
    }

    private List<String> makeParameters(GroovyClassLoader parentClassLoader) {
        Map<String, Object> options = config.getJointCompilationOptions();
        List<String> params = new ArrayList<>();

        File target = config.getTargetDirectory();
        if (target == null) target = new File(".");

        params.add("-d");
        params.add(target.getAbsolutePath());

        String[] flags = (String[]) options.get("flags");
        if (flags != null) {
            for (String flag : flags) {
                params.add("-" + flag);
            }
        }

        boolean hadClasspath = false;
        String[] namedValues = (String[]) options.get("namedValues");
        if (namedValues != null) {
            for (int i = 0, n = namedValues.length; i < n; i += 2) {
                String name = namedValues[i];
                if (name.equals("classpath")) hadClasspath = true;
                params.add("-" + name);
                params.add(namedValues[i + 1]);
            }
        }

        // append classpath if not already defined
        if (!hadClasspath) {
            // add all classpaths that compilation unit sees
            List<String> paths = new ArrayList<>(config.getClasspath());
            ClassLoader loader = parentClassLoader;
            while (loader != null) {
                if (loader instanceof URLClassLoader) {
                    for (URL u : ((URLClassLoader) loader).getURLs()) {
                        try {
                            paths.add(new File(u.toURI()).getPath());
                        } catch (URISyntaxException ignore) {
                        }
                    }
                }
                loader = loader.getParent();
            }

            try {
                CodeSource codeSource = AccessController.doPrivileged(
                        (PrivilegedAction<CodeSource>) () -> GroovyObject.class.getProtectionDomain().getCodeSource());
                if (codeSource != null) {
                    paths.add(new File(codeSource.getLocation().toURI()).getPath());
                }
            } catch (URISyntaxException ignore) {
            }

            params.add("-classpath");
            params.add(DefaultGroovyMethods.join((Iterable<String>) paths, File.pathSeparator));
        }

        return params;
    }
}
