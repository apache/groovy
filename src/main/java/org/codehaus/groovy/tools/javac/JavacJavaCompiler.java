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
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.messages.ExceptionMessage;
import org.codehaus.groovy.control.messages.SimpleMessage;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class JavacJavaCompiler implements JavaCompiler {
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private final CompilerConfiguration config;
    private final boolean memStubEnabled;
    private final Charset charset;

    public JavacJavaCompiler(CompilerConfiguration config) {
        this.config = config;
        this.memStubEnabled = config.isMemStubEnabled();
        this.charset = Charset.forName(config.getSourceEncoding());
    }

    private int doCompileWithJavac(CompilationUnit cu, String[] javacParameters, StringBuilderWriter javacOutput) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        int javacReturnValue = 0;

        Class javac = findJavac(cu);
        Method method = null;
        try {
            method = javac.getMethod("compile", String[].class, PrintWriter.class);
            PrintWriter writer = new PrintWriter(javacOutput);
            Object ret = method.invoke(null, javacParameters, writer);
            javacReturnValue = (Integer) ret;
        } catch (NoSuchMethodException e) { }
        if (method == null) {
            method = javac.getMethod("compile", String[].class);
            Object ret = method.invoke(null, new Object[]{javacParameters});
            javacReturnValue = (Integer) ret;
        }

        return javacReturnValue;
    }

    private boolean doCompileWithSystemJavaCompiler(CompilationUnit cu, List<String> files, String[] javacParameters, StringBuilderWriter javacOutput) throws IOException {
        javax.tools.JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, charset)) {
            final Set<JavaFileObject> compilationUnitSet = cu.getJavaCompilationUnitSet(); // java stubs already added

            // add java source files to compile
            fileManager.getJavaFileObjectsFromFiles(
                    files.stream()
                            .map(File::new)
                            .collect(Collectors.toList())
            ).forEach(compilationUnitSet::add);

            javax.tools.JavaCompiler.CompilationTask compilationTask = compiler.getTask(
                    javacOutput,
                    fileManager,
                    null,
                    Arrays.asList(javacParameters),
                    Collections.emptyList(),
                    compilationUnitSet
            );

            return compilationTask.call();
        }
    }

    public void compile(List<String> files, CompilationUnit cu) {
        compile(files, cu, memStubEnabled);
    }

    private void compile(List<String> files, CompilationUnit cu, boolean toCompileStubInMem) {
        String[] javacParameters = makeParameters(files, cu.getClassLoader(), toCompileStubInMem);
        StringBuilderWriter javacOutput = new StringBuilderWriter();
        int javacReturnValue = 0;
        try {
            if (toCompileStubInMem) {
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
            } else {
                try {
                    javacReturnValue = doCompileWithJavac(cu, javacParameters, javacOutput);
                } catch (InvocationTargetException ite) {
                    cu.getErrorCollector().addFatalError(new ExceptionMessage((Exception) ite.getCause(), true, cu));
                }
            }
        } catch (Exception e) {
            cu.getErrorCollector().addFatalError(new ExceptionMessage(e, true, cu));
        }

        if (javacReturnValue != 0) {
            switch (javacReturnValue) {
                case 1: addJavacError("Compile error during compilation with javac.", cu, javacOutput); break;
                case 2: addJavacError("Invalid commandline usage for javac.", cu, javacOutput); break;
                case 3: addJavacError("System error during compilation with javac.", cu, javacOutput); break;
                case 4: addJavacError("Abnormal termination of javac.", cu, javacOutput); break;
                default: addJavacError("unexpected return value by javac.", cu, javacOutput); break;
            }
        } else {
            // print warnings if any
            System.out.print(javacOutput);
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

    private String[] makeParameters(List<String> files, GroovyClassLoader parentClassLoader, boolean toCompileStubInMem) {
        Map options = config.getJointCompilationOptions();
        LinkedList<String> paras = new LinkedList<String>();

        File target = config.getTargetDirectory();
        if (target == null) target = new File(".");

        // defaults
        paras.add("-d");
        paras.add(target.getAbsolutePath());

        if (!toCompileStubInMem) {
            paras.add("-sourcepath");
            paras.add(((File) options.get("stubDir")).getAbsolutePath());
        }

        // add flags
        String[] flags = (String[]) options.get("flags");
        if (flags != null) {
            for (String flag : flags) {
                paras.add('-' + flag);
            }
        }

        boolean hadClasspath = false;
        // add namedValues
        String[] namedValues = (String[]) options.get("namedValues");
        if (namedValues != null) {
            for (int i = 0; i < namedValues.length; i += 2) {
                String name = namedValues[i];
                if (name.equals("classpath")) hadClasspath = true;
                paras.add('-' + name);
                paras.add(namedValues[i + 1]);
            }
        }

        // append classpath if not already defined
        if (!hadClasspath) {
            // add all classpaths that compilation unit sees
            List<String> paths = new ArrayList<String>(config.getClasspath());
            ClassLoader cl = parentClassLoader;
            while (cl != null) {
                if (cl instanceof URLClassLoader) {
                    for (URL u : ((URLClassLoader) cl).getURLs()) {
                        try {
                            paths.add(new File(u.toURI()).getPath());
                        } catch (URISyntaxException e) {
                            // ignore it
                        }
                    }
                }
                cl = cl.getParent();
            }

            try {
                CodeSource codeSource =AccessController.doPrivileged(new PrivilegedAction<CodeSource>() {
                    @Override
                    public CodeSource run() {
                        return GroovyObject.class.getProtectionDomain().getCodeSource();
                    }
                });
                if (codeSource != null) {
                    paths.add(new File(codeSource.getLocation().toURI()).getPath());
                }
            } catch (URISyntaxException e) {
                // ignore it
            }

            paras.add("-classpath");
            paras.add(DefaultGroovyMethods.join((Iterable) paths, File.pathSeparator));
        }

        if (!toCompileStubInMem) {
            // files to compile
            paras.addAll(files);
        }

        return paras.toArray(EMPTY_STRING_ARRAY);
    }

    private Class findJavac(CompilationUnit cu) throws ClassNotFoundException {
        String main = "com.sun.tools.javac.Main";
        try {
            return Class.forName(main);
        } catch (ClassNotFoundException e) {}

        try {
            ClassLoader cl = this.getClass().getClassLoader();
            return cl.loadClass(main);
        } catch (ClassNotFoundException e) {}

        try {
            return ClassLoader.getSystemClassLoader().loadClass(main);
        } catch (ClassNotFoundException e) {}

        try {
            return cu.getClassLoader().getParent().loadClass(main);
        } catch (ClassNotFoundException e3) {}


        // couldn't find compiler - try to find tools.jar
        // based on java.home setting
        String javaHome = System.getProperty("java.home");
        if (javaHome.toLowerCase(Locale.US).endsWith("jre")) {
            javaHome = javaHome.substring(0, javaHome.length() - 4);
        }
        File toolsJar = new File((javaHome + "/lib/tools.jar"));
        if (toolsJar.exists()) {
            GroovyClassLoader loader = cu.getClassLoader();
            loader.addClasspath(toolsJar.getAbsolutePath());
            return loader.loadClass(main);
        }

        throw new ClassNotFoundException("unable to locate the java compiler com.sun.tools.javac.Main, please change your classloader settings");
    }
}
