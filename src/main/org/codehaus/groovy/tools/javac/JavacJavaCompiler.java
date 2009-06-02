/*
 * Copyright 2003-2009 the original author or authors.
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

package org.codehaus.groovy.tools.javac;

import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.net.URLClassLoader;
import java.net.URL;
import java.net.URISyntaxException;

import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.messages.ExceptionMessage;
import org.codehaus.groovy.control.messages.SimpleMessage;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

public class JavacJavaCompiler implements JavaCompiler {
    private CompilerConfiguration config;

    public JavacJavaCompiler(CompilerConfiguration config) {
        this.config = config;
    }

    public void compile(List<String> files, CompilationUnit cu) {
        String[] javacParameters = makeParameters(files, cu.getClassLoader());
        StringWriter javacOutput = null;
        int javacReturnValue = 0;
        try {
            Class javac = findJavac(cu);
            Method method = null;
            try {
                method = javac.getMethod("compile", new Class[]{String[].class, PrintWriter.class});
                javacOutput = new StringWriter();
                PrintWriter writer = new PrintWriter(javacOutput);
                Object ret = method.invoke(null, javacParameters, writer);
                javacReturnValue = (Integer) ret;
            } catch (NoSuchMethodException e) { }
            if (method == null) {
                method = javac.getMethod("compile", new Class[]{String[].class});
                Object ret = method.invoke(null, new Object[]{javacParameters});
                javacReturnValue = (Integer) ret;
            }
            cu.getConfiguration().getOutput();
        } catch (InvocationTargetException ite) {
            cu.getErrorCollector().addFatalError(new ExceptionMessage((Exception) ite.getCause(), true, cu));
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

    private void addJavacError(String header, CompilationUnit cu, StringWriter msg) {
        if (msg != null) {
            header = header + "\n" + msg.getBuffer().toString();
        } else {
            header = header +
                    "\nThis javac version does not support compile(String[],PrintWriter), " +
                    "so no further details of the error are available. The message error text " +
                    "should be found on System.err.\n";
        }
        cu.getErrorCollector().addFatalError(new SimpleMessage(header, cu));
    }

    private String[] makeParameters(List<String> files, GroovyClassLoader parentClassLoader) {
        Map options = config.getJointCompilationOptions();
        LinkedList<String> paras = new LinkedList<String>();

        File target = config.getTargetDirectory();
        if (target == null) target = new File(".");

        // defaults
        paras.add("-d");
        paras.add(target.getAbsolutePath());
        paras.add("-sourcepath");
        paras.add(((File) options.get("stubDir")).getAbsolutePath());

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
            StringBuffer resultPath = new StringBuffer(DefaultGroovyMethods.join(config.getClasspath(), File.pathSeparator));
            ClassLoader cl = parentClassLoader;
            while (cl != null) {
                if (cl instanceof URLClassLoader) {
                    for (URL u : ((URLClassLoader) cl).getURLs()) {
                        try {
                            resultPath.append(File.pathSeparator);
                            resultPath.append(new File(u.toURI()).getPath());
                        } catch (URISyntaxException e) {
                            // ignore it
                        }
                    }
                }
                cl = cl.getParent();
            }

            paras.add("-classpath");
            paras.add(resultPath.toString());
        }

        // files to compile
        paras.addAll(files);

        return paras.toArray(new String[paras.size()]);
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
