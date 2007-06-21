package org.codehaus.groovy.tools.javac;

import org.codehaus.groovy.control.CompilerConfiguration;

public class JavacCompilerFactory implements JavaCompilerFactory {
    public JavaCompiler createCompiler(CompilerConfiguration config) {
        return new JavacJavaCompiler(config);
    }
}
