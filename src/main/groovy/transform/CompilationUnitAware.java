package groovy.transform;

import org.codehaus.groovy.control.CompilationUnit;

/**
 * This interface is for AST transformations which must be aware of the compilation unit where they are applied.
 *
 * @author Cedric Champeau
 */
public interface CompilationUnitAware {
    void setCompilationUnit(CompilationUnit unit);
}
