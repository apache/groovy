package org.codehaus.groovy.tools.javac;

import java.util.List;

import org.codehaus.groovy.control.CompilationUnit;

/**
 * Created by IntelliJ IDEA. User: Alex.Tkachman Date: May 30, 2007 Time:
 * 6:59:25 PM To change this template use File | Settings | File Templates.
 */
public interface JavaCompiler {

    public void compile(List files, CompilationUnit cu);
}