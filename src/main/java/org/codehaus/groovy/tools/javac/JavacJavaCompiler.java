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

import groovy.lang.GroovyObject;
import org.apache.groovy.io.StringBuilderWriter;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.messages.ExceptionMessage;
import org.codehaus.groovy.control.messages.SimpleMessage;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class JavacJavaCompiler implements JavaCompiler {

    private final CompilerConfiguration config;

    public JavacJavaCompiler(final CompilerConfiguration config) {
        this.config = Objects.requireNonNull(config);
    }

    @Override
    public void compile(final List<String> files, final CompilationUnit cu) {
        List<String> javacParameters = makeParameters(cu.getClassLoader());
        var javacOutput = new StringBuilderWriter();
        int javacReturnValue = 0;
        try {
            boolean successful = doCompileWithSystemJavaCompiler(cu, files, javacParameters, javacOutput);
            if (!successful) {
                javacReturnValue = 1;
            }
        } catch (IllegalArgumentException e) {
            javacReturnValue = 2; // invalid options
            cu.getErrorCollector().addFatalError(new ExceptionMessage(e, true, cu));
        } catch (IOException e) {
            javacReturnValue = 1;
            cu.getErrorCollector().addFatalError(new ExceptionMessage(e, true, cu));
        } catch (Exception e) {
            cu.getErrorCollector().addFatalError(new ExceptionMessage(e, true, cu));
        }

        if (javacReturnValue != 0) {
            addJavacError(switch (javacReturnValue) {
                case  1 -> "Compile error during compilation with javac.";
                case  2 -> "Invalid commandline usage for javac.";
                default -> "unexpected return value by javac.";
            }, javacOutput.toString(), cu);
        } else {
            System.out.print(javacOutput); // print errors/warnings
        }
    }

    private boolean doCompileWithSystemJavaCompiler(final CompilationUnit cu, final List<String> files, final List<String> javacParameters, final Writer javacOutput) throws IOException {
        Locale locale = Locale.ENGLISH;
        Charset charset = Charset.forName(config.getSourceEncoding());
        javax.tools.JavaCompiler compiler = javax.tools.ToolProvider.getSystemJavaCompiler();
        try (javax.tools.StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, locale, charset)) {
            Set<javax.tools.JavaFileObject> compilationUnitSet = cu.getJavaCompilationUnitSet(); // java stubs already added

            Map<String, Object> options = config.getJointCompilationOptions();
            if (!Boolean.TRUE.equals(options.get(CompilerConfiguration.MEM_STUB))) {
                // clear the java stubs in the source set of Java compilation
                compilationUnitSet = new HashSet<>();

                // use sourcepath to specify the root directory of java stubs
                if (options.get("stubDir") instanceof File stubDir) {
                    javacParameters.add("-sourcepath");
                    javacParameters.add(stubDir.getAbsolutePath());
                } else {
                    throw new GroovyBugError("stubDir not specified");
                }
            }

            // add java source files to compile
            fileManager.getJavaFileObjectsFromFiles(
                    files.stream().map(File::new).toList()
            ).forEach(compilationUnitSet::add);

            javax.tools.JavaCompiler.CompilationTask compilationTask = compiler.getTask(
                    javacOutput,
                    fileManager,
                    null,
                    javacParameters,
                    null,
                    compilationUnitSet
            );
            compilationTask.setLocale(locale);

            return compilationTask.call();
        }
    }

    private static void addJavacError(final String head, final String text, final CompilationUnit unit) {
        String message;
        if (text != null && !text.trim().isEmpty()) {
            message = head + "\n" + text;
        } else {
            message = head +
                    "\nThis javac version does not support compile(String[],PrintWriter), " +
                    "so no further details of the error are available. The message error text " +
                    "should be found on System.err.\n";
        }
        unit.getErrorCollector().addFatalError(new SimpleMessage(message, unit));
    }

    private List<String> makeParameters(final ClassLoader classLoader) {
        List<String> params = new ArrayList<>();

        File targetDir = config.getTargetDirectory();
        if (targetDir == null) targetDir = new File(".");

        params.add("-d");
        params.add(targetDir.getAbsolutePath());

        Map<String, Object> options = config.getJointCompilationOptions();
        boolean classpath = false;

        if (options.get("flags") instanceof String[] flags) {
            for (String flag : flags) {
                if (flag == null) continue;

                params.add("-" + flag);
            }
        }

        if (options.get("namedValues") instanceof String[] namedValues) {
            for (int i = 0, n = namedValues.length; i < n; i += 2) {
                var name = namedValues[i];
                if (name == null) continue;
                if (!classpath) classpath = isClasspathParameter(name);

                params.add("-" + name);
                params.add(namedValues[i + 1]);
            }
        }

        // append classpath if not already defined
        if (!classpath) {
            // add class paths of compilation unit
            List<String> paths = new ArrayList<>(config.getClasspath());
            for (ClassLoader loader = classLoader; loader != null; loader = loader.getParent()) {
                if (loader instanceof URLClassLoader ucl) {
                    for (URL u : ucl.getURLs()) {
                        try {
                            paths.add(new File(u.toURI()).getPath());
                        } catch (URISyntaxException ignore) {
                        }
                    }
                }
            }

            try {
                var codeSource = getCodeSource();
                if (codeSource != null) { // add class path of groovy runtime
                    paths.add(new File(codeSource.getLocation().toURI()).getPath());
                }
            } catch (URISyntaxException ignore) {
            }

            params.add("-classpath");
            params.add(String.join(File.pathSeparator, paths));
        }

        return params;
    }

    private static boolean isClasspathParameter(final String param) {
        return param.equals("cp") || param.equals("classpath") || param.equals("-class-path");
    }

    @SuppressWarnings("removal") // TODO a future Groovy version should perform the operation not as a privileged action
    private static java.security.CodeSource getCodeSource() {
        return java.security.AccessController.doPrivileged((java.security.PrivilegedAction<java.security.CodeSource>) () ->
            GroovyObject.class.getProtectionDomain().getCodeSource()
        );
    }
}
