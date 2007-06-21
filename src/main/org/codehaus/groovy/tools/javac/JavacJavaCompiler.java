package org.codehaus.groovy.tools.javac;

import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.messages.ExceptionMessage;

public class JavacJavaCompiler implements JavaCompiler {
    private CompilerConfiguration config;
    
    public JavacJavaCompiler(CompilerConfiguration config) {
        this.config = config;
    }
    
    public void compile(List files, CompilationUnit cu) {
        String[] javacParameters = makeParameters(files);
        org.apache.tools.ant.taskdefs.Javac c;
        try {
            Class javac = findJavac(cu);
            Method method = javac.getMethod("compile",
                    new Class[] { String[].class });
            method.invoke(null, new Object[] { javacParameters });
        } catch (Exception e) {
            cu.getErrorCollector().addFatalError(new ExceptionMessage(e,true,cu));
        }
    }

    private String[] makeParameters(List files) {
        Map options = config.getJointCompilationOptions();
        LinkedList paras = new LinkedList();

        // defaults
        paras.add("-d");
        paras.add(config.getTargetDirectory().getAbsolutePath());
        paras.add("-sourcepath");
        paras.add(((File)options.get("stubDir")).getAbsolutePath());
        
        // add flags
        String[] flags = (String[]) options.get("flags");
        if (flags!=null) {
            for (int i = 0; i < flags.length; i++) {
                paras.add('-'+flags[i]);
            }
        }
        
        // add namedValues
        String[] namedValues = (String[]) options.get("namedValues");
        if (namedValues!=null) {
            for (int i = 0; i < namedValues.length; i+=2) {
                paras.add('-'+namedValues[i]);
                paras.add(namedValues[i+1]);
            }
        }
        
        // files to compile
        paras.addAll(files);
        
        return (String[]) paras.toArray(new String[0]);        
    }

    private Class findJavac(CompilationUnit cu) throws ClassNotFoundException {
        Class javac = null;
        String MAIN = "com.sun.tools.javac.Main";
        try {
            javac = Class.forName(MAIN);
        } catch (ClassNotFoundException e) {
            ClassLoader cl = this.getClass().getClassLoader();
            try {
                javac = cl.loadClass(MAIN);
            } catch (ClassNotFoundException e1) {
                try {
                    javac = ClassLoader.getSystemClassLoader().loadClass(
                            MAIN);
                } catch (ClassNotFoundException e2) {
                    try {
                        javac = cu.getClassLoader().getParent().loadClass(
                                MAIN);
                    } catch (ClassNotFoundException e3) {
                        // couldn't find compiler - try to find tools.jar
                        // based on java.home setting
                        String javaHome = System.getProperty("java.home");
                        if (javaHome.toLowerCase(Locale.US).endsWith("jre")) {
                            javaHome = javaHome.substring(0, javaHome
                                    .length() - 4);
                        }
                        File toolsJar = new File(
                                (javaHome + "/lib/tools.jar"));
                        if (toolsJar.exists()) {
                            GroovyClassLoader loader = cu.getClassLoader();
                            loader.addClasspath(toolsJar.getAbsolutePath());
                            javac = loader.loadClass(MAIN);
                        }
                    }
                }
            }
        }
        return javac;
    }
}
