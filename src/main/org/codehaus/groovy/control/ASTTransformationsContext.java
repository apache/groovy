package org.codehaus.groovy.control;
/**
 * Created by IntelliJ IDEA.
 * User: cedric
 * Date: 06/10/11
 * Time: 15:13
 */

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilationUnit;

import java.util.HashSet;
import java.util.Set;

/**
* Stores state information about global AST transformations applied to a compilation unit.
 *
 * @author Cedric Champeau
*/
public class ASTTransformationsContext {
    protected final GroovyClassLoader transformLoader;  // Classloader for global and local transforms

    protected final CompilationUnit compilationUnit; // The compilation unit global AST transformations are applied on
    protected final Set<String> globalTransformNames = new HashSet<String>(); // collected AST transformation names

    public ASTTransformationsContext(final CompilationUnit compilationUnit, final GroovyClassLoader transformLoader) {
        this.compilationUnit = compilationUnit;
        this.transformLoader = transformLoader;
    }

    public CompilationUnit getCompilationUnit() {
        return compilationUnit;
    }

    public Set<String> getGlobalTransformNames() {
        return globalTransformNames;
    }

    public GroovyClassLoader getTransformLoader() {
        return transformLoader;
    }
}
