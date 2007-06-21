package org.codehaus.groovy.tools.javac;

import org.codehaus.groovy.control.CompilerConfiguration;

public interface JavaCompilerFactory {
    JavaCompiler createCompiler(CompilerConfiguration config);
}